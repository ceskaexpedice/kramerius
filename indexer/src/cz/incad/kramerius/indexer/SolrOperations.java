package cz.incad.kramerius.indexer;

import cz.incad.kramerius.Constants;
import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.impl.SolrAccessImpl;
import cz.incad.kramerius.resourceindex.IResourceIndex;
import cz.incad.kramerius.resourceindex.ResourceIndexService;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.solr.SolrUtils;
import cz.incad.utils.ISODateUtils;
import cz.incad.utils.PrepareIndexDocUtils;
import dk.defxws.fedoragsearch.server.GTransformer;
import org.apache.commons.configuration.Configuration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * performs the Solr specific parts of the operations
 */

//TODO: rewrite
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
    private ArrayList<String> indexedCache = new ArrayList<String>();
    private boolean isSoftCommit = true;
    String pidSeparator;

    public SolrOperations(FedoraOperations _fedoraOperations) throws IOException {
        fedoraOperations = _fedoraOperations;
        config = KConfiguration.getInstance().getConfiguration();
        isSoftCommit = config.getBoolean("indexer.isSoftCommit", false);
        pidSeparator = config.getString("indexer.pidSeparator", ";");
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
                for(String v : value.split(pidSeparator)){
                    SolrAccess sa = new SolrAccessImpl();
                    ObjectPidsPath[] path = sa.getPath(v);
                    // don't need iterate over all array
                    ObjectPidsPath one = path[0];
                    String[] pathFromRootToLeaf = one.getPathFromRootToLeaf();
                    String joined = String.join("/", pathFromRootToLeaf);
                    logger.info(" Deleting pidpath "+joined);
                    deleteDocument(joined);
                    commit();
                }
//                deleteDocument(value);
            } else if ("deleteModel".equals(action)) {
                for(String v : value.split(pidSeparator)){
                    deleteModel(v);
                    commit();
                }
//                deleteModel(value);
            } else if ("deletePid".equals(action)) {
                for(String v : value.split(pidSeparator)){
                    deletePid(v);
                    commit();
                }
//                deletePid(value);
            } else if ("fromPid".equals(action)) {
                for(String v : value.split(pidSeparator)){
                    fromPid(v);
                    commit();
                }
//                fromPid(value);
            } else if ("fullRepo".equals(action)) {
                fullRepo();
            } else if ("fullRepoWithClean".equals(action)) {
                fullRepoWithClean();
            } else if ("optimize".equals(action)) {
                optimize();
            } else if ("fromKrameriusModel".equals(action)) {
                for(String v : value.split(pidSeparator)){
                    deleteDocument(v);
                    fromKrameriusModel(v);
                    commit();
                }
//                deleteDocument(value);
//                fromKrameriusModel(value);
//                commit();
            } else if ("fromKrameriusModelNoCheck".equals(action)) {
                for(String v : value.split(pidSeparator)){
                    fromKrameriusModel(v);
                    commit();
                }
//                fromKrameriusModel(value);
//                commit();
            } else if ("krameriusModel".equals(action)) {
                for(String v : value.split(pidSeparator)){
                    deleteModel(v);
                    krameriusModel(v);
                    commit();
                }
//                deleteModel(value);
//                krameriusModel(value);
//                commit();
            } else if ("krameriusModelNoCheck".equals(action)) {
                for(String v : value.split(pidSeparator)){
                    krameriusModel(v);
                    commit();
                }
//                krameriusModel(value);
//                commit();
            } else if ("reindexDoc".equals(action)) {
                for(String v : value.split(pidSeparator)){
                    reindexDoc(v, false);
                    commit();
                }
            
                //reindexDoc(value, false);
                //commit();
            } else if ("reindexDocForced".equals(action)) {
                for(String v : value.split(pidSeparator)){
                    reindexDoc(v, true);
                    commit();
                }
//                reindexDoc(value, true);
//                commit();
            } else if ("checkIntegrity".equals(action)) {
                checkIntegrity();
            } else if ("checkIntegrityByModel".equals(action)) {
                checkIntegrityByModel(value);
            } else if ("checkIntegrityByDocument".equals(action)) {
                checkIntegrityByDocument(value);
            } else if ("getPidPaths".equals(action)) {
                fedoraOperations.getPidPaths(value);
            }else if ("reindexCollection".equals(action)) {
                reindexCollection(value);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Cant index. Action:" + action + " Value: " + value, ex);
            throw new Exception(ex);
        } finally {

            extendedFields.closePDFDocument();
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
            HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            urlc.setConnectTimeout(config.getInt("http.timeout", 10000));
            org.w3c.dom.Document solrDom = builder.parse(urlc.getInputStream());
            String xPathStr = "/response/result/@numFound";
            expr = xpath.compile(xPathStr);
            Node node = (Node) expr.evaluate(solrDom, XPathConstants.NODE);
            return Integer.parseInt(node.getFirstChild().getNodeValue());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error retrieving index doc count", e);
            return 0;
        }
    }

    private void optimize()
            throws java.rmi.RemoteException, Exception {
        StringBuilder sb = new StringBuilder("<optimize/>");
        logger.log(Level.FINE, "indexDoc=\n{0}", sb.toString());

        postData(config.getString("IndexBase") + "/update", sb.toString(), new StringBuilder());

    }

    private int reindexDoc(String uuid, boolean force)
            throws java.rmi.RemoteException, IOException, Exception {
        if (uuid == null || uuid.length() < 1) {
            return 0;
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
            HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
            urlc.setConnectTimeout(config.getInt("http.timeout", 10000));
            org.w3c.dom.Document solrDom = builder.parse(urlc.getInputStream());
            String xPathStr = "/response/result/doc/str[@name='root_pid']";
            expr = xpath.compile(xPathStr);
            Node node = (Node) expr.evaluate(solrDom, XPathConstants.NODE);
            //System.out.println(node.getFirstChild().getNodeValue());

            xPathStr = "/response/result/doc/date[@name='modified_date']";
            expr = xpath.compile(xPathStr);
            node = (Node) expr.evaluate(solrDom, XPathConstants.NODE);

            Date date = null;
            try{
                date = ISODateUtils.parseISODate(node.getFirstChild().getNodeValue());
                //date = formatter.parse(node.getFirstChild().getNodeValue());
            }catch(Exception e){
                logger.info("Problem parsing modified_date, document "+ uuid +" will be fully reindexed. ("+e+")");
            }

            return indexByPid(uuid, date, force, new ByteArrayInputStream(fedoraOperations.foxmlRecord));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error reindexing doc " + uuid + " with: " + ex.toString());
            throw new Exception(ex);
        }
    }

    private void fromPid(String pid)
            throws java.rmi.RemoteException, IOException, Exception {
        if (pid == null || pid.length() < 1) {
            return;
        }
        fedoraOperations.getFoxmlFromPid(pid);
        contentDom = getDocument(new ByteArrayInputStream(fedoraOperations.foxmlRecord));
        extendedFields.setFields(pid);

        expr = xpath.compile("//datastream[@ID='IMG_FULL']/datastreamVersion[last()]");
        Node imgFullMimeNode = (Node) expr.evaluate(contentDom, XPathConstants.NODE);
        int docs = 0;
        if (imgFullMimeNode != null) {
            if (imgFullMimeNode.getAttributes().getNamedItem("MIMETYPE").getNodeValue().indexOf("pdf") > -1) {
                extendedFields.setPDFDocument(pid);
                docs = extendedFields.getPDFPagesCount();
            }
        }
        indexDoc(new ByteArrayInputStream(fedoraOperations.foxmlRecord), String.valueOf(docs));
        commit();
    }
    /* kramerius */
    XPathFactory factory = XPathFactory.newInstance();
    XPath xpath;
    XPathExpression expr;
    Document contentDom;

    private void fullRepo() throws Exception {
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

        postData(config.getString("IndexBase") + "/update", sb.toString(), new StringBuilder());
        deleteTotal++;
    }

    private void krameriusModel(String model, int offset) throws Exception {
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
            logger.log(Level.SEVERE, "Error indexing model " + model, e);
            throw new Exception(e);
        }

    }

    private void krameriusModel(String model) throws Exception {
            logger.log(Level.INFO, "Indexing from kramerius model: {0}", model);
            krameriusModel(model, 0);
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

    private int indexByPid(String pid, Date date, boolean force, InputStream foxmlStream) throws Exception {

        if(indexedCache.contains(pid)){
            logger.log(Level.INFO, "Pid {0} already indexed", new Object[]{pid});
            return 0;
        }
        logger.log(Level.INFO, "indexing -> {0}; count: {1}", new Object[]{pid, updateTotal});
        int num = 0;
        ArrayList<String> pids = new ArrayList<String>();
        ArrayList<String> models = new ArrayList<String>();
        try {
            indexedCache.add(pid);
            contentDom = getDocument(foxmlStream);
            //tady testujeme datum
            if (date != null) {
                expr = xpath.compile("//objectProperties/property[@NAME='info:fedora/fedora-system:def/view#lastModifiedDate']/@VALUE");
                Node dateNode = (Node) expr.evaluate(contentDom, XPathConstants.NODE);
                if (dateNode != null) {
                    Date dateValue = ISODateUtils.parseISODate(dateNode.getNodeValue());
                    //logger.info("FOXMLDATE:"+dateValue+" INDEXDATE:"+date);
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
                        && !nodeName.contains("isMemberOfCollection")
                        && childnode.hasAttributes()
                        && childnode.getAttributes().getNamedItem("rdf:resource") != null) {
                    String p = childnode.getAttributes().getNamedItem("rdf:resource").getNodeValue().split("/")[1];
                    if(pid.equals(p)){
                        logger.log(Level.WARNING, "Cyclic reference on {0}", p);
                    }else{
                        pids.add(p);
                        //models.add(KrameriusModels.toString(RDFModels.convertRDFToModel(nodeName)));
                        models.add(nodeName);
                    }
                } else {
                }
            }


            expr = xpath.compile("//datastream[@ID='IMG_FULL']/datastreamVersion[last()]");
            Node imgFullMimeNode = (Node) expr.evaluate(contentDom, XPathConstants.NODE);
            int docs = 0;
            extendedFields.setFields(pid);
            if (imgFullMimeNode != null) {
                if (imgFullMimeNode.getAttributes().getNamedItem("MIMETYPE").getNodeValue().indexOf("pdf") > -1) {
                    extendedFields.setPDFDocument(pid);
                    docs = extendedFields.getPDFPagesCount();
                    //docs = fedoraOperations.getPdfPagesCount_(pid, "IMG_FULL");
                }
            }
            indexDoc(foxmlStream, String.valueOf(docs));

            for (int i = 0; i < pids.size(); i++) {
                String relpid = pids.get(i);
                String model = models.get(i);
                try {
                    if(date==null){
                        byte[] foxmlRecord2 = fedoraOperations.getAndReturnFoxmlFromPid(relpid);
                        InputStream foxmlStream2 = new ByteArrayInputStream(foxmlRecord2);
                        contentDom = getDocument(foxmlStream2);
                        foxmlStream2.reset();
                        num += indexByPid(relpid, date, force, foxmlStream2);
                    }else{
                        num += reindexDoc(relpid, force);
                    }
                    
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "Can't index doc: " + relpid, ex);
                    throw new Exception(ex);
                }
            }

            num += docs;

        } catch (Exception e) {
            if(config.getBoolean("indexer.continueOnError", false)){
                // continuing
                logger.log(Level.SEVERE, "Error indexing document " + pid + ". Continuing.", e);
            }else{
                logger.log(Level.SEVERE, "Error indexing document " + pid, e);
                throw new Exception(e);
            }
            
        }

        return num;
    }



    private void indexDoc(
            InputStream foxmlStream,
            String docCount)
            throws java.rmi.RemoteException, IOException, Exception {

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("DOCCOUNT", docCount);
        params.put("PAGENUM", "0");
        params.put("BROWSEMODELS", Arrays.toString(config.getStringArray("indexer.browseModels")));
        String xsltPath = config.getString("UpdateIndexDocXslt");

        for (int i = 0; i <= Integer.parseInt(docCount); i++) {
            if(i>0){
                logger.log(Level.INFO, "indexing pdf page {0}", i);
            }
            foxmlStream.reset();
            params.put("PAGENUM", i + "");
            StringBuffer sb = transformer.transform(
                    xsltPath,
                    new StreamSource(foxmlStream),
                    new GTransformer.ClasspathResolver(),
                    params,
                    true);
            
            applyCustomTransformations(sb, foxmlStream, params);
            
            String rawXML = "<doc>" + sb.toString() + extendedFields.toXmlString(i) + "</doc>";
            String docSrc = prepareDocForIndexing(rawXML);
            
            logger.log(Level.FINE, "indexDoc=\n{0}", docSrc);
            if (sb.indexOf("name=\"" + UNIQUEKEY) > 0) {
                postData(config.getString("IndexBase") + "/update", docSrc, new StringBuilder());
                updateTotal++;
            }
        }
    }

    public static String prepareDocForIndexing(boolean compositeId, String rawXML) throws ParserConfigurationException, SAXException, IOException,
            TransformerException, UnsupportedEncodingException {
        rawXML = removeTroublesomeCharacters(rawXML);
        Document document = XMLUtils.parseDocument(new StringReader(rawXML));
        if (compositeId) {
            PrepareIndexDocUtils.enhanceByCompositeId(document, document.getDocumentElement());
        }
        rawXML = PrepareIndexDocUtils.wrapByAddCommand(document);
        return rawXML;
    }

    public static String prepareDocForIndexing(String rawXML) throws ParserConfigurationException, SAXException, IOException,
            TransformerException, UnsupportedEncodingException {
        return prepareDocForIndexing(KConfiguration.getInstance().getConfiguration().getBoolean("indexer.compositeId", false), rawXML);
    }

    
    public static String removeTroublesomeCharacters(String inString) throws UnsupportedEncodingException {
        // XML 1.0
        // #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
        String xml10pattern = "[^"
                + "\u0009\r\n"
                + "\u0020-\uD7FF"
                + "\uE000-\uFFFD"
                + "\ud800\udc00-\udbff\udfff"
                + "]";
        return inString.replaceAll(xml10pattern, "");
        //return inString.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", " ");

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
                    new GTransformer.ClasspathResolver(),
                    params,
                    false);
            //logger.info("newSb: " +newSb.toString());
            sb.append(newSb);
        }
    }

    private void deletePid(String pid) throws Exception {
        StringBuilder sb = new StringBuilder("<delete><query>PID:" + pid.replaceAll("(?=[]\\[+&|!(){}^\"~*?:\\\\-])", "\\\\") + "</query></delete>");
        logger.log(Level.FINE, "indexDoc=\n{0}", sb.toString());
        postData(config.getString("IndexBase") + "/update", sb.toString(), new StringBuilder());
        deleteTotal++;
    }

    private void deleteDocument(String pid_path) throws Exception {

        StringBuilder sb = new StringBuilder("<delete><query>pid_path:" + pid_path.replaceAll("(?=[]\\[+&|!(){}^\"~*?:\\\\])", "\\\\") + "*</query></delete>");
        logger.log(Level.INFO, "deleting document with {0}", sb.toString());
        postData(config.getString("IndexBase") + "/update", sb.toString(), new StringBuilder());
        commit();
        deleteTotal++;
    }

    private void deleteModel(String path) throws Exception {
        StringBuilder sb = new StringBuilder("<delete><query>model_path:" + path + "*</query></delete>");
        logger.log(Level.FINE, "indexDoc=\n{0}", sb.toString());
        postData(config.getString("IndexBase") + "/update", sb.toString(), new StringBuilder());
        commit();
        deleteTotal++;
    }

    /**
     * Reads data from the data reader and posts it to solr,
     * writes the response to output
     */
    private void postData(String solrUrlString, String data, StringBuilder output)
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
            urlc.setConnectTimeout(config.getInt("http.timeout", 10000));
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
                writer.write(data);
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
            throw new Exception("Solr has throw an error. Check tomcat log. " + e);
        } finally {
            if (urlc != null) {
                urlc.disconnect();
            }
        }
    }
    
    

    private void commit() throws java.rmi.RemoteException, Exception {
        String s;
        if(isSoftCommit){
            s = "<commit softCommit=\"true\" />";
        }else{
            s = "<commit softCommit=\"false\" />";
        }
        logger.log(Level.FINE, "commit");

        postData(config.getString("IndexBase") + "/update", s, new StringBuilder());

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
            HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
            urlc.setConnectTimeout(config.getInt("http.timeout", 10000));
            org.w3c.dom.Document solrDom = builder.parse(urlc.getInputStream());
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
        String urlStr = config.getString("solrHost") + "/select/?q=pid_path:" + pid_path.replaceAll("(?=[]\\[+&|!(){}^\"~*?:\\\\-])", "\\\\")
                + "*&fl=PID&start=" + offset + "&rows=" + numHits;
        factory = XPathFactory.newInstance();
        xpath = factory.newXPath();
        java.net.URL url = new java.net.URL(urlStr);

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
            urlc.setConnectTimeout(config.getInt("http.timeout", 10000));
            org.w3c.dom.Document solrDom = builder.parse(urlc.getInputStream());
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
            checkIntegrityByModel(model);
        }
        commit();
    }

    private void checkIntegrityByModel(String model) throws Exception {
        int offset = 0;
        int numHits = 200;
        while(checkIntegrityByModel(model, offset, numHits)){
            offset += numHits;
        }
        commit();
    }

    private boolean checkIntegrityByModel(String model, int offset, int numHits) throws Exception {
        logger.log(Level.INFO, "checkIntegrityByModel. model: {0}; offset: {1}", new String[]{model, Integer.toString(offset)});
        if (model == null || model.length() < 1) {
            return false;
        }
        String PID;
        String pid_path;
        String urlStr = config.getString("solrHost") + "/select/?q=model_path:" + model + "*&fl=PID,pid_path&start="
                + offset + "&rows=" + numHits;
        factory = XPathFactory.newInstance();
        xpath = factory.newXPath();
        java.net.URL url = new java.net.URL(urlStr);

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
            urlc.setConnectTimeout(config.getInt("http.timeout", 10000));
            org.w3c.dom.Document solrDom = builder.parse(urlc.getInputStream());
        String xPathStr = "/response/result/doc/str[@name='PID']";
        expr = xpath.compile(xPathStr);
        NodeList nodeList = (NodeList) expr.evaluate(solrDom, XPathConstants.NODESET);
        Node node;
        for (int i = 0; i < nodeList.getLength(); i++) {
            node = nodeList.item(i);
            PID = node.getFirstChild().getNodeValue();
            pid_path = node.getNextSibling().getFirstChild().getNodeValue();
            
            //PID with @ are virtual only in index. Test parent.
            String simplePid = PID;
            if(PID.indexOf("/@")>-1){
              simplePid = PID.substring(0, PID.indexOf("/@")-1);
            }
            

            if(!rindex.existsPid(simplePid)){
                logger.log(Level.INFO, simplePid + " doesn't exist. Deleting...");
                deletePid(PID);
            }
        }
        return (nodeList.getLength() > 0);
        
    }

    private void reindexCollection(String collection) throws Exception{
        logger.log(Level.INFO, "Reindex documents in collection: {0}", collection);
        if (collection == null || collection.length() < 1) {
            return;
        }
        int numHits = 200;
        String PID;
        String urlStr = config.getString("solrHost") + "/select?q=collection:\"" + collection + "\"&fl=PID&rows=" + numHits;
        factory = XPathFactory.newInstance();
        xpath = factory.newXPath();
        java.net.URL url = new java.net.URL(urlStr);

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
            urlc.setConnectTimeout(config.getInt("http.timeout", 10000));
            org.w3c.dom.Document solrDom = builder.parse(urlc.getInputStream());
        String xPathStr = "/response/result/doc/str[@name='PID']";
        expr = xpath.compile(xPathStr);
        NodeList nodeList = (NodeList) expr.evaluate(solrDom, XPathConstants.NODESET);
        Node node;
        for (int i = 0; i < nodeList.getLength(); i++) {
            node = nodeList.item(i);
            PID = node.getFirstChild().getNodeValue();
            fromPid(PID);
        }
        commit();
        if (nodeList.getLength() > 0) {
            reindexCollection(collection);
        }
    }
}
