package org.kramerius.replications;

import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static cz.incad.kramerius.utils.IterationUtils.getSortField;

public class IterateThroughIndexPhase extends AbstractPhase {

    public static final Logger LOGGER =
            Logger.getLogger(IterateThroughIndexPhase.class.getName());

    public static final String POSTFIX = "api/v5.0/search?q=*:*";
    public static final int ROWS = 1000;

    @Override
    public void start(String url,
                      String userName,
                      String pswd,
                      String replicationCollections,
                      String replicateImages) throws PhaseException {

        try {

            File descriptionFile = createDescriptionFile();
            JSONObject object = new JSONObject();
            object.put("handle", url);
            FileUtils.write(descriptionFile, object.toString(), "UTF-8");

            StringBuilder builder = new StringBuilder("{'pids':[");
            boolean first = true;

            boolean useCursor = KConfiguration.getInstance()
                    .getConfiguration()
                    .getBoolean("solr.migration.usecursor", false);

            LOGGER.info(useCursor ? "Using cursor" : "Using paging");

            for (String pid : useCursor
                    ? cursorPids(url)
                    : pagingPids(url)) {

                if (!pid.contains("@")) {
                    if (!first) builder.append(',');
                    builder.append('\'').append(pid).append('\'');
                    first = false;
                }
            }

            builder.append("]}");

            FileUtils.write(createIterateFile(),
                    builder.toString(),
                    Charset.forName("UTF-8"));

        } catch (Exception e) {
            throw new PhaseException(this, e);
        }
    }

    /* ================================
       ========= CURSOR MODE =========
       ================================ */

    public static List<String> cursorPids(String surl)
            throws PhaseException, IOException,
            ParserConfigurationException, SAXException {

        List<String> retvals = new ArrayList<>();
        int rows = ROWS;
        int fetchedRows;
        String cursorMark = "*";

        Client client = ClientBuilder.newClient();

        try {

            do {
                String url = cursorUrl(surl, rows, cursorMark);
                WebTarget target = client.target(url);

                String response = target
                        .request(MediaType.APPLICATION_XML)
                        .get(String.class);

                Document document =
                        XMLUtils.parseDocument(new StringReader(response));

                String nextCursor = nextCursor(document);

                if (!nextCursor.equals(cursorMark)) {

                    cursorMark = nextCursor;

                    List<String> pids =
                            XMLUtils.getElementsRecursive(
                                            document.getDocumentElement(),
                                            elm -> elm.getNodeName().equals("str")
                                                    && elm.getAttribute("name").equals("PID")
                                    ).stream()
                                    .map(Element::getTextContent)
                                    .collect(Collectors.toList());

                    retvals.addAll(pids);
                    fetchedRows = pids.size();

                } else {
                    break;
                }

            } while (fetchedRows > 0);

        } finally {
            client.close();
        }

        return retvals;
    }

    /* ================================
       ========= PAGING MODE =========
       ================================ */

    public static List<String> pagingPids(String surl)
            throws PhaseException, IOException,
            ParserConfigurationException, SAXException {

        List<String> retvals = new ArrayList<>();
        int numberOfDocs;
        String lastPid = null;

        Client client = ClientBuilder.newClient();

        try {

            do {

                String url = pagingUrl(surl, ROWS, lastPid);
                WebTarget target = client.target(url);

                String response = target
                        .request(MediaType.APPLICATION_XML)
                        .get(String.class);

                Document document =
                        XMLUtils.parseDocument(new StringReader(response));

                numberOfDocs = numFound(document);

                List<String> pids =
                        XMLUtils.getElementsRecursive(
                                        document.getDocumentElement(),
                                        elm -> elm.getNodeName().equals("str")
                                                && elm.getAttribute("name").equals("PID")
                                ).stream()
                                .map(Element::getTextContent)
                                .collect(Collectors.toList());

                if (!pids.isEmpty()) {
                    lastPid = pids.get(0);
                }

                retvals.addAll(pids);

            } while (numberOfDocs > 0);

        } finally {
            client.close();
        }

        return retvals;
    }

    /* ================================
       ========= HELPERS =============
       ================================ */

    private static String nextCursor(Document doc) {
        Element elm = XMLUtils.findElement(
                doc.getDocumentElement(),
                element -> element.getNodeName().equals("str")
                        && element.getAttribute("name").equals("nextCursorMark"));

        return elm != null ? elm.getTextContent() : "*";
    }

    private static int numFound(Document doc) {
        Element elm = XMLUtils.findElement(
                doc.getDocumentElement(),
                element -> element.getNodeName().equals("result"));

        return elm != null
                ? Integer.parseInt(elm.getAttribute("numFound"))
                : 0;
    }

    private static String pagingUrl(String surl,
                                    int rows,
                                    String pid)
            throws UnsupportedEncodingException {

        return surl + (surl.endsWith("/") ? "" : "/")
                + POSTFIX
                + "&fl=PID"
                + "&sort=" + getSortField() + "+asc"
                + "&rows=" + rows
                + (pid != null
                ? "&fq=" + URLEncoder.encode(
                "PID:{\"" + pid + "\" TO *]", "UTF-8")
                : "")
                + "&wt=xml";
    }

    private static String cursorUrl(String surl,
                                    int rows,
                                    String cursorMark) {

        return surl + (surl.endsWith("/") ? "" : "/")
                + POSTFIX
                + "&rows=" + rows
                + "&sort=" + getSortField() + "+asc"
                + "&cursorMark=" + cursorMark
                + "&wt=xml&fl=PID";
    }

    /* ================================
       ========= RESTART =============
       ================================ */

    @Override
    public void restart(String previousProcessUUID,
                        File previousProcessRoot,
                        boolean phaseCompleted,
                        String url,
                        String userName,
                        String pswd,
                        String replicationCollections,
                        String replicateImages)
            throws PhaseException {

        try {

            if (!getIterateFile().exists()) {

                File previousIterateFile =
                        getIterateFile(previousProcessRoot);

                try (FileChannel fiChannel =
                             new FileInputStream(previousIterateFile).getChannel();
                     FileChannel foChannel =
                             new FileOutputStream(createIterateFile()).getChannel()) {

                    long size = fiChannel.size();
                    fiChannel.transferTo(0, size, foChannel);
                }

            } else {
                this.start(url, userName, pswd,
                        replicationCollections, replicateImages);
            }

        } catch (IOException e) {
            throw new PhaseException(this, e);
        }
    }
}