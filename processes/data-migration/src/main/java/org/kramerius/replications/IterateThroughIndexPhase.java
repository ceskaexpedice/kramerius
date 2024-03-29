package org.kramerius.replications;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.ws.rs.core.MediaType;
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

public class IterateThroughIndexPhase extends  AbstractPhase {

    public static final Logger LOGGER = Logger.getLogger(IterateThroughIndexPhase.class.getName());


    public static final String POSTFIX = "api/v5.0/search?q=*:*";
    public static final int ROWS = 1000;

    @Override
    public void start(String url, String userName, String pswd, String replicationCollections, String replicateImages) throws PhaseException {
        try {
            // store description file
            File descriptionFile = createDescriptionFile();
            JSONObject object = new JSONObject();
            object.put("handle", url);
            FileUtils.write(descriptionFile, object.toString(), "UTF-8");

            StringBuilder builder = new StringBuilder("{'pids':[");
            boolean first = true;
            boolean userCursor = KConfiguration.getInstance().getConfiguration().getBoolean("solr.migration.usecursor", false);
            LOGGER.info(userCursor ? "Using cursor": "Using paging");
            for (String pid: userCursor ? cursorPids(url) :  pagingPids(url)) {
                if (!pid.contains("@")) {
                    if (!first) { builder.append(','); }
                    builder.append('\'').append(pid).append('\'');
                    first = false;
                }
            }
            builder.append("]}");

            FileUtils.write(createIterateFile(), builder.toString(), Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw new PhaseException(this,e);
        } catch (ParserConfigurationException e) {
            throw new PhaseException(this,e);
        } catch (SAXException e) {
            throw new PhaseException(this,e);
        }

    }

    public static List<String> cursorPids(String surl) throws PhaseException, IOException, ParserConfigurationException, SAXException {
        List<String> retvals = new ArrayList<>();
        int rows = ROWS;
        int fetchedRows = 0;
        String cursorMark = "*";
        Client c = Client.create();
        do {
            String url = cursorUrl(surl, rows, cursorMark);
            WebResource r = c.resource(url);
            String t = r.accept(MediaType.APPLICATION_JSON).get(String.class);
            Document document = XMLUtils.parseDocument(new StringReader(t));
            String nCursor = nextCursor(document);
            if (!nCursor.equals(cursorMark)) {
                cursorMark = nCursor;
                List<String> pids = XMLUtils.getElementsRecursive(document.getDocumentElement(), (elm) -> {
                    if (elm.getNodeName().equals("str")) {
                        return elm.getAttribute("name").equals("PID");
                    } else return false;
                }).stream().map((elm)->elm.getTextContent()).collect(Collectors.toList());
                retvals.addAll(pids);
                fetchedRows = pids.size();
            } else break;
        } while(fetchedRows > 0);

        return  retvals;
    }

    public static List<String> pagingPids(String surl) throws PhaseException, IOException, ParserConfigurationException, SAXException {
        List<String> retvals = new ArrayList<>();
        int rows = ROWS;
        int numberOfDocs = 0;
        String lastPid = null;
        Client c = Client.create();
        do {
            String url = pagingUrl(surl, ROWS, lastPid);
            WebResource r = c.resource(url);
            String t = r.accept(MediaType.APPLICATION_XML).get(String.class);
            Document document = XMLUtils.parseDocument(new StringReader(t));
            numberOfDocs = numFound(document);
            List<String> pids = XMLUtils.getElementsRecursive(document.getDocumentElement(), (elm) -> {
                if (elm.getNodeName().equals("str")) {
                    return elm.getAttribute("name").equals("PID");
                } else return false;
            }).stream().map((elm)->elm.getTextContent()).collect(Collectors.toList());

            if(!pids.isEmpty()) {
                lastPid = pids.get(0);
            }

            retvals.addAll(pids);


        } while( numberOfDocs > 0 );
        return  retvals;
    }


    private static String nextCursor(Document doc) {
        Element elm = XMLUtils.findElement(doc.getDocumentElement(), (element) -> {
            if (element.getNodeName().equals("str") && element.getAttribute("name").equals("nextCursorMark")) {
                return true;
            } else return false;
        });
        return elm != null ? elm.getTextContent() : "*";
    }

    private static String pagingUrl(String surl,int rows,String pid) throws UnsupportedEncodingException {
        return surl + (surl.endsWith("/") ? "" : "/") + POSTFIX+"&fl=PID&sort="+getSortField()+"+asc&rows="+rows+(pid != null ? "&fq="+URLEncoder.encode("PID:{\"" + pid + "\" TO *]", "UTF-8") : "")+"&wt=xml";
    }
    private static String cursorUrl(String surl, int rows, String cursorMark) {
        return surl + (surl.endsWith("/") ? "" : "/") + POSTFIX+"&rows="+rows+"&sort="+getSortField()+"+asc&cursorMark="+cursorMark+"&wt=xml&fl=PID";
    }

    private static int numFound(Document doc) {
        Element elm = XMLUtils.findElement(doc.getDocumentElement(), (element) -> {
            if (element.getNodeName().equals("result")) {
                return true;
            } else return false;
        });
        return elm != null ? Integer.parseInt(elm.getAttribute("numFound")) : 0;
    }



    @Override
    public void restart(String previousProcessUUID, File previousProcessRoot, boolean phaseCompleted, String url, String userName, String pswd, String replicationCollections, String replicateImages) throws PhaseException {
        try {
            if (!getIterateFile().exists()) {
                File previousIterateFile = getIterateFile(previousProcessRoot);
                FileChannel fiChannel = new FileInputStream(previousIterateFile).getChannel();
                FileChannel foChannel = new FileOutputStream(createIterateFile()).getChannel();

                long size = fiChannel.size();
                fiChannel.transferTo(0, size, foChannel);

                // preparse if scenario is valid
                // preparseIterate();
            } else {
                this.start(url, userName, pswd, replicationCollections, replicateImages);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
