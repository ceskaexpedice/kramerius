package cz.incad.kramerius.k5indexer;

/**
 *
 * @author Alberto
 */
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.resourceindex.IResourceIndex;
import cz.incad.kramerius.resourceindex.ResourceIndexService;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.virtualcollections.VirtualCollection;
import cz.incad.kramerius.virtualcollections.VirtualCollectionsManager;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.configuration.Configuration;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;

public class Indexer {

    private static final Logger logger = Logger.getLogger(Indexer.class.getName());
    public final ProgramArguments arguments;
    public final Configuration config;
    IResourceIndex rindex;
    Commiter commiter;

    XPathFactory factory = XPathFactory.newInstance();
    XPath xpath;
    XPathExpression expr;

    public static int total;
    public static int success;
    public static int skipped;
    public static int pdfpages;
    public static int errors;
    public static int warnings;

    public static String PID_PATH_SEPARATOR = "/";
    public static String PDF_PAGE_SEPARATOR = "/@";

    public static HashMap<String, String> models_cache = new HashMap<String, String>();
    public static HashMap<String, String> dates_cache = new HashMap<String, String>();
    public static HashMap<String, String> root_title_cache = new HashMap<String, String>();
    public static ArrayList<String> indexed_cache = new ArrayList<String>();
    public static HashMap<String, ArrayList<String>> pid_paths_cache = new HashMap<String, ArrayList<String>>();

    public Indexer(ProgramArguments args) throws Exception {
        arguments = args;
        config = KConfiguration.getInstance().getConfiguration();
        commiter = Commiter.getInstance();

        rindex = ResourceIndexService.getResourceIndexImpl();
        logger.info("Indexer initialized");
    }

    public void run() throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            Date date = new Date();
            DateFormat formatter = new SimpleDateFormat("");
            formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
            String to = formatter.format(date);
            logger.log(Level.INFO, "Current index time: {0}", date);

            Actions actionToDo = Actions.valueOf(arguments.action);
            actionToDo.doPerform(this);

            long timeInMiliseconds = System.currentTimeMillis() - startTime;
            showResults(timeInMiliseconds);

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Run failed", ex);
            throw new Exception(ex);
        }
    }

    public static ArrayList<String> getCollections(Indexer indexer) throws Exception {
        String query = "<fedora-model:hasModel> <info:fedora/model:collection>";
        ArrayList<String> collsList = new ArrayList<String>();
        String urlStr = indexer.config.getString("FedoraResourceIndex") + "?type=triples&flush=true&lang=spo&format=N-Triples&limit=&distinct=off&stream=off"
                + "&query=" + java.net.URLEncoder.encode(query, "UTF-8");
        java.net.URL url = new java.net.URL(urlStr);
        java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(url.openStream()));
        String inputLine;
        int end;
        while ((inputLine = in.readLine()) != null) {
            //<info:fedora/vc:534b8b98-82d8-49c7-a751-33e88aaeeea9> <info:fedora/fedora-system:def/model#hasModel> <info:fedora/model:collection>
            end = inputLine.indexOf(">");
            //13 je velikost   <info:fedora/
            inputLine = inputLine.substring(13, end);
            collsList.add(inputLine);
        }
        in.close();
        return collsList;
    }

    enum Actions {

        //Index collections
        INDEXCOLLECTIONS {
                    @Override
                    void doPerform(Indexer indexer) throws Exception {
                        List<VirtualCollection> collsList
                        = VirtualCollectionsManager.getVirtualCollections(new FedoraAccessImpl(KConfiguration.getInstance(), null), null);
                        Commiter commiter = new Commiter(KConfiguration.getInstance().getConfiguration().getString("k5indexer.solr.virtualCollections.core", "vc"));
                        for (VirtualCollection collection : collsList) {
                            logger.log(Level.INFO, "Reindex collection: {0}", collection.getLabel());
                            CollectionDocument cd = new CollectionDocument(collection, commiter);
                            cd.index();
                        }
                        commiter.commit();
                    }
                },
        //Index all documents in collection
        INDEXCOLLECTION {
                    @Override
                    void doPerform(Indexer indexer) throws Exception {
                        String pidSeparator = indexer.config.getString("k5indexer.pidSeparator", ";");
                        for (String collection : indexer.arguments.value.split(pidSeparator)) {
                            logger.log(Level.INFO, "Reindex documents in collection: {0}", collection);
                            if (collection == null || collection.length() < 1) {
                                return;
                            }
                            int rows = 100;
                            int offset = 0;
                            ArrayList<String> pids = indexer.rindex.getObjectsInCollection(collection, rows, offset);
                            //int modelsProcessed = 0;
                            while (!pids.isEmpty()) {
                                KrameriusDocument kdoc = new KrameriusDocument();
                                for (String pid : pids) {
                                    if (pid.startsWith("info:fedora/")) {
                                        pid = pid.substring("info:fedora/".length());
                                    }
                                    kdoc.indexOne(pid);
                                }
                                offset += rows;
                                pids = indexer.rindex.getObjectsInCollection(collection, rows, offset);
                            }
                        }
                    }
                },
        //Index all documents in collection if newer
        UPDATECOLLECTION {
                    @Override
                    void doPerform(Indexer indexer) throws Exception {
                        String pidSeparator = indexer.config.getString("k5indexer.pidSeparator", ";");
                        for (String collection : indexer.arguments.value.split(pidSeparator)) {
                            logger.log(Level.INFO, "Reindex documents in collection: {0}", collection);
                            if (collection == null || collection.length() < 1) {
                                return;
                            }
                            int rows = 100;
                            int offset = 0;
                            ArrayList<String> pids = indexer.rindex.getObjectsInCollection(collection, rows, offset);
                            //int modelsProcessed = 0;
                            while (!pids.isEmpty()) {
                                KrameriusDocument kdoc = new KrameriusDocument();
                                for (String pid : pids) {
                                    if (pid.startsWith("info:fedora/")) {
                                        pid = pid.substring("info:fedora/".length());
                                    }
                                    kdoc.updateOne(pid);
                                }
                                offset += rows;
                                pids = indexer.rindex.getObjectsInCollection(collection, rows, offset);
                            }
                        }
                    }
                },
        CHECK {
                    @Override
                    void doPerform(Indexer indexer) throws Exception {
                    }
                },
        //Index all documents in model, recursive down
        INDEXMODEL {
                    @Override
                    void doPerform(Indexer indexer) throws Exception {
                        String pidSeparator = indexer.config.getString("k5indexer.pidSeparator", ";");
                        for (String model : indexer.arguments.value.split(pidSeparator)) {
                            logger.log(Level.INFO, "MODEL {0}", model);
                            int rows = 100;
                            int offset = 0;
                            ArrayList<String> pids = indexer.rindex.getFedoraPidsFromModel(model, rows, offset);
                            //int modelsProcessed = 0;
                            while (!pids.isEmpty()) {
                                KrameriusDocument kdoc = new KrameriusDocument();
                                for (String pid : pids) {
                                    kdoc.indexDown(pid);
                                }
                                offset += rows;
                                pids = indexer.rindex.getFedoraPidsFromModel(model, rows, offset);
                            }
                        }
                    }
                },
        //Index document recursive down
        INDEXDOC {
                    @Override
                    void doPerform(Indexer indexer) throws Exception {
                        String pidSeparator = indexer.config.getString("k5indexer.pidSeparator", ";");
                        KrameriusDocument kdoc = new KrameriusDocument();
                        for (String pid : indexer.arguments.value.split(pidSeparator)) {
                            kdoc.indexDown(pid);
                        }
                    }
                },
        //Index document recursive down if newer.
        UPDATEDOC {
                    @Override
                    void doPerform(Indexer indexer) throws Exception {
                        String pidSeparator = indexer.config.getString("k5indexer.pidSeparator", ";");
                        KrameriusDocument kdoc = new KrameriusDocument();
                        for (String pid : indexer.arguments.value.split(pidSeparator)) {
                            kdoc.updateDown(pid);
                        }
                    }
                },
        //Delete document recursive down
        DELETEDOC {
                    @Override
                    void doPerform(Indexer indexer) throws Exception {
                        String pidSeparator = indexer.config.getString("k5indexer.pidSeparator", ";");
                        for (String pid_path : indexer.arguments.value.split(pidSeparator)) {
                            Commiter commiter = Commiter.getInstance();
                            String q = "pid_path:" + ClientUtils.escapeQueryChars(pid_path) + "*";
                            commiter.deleteByQuery(q);
                            commiter.commit();
                        }

                    }
                },
        FIX_ROOT_TITLE {
                    void fix(Indexer indexer, String pid, String title) throws Exception {
                        String query = "root_pid:\"" + pid + "\" AND -PID:\"" + pid + "\"";
                        IndexDocs roots = new IndexDocs(query);
                        roots.setFl("PID");
                        Iterator it = roots.iterator();
                        JSONArray ja = new JSONArray();
                        int i = 0;
                        int rows = indexer.config.getInt("k5indexer.batchSize", 100);
                        while (it.hasNext()) {
                            JSONObject doc = (JSONObject) it.next();
                            JSONObject addDoc = new JSONObject();
                            addDoc.put("PID", doc.getString("PID"));
                            addDoc.put("root_title", (new JSONObject()).put("set", title));
                            ja.put(addDoc);
                            Indexer.total++;
                            if (i++ > rows) {
                                logger.log(Level.FINE, ja.toString());
                                indexer.commiter.postJson(ja.toString());
                                i = 0;
                                ja = new JSONArray();
                                logger.log(Level.INFO, "{0} total processed", Indexer.total);
                            }
                        }
                        logger.log(Level.FINE, ja.toString());
                        indexer.commiter.postJson(ja.toString());
                    }

                    @Override
                    void doPerform(Indexer indexer) throws Exception {
                        Indexer.total = 0;
                        IndexDocs roots = new IndexDocs("level:0");
                        roots.setFl("PID,title");
                        Iterator it = roots.iterator();
                        while (it.hasNext()) {
                            JSONObject doc = (JSONObject) it.next();
                            String pid = doc.getString("PID");
                            String root_title = doc.getString("title");
                            fix(indexer, pid, root_title);
                        }
                        indexer.commiter.commit();
                    }
                },
        CONVERT {
                    @Override
                    void doPerform(Indexer indexer) throws Exception {
                        int sent = 0;
                        TransformerFactory tfactory = TransformerFactory.newInstance();
                        InputStream stylesheet = this.getClass().getResourceAsStream("/cz/incad/kramerius/k5indexer/res/convert.xsl");
                        StreamSource xslt = new StreamSource(stylesheet);
                        Transformer transformer = tfactory.newTransformer(xslt);

                        String k5Index = indexer.config.getString("k5indexer.convert.dest");

                        String k4Index = indexer.config.getString("k5indexer.convert.orig");
                        int rows = indexer.config.getInt("k5indexer.convert.rows", 100);
                        int total = 0;
                        IndexBrowser docs = new IndexBrowser();
                        docs.setWt("xml");
                        docs.setHost(k4Index);
                        docs.setFl(indexer.config.getString("k5indexer.convert.fl"));
                        if(indexer.arguments.value.startsWith("uuid:")){
                            docs.setStart(indexer.arguments.value);
                        }
                        Iterator it = docs.iterator();
                        int page = 0;
                        while (it.hasNext()) {
                            logger.log(Level.INFO, "retrieving...");
                            Document xml = (Document) it.next();
                            StreamResult destStream2 = new StreamResult(new StringWriter());
                            logger.fine("transforming...");
                            transformer.transform(new DOMSource(xml), destStream2);
                            StringWriter sw2 = (StringWriter) destStream2.getWriter();
                            logger.log(Level.INFO, "sending to index...");
                            indexer.commiter.postXml(k5Index, sw2.toString());
//                            page++;
//                            if (page > 200) {
//                                logger.log(Level.INFO, "commiting...");
//                                indexer.commiter.postXml(k5Index, "<commit softCommit = \"true\" />");
//                                page = 0;
//                            }
                            total += rows;
                            logger.log(Level.INFO, "{0} sent to index", total);
                        }
                        indexer.commiter.postXml(k5Index, "<commit />");
                    }
                };

        abstract void doPerform(Indexer indexer) throws Exception;
    }

    private static String formatElapsedTime(long timeInMiliseconds) {
        long hours, minutes, seconds;
        long timeInSeconds = timeInMiliseconds / 1000;
        hours = timeInSeconds / 3600;
        timeInSeconds = timeInSeconds - (hours * 3600);
        minutes = timeInSeconds / 60;
        timeInSeconds = timeInSeconds - (minutes * 60);
        seconds = timeInSeconds;
        return hours + " hour(s) " + minutes + " minute(s) " + seconds + " second(s)";
    }

    private static void showResults(long timeInMiliseconds) {
        logger.log(Level.INFO, "{0} total docs processed. {1} success, {2} skipped, {3} pdf pages, {4} warnings, {5} errors\n {6}",
                new Object[]{Indexer.total, Indexer.success,
                    Indexer.skipped, Indexer.pdfpages, Indexer.warnings, Indexer.errors,
                    formatElapsedTime(timeInMiliseconds)});
    }
}

