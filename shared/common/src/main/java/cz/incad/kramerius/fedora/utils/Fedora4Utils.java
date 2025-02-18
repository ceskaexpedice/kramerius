package cz.incad.kramerius.fedora.utils;

import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.PIDParser;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.solr.client.solrj.SolrServerException;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.core.repository.RepositoryException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by pstastny on 10/10/2017.
 */
// TODO AK_NEW
public class Fedora4Utils {


    public static final Logger LOGGER = Logger.getLogger(Fedora4Utils.class.getName());

    // divide factor for storing in repo
    public static final int DIVIDE_FACTOR = 3;

    public static final int MAX_DIVS_PART = 3;

    // cannot be configured; part of relative paths
    public static final String BOUND_CONTEXT = "rest";

    // prefixes used in repo for string stuff
    public static final String DATA_PREFIX_PATH = "data";
    public static final String MODELS_PREFIX_PATH = "model";
    public static final String DONATORS_PREFIX_PATH = "donator";
    public static final String COLLECTIONS_PREFIX_PATH = "collections";

    public static final Map<String, String> PREFIX_PATH_MAPPING = new HashMap<>();

    static {
        PREFIX_PATH_MAPPING.put("vc", COLLECTIONS_PREFIX_PATH);
        PREFIX_PATH_MAPPING.put("model", MODELS_PREFIX_PATH);
        PREFIX_PATH_MAPPING.put("donator", DONATORS_PREFIX_PATH);

    }

    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String DATE_FORMAT_WITHOUTMILIS = "yyyy-MM-dd'T'HH:mm:ss'Z'";

//    /** DEFAULT Date formatter*/
//    public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");


    /**
     * Normalizing path in order to store object into repo
     * @param pid Given pid which should be normalized
     * @return Path into repository  disected into pieces
     */
    public static final List<String> normalizePath(String pid) {
        if (pid.startsWith(PIDParser.INFO_FEDORA_PREFIX)) {
            pid = StringUtils.minus(pid, PIDParser.INFO_FEDORA_PREFIX);
        }
        String prefix = DATA_PREFIX_PATH;
        String[] splitted = pid.split(":");
        if (splitted.length > 1 && PREFIX_PATH_MAPPING.containsKey(splitted[0])) {
            prefix = PREFIX_PATH_MAPPING.get(splitted[0]);
        }
        if (pid.contains(":")) {
            pid = pid.substring(pid.indexOf(':') + 1);
        }
        return prefix.equals(DATA_PREFIX_PATH) ? dividePid(pid, prefix) : notDividedPid(pid, prefix);
    }


    /**
     * Utility method returns whole path from given containers
     * @param parts
     * @return
     */
    public static final String path(List<String> parts) {
        return parts.stream().reduce("", (p, s) -> {
            return p = p + "/" + s;
        });
    }

    /**
     * Returns path but not divide into pieces
     * @param pid
     * @param prefix
     * @return
     */
    private static List<String> notDividedPid(String pid, String prefix) {
        return new ArrayList<>(Arrays.asList(prefix, pid));
    }

    private static List<String> dividePid(String pid, String prefix) {

        pid = pid.replaceAll("-", "");
        List<String> list = new ArrayList<>(Arrays.asList(prefix));
        StringBuilder sbuilder = new StringBuilder();
        char[] chars = pid.toCharArray();
        int divparts = 0;
        for (int j = 0; j < chars.length; j++) {
            if (j % DIVIDE_FACTOR == 0 && divparts < MAX_DIVS_PART) {
                if (sbuilder.length() > 0) {
                    list.add(sbuilder.toString());
                }
                sbuilder = new StringBuilder();
                divparts++;
            }
            sbuilder.append(chars[j]);
        }

        if (sbuilder.length() > 0) {
            list.add(sbuilder.toString());
        }
        return list;
    }

    /**
     * Returns configured endpoint
     * @return
     */
    public static String endpoint() {
        String fedoraHost = KConfiguration.getInstance().getConfiguration().getString("fedora4Host");
        return fedoraHost + (fedoraHost.endsWith("/") ? "" : "/") + BOUND_CONTEXT;
    }

    public static String pathInEndpoint() throws MalformedURLException {
        String fedoraHost = KConfiguration.getInstance().getConfiguration().getString("fedora4Host");
        URL url = new URL(fedoraHost);
        String path = url.getPath();
        if (path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        } else return path;
    }


    public static List<String> link(String link) {
        String endpoint = endpoint();
        link = StringUtils.minus(link, endpoint);
        if (link.endsWith("/")) {
            link = StringUtils.minus(link, "/" + BOUND_CONTEXT + "/");
        } else {
            link = StringUtils.minus(link, "/" + BOUND_CONTEXT);
        }
        if (link.startsWith("/")) link = link.substring(1);
        return Arrays.asList(link.split("/"));
    }


    public static Date extractDate(InputStream body, String name, String namespace) throws ParserConfigurationException, SAXException, IOException, ParseException {
        Document document = XMLUtils.parseDocument(body, true);
        Element lastModified = XMLUtils.findElement(document.getDocumentElement(), name, namespace);
        if (lastModified != null) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
                return dateFormat.parse(lastModified.getTextContent());
            } catch (ParseException e) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_WITHOUTMILIS);
                return dateFormat.parse(lastModified.getTextContent());
            }
        } else return null;
    }

    public static String extractText(InputStream body, String localName, String namespace) throws ParserConfigurationException, SAXException, IOException {
        Document document = XMLUtils.parseDocument(body, true);
        Element hasMimeType = XMLUtils.findElement(document.getDocumentElement(), localName, namespace);
        if (hasMimeType != null) {
            return hasMimeType.getTextContent();
        } else throw new IOException("cannot find  mimetype element");
    }

//    TODO: Change it, must be done together with processing index
//    public static void doInTransaction(Repository rep, OperationsHandler op) throws RepositoryException {
//        try {
//            op.operations(rep);
//            rep.commitTransaction();
//        }catch(Throwable e) {
//            rep.rollbackTransaction();
//            LOGGER.log(Level.SEVERE,e.getMessage(),e);
//        }
//    }

    // it doesn't make sense - processing index contains everything
    public static void doWithProcessingIndexCommit(AkubraRepository rep, OperationsHandler op) throws RepositoryException {
        try {
            op.operations(rep);
        } finally {
            rep.commitProcessingIndex();
        }
    }

    public static List<Triple<String, String, String>> triplesToDeleteByHref(AkubraRepository repo, Document metadata, final String relation, final String namespace, String target) throws RepositoryException, IOException {
        // update sparql
        List<Triple<String, String, String>> deletingTriples = new ArrayList<>();
        deletingTriples.add(new ImmutableTriple<>("<>", "<" + namespace + relation + ">", "<" + target + ">"));
        if (target.contains("#")) {
            deletingTriples.add(new ImmutableTriple<>("<" + target + ">", "?anyRelation", "?anyValue "));
        }
        return deletingTriples;
    }

    /* TODO AK_NEW
    public static List<Triple<String, String, String>> triplesToDeleteByPid(AkubraRepository repo, Document metadata, final String relation, final String namespace, String target) throws RepositoryException, IOException {
        final String targetFullPath = repo.getObject(target).getFullPath();
        String toRemoveReference = targetFullPath;
        boolean indirectReference = false;
        // get metadata - detect reference
        Element sameAsElement = XMLUtils.findElement(metadata.getDocumentElement(), (element) -> {
            String elocalName = element.getLocalName();
            String enamespace = element.getNamespaceURI();
            if (elocalName.equals("sameAs") && enamespace.equals("http://www.w3.org/2002/07/owl#")) {
                String resource = element.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
                if ((resource != null) && (resource.endsWith(targetFullPath))) {
                    return true;
                } else return false;
            } else return false;
        });

        if (sameAsElement != null) {
            String about = ((Element) sameAsElement.getParentNode()).getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "about");
            if (about != null) {
                toRemoveReference = about;
                indirectReference = true;
            }
        }
        // update sparql
        List<Triple<String, String, String>> deletingTriples = new ArrayList<>();
        deletingTriples.add(new ImmutableTriple<>("<>", "<" + namespace + relation + ">", "<" + toRemoveReference + ">"));
        if (indirectReference) {
            deletingTriples.add(new ImmutableTriple<>("<" + toRemoveReference + ">", "?anyRelation", "?anyValue "));
        }
        return deletingTriples;
    }

     */

    public static interface OperationsHandler {
        public void operations(AkubraRepository rep) throws RepositoryException;
    }
}
