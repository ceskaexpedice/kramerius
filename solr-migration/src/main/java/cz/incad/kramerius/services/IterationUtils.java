package cz.incad.kramerius.services;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.logging.Logger;

public class IterationUtils {

    public static Logger LOGGER = Logger.getLogger(IterationUtils.class.getName());

    private IterationUtils() {}

    public static void cursorIteration(Client client,String address, String masterQuery,IterationCallback callback, IterationEndCallback endCallback) throws ParserConfigurationException, MigrateSolrIndexException, SAXException, IOException, InterruptedException, BrokenBarrierException {
        List<SolrWorker>  worksWhatHasToBeDone = new ArrayList<>();
        String cursorMark = null;
        String queryCursorMark = null;
        do {
            Element element = MigrationUtils.pidsCursorQuery(client, address, masterQuery, cursorMark);
            cursorMark = MigrationUtils.findCursorMark(element);
            queryCursorMark = MigrationUtils.findQueryCursorMark(element);
            callback.call(element, cursorMark);
        } while((cursorMark != null && queryCursorMark != null) && !cursorMark.equals(queryCursorMark));
        // callback after iteration
        endCallback.end();
    }


    public static void queryFilterIteration(Client client, String address, String masterQuery,IterationCallback callback, IterationEndCallback endCallback) throws MigrateSolrIndexException, IOException, SAXException, ParserConfigurationException, BrokenBarrierException, InterruptedException {
        List<SolrWorker> worksWhatHasToBeDone = new ArrayList<>();
        String lastPid = null;
        String previousPid = null;
        do {
            Element element = pidsFilterQuery(client, address,masterQuery,  lastPid);
            previousPid = lastPid;
            lastPid = MigrationUtils.findLastPid(element);
            callback.call(element, lastPid);
        }while(lastPid != null  && !lastPid.equals(previousPid));
        // callback after iteration
        endCallback.end();
    }


    public static Element pidsFilterQuery(Client client, String url, String mq, String lastPid)
            throws ParserConfigurationException, SAXException, IOException, MigrateSolrIndexException {
        int rows = MigrationUtils.configuredRowsSize();
        String query = MigrationUtils.SELECT_ENDPOINT + "?q="+mq + (lastPid!= null ? String.format("&rows=%d&fq=PID:%s", rows, URLEncoder.encode("[\""+lastPid+"\" TO *]", "UTF-8")) : String.format("&rows=%d", rows))+"&sort=" + URLEncoder.encode(MigrationUtils.DEFAULT_SORT_FIELD, "UTF-8")+"&fl=PID";
        return executeQuery(client, url, query);
    }


    public interface IterationCallback {
            public void call(Element results, String iterationToken);
    }

    public interface IterationEndCallback {
        public void end();
    }

    static Element executeQuery(Client client, String url, String query) throws ParserConfigurationException, SAXException, IOException {
        LOGGER.info(String.format("[" + Thread.currentThread().getName() + "] processing %s", query));
        WebResource r = client.resource(url+(url.endsWith("/") ? "" : "/")+ query);
        String t = r.accept(MediaType.APPLICATION_XML).get(String.class);
        Document parseDocument = XMLUtils.parseDocument(new StringReader(t));
        return parseDocument.getDocumentElement();
    }

}
