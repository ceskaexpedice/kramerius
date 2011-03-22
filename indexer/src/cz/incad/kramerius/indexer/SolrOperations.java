package cz.incad.kramerius.indexer;

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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;

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
import org.apache.lucene.store.SimpleFSDirectory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * performs the Solr specific parts of the operations
 */
public class SolrOperations {

    private static final Logger logger = Logger.getLogger(SolrOperations.class.getName());
    private static final String UNIQUEKEY = "PID";
    private IndexReader ir = null;
    protected Configuration config;
    protected int insertTotal = 0;
    protected int updateTotal = 0;
    protected int deleteTotal = 0;
    protected int docCount = 0;
    protected int warnCount = 0;
    //protected String[] params = null;
    private FedoraOperations fedoraOperations;
    IResourceIndex rindex;

    public SolrOperations(FedoraOperations _fedoraOperations) {
        fedoraOperations = _fedoraOperations;
        config = KConfiguration.getInstance().getConfiguration();
    }

    public void updateIndex(
            String action,
            String value,
            ArrayList<String> requestParams)
            throws java.rmi.RemoteException, Exception {
        rindex = ResourceIndexService.getResourceIndexImpl();
        insertTotal = 0;
        updateTotal = 0;
        deleteTotal = 0;
        int initDocCount = 0;

        try {

            getIndexReader();
            initDocCount = docCount;
            closeIndexReader();
            if ("deleteDocument".equals(action)) {
                deleteDocument(value);
            } else if ("deleteModel".equals(action)) {
                deleteModel(value);
            } else if ("deletePid".equals(action)) {
                deletePid(value);
            } else if ("fromPid".equals(action)) {
                fromPid(value, requestParams);
            } else if ("fullRepo".equals(action)) {
                fullRepo();
            } else if ("fullRepoWithClean".equals(action)) {
                fullRepoWithClean();
            } else if ("optimize".equals(action)) {
                optimize();
            } else if ("fromKrameriusModel".equals(action)) {
                if (!value.startsWith("uuid:")) {
                    value = "uuid:" + value;
                }
                fromKrameriusModel(value, requestParams);
            } else if ("krameriusModel".equals(action)) {
                krameriusModel(value, requestParams);
            } else if ("reindexDoc".equals(action)) {
                reindexDoc(value, false, requestParams);
            } else if ("reindexDocForced".equals(action)) {
                reindexDoc(value, true, requestParams);
            } else if ("checkIntegrityByModel".equals(action)) {
                checkIntegrityByModel(value);
            } else if ("checkIntegrityByDocument".equals(action)) {
                checkIntegrityByDocument(value);
            }

        } catch (Exception ex) {
            logger.severe(ex.toString());
        } finally {

            getIndexReader();
            closeIndexReader();
            logger.fine("initDocCount=" + initDocCount + " docCount=" + docCount + " updateTotal=" + updateTotal);

            if (updateTotal > 0) {
                int diff = docCount - initDocCount;
                insertTotal = diff;
                updateTotal -= diff;
            }
            docCount = docCount - deleteTotal;
        }
        logger.info("updateIndex " + action + " indexDirSpace=" + indexDirSpace(new File(config.getString("IndexDir"))) + " docCount=" + docCount);
        logger.info("insertTotal: " + insertTotal + "; updateTotal: " + updateTotal
                + "; deleteTotal: " + deleteTotal
                + "; warnCount: " + warnCount);

    }

    private void optimize()
            throws java.rmi.RemoteException, Exception {
        StringBuilder sb = new StringBuilder("<optimize/>");
        logger.fine("indexDoc=\n" + sb.toString());

        postData(config.getString("IndexBase") + "/update", new StringReader(sb.toString()), new StringBuilder());

    }

    private void reindexDoc(
            String pid,
            boolean force,
            ArrayList<String> requestParams)
            throws java.rmi.RemoteException, IOException, Exception {
        if (pid == null || pid.length() < 1) {
            return;
        }
        try {
            String urlStr = config.getString("solrHost") + "/select/select?q=PID:\"" + pid + "\"";
            String uuid = pid.startsWith("uuid:") ? pid : "uuid:" + pid;
            fedoraOperations.getFoxmlFromPid(uuid);
            contentDom = getDocument(new ByteArrayInputStream(fedoraOperations.foxmlRecord));
            factory = XPathFactory.newInstance();
            xpath = factory.newXPath();
            IndexParams indexParams = new IndexParams(uuid, contentDom);

            /* get current values */
            java.net.URL url = new java.net.URL(urlStr);

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            org.w3c.dom.Document solrDom = builder.parse(url.openStream());
            String xPathStr = "/response/result/doc/str[@name='root_pid']";
            expr = xpath.compile(xPathStr);
            Node node = (Node) expr.evaluate(solrDom, XPathConstants.NODE);
            //System.out.println(node.getFirstChild().getNodeValue());
            indexParams.setParam("ROOT_PID", node.getFirstChild().getNodeValue());

            xPathStr = "/response/result/doc/arr[@name='parent_pid']/str";
            expr = xpath.compile(xPathStr);
            node = (Node) expr.evaluate(solrDom, XPathConstants.NODE);
            if (node != null && node.hasChildNodes()) {
                indexParams.setParam("PARENT_PID", node.getFirstChild().getNodeValue());
            }

            xPathStr = "/response/result/doc/str[@name='parent_model']";
            expr = xpath.compile(xPathStr);
            node = (Node) expr.evaluate(solrDom, XPathConstants.NODE);
            if (node != null && node.hasChildNodes()) {
                indexParams.setParam("PARENT_MODEL", node.getFirstChild().getNodeValue());
            }

            xPathStr = "/response/result/doc/str[@name='pid_path']";
            expr = xpath.compile(xPathStr);
            node = (Node) expr.evaluate(solrDom, XPathConstants.NODE);
            if (node != null && node.hasChildNodes()) {
                indexParams.setParam("PID_PATH", node.getFirstChild().getNodeValue());
            }

            xPathStr = "/response/result/doc/str[@name='root_title']";
            expr = xpath.compile(xPathStr);
            node = (Node) expr.evaluate(solrDom, XPathConstants.NODE);
            indexParams.setParam("ROOT_TITLE", node.getFirstChild().getNodeValue());


            xPathStr = "/response/result/doc/str[@name='path']";
            expr = xpath.compile(xPathStr);
            node = (Node) expr.evaluate(solrDom, XPathConstants.NODE);
            String path = node.getFirstChild().getNodeValue();
            //jen do posledni
            if (path.indexOf("/") > -1) {
                path = path.substring(0, path.lastIndexOf("/"));
                indexParams.setParam("PATH", path);
            } else {
                indexParams.removeParam("PATH");
            }

            xPathStr = "/response/result/doc/str[@name='root_model']";
            expr = xpath.compile(xPathStr);
            node = (Node) expr.evaluate(solrDom, XPathConstants.NODE);
            indexParams.setParam("ROOT_MODEL", node.getFirstChild().getNodeValue());

            xPathStr = "/response/result/doc/int[@name='level']";
            expr = xpath.compile(xPathStr);
            node = (Node) expr.evaluate(solrDom, XPathConstants.NODE);
            indexParams.setParam("LEVEL", node.getFirstChild().getNodeValue());

            xPathStr = "/response/result/doc/str[@name='language']";
            expr = xpath.compile(xPathStr);
            node = (Node) expr.evaluate(solrDom, XPathConstants.NODE);
            indexParams.setParam("LANGUAGE", node.getFirstChild().getNodeValue());

            xPathStr = "/response/result/doc/str[@name='datum']";
            expr = xpath.compile(xPathStr);
            node = (Node) expr.evaluate(solrDom, XPathConstants.NODE);
            indexParams.setParam("DATUM", node.getFirstChild().getNodeValue());

            xPathStr = "/response/result/doc/date[@name='timestamp']";
            expr = xpath.compile(xPathStr);
            node = (Node) expr.evaluate(solrDom, XPathConstants.NODE);
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            Date date = formatter.parse(node.getFirstChild().getNodeValue());

            indexByPid(pid, date, force, new ByteArrayInputStream(fedoraOperations.foxmlRecord), requestParams, indexParams);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error reindexing doc " + pid, ex);
        }
    }

    private void fromPid(
            String pid,
            ArrayList<String> requestParams)
            throws java.rmi.RemoteException, IOException, Exception {
        if (pid == null || pid.length() < 1) {
            return;
        }
        fedoraOperations.getFoxmlFromPid(pid);
        indexDoc(new ByteArrayInputStream(fedoraOperations.foxmlRecord), requestParams, "1");
    }
    /* kramerius */
    XPathFactory factory = XPathFactory.newInstance();
    XPath xpath;
    XPathExpression expr;
    Document contentDom;

    private void fullRepo() {
        String[] models = config.getStringArray("fedora.topLevelModels");
        for (String model : models) {
            krameriusModel(model, new ArrayList<String>());

        }
    }

    private void fullRepoWithClean() throws Exception {
        clearIndex();
        fullRepo();
    }

    private void clearIndex() throws Exception {
        StringBuilder sb = new StringBuilder("<delete><query>*:*</query></delete>");
        logger.fine("indexDoc=\n" + sb.toString());

        postData(config.getString("IndexBase") + "/update", new StringReader(sb.toString()), new StringBuilder());
        deleteTotal++;
    }

    private void krameriusModel(String model,
            ArrayList<String> requestParams,
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

            for (int i = 0; i < nodes.getLength(); i++) {
                Node childnode = nodes.item(i);
                fromKrameriusModel(childnode.getAttributes().getNamedItem("uri").getNodeValue(), requestParams);
                hasRecords = true;
            }
            if (hasRecords) {
                krameriusModel(model, requestParams, offset + pageSize);
            }

        } catch (Exception e) {
            logger.severe(e.toString());
        }

    }

    private void krameriusModel(
            String model,
            ArrayList<String> requestParams) {
        try {
            logger.info("Indexing from kramerius model: " + model);
            krameriusModel(model, requestParams, 0);

        } catch (Exception e) {
            logger.severe(e.toString());
        }
    }

    private void fromKrameriusModel(
            String uuid,
            ArrayList<String> requestParams)
            throws java.rmi.RemoteException, Exception {
        String pid = uuid;
        if (pid == null || pid.length() < 1) {
            return;
        }
        if (!pid.startsWith("uuid:")) {
            pid = "uuid:" + pid;
        }
        logger.fine("fromKrameriusModel: " + pid);
        fedoraOperations.getFoxmlFromPid(pid);
        factory = XPathFactory.newInstance();
        xpath = factory.newXPath();

        indexByPid(pid, null, true, new ByteArrayInputStream(fedoraOperations.foxmlRecord), requestParams, null);

    }

    private Document getDocument(InputStream foxmlStream) throws Exception {
        try {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(false);
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            //InputSource source = new InputSource(new StringReader(result.toString()));

            return builder.parse(foxmlStream);

        } catch (Exception e) {
            logger.severe("getDocument error");
            logger.severe(e.toString());
            throw new Exception(e);
        }
    }

    private int indexByPid(String pid,
            Date date,
            boolean force,
            InputStream foxmlStream,
            ArrayList<String> requestParams,
            IndexParams indexParams) {

        logger.fine("indexByPid: pid -> " + pid);
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
                    if (dateValue.before(date)) {
                        if (!force) {
                            logger.info(String.format("Document %s is up to date. Skipping", pid));
                            return getActualNumOfPagesFromSolr(pid);
                        }
                    }
                }
            }

            String fmodel = "";
            if (indexParams == null) {
                indexParams = new IndexParams(pid, contentDom);
                fmodel = (String) indexParams.paramsMap.get("MODEL");
            } else {
                expr = xpath.compile("//datastream/datastreamVersion[last()]/xmlContent/RDF/Description/hasModel");
                Node modelNode = (Node) expr.evaluate(contentDom, XPathConstants.NODE);
                if (modelNode != null) {
                    fmodel = modelNode.getAttributes().getNamedItem("rdf:resource").getNodeValue().split("model:")[1];
                    indexParams.addPath(fmodel);

                }
            }
            if (fmodel.equals("page")) {
                String p = fedoraOperations.getParents(pid);
                indexParams.addParents(p);
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

            for (int i = 0; i < pids.size(); i++) {
                String relpid = pids.get(i);
                String model = models.get(i);
                try {
                    byte[] foxmlRecord2 = fedoraOperations.getAndReturnFoxmlFromPid(relpid);
                    InputStream foxmlStream2 = new ByteArrayInputStream(foxmlRecord2);
                    contentDom = getDocument(foxmlStream2);
                    IndexParams childParams = new IndexParams(relpid, model, contentDom, i);
                    childParams.merge(indexParams);
                    foxmlStream2.reset();
                    num += indexByPid(relpid, date, force, foxmlStream2, requestParams, childParams);
                } catch (Exception ex) {
                    //logger.severe("Can't index doc: " + relpid + " Continuing...");
                    logger.log(Level.SEVERE, "Can't index doc: " + relpid + " Continuing...", ex);
                }
            }


            num += docs - 1;
            indexDoc(foxmlStream, indexParams.toArrayList(Integer.toString(num)), String.valueOf(docs - 1));

        } catch (Exception e) {
            logger.severe("indexByPid error: " + e.toString());
        }
        return num;
    }
    /* kramerius */

    private void indexDoc(
            InputStream foxmlStream,
            ArrayList<String> requestParams,
            String docCount)
            throws java.rmi.RemoteException, IOException, Exception {
        foxmlStream.reset();
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("DOCCOUNT", docCount);

        for (int i = 0; i < requestParams.size(); i = i + 2) {
            params.put(requestParams.get(i), requestParams.get(i + 1));
        }

        String xsltPath = config.getString("UpdateIndexDocXslt");
        StringBuffer sb = (new GTransformer()).transform(
                xsltPath,
                new StreamSource(foxmlStream),
                null,
                params);
        logger.fine("indexDoc=\n" + sb.toString());
        //logger.info("indexDoc=\n" + sb.toString());
        if (sb.indexOf("name=\"" + UNIQUEKEY) > 0) {
            postData(config.getString("IndexBase") + "/update", new StringReader(sb.toString()), new StringBuilder());
            updateTotal++;
        }
    }

    private void deletePid(String pid) throws Exception {
        StringBuilder sb = new StringBuilder("<delete><id>" + pid + "</id></delete>");
        logger.fine("indexDoc=\n" + sb.toString());
        postData(config.getString("IndexBase") + "/update", new StringReader(sb.toString()), new StringBuilder());
        deleteTotal++;
    }

    private void deleteDocument(String pid_path) throws Exception {
        StringBuilder sb = new StringBuilder("<delete><query>pid_path:" + pid_path + "*</query></delete>");
        logger.fine("indexDoc=\n" + sb.toString());
        postData(config.getString("IndexBase") + "/update", new StringReader(sb.toString()), new StringBuilder());
        deleteTotal++;
    }

    private void deleteModel(String path) throws Exception {
        StringBuilder sb = new StringBuilder("<delete><query>path:" + path + "*</query></delete>");
        logger.fine("indexDoc=\n" + sb.toString());
        postData(config.getString("IndexBase") + "/update", new StringReader(sb.toString()), new StringBuilder());
        deleteTotal++;
    }

    public Analyzer getAnalyzer(String analyzerClassName)
            throws Exception {
        Analyzer analyzer = null;
        logger.fine("analyzerClassName=" + analyzerClassName);
        try {
            Class analyzerClass = Class.forName(analyzerClassName);

            logger.fine("analyzerClass=" + analyzerClass.toString());

            analyzer = (Analyzer) analyzerClass.getConstructor(new Class[]{}).newInstance(new Object[]{});

            logger.fine("analyzer=" + analyzer.toString());

        } catch (ClassNotFoundException e) {
            throw new Exception(analyzerClassName + ": class not found.\n", e);
        } catch (InstantiationException e) {
            throw new Exception(analyzerClassName + ": instantiation error.\n", e);
        } catch (IllegalAccessException e) {
            throw new Exception(analyzerClassName + ": instantiation error.\n", e);
        } catch (InvocationTargetException e) {
            throw new Exception(analyzerClassName + ": instantiation error.\n", e);
        } catch (NoSuchMethodException e) {
            throw new Exception(analyzerClassName + ": instantiation error.\n", e);
        }
        return analyzer;
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
            logger.severe("solrUrl=" + solrUrlString + ": " + e.toString());
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

    private void getIndexReader()
            throws Exception {
        IndexReader irreopened = null;
        if (ir != null) {
            try {
                irreopened = ir.reopen();
            } catch (CorruptIndexException e) {
                throw new Exception("IndexReader reopen :\n", e);
            } catch (IOException e) {
                throw new Exception("IndexReader reopen :\n", e);
            }
            if (ir != irreopened) {
                try {
                    ir.close();
                } catch (IOException e) {
                    ir = null;
                    throw new Exception("IndexReader close after reopen error :\n", e);
                }
                ir = irreopened;
            }
        } else {
            String s = config.getString("IndexDir");
            try {
                ir = IndexReader.open(SimpleFSDirectory.open(new File(s)), true);
            } catch (CorruptIndexException e) {
                throw new Exception("IndexReader open error IndexDir=" + s + " :\n", e);
            } catch (IOException e) {
                throw new Exception("IndexReader open error IndexDir=" + s + " :\n", e);
            }
        }
        docCount = ir.numDocs();
        logger.fine("getIndexReader  docCount=" + docCount);

    }

    private void closeIndexReader()
            throws Exception {
        if (ir != null) {
            docCount = ir.numDocs();
            try {
                ir.close();
            } catch (IOException e) {
                throw new Exception("IndexReader close error:\n", e);
            } finally {
                ir = null;
                logger.fine("closeIndexReader docCount=" + docCount);

            }
        }
    }

    private long indexDirSpace(File dir) {
        long ids = 0;
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            if (f.isDirectory()) {
                ids += indexDirSpace(f);
            } else {
                ids += f.length();
            }
        }
        return ids;
    }

    private int getActualNumOfPagesFromSolr(String pid) {
        if (pid == null || pid.length() < 1) {
            return 0;
        }
        try {
            String urlStr = config.getString("solrHost") + "/select/select?fl=pages_count&q=PID:\"" + pid.replaceAll("uuid:", "") + "\"";
            String uuid = pid.startsWith("uuid:") ? pid : "uuid:" + pid;
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
        if (pid_path == null || pid_path.length() < 1) {
            return;
        }
        String PID;
        String urlStr = config.getString("solrHost") + "/select/select?q=pid_path:" + pid_path + "*&fl=PID";


        factory = XPathFactory.newInstance();
        xpath = factory.newXPath();

        /* get current values */
        java.net.URL url = new java.net.URL(urlStr);

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document solrDom = builder.parse(url.openStream());
        String xPathStr = "/response/result/doc/str[@name='PID']";
        expr = xpath.compile(xPathStr);
        NodeList nodeList = (NodeList) expr.evaluate(solrDom, XPathConstants.NODESET);
        Node node;
        for(int i=0; i < nodeList.getLength(); i++){
            node = nodeList.item(i);
            PID = node.getFirstChild().getNodeValue();
            try{
                fedoraOperations.fa.getAPIM().getObjectXML("uuid:"+PID);
            }catch (Exception e){
                logger.info(PID + " doesn't exist. Deleting...");
                deletePid(PID);
            }
        }
    }

    private void checkIntegrityByModel(String model) throws Exception {
        if (model == null || model.length() < 1) {
            return;
        }
        String PID;
        String pid_path;
        String urlStr = config.getString("solrHost") + "/select/select?q=fedora.model:\"" + model + "\"&fl=PID,pid_path";
        factory = XPathFactory.newInstance();
        xpath = factory.newXPath();

        /* get current values */
        java.net.URL url = new java.net.URL(urlStr);

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document solrDom = builder.parse(url.openStream());
        String xPathStr = "/response/result/doc/str[@name='PID']";
        expr = xpath.compile(xPathStr);
        NodeList nodeList = (NodeList) expr.evaluate(solrDom, XPathConstants.NODESET);
        Node node;
        for(int i=0; i < nodeList.getLength(); i++){
            node = nodeList.item(i);
            PID = node.getFirstChild().getNodeValue();
            pid_path = node.getNextSibling().getFirstChild().getNodeValue();
            try{
                fedoraOperations.fa.getAPIM().getObjectXML("uuid:"+PID);
                logger.info("je: " + PID+" ----- " + pid_path);
            }catch (Exception e){
                logger.info(PID + " doesn't exist. Deleting...");
                deleteDocument(pid_path);
            }
        }
    }
}
