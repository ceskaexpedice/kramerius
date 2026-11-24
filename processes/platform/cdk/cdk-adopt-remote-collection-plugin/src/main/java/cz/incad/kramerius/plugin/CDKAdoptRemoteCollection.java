package cz.incad.kramerius.plugin;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.kramerius.searchIndex.NewIndexerProcessIndexObject;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.reference.CDKReference;
import org.ceskaexpedice.akubra.utils.Dom4jUtils;
import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;
import org.ceskaexpedice.processplatform.api.context.PluginContextHolder;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/** Adopts a remote CDK collection locally while preserving its PID. */
public final class CDKAdoptRemoteCollection {
    private static final Logger LOG = Logger.getLogger(CDKAdoptRemoteCollection.class.getName());
    private static final String RELS_EXT = "RELS-EXT";
    private static final String MODS = "BIBLIO_MODS";
    private static final String PROVENANCE_ID = "CDK_ADOPTION_PROVENANCE";
    private static final String REL_NS = "http://www.nsdl.org/ontologies/relationships#";
    private static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    private static final String MODS_NS = "http://www.loc.gov/mods/v3";
    private static final String FOXML_NS = "info:fedora/fedora-system:def/foxml#";
    private static final String PROVENANCE_NS = "https://kramerius.cz/ns/cdk-adoption/1.0";

    @ProcessMethod
    public static void adopt(@ParameterName("sourceLibrary") @IsRequired String sourceLibrary,
                             @ParameterName("sourcePid") @IsRequired String sourcePid,
                             @ParameterName("title") String title) throws Exception {
        String library = required(sourceLibrary, "sourceLibrary");
        String source = required(sourcePid, "sourcePid");
        String baseUrl = config(library, "baseurl");
        String apiKey = config(library, "apikey");
        if (blank(baseUrl) || blank(apiKey)) throw new IllegalArgumentException("CDK source needs baseurl and apikey: " + library);

        String fetchUrl = foxmlUrl(baseUrl, source);
        Document sourceFoxml = fetchFoxml(fetchUrl, apiKey);
        prepareAdoptedCollection(sourceFoxml, library, source, baseUrl, fetchUrl, title);

        Injector injector = Guice.createInjector(new RepoModule());
        AkubraRepository repository = injector.getInstance(Key.get(AkubraRepository.class));
        try {
            repository.ingest(Dom4jUtils.foxmlDocToDigitalObject(sourceFoxml, repository));
        } finally {
            repository.shutdown();
        }

        PluginContextHolder.getContext().updateProcessName("Adopt remote collection " + source + " from " + library);
        NewIndexerProcessIndexObject.indexerMain("OBJECT", source, false, "Source override " + source);
        NewIndexerProcessIndexObject.indexerMain("OBJECT", source, false,
                blank(title) ? source : title);
        LOG.info("Adopted remote collection " + library + "/" + source + " locally with the original PID");
    }

    private static String config(String library, String property) {
        return KConfiguration.getInstance().getConfiguration().getString("cdk.collections.sources." + library + "." + property);
    }

    private static String foxmlUrl(String baseUrl, String pid) {
        String base = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        return base + "api/client/v7.0/items/" + URLEncoder.encode(pid, StandardCharsets.UTF_8) + "/foxml";
    }

    private static Document fetchFoxml(String url, String apiKey) throws IOException {
        HttpGet request = new HttpGet(url);
        request.setHeader("X-API-KEY", apiKey);
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            return client.execute(request, response -> {
                HttpEntity entity = response.getEntity();
                if (response.getCode() < 200 || response.getCode() >= 300) {
                    String body = entity == null ? "" : EntityUtils.toString(entity, StandardCharsets.UTF_8);
                    throw new IOException("FOXML fetch failed with HTTP " + response.getCode() + ": " + body);
                }
                if (entity == null) throw new IOException("FOXML response was empty");
                try {
                    SAXReader reader = new SAXReader();
                    reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                    reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
                    reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                    return reader.read(new ByteArrayInputStream(EntityUtils.toByteArray(entity)));
                } catch (DocumentException | SAXException e) {
                    throw new IOException("FOXML response is invalid", e);
                }
            });
        }
    }

    private static void prepareAdoptedCollection(Document doc, String library, String sourcePid,
                                                 String baseUrl, String fetchUrl, String title) {
        Element object = doc.getRootElement();
        String objectPid = object.attributeValue("PID");
        if (!sourcePid.equals(objectPid)) {
            throw new IllegalArgumentException("Fetched FOXML PID " + objectPid + " does not match requested PID " + sourcePid);
        }
        Element rels = findRelsDescription(object);
        if (rels == null) throw new IllegalArgumentException("RELS-EXT rdf:Description not found for " + sourcePid);
        rewriteDirectChildren(rels);
        if (!blank(title)) replacePrimaryTitle(object, title);
        addProvenance(object, sourcePid, library, sourcePid, baseUrl, fetchUrl);
    }

    private static void rewriteDirectChildren(Element rels) {
        QName rdfResource = QName.get("resource", RDF_NS);
        for (Object child : rels.elements()) {
            Element relation = (Element) child;
            if (!"contains".equals(relation.getName()) || !REL_NS.equals(relation.getNamespaceURI())) continue;
            String target = relation.attributeValue(rdfResource);
            if (target != null && target.startsWith("info:fedora/")) {
                String childPid = target.substring("info:fedora/".length());
                if (!blank(childPid)) relation.addAttribute(rdfResource, new CDKReference(childPid).getPrefixedPid());
            }
        }
    }

    private static void replacePrimaryTitle(Element object, String title) {
        Element content = datastreamContent(object, MODS);
        if (content == null || content.elements().isEmpty()) return;

        Element modsRoot = (Element) content.elements().get(0);
        if ("modsCollection".equals(modsRoot.getName())) {
            Element collectionRoot = modsRoot;
            modsRoot = null;
            for (Object node : collectionRoot.elements()) {
                Element candidate = (Element) node;
                if ("mods".equals(candidate.getName()) && MODS_NS.equals(candidate.getNamespaceURI())) {
                    modsRoot = candidate;
                    break;
                }
            }
        }
        if (modsRoot == null) return;

        for (Object titleInfoNode : modsRoot.elements()) {
            Element titleInfo = (Element) titleInfoNode;
            if (!"titleInfo".equals(titleInfo.getName()) || !MODS_NS.equals(titleInfo.getNamespaceURI())) continue;
            for (Object titleNode : titleInfo.elements()) {
                Element titleElement = (Element) titleNode;
                if ("title".equals(titleElement.getName()) && MODS_NS.equals(titleElement.getNamespaceURI())) {
                    titleElement.setText(title);
                    return;
                }
            }
        }

        Element titleInfo = modsRoot.addElement(new QName("titleInfo", Namespace.get("mods", MODS_NS)));
        titleInfo.addElement(new QName("title", Namespace.get("mods", MODS_NS))).setText(title);
    }
    private static Element datastreamContent(Element object, String id) {
        for (Object node : object.elements()) {
            Element ds = (Element) node;
            if (!"datastream".equals(ds.getName()) || !id.equals(ds.attributeValue("ID"))) continue;
            Element latest = null;
            for (Object version : ds.elements()) if ("datastreamVersion".equals(((Element) version).getName())) latest = (Element) version;
            if (latest != null) for (Object child : latest.elements()) {
                Element content = (Element) child;
                if ("xmlContent".equals(content.getName())) return content;
            }
        }
        return null;
    }

    private static Element findRelsDescription(Element object) {
        Element content = datastreamContent(object, RELS_EXT);
        if (content == null) return null;
        for (Object rdfNode : content.elements()) for (Object descNode : ((Element) rdfNode).elements()) {
            Element desc = (Element) descNode;
            if ("Description".equals(desc.getName()) && RDF_NS.equals(desc.getNamespaceURI())) return desc;
        }
        return null;
    }

    private static void addProvenance(Element object, String localPid, String library, String sourcePid,
                                      String baseUrl, String fetchUrl) {
        for (Object node : new ArrayList<>(object.elements())) {
            Element ds = (Element) node;
            if ("datastream".equals(ds.getName()) && PROVENANCE_ID.equals(ds.attributeValue("ID"))) ds.detach();
        }
        Instant now = Instant.now();
        Namespace foxml = Namespace.get("foxml", FOXML_NS);
        Namespace provenance = Namespace.get("adopt", PROVENANCE_NS);
        Element ds = object.addElement(new QName("datastream", foxml));
        ds.addAttribute("ID", PROVENANCE_ID); ds.addAttribute("STATE", "A");
        ds.addAttribute("CONTROL_GROUP", "X"); ds.addAttribute("VERSIONABLE", "true");
        Element version = ds.addElement(new QName("datastreamVersion", foxml));
        version.addAttribute("ID", PROVENANCE_ID + ".0"); version.addAttribute("MIMETYPE", "application/xml");
        version.addAttribute("FORMAT_URI", PROVENANCE_NS); version.addAttribute("CREATED", now.toString());
        Element xml = version.addElement(new QName("xmlContent", foxml));
        Element root = xml.addElement(new QName("adoption", provenance)); root.addAttribute("localPid", localPid);
        root.addElement(new QName("sourceLibrary", provenance)).setText(library);
        root.addElement(new QName("sourcePid", provenance)).setText(sourcePid);
        root.addElement(new QName("sourceBaseUrl", provenance)).setText(baseUrl);
        root.addElement(new QName("sourceFoxmlUrl", provenance)).setText(fetchUrl);
        root.addElement(new QName("adoptedAt", provenance)).setText(now.toString());
    }

    private static boolean blank(String s) { return s == null || s.isBlank(); }
    private static String required(String s, String name) {
        if (blank(s)) throw new IllegalArgumentException(name + " must not be blank");
        return s.trim();
    }
}
