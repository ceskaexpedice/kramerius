package cz.incad.kramerius.indexer;

import cz.incad.kramerius.Constants;
import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.resourceindex.IResourceIndex;
import cz.incad.kramerius.resourceindex.ResourceIndexService;
import cz.incad.kramerius.utils.conf.KConfiguration;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import javax.xml.transform.stream.StreamSource;

import dk.defxws.fedoragsearch.server.GTransformer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.configuration.Configuration;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * performs the Solr specific parts of the operations
 */
public class SolrOperations {

    private static final Logger logger = Logger.getLogger(SolrOperations.class.getName());
    private static final String UNIQUEKEY = "PID";
    //private IndexReader ir = null;
    protected Configuration config;
    protected int insertTotal = 0;
    protected int updateTotal = 0;
    protected int deleteTotal = 0;
    protected int warnCount = 0;
    //protected String[] params = null;
    private FedoraOperations fedoraOperations;
    IResourceIndex rindex;
    ExtendedFields extendedFields;
    private GTransformer transformer;
    private ArrayList<String> customTransformations;

    public SolrOperations(FedoraOperations _fedoraOperations) throws IOException {
        fedoraOperations = _fedoraOperations;
        config = KConfiguration.getInstance().getConfiguration();
        transformer = new GTransformer();
        initCustomTransformations();
        extendedFields = new ExtendedFields(fedoraOperations);
    }

    public void updateIndex(String action, String value)
            throws java.rmi.RemoteException, Exception {
        rindex = ResourceIndexService.getResourceIndexImpl();
        insertTotal = 0;
        updateTotal = 0;
        deleteTotal = 0;
        int initDocCount = 0;
        int finalDocCount = 0;
        try {
            initDocCount = getDocCount();
            if ("deleteDocument".equals(action)) {
                deleteDocument(value);
            } else if ("deleteModel".equals(action)) {
                deleteModel(value);
            } else if ("deletePid".equals(action)) {
                deletePid(value);
            } else if ("fromPid".equals(action)) {
                fromPid(value);
            } else if ("fullRepo".equals(action)) {
                fullRepo();
            } else if ("fullRepoWithClean".equals(action)) {
                fullRepoWithClean();
            } else if ("optimize".equals(action)) {
                optimize();
            } else if ("fromKrameriusModel".equals(action)) {
                deleteDocument(value);
                fromKrameriusModel(value);
                optimize();
            } else if ("fromKrameriusModelNoCheck".equals(action)) {
                fromKrameriusModel(value);
                optimize();
            } else if ("krameriusModel".equals(action)) {
                deleteModel(value);
                krameriusModel(value);
                optimize();
            } else if ("krameriusModelNoCheck".equals(action)) {
                krameriusModel(value);
                optimize();
            } else if ("reindexDoc".equals(action)) {
                reindexDoc(value, false);
            } else if ("reindexDocForced".equals(action)) {
                reindexDoc(value, true);
            } else if ("checkIntegrity".equals(action)) {
                checkIntegrity();
            } else if ("checkIntegrityByModel".equals(action)) {
                checkIntegrityByModel(value);
            } else if ("checkIntegrityByDocument".equals(action)) {
                checkIntegrityByDocument(value);
            } else if ("getPidPaths".equals(action)) {
                fedoraOperations.getPidPaths(value);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Cant index. Action:" + action +
                    " Value: " + value, ex);
        } finally {

            finalDocCount = getDocCount();
            logger.log(Level.FINE, "initDocCount={0} docCount={1} updateTotal={2}", new Object[]{initDocCount, finalDocCount, updateTotal});

            if (updateTotal > 0) {
                int diff = finalDocCount - initDocCount;
                insertTotal = diff;
                updateTotal -= diff;
            }
            finalDocCount = finalDocCount - deleteTotal;
        }
        logger.log(Level.INFO, " {0} docCount={1}", new Object[]{action, finalDocCount});
        logger.log(Level.INFO, "insertTotal: {0}; updateTotal: {1}; deleteTotal: {2}; warnCount: {3}", new Object[]{insertTotal, updateTotal, deleteTotal, warnCount});

    }

    private int getDocCount() {
        try {
            String urlStr = config.getString("solrHost") + "/select/?q=*:*&rows=0";
            factory = XPathFactory.newInstance();
            xpath = factory.newXPath();
            java.net.URL url = new java.net.URL(urlStr);
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            org.w3c.dom.Document solrDom = builder.parse(url.openStream());
            String xPathStr = "/response/result/@numFound";
            expr = xpath.compile(xPathStr);
            Node node = (Node) expr.evaluate(solrDom, XPathConstants.NODE);
            return Integer.parseInt(node.getFirstChild().getNodeValue());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving index doc count", e);
            return 0;
        }
    }

    private void optimize()
            throws java.rmi.RemoteException, Exception {
        StringBuilder sb = new StringBuilder("<optimize/>");
        logger.log(Level.FINE, "indexDoc=\n{0}", sb.toString());

        postData(config.getString("IndexBase") + "/update", new StringReader(sb.toString()), new StringBuilder());

    }

    private void reindexDoc(String uuid, boolean force)
            throws java.rmi.RemoteException, IOException, Exception {
        if (uuid == null || uuid.length() < 1) {
            return;
        }
        try {
            String urlStr = config.getString("solrHost") + "/select/?q=PID:\"" + uuid + "\"";

            fedoraOperations.getFoxmlFromPid(uuid);
            //contentDom = getDocument(new ByteArrayInputStream(fedoraOperations.foxmlRecord));
            factory = XPathFactory.newInstance();
            xpath = factory.newXPath();

            /* get current values */
            java.net.URL url = new java.net.URL(urlStr);

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            org.w3c.dom.Document solrDom = builder.parse(url.openStream());
            String xPathStr = "/response/result/doc/str[@name='root_pid']";
            expr = xpath.compile(xPathStr);
            Node node = (Node) expr.evaluate(solrDom, XPathConstants.NODE);
            //System.out.println(node.getFirstChild().getNodeValue());

            xPathStr = "/response/result/doc/date[@name='modified_date']";
            expr = xpath.compile(xPathStr);
            node = (Node) expr.evaluate(solrDom, XPathConstants.NODE);
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            Date date = formatter.parse(node.getFirstChild().getNodeValue());

            indexByPid(uuid, date, force, new ByteArrayInputStream(fedoraOperations.foxmlRecord));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error reindexing doc " + uuid, ex);
        }
    }

    private void fromPid(
            String pid)
            throws java.rmi.RemoteException, IOException, Exception {
        if (pid == null || pid.length() < 1) {
            return;
        }
        fedoraOperations.getFoxmlFromPid(pid);
        extendedFields.setFields(pid);
        indexDoc(new ByteArrayInputStream(fedoraOperations.foxmlRecord), "1");
    }
    /* kramerius */
    XPathFactory factory = XPathFactory.newInstance();
    XPath xpath;
    XPathExpression expr;
    Document contentDom;

    private void fullRepo() {
        String[] models = config.getStringArray("fedora.topLevelModels");
        for (String model : models) {
            krameriusModel(model);

        }
    }

    private void fullRepoWithClean() throws Exception {
        clearIndex();
        fullRepo();
    }

    private void clearIndex() throws Exception {
        StringBuilder sb = new StringBuilder("<delete><query>*:*</query></delete>");
        logger.log(Level.FINE, "indexDoc=\n{0}", sb.toString());

        postData(config.getString("IndexBase") + "/update", new StringReader(sb.toString()), new StringBuilder());
        deleteTotal++;
    }

    private void krameriusModel(String model,
            int offset) {
        int pageSize = 100;
        try {
            boolean hasRecords = false;

            org.w3c.dom.Document doc = rindex.getFedoraObjectsFromModelExt(model, pageSize, offset, "date", "asc");
            factory = XPathFactory.newInstance();
            xpath = factory.newXPath();

            xpath.setNamespaceContext(new FedoraNamespaceContext());
            String xPathStr = "/sparql:sparql/sparql:results/sparql:result/sparql:object";
            expr = xpath.compile(xPathStr);
            NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            String pid;

            for (int i = 0; i < nodes.getLength(); i++) {
                Node childnode = nodes.item(i);
                pid = childnode.getAttributes().getNamedItem("uri").getNodeValue();
                pid = pid.replaceAll("info:fedora/", "");
                fromKrameriusModel(pid);
                hasRecords = true;
            }
            if (hasRecords) {
                krameriusModel(model, offset + pageSize);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        }

    }

    private void krameriusModel(String model) {
        try {
            logger.log(Level.INFO, "Indexing from kramerius model: {0}", model);
            krameriusModel(model, 0);

        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        }
    }

    private void fromKrameriusModel(String uuid)
            throws java.rmi.RemoteException, Exception {
        if (uuid == null || uuid.length() < 1) {
            return;
        }
        logger.log(Level.FINE, "fromKrameriusModel: {0}", uuid);

        fedoraOperations.getFoxmlFromPid(uuid);
        factory = XPathFactory.newInstance();
        xpath = factory.newXPath();

        indexByPid(uuid, null, true, new ByteArrayInputStream(fedoraOperations.foxmlRecord));

    }

    private Document getDocument(InputStream foxmlStream) throws Exception {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(false);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        //InputSource source = new InputSource(new StringReader(result.toString()));

        return builder.parse(foxmlStream);
    }

    private int indexByPid(String pid,
            Date date,
            boolean force,
            InputStream foxmlStream) {

        logger.log(Level.INFO, "indexing -> {0}; count: {1}", new Object[]{pid, updateTotal});
        int num = 0;
        ArrayList<String> pids = new ArrayList<String>();
        ArrayList<String> models = new ArrayList<String>();
        try {
            contentDom = getDocument(foxmlStream);
            //tady testujeme datum
            if (date != null) {
                expr = xpath.compile("//objectProperties/property[@NAME='info:fedora/fedora-system:def/view#lastModifiedDate']/@VALUE");
                Node dateNode = (Node) expr.evaluate(contentDom, XPathConstants.NODE);
                if (dateNode != null) {
                    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    Date dateValue = formatter.parse(dateNode.getNodeValue());
                    if (!dateValue.after(date)) {
                        if (!force) {
                            logger.info(String.format("Document %s is up to date. Skipping", pid));
                            return getActualNumOfPagesFromSolr(pid);
                        }
                    }
                }
            }

            String fmodel = "";
            expr = xpath.compile("//datastream/datastreamVersion[last()]/xmlContent/RDF/Description/hasModel");
            Node modelNode = (Node) expr.evaluate(contentDom, XPathConstants.NODE);
            if (modelNode != null) {
                fmodel = modelNode.getAttributes().getNamedItem("rdf:resource").getNodeValue().split("model:")[1];

            }
            //tady testujeme pripadne vicestrankovy pdf
            ///foxml:digitalObject/foxml:datastream[@ID='IMG_FULL']/foxml:datastreamVersion[last()]
            expr = xpath.compile("//datastream[@ID='IMG_FULL']/datastreamVersion[last()]");
            Node imgFullMimeNode = (Node) expr.evaluate(contentDom, XPathConstants.NODE);
            int docs = 1;
            if (imgFullMimeNode != null) {
                if (imgFullMimeNode.getAttributes().getNamedItem("MIMETYPE").getNodeValue().indexOf("pdf") > -1) {
                    docs = fedoraOperations.getPdfPagesCount(pid, "IMG_FULL");
                }
            }

            expr = xpath.compile("//datastream/datastreamVersion[last()]/xmlContent/RDF/Description/*");
            NodeList nodes = (NodeList) expr.evaluate(contentDom, XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++) {
                Node childnode = nodes.item(i);
                String nodeName = childnode.getNodeName();
                if (nodeName.indexOf(":") > 0) {
                    nodeName = nodeName.substring(nodeName.indexOf(":") + 1);
                }

                if (nodeName.contains("hasPage")) {
                    num++;
                }
                if (!nodeName.contains("hasModel") && !nodeName.contains("isOnPage")
                        && !nodeName.contains("hasDonator")
                        && childnode.hasAttributes()
                        && childnode.getAttributes().getNamedItem("rdf:resource") != null) {
                    pids.add(childnode.getAttributes().getNamedItem("rdf:resource").getNodeValue().split("/")[1]);
                    //models.add(KrameriusModels.toString(RDFModels.convertRDFToModel(nodeName)));
                    models.add(nodeName);
                } else {
                }
            }
            
            extendedFields.setFields(pid);
            indexDoc(foxmlStream, String.valueOf(docs - 1));

            for (int i = 0; i < pids.size(); i++) {
                String relpid = pids.get(i);
                String model = models.get(i);
                try {
                    byte[] foxmlRecord2 = fedoraOperations.getAndReturnFoxmlFromPid(relpid);
                    InputStream foxmlStream2 = new ByteArrayInputStream(foxmlRecord2);
                    contentDom = getDocument(foxmlStream2);
                    foxmlStream2.reset();
                    num += indexByPid(relpid, date, force, foxmlStream2);
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "Can't index doc: " + relpid + " Continuing...", ex);
                }
            }

            num += docs - 1;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "indexByPid error", e);
        }
        return num;
    }
    /* kramerius */

    private void indexDoc(
            InputStream foxmlStream,
            String docCount)
            throws java.rmi.RemoteException, IOException, Exception {

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("DOCCOUNT", docCount);
        params.put("PAGENUM", "0");

        String xsltPath = config.getString("UpdateIndexDocXslt");

        for (int i = 0; i <= Integer.parseInt(docCount); i++) {
            foxmlStream.reset();
            params.put("PAGENUM", i + "");
            StringBuffer sb = transformer.transform(
                    xsltPath,
                    new StreamSource(foxmlStream),
                    null,
                    params,
                    true);

            applyCustomTransformations(sb, foxmlStream, params);
            //logger.info("indexDoc=\n" + sb.toString());
            String doc = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><add><doc>"
                    + sb.toString()
                    + extendedFields.toXmlString(i)
                    + "</doc></add>";
            //logger.info(doc);
            logger.log(Level.FINE, "indexDoc=\n{0}", sb.toString());
            if (sb.indexOf("name=\"" + UNIQUEKEY) > 0) {
                postData(config.getString("IndexBase") + "/update", new StringReader(doc), new StringBuilder());
                updateTotal++;
            }
        }
    }

    private void initCustomTransformations() {
        customTransformations = new ArrayList<String>();
        String dirname = Constants.WORKING_DIR + File.separator + "indexer" + File.separator + "xsl";

        File dir = new File(dirname);
        if (dir.exists() && dir.isDirectory()) {
            for (File f : dir.listFiles()) {
                customTransformations.add(f.getAbsolutePath());
            }
        }

    }

    private void applyCustomTransformations(StringBuffer sb, InputStream foxmlStream, HashMap<String, String> params) throws Exception {
        for (String f : customTransformations) {
            foxmlStream.reset();
            StringBuffer newSb = transformer.transform(
                    f,
                    new StreamSource(foxmlStream),
                    null,
                    params,
                    false);
            //logger.info("newSb: " +newSb.toString());
            sb.append(newSb);
        }
    }

    private void deletePid(String pid) throws Exception {
        StringBuilder sb = new StringBuilder("<delete><query>PID:" + pid.replace(":", "\\:") + "</query></delete>");
        logger.log(Level.FINE, "indexDoc=\n{0}", sb.toString());
        postData(config.getString("IndexBase") + "/update", new StringReader(sb.toString()), new StringBuilder());
        deleteTotal++;
    }

    private void deleteDocument(String pid_path) throws Exception {
        StringBuilder sb = new StringBuilder("<delete><query>pid_path:" + pid_path.replace(":", "\\:") + "*</query></delete>");
        logger.log(Level.FINE, "indexDoc=\n{0}", sb.toString());
        postData(config.getString("IndexBase") + "/update", new StringReader(sb.toString()), new StringBuilder());
        sb = new StringBuilder("<delete><query>pid_path:" + pid_path.replace(":", "\\:") + "</query></delete>");
        postData(config.getString("IndexBase") + "/update", new StringReader(sb.toString()), new StringBuilder());
        optimize();
        deleteTotal++;
    }

    private void deleteModel(String path) throws Exception {
        StringBuilder sb = new StringBuilder("<delete><query>path:" + path + "*</query></delete>");
        logger.log(Level.FINE, "indexDoc=\n{0}", sb.toString());
        postData(config.getString("IndexBase") + "/update", new StringReader(sb.toString()), new StringBuilder());
        optimize();
        deleteTotal++;
    }

    /**
     * Reads data from the data reader and posts it to solr,
     * writes the response to output
     */
    private void postData(String solrUrlString, Reader data, StringBuilder output)
            throws Exception {
        URL solrUrl = null;
        try {
            solrUrl = new URL(solrUrlString);
        } catch (MalformedURLException e) {
            throw new Exception("solrUrl=" + solrUrlString + ": ", e);
        }
        HttpURLConnection urlc = null;
        String POST_ENCODING = "UTF-8";
        try {
            urlc = (HttpURLConnection) solrUrl.openConnection();
            try {
                urlc.setRequestMethod("POST");
            } catch (ProtocolException e) {
                throw new Exception("Shouldn't happen: HttpURLConnection doesn't support POST??", e);
            }
            urlc.setDoOutput(true);
            urlc.setDoInput(true);
            urlc.setUseCaches(false);
            urlc.setAllowUserInteraction(false);
            urlc.setRequestProperty("Content-type", "text/xml; charset=" + POST_ENCODING);

            OutputStream out = urlc.getOutputStream();

            try {
                Writer writer = new OutputStreamWriter(out, POST_ENCODING);
                pipe(data, writer);
                writer.close();
            } catch (IOException e) {
                throw new Exception("IOException while posting data", e);
            } finally {
                if (out != null) {
                    out.close();
                }
            }

            InputStream in = urlc.getInputStream();
            int status = urlc.getResponseCode();
            StringBuilder errorStream = new StringBuilder();
            try {
                if (status != HttpURLConnection.HTTP_OK) {
                    errorStream.append("postData URL=").append(solrUrlString).append(" HTTP response code=").append(status).append(" ");
                    throw new Exception("URL=" + solrUrlString + " HTTP response code=" + status);
                }
                Reader reader = new InputStreamReader(in);
                pipeString(reader, output);
                reader.close();
            } catch (IOException e) {
                throw new Exception("IOException while reading response", e);
            } finally {
                if (in != null) {
                    in.close();
                }
            }

            InputStream es = urlc.getErrorStream();
            if (es != null) {
                try {
                    Reader reader = new InputStreamReader(es);
                    pipeString(reader, errorStream);
                    reader.close();
                } catch (IOException e) {
                    throw new Exception("IOException while reading response", e);
                } finally {
                    if (es != null) {
                        es.close();
                    }
                }
            }
            if (errorStream.length() > 0) {
                throw new Exception("postData error: " + errorStream.toString());
            }

        } catch (IOException e) {
            throw new Exception("Connection error (is Solr running at " + solrUrl + " ?): " + e);
        } finally {
            if (urlc != null) {
                urlc.disconnect();
            }
        }
    }

    /**
     * Pipes everything from the reader to the writer via a buffer
     */
    private static void pipe(Reader reader, Writer writer) throws IOException {
        char[] buf = new char[1024];
        int read = 0;
        while ((read = reader.read(buf)) >= 0) {
            writer.write(buf, 0, read);
        }
        writer.flush();
    }

    /**
     * Pipes everything from the reader to the writer via a buffer
     * except lines starting with '<?'
     */
    private static void pipeString(Reader reader, StringBuilder writer) throws IOException {
        char[] buf = new char[1024];
        int read = 0;
        while ((read = reader.read(buf)) >= 0) {
            if (!(buf[0] == '<' && buf[1] == '?')) {
                writer.append(buf, 0, read);
            }
        }
    }

    private int getActualNumOfPagesFromSolr(String uuid) {
        if (uuid == null || uuid.length() < 1) {
            return 0;
        }
        try {
            String urlStr = config.getString("solrHost") + "/select/?fl=pages_count&q=PID:\"" + uuid + "\"";
            fedoraOperations.getFoxmlFromPid(uuid);
            contentDom = getDocument(new ByteArrayInputStream(fedoraOperations.foxmlRecord));
            factory = XPathFactory.newInstance();
            xpath = factory.newXPath();
            java.net.URL url = new java.net.URL(urlStr);

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            org.w3c.dom.Document solrDom = builder.parse(url.openStream());
            String xPathStr = "/response/result/doc/int[@name='pages_count']";
            expr = xpath.compile(xPathStr);
            Node node = (Node) expr.evaluate(solrDom, XPathConstants.NODE);
            //System.out.println(node.getFirstChild().getNodeValue());
            return Integer.parseInt(node.getFirstChild().getNodeValue());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Can't retrieve actual number of pages.", e);
            return 0;
        }
    }

    private void checkIntegrityByDocument(String pid_path) throws Exception {
        checkIntegrityByDocument(pid_path, 0);
    }

    private void checkIntegrityByDocument(String pid_path, int offset) throws Exception {
        logger.log(Level.INFO, "checkIntegrityByDocument. offset: {0}", offset);
        if (pid_path == null || pid_path.length() < 1) {
            return;
        }
        int numHits = 200;
        String PID;
        String urlStr = config.getString("solrHost") + "/select/?q=pid_path:" + pid_path.replace(":", "\\:")
                + "*&fl=PID&start=" + offset + "&rows=" + numHits;
        factory = XPathFactory.newInstance();
        xpath = factory.newXPath();
        java.net.URL url = new java.net.URL(urlStr);

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document solrDom = builder.parse(url.openStream());
        String xPathStr = "/response/result/doc/str[@name='PID']";
        expr = xpath.compile(xPathStr);
        NodeList nodeList = (NodeList) expr.evaluate(solrDom, XPathConstants.NODESET);
        Node node;
        for (int i = 0; i < nodeList.getLength(); i++) {
            node = nodeList.item(i);
            PID = node.getFirstChild().getNodeValue();
            try {
                fedoraOperations.fa.getAPIM().getObjectXML(PID);
            } catch (Exception e) {
                logger.log(Level.INFO, PID + " doesn't exist. Deleting...");
                deletePid(PID);
            }
        }

        if (nodeList.getLength() > 0) {
            checkIntegrityByDocument(pid_path, offset + numHits);
        }
    }

    private void checkIntegrity() throws Exception {
        String[] models = config.getStringArray("fedora.topLevelModels");
        for (String model : models) {
            checkIntegrityByModel(model, 0);
        }
    }

    private void checkIntegrityByModel(String model) throws Exception {
        checkIntegrityByModel(model, 0);
        optimize();
    }

    private void checkIntegrityByModel(String model, int offset) throws Exception {
        logger.log(Level.INFO, "checkIntegrityByModel. model: {0}; offset: {1}", new String[]{model, Integer.toString(offset)});
        if (model == null || model.length() < 1) {
            return;
        }
        int numHits = 200;
        String PID;
        String pid_path;
        String urlStr = config.getString("solrHost") + "/select/?q=model_path:" + model + "*&fl=PID,pid_path&start="
                + offset + "&rows=" + numHits;
        factory = XPathFactory.newInstance();
        xpath = factory.newXPath();
        java.net.URL url = new java.net.URL(urlStr);

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document solrDom = builder.parse(url.openStream());
        String xPathStr = "/response/result/doc/str[@name='PID']";
        expr = xpath.compile(xPathStr);
        NodeList nodeList = (NodeList) expr.evaluate(solrDom, XPathConstants.NODESET);
        Node node;
        for (int i = 0; i < nodeList.getLength(); i++) {
            node = nodeList.item(i);
            PID = node.getFirstChild().getNodeValue();
            pid_path = node.getNextSibling().getFirstChild().getNodeValue();
            
            if(!rindex.existsPid(PID)){
                logger.log(Level.INFO, PID + " doesn't exist. Deleting...");
                deletePid(PID);
            }
//            try {
//                fedoraOperations.fa.getAPIM().getObjectXML(PID);
//            } catch (Exception e) {
//                logger.log(Level.INFO, PID + " doesn't exist. Deleting...", e);
//                deleteDocument(pid_path);
//            }
        }
        if (nodeList.getLength() > 0) {
            checkIntegrityByModel(model, offset + numHits);
        }
    }
}
