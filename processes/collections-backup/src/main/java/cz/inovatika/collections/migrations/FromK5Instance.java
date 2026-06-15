/*
 * Copyright (C) Dec 3, 2023 Pavel Stastny
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.inovatika.collections.migrations;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.RepositoryNamespaces;
import org.json.JSONArray;
import org.json.JSONObject;
import org.kramerius.Import;
import org.kramerius.ImportModule;
import org.kramerius.importer.inventory.ScheduleStrategy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;

import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.incad.kramerius.utils.IterationUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.IterationUtils.IterationCallback;
import cz.incad.kramerius.utils.IterationUtils.IterationEndCallback;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.collections.Restore;

import javax.xml.parsers.ParserConfigurationException;

public class FromK5Instance {

    private static final String ALLOWED_CHARACTERS =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private static final Client CLIENT = ClientBuilder.newClient();

    public static Logger LOGGER =
            Logger.getLogger(FromK5Instance.class.getName());

    public static String generateRandomString(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALLOWED_CHARACTERS.charAt(
                    random.nextInt(ALLOWED_CHARACTERS.length())));
        }
        return sb.toString();
    }

    public static void migrateMain(String url) throws Exception {

        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        String tmpDirPath = System.getProperty("java.io.tmpdir");
        String subdirectoryPath =
                tmpDirPath + File.separator + generateRandomString(5);

        FileUtils.forceMkdir(new File(subdirectoryPath));
        LOGGER.info("Generating temp folder " + subdirectoryPath);

        JSONArray collections = collections(CLIENT, url);

        for (int i = 0; i < collections.length(); i++) {

            JSONObject col = collections.getJSONObject(i);
            String pid = col.getString("pid");
            JSONObject desc = col.getJSONObject("descs");

            List<String> pids = pids(CLIENT, pid, url);

            Document foxml = foxml(CLIENT, pid, url);

            createBiblioMods(foxml, desc);
            removeStream(foxml, "DC");
            removeStream(foxml, "AUDIT");
            removeStream(foxml, "TEXT");
            removeStream(foxml, "TEXT_cs");
            removeStream(foxml, "TEXT_en");

            addContainsRelations(foxml, pids);
            setStandalone(foxml);

            File vcFile =
                    new File(subdirectoryPath, pid.replace(':', '_') + ".xml");

            try (FileOutputStream fos = new FileOutputStream(vcFile)) {
                XMLUtils.print(foxml, fos);
            }
        }

        LOGGER.info("Scheduling import " + subdirectoryPath);
        importTmpDir(subdirectoryPath, true);
    }

    /* ================================
       ========== HTTP PART ===========
       ================================ */

    private static JSONArray collections(Client client, String api)
            throws IOException {

        String url = api + "/search/api/v5.0/vc";
        LOGGER.info("Requesting url " + url);

        WebTarget target = client.target(url);

        try (InputStream is =
                     target.request(MediaType.APPLICATION_JSON)
                             .get(InputStream.class)) {

            String string = IOUtils.toString(is, "UTF-8");
            return new JSONArray(string);
        }
    }

    private static Document foxml(Client client, String vcPid, String api)
            throws IOException, SAXException, ParserConfigurationException {

        String url =
                String.format("%s/search/api/v5.0/item/%s/foxml", api, vcPid);

        LOGGER.info("Requesting url " + url);

        WebTarget target = client.target(url);

        try (InputStream is =
                     target.request(MediaType.APPLICATION_XML)
                             .get(InputStream.class)) {

            String document = IOUtils.toString(is, "UTF-8");
            String replaced = document.replaceAll("vc\\:", "uuid:");
            return XMLUtils.parseDocument(
                    new StringReader(replaced), true);
        }
    }

    /* ================================
       ========= QUERY PART ==========
       ================================ */

    static Element pidsCursorQuery(Client client,
                                   String url,
                                   String mq,
                                   String cursor)
            throws Exception {

        int rows = 1000;

        String query =
                "search?q=" + mq +
                        (cursor != null
                                ? "&rows=" + rows + "&cursorMark=" + cursor
                                : "&rows=" + rows + "&cursorMark=*")
                        + "&sort=" + URLEncoder.encode("PID desc", "UTF-8")
                        + "&fl=PID";

        return IterationUtils.executeQuery(client, url, query);
    }

    public static void collectionIterations(Client client,
                                            String address,
                                            String masterQuery,
                                            IterationCallback callback,
                                            IterationEndCallback endCallback)
            throws Exception {

        String cursorMark = null;
        String queryCursorMark;

        do {
            Element element =
                    pidsCursorQuery(client, address, masterQuery, cursorMark);

            cursorMark = IterationUtils.findCursorMark(element);
            queryCursorMark =
                    IterationUtils.findQueryCursorMark(element);

            callback.call(element, cursorMark);

        } while (cursorMark != null &&
                queryCursorMark != null &&
                !cursorMark.equals(queryCursorMark));

        endCallback.end();
    }

    private static List<String> pids(Client client,
                                     String vcPid,
                                     String api)
            throws Exception {

        final List<String> returnPids = new ArrayList<>();

        String url = api.endsWith("/") ?
                api + "search/api/v5.0/" :
                api + "/search/api/v5.0/";

        List<String> models =
                KConfiguration.getInstance()
                        .getConfiguration()
                        .getList("collections.migrate.models",
                                Arrays.asList("monograph",
                                        "periodical",
                                        "sheetmusic",
                                        "monographunit"))
                        .stream()
                        .map(Object::toString)
                        .toList();

        String fqModel =
                "fedora.model:(" +
                        String.join(" OR ", models) +
                        ") AND ";

        String collectionPid =
                "(collection:\"" + vcPid + "\")";

        String masterQuery =
                URLEncoder.encode(fqModel + collectionPid, "UTF-8");

        collectionIterations(client, url, masterQuery,
                (elm, i) -> {

                    Element result =
                            XMLUtils.findElement(elm,
                                    e -> e.getNodeName().equals("result"));

                    if (result != null) {
                        List<Element> elements =
                                XMLUtils.getElements(result,
                                        e -> e.getNodeName().equals("doc"));

                        List<String> idents =
                                elements.stream()
                                        .map(item ->
                                                XMLUtils.findElement(item,
                                                                e -> e.getNodeName()
                                                                        .equals("str"))
                                                        .getTextContent())
                                        .collect(Collectors.toList());

                        returnPids.addAll(idents);
                    }

                }, () -> {
                });

        return returnPids;
    }

    /* ================================
       ========= IMPORT PART =========
       ================================ */

    public static void importTmpDir(String exportRoot,
                                    boolean startIndexer)
            throws IOException, SolrServerException {

        Injector injector =
                Guice.createInjector(
                        new SolrModule(),
                        new RepoModule(),
                        new NullStatisticsModule(),
                        new ImportModule());

        AkubraRepository akubraRepository =
                injector.getInstance(Key.get(AkubraRepository.class));

        try {

            Import.run(
                    akubraRepository,
                    akubraRepository.pi(),
                    null,
                    KConfiguration.getInstance().getProperty("ingest.url"),
                    KConfiguration.getInstance().getProperty("ingest.user"),
                    KConfiguration.getInstance().getProperty("ingest.password"),
                    exportRoot,
                    startIndexer,
                    null,
                    ScheduleStrategy.indexRoots
            );

            FileUtils.deleteDirectory(new File(exportRoot));

        } finally {
            akubraRepository.shutdown();
        }
    }

    /* ================================
       ======= XML HELPERS ===========
       ================================ */

    private static void removeStream(Document foxml, String id) {
        Element elm = XMLUtils.findElement(
                foxml.getDocumentElement(),
                e -> id.equals(e.getAttribute("ID")));
        if (elm != null)
            elm.getParentNode().removeChild(elm);
    }

    private static void createBiblioMods(Document foxml, JSONObject desc) {
        Element dataStream = foxml.createElementNS(RepositoryNamespaces.FEDORA_FOXML_URI, "datastream");
        dataStream.setAttribute("ID", "BIBLIO_MODS");
        dataStream.setAttribute("CONTROL_GROUP", "X");
        dataStream.setAttribute("STATE", "A");
        dataStream.setAttribute("VERSIONABLE", "false");
        foxml.getDocumentElement().appendChild(dataStream);


        Element datastreamVersion = foxml.createElementNS(RepositoryNamespaces.FEDORA_FOXML_URI, "datastreamVersion");
        datastreamVersion.setAttribute("ID", "BIBLIO_MODS.0");

        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        // Získejte aktuální datum a čas jako LocalDateTime
        LocalDateTime currentDateTime = LocalDateTime.now();
        // Naformátujte aktuální datum a čas podle zadaného formátu
        String formattedDateTime = currentDateTime.format(formatter);
        datastreamVersion.setAttribute("CREATED", formattedDateTime);
        datastreamVersion.setAttribute("MIMETYPE", "text/xml");
        datastreamVersion.setAttribute("FORMAT_URI", "http://www.loc.gov/mods/v3");
        dataStream.appendChild(datastreamVersion);

        Element xmlContent = foxml.createElementNS(RepositoryNamespaces.FEDORA_FOXML_URI, "xmlContent");
        datastreamVersion.appendChild(xmlContent);

        Element modsCollection = foxml.createElementNS(RepositoryNamespaces.BIBILO_MODS_URI, "modsCollection");
        xmlContent.appendChild(modsCollection);

        Element mods = foxml.createElementNS(RepositoryNamespaces.BIBILO_MODS_URI, "mods");
        mods.setAttribute("version", "3.4");
        modsCollection.appendChild(mods);

        if (desc.has("cs")) {
            String csAbstract = null;

            // TEXT_cs - stream
            Element textcs = XMLUtils.findElement(foxml.getDocumentElement(), new XMLUtils.ElementsFilter() {

                @Override
                public boolean acceptElement(Element element) {
                    String id = element.getAttribute("ID");
                    return id != null && id.equals("TEXT_cs");
                }
            });
            if (textcs != null) {
                Element binary = XMLUtils.findElement(textcs, new XMLUtils.ElementsFilter() {

                    @Override
                    public boolean acceptElement(Element element) {
                        String lname = element.getLocalName();
                        return lname != null && lname.equals("binaryContent");
                    }
                });

                csAbstract = new String(Base64.decodeBase64(binary.getTextContent()));
                Element abstractElm = foxml.createElementNS(RepositoryNamespaces.BIBILO_MODS_URI, "abstract");
                abstractElm.setAttribute("lang", "cze");
                abstractElm.setTextContent(csAbstract);
                mods.appendChild(abstractElm);
            }


            Element titleInfo = foxml.createElementNS(RepositoryNamespaces.BIBILO_MODS_URI, "titleInfo");
            titleInfo.setAttribute("lang", "cze");
            mods.appendChild(titleInfo);

            Element title = foxml.createElementNS(RepositoryNamespaces.BIBILO_MODS_URI, "title");
            title.setTextContent(desc.getString("cs"));
            titleInfo.appendChild(title);

        }

        if (desc.has("en")) {
            String csAbstract = null;

            // TEXT_cs - stream
            Element textcs = XMLUtils.findElement(foxml.getDocumentElement(), new XMLUtils.ElementsFilter() {

                @Override
                public boolean acceptElement(Element element) {
                    String id = element.getAttribute("ID");
                    return id != null && id.equals("TEXT_en");
                }
            });
            if (textcs != null) {
                Element binary = XMLUtils.findElement(textcs, new XMLUtils.ElementsFilter() {

                    @Override
                    public boolean acceptElement(Element element) {
                        String lname = element.getLocalName();
                        return lname != null && lname.equals("binaryContent");
                    }
                });

                csAbstract = new String(Base64.decodeBase64(binary.getTextContent()));
                Element abstractElm = foxml.createElementNS(RepositoryNamespaces.BIBILO_MODS_URI, "abstract");
                abstractElm.setAttribute("lang", "eng");
                abstractElm.setTextContent(csAbstract);
                mods.appendChild(abstractElm);
            }

            Element titleInfo = foxml.createElementNS(RepositoryNamespaces.BIBILO_MODS_URI, "titleInfo");
            titleInfo.setAttribute("lang", "eng");
            mods.appendChild(titleInfo);

            Element title = foxml.createElementNS(RepositoryNamespaces.BIBILO_MODS_URI, "title");
            title.setTextContent(desc.getString("en"));
            titleInfo.appendChild(title);
        }
    }

    private static void addContainsRelations(Document foxml, List<String> pids) {
        //<rel:contains xmlns:rel="http://www.nsdl.org/ontologies/relationships#" rdf:resource="info:fedora/uuid:fe91d1d0-fc5c-4ea7-9300-3504922a08d4"/>
        Element relsExt = XMLUtils.findElement(foxml.getDocumentElement(), new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                String localName = element.getLocalName();
                String attribute = element.getAttribute("ID");
                return localName != null && localName.equals("datastream") && attribute != null && attribute.equals("RELS-EXT");
            }
        });

        if (relsExt != null) {
            Element rdfDecription = XMLUtils.findElement(relsExt, new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    String localName = element.getLocalName();
                    String nameSpace = element.getNamespaceURI();
                    return localName != null && localName.equals("Description") && nameSpace.equals(RepositoryNamespaces.RDF_NAMESPACE_URI);
                }
            });

            if (rdfDecription != null) {
                for (String pid : pids) {
                    Element contains = foxml.createElementNS(RepositoryNamespaces.ONTOLOGY_RELATIONSHIP_NAMESPACE_URI, "rel:contains");
                    contains.setPrefix("rel");
                    contains.setAttributeNS(RepositoryNamespaces.RDF_NAMESPACE_URI, "rdf:resource", String.format("info:fedora/%s", pid));
                    rdfDecription.appendChild(contains);
                }
            }
        }
    }

    public static void setStandalone(Document foxml) {
        Element relsExt = XMLUtils.findElement(foxml.getDocumentElement(), new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                String localName = element.getLocalName();
                String attribute = element.getAttribute("ID");
                return localName != null && localName.equals("datastream") && attribute != null && attribute.equals("RELS-EXT");
            }
        });

        if (relsExt != null) {
            Element rdfDecription = XMLUtils.findElement(relsExt, new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    String localName = element.getLocalName();
                    String nameSpace = element.getNamespaceURI();
                    return localName != null && localName.equals("Description") && nameSpace.equals(RepositoryNamespaces.RDF_NAMESPACE_URI);
                }
            });

            if (rdfDecription != null) {
                Element contains = foxml.createElementNS(RepositoryNamespaces.ONTOLOGY_RELATIONSHIP_NAMESPACE_URI, "rel:standalone");
                contains.setPrefix("rel");
                contains.setTextContent("true");
                rdfDecription.appendChild(contains);
            }
        }

    }

}