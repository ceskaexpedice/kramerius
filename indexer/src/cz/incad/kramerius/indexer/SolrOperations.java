//$Id: SolrOperations.java 6565 2008-02-07 14:53:30Z gertsp $
/*
 * <p><b>License and Copyright: </b>The contents of this file is subject to the
 * same open source license as the Fedora Repository System at www.fedora-commons.org
 * Copyright &copy; 2006, 2007, 2008 by The Technical University of Denmark.
 * All rights reserved.</p>
 */
package cz.incad.kramerius.indexer;

//import dk.defxws.fgssolr.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.rmi.RemoteException;
import java.util.StringTokenizer;

import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;

import dk.defxws.fedoragsearch.server.GTransformer;
import dk.defxws.fedoragsearch.server.GenericOperationsImpl;
import dk.defxws.fedoragsearch.server.URIResolverImpl;

import fedora.client.FedoraClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.URIResolver;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * performs the Solr specific parts of the operations
 * 
 * @author  gsp@dtv.dk
 * @version 
 */
public class SolrOperations {

    private static final Logger logger = Logger.getLogger(SolrOperations.class);
    private static final String UNIQUEKEY = "PID";
    private IndexReader ir = null;
    protected Properties config;
    protected int insertTotal = 0;
    protected int updateTotal = 0;
    protected int deleteTotal = 0;
    protected int docCount = 0;
    protected int warnCount = 0;
    protected String[] params = null;
    private GenericOperationsImpl fedoraOperations;

    public SolrOperations(GenericOperationsImpl _fedoraOperations) {
        fedoraOperations = _fedoraOperations;
        config = fedoraOperations.config;
    }

    public void updateIndex(
            String action,
            String value,
            String repositoryName,
            String indexName,
            String indexDocXslt,
            String resultPageXslt,
            ArrayList<String> requestParams)
            throws java.rmi.RemoteException, Exception {
        insertTotal = 0;
        updateTotal = 0;
        deleteTotal = 0;
        int initDocCount = 0;
        
        try {

            getIndexReader(indexName);
            initDocCount = docCount;
            closeIndexReader(indexName);
            if ("deletePid".equals(action)) {
                //deletePid(value, indexName, resultXml);
                } else if ("fromPid".equals(action)) {
                fromPid(value, repositoryName, indexName, indexDocXslt, requestParams);
            } else if ("fromFoxmlFiles".equals(action)) {
                fromFoxmlFiles(value, repositoryName, indexName, indexDocXslt);
            } else if ("optimize".equals(action)) {
                optimize();
            } else if ("fromKrameriusModel".equals(action)) {
                if(!value.startsWith("uuid:")) value = "uuid:" + value;
                fromKrameriusModel(value, repositoryName, indexName, indexDocXslt, requestParams);
            }else if ("krameriusModel".equals(action)) {
                krameriusModel(value, repositoryName, indexName, indexDocXslt, requestParams);
            }

        } catch (Exception ex) {
            logger.error(ex);
        } finally {

            getIndexReader(indexName);
            closeIndexReader(indexName);
            if (logger.isDebugEnabled()) {
                logger.debug("initDocCount=" + initDocCount + " docCount=" + docCount + " updateTotal=" + updateTotal);
            }
            if (updateTotal > 0) {
                int diff = docCount - initDocCount;
                insertTotal = diff;
                updateTotal -= diff;
            }
            docCount = docCount - deleteTotal;
        }
        logger.info("updateIndex " + action + " indexName=" + indexName + " indexDirSpace=" + indexDirSpace(new File(config.getProperty("IndexDir"))) + " docCount=" + docCount);
        logger.info("updateIndex " + action + " indexName=" + indexName + " indexDirSpace=" + indexDirSpace(new File(config.getProperty("IndexDir"))) + " docCount=" + docCount);
        
        logger.info("insertTotal: " + insertTotal);
        logger.info("updateTotal: " + updateTotal);
        logger.info("deleteTotal: " + deleteTotal);
        logger.info("docCount: " + docCount);
        logger.info("warnCount: " + warnCount);
        
    }

    private void optimize()
            throws java.rmi.RemoteException, Exception {
        StringBuffer sb = new StringBuffer("<optimize/>");
        if (logger.isDebugEnabled()) {
            logger.debug("indexDoc=\n" + sb.toString());
        }
        postData(config.getProperty("IndexBase") + "/update", new StringReader(sb.toString()), new StringBuffer());
        
    }

    private void fromFoxmlFiles(
            String filePath,
            String repositoryName,
            String indexName,
            String indexDocXslt)
            throws java.rmi.RemoteException, IOException, Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("fromFoxmlFiles filePath=" + filePath + " repositoryName=" + repositoryName + " indexName=" + indexName);
        }
        File objectDir = null;
        if (filePath == null || filePath.equals("")) {
            objectDir = new File(config.getProperty("FedoraObjectDir"));
        } else {
            objectDir = new File(filePath);
        }
        indexDocs(objectDir, repositoryName, indexName, indexDocXslt);
        docCount = docCount - warnCount;
    }

    private void indexDocs(
            File file,
            String repositoryName,
            String indexName,
            String indexDocXslt)
            throws java.rmi.RemoteException, IOException, Exception {
        if (file.isHidden()) {
            return;
        }
        if (file.isDirectory()) {
            String[] files = file.list();
            for (int i = 0; i < files.length; i++) {
                if (i % 100 == 0) {
                    logger.info("updateIndex fromFoxmlFiles " + file.getAbsolutePath() + " indexDirSpace=" +
                            indexDirSpace(new File(config.getProperty("IndexDir"))) + " docCount=" + docCount);
                }
                indexDocs(new File(file, files[i]), repositoryName, indexName, indexDocXslt);
            }
        } else {
            try {
                indexDoc(file.getName(), repositoryName, indexName, new FileInputStream(file), indexDocXslt, new ArrayList<String>());
            } catch (RemoteException e) {
                warnCount++;
                logger.warn("<warning no=\"" + (warnCount) + "\">file=" + file.getAbsolutePath() + " exception=" + e.toString() + "</warning>");
            } catch (FileNotFoundException e) {
                warnCount++;
                logger.warn("<warning no=\"" + (warnCount) + "\">file=" + file.getAbsolutePath() + " exception=" + e.toString() + "</warning>");
            }
        }
    }

    private void fromPid(
            String pid,
            String repositoryName,
            String indexName,
            String indexDocXslt,
            ArrayList<String> requestParams)
            throws java.rmi.RemoteException, IOException, Exception {
        if (pid == null || pid.length() < 1) {
            return;
        }
        fedoraOperations.getFoxmlFromPid(pid, repositoryName);
        indexDoc(pid, repositoryName, indexName, new ByteArrayInputStream(fedoraOperations.foxmlRecord), indexDocXslt, requestParams);
    }
    /* kramerius */
    XPathFactory factory = XPathFactory.newInstance();
    XPath xpath;
    XPathExpression expr;
    Document contentDom;

    private void krameriusModel(
            String model,
            String repositoryName,
            String indexName,
            String indexDocXslt,
            ArrayList<String> requestParams){
    try {
            /*
            select $object from <#ri> 
            where  $object <fedora-model:hasModel> <info:fedora/model:monograph> 
            order by $object
            limit 100 
            offset 0
             */
        logger.info("Indexing from kramerius model: " + model);
            String query = "select $object from <#ri> " +
                    "where $object <fedora-model:hasModel> <info:fedora/model:" + model + ">  " +
                    "order by $object  " +
                    "limit 100  " +
                    "offset 0 ";
            
            FedoraClient client = GenericOperationsImpl.getFedoraClient(repositoryName, config.getProperty("FedoraSoap"),
                config.getProperty("FedoraUser"),
                config.getProperty("FedoraPass"));
            Map tMap = new HashMap();
            tMap.put("query", query);
            tMap.put("format", "TSV");
            tMap.put("lang", "itql");
            Map m;
            org.trippi.TupleIterator tuples = client.getTuples(tMap);
            while(tuples.hasNext()){
                String pid = tuples.next().get("object").toString();
                 //logger.info(pid);
                 fromKrameriusModel(pid.split("/")[1], repositoryName, indexName, indexDocXslt, requestParams);
            }
//            String[] names = client.getTuples(tMap).names();
//            for(String name:names){
//                logger.info(name);
//            }
            if(true) return;
            
            String urlStr = config.getProperty("FedoraResourceIndex") + "?type=tuples&flush=true&lang=itql&format=TSV&distinct=off&stream=off" +
                    "&query=" + java.net.URLEncoder.encode(query, "UTF-8");
            //int lines = 0;
                
            java.net.URL url = new java.net.URL(urlStr);

            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(url.openStream()));
            String inputLine = in.readLine();
            while ((inputLine = in.readLine()) != null) {
                //out.println(inputLine);
                //sendToIndex(inputLine.split("/")[1]);
            
                fromKrameriusModel(inputLine.split("/")[1], repositoryName, indexName, indexDocXslt, requestParams);
            }
            in.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void fromKrameriusModel(
            String pid,
            String repositoryName,
            String indexName,
            String indexDocXslt,
            ArrayList<String> requestParams)
            throws java.rmi.RemoteException, Exception {
        if (pid == null || pid.length() < 1) {
            return;
        }
        logger.debug("fromKrameriusModel: " + pid);
        fedoraOperations.getFoxmlFromPid(pid, repositoryName);
        factory = XPathFactory.newInstance();
        xpath = factory.newXPath();

        indexByPid(pid, repositoryName, indexName, new ByteArrayInputStream(fedoraOperations.foxmlRecord), requestParams, null);
    //indexDoc(pid, repositoryName, indexName, new ByteArrayInputStream(foxmlRecord), resultXml, indexDocXslt, requestParams);
    }
    
    

    private Document getDocument(InputStream foxmlStream) throws Exception {
        try {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(false);
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            //InputSource source = new InputSource(new StringReader(result.toString()));

            return builder.parse(foxmlStream);

        } catch (Exception e) {
            logger.error("getDocument error");
            logger.error(e);
            throw new Exception(e);
        }
    }
    boolean full = true;

    private int indexByPid(String pid,
            String repositoryName,
            String indexName,
            InputStream foxmlStream,
            ArrayList<String> requestParams,
            IndexParams indexParams) {

        if (logger.isDebugEnabled()) {
            logger.debug("indexByPid: pid -> " + pid);
        }
        int num = 0;
        ArrayList<String> pids = new ArrayList<String>();
        ArrayList<String> models = new ArrayList<String>();
        try {
            contentDom = getDocument(foxmlStream);

            if (indexParams == null) {
                indexParams = new IndexParams(pid, contentDom);
            }
            if (full) {
                expr = xpath.compile("//datastream/datastreamVersion[last()]/xmlContent/RDF/Description/*");

                NodeList nodes = (NodeList) expr.evaluate(contentDom, XPathConstants.NODESET);

                for (int i = 0; i < nodes.getLength(); i++) {
                    Node childnode = nodes.item(i);
                    String nodeName = childnode.getNodeName();
                    if (nodeName.contains("hasPage")) {
                        num++;
                    }
                    if (!nodeName.contains("hasModel") && !nodeName.contains("isOnPage") &&
                            !nodeName.contains("hasDonator") &&
                            childnode.hasAttributes() &&
                            childnode.getAttributes().getNamedItem("rdf:resource")!=null 
                            ) {
                        pids.add(childnode.getAttributes().getNamedItem("rdf:resource").getNodeValue().split("/")[1]);
                        models.add(KrameriusModels.toString(RDFModels.convertRDFToModel(nodeName)));
                    } else {
                    }
//if(pids.size()>2){
//    break;
//}                    
                }

                for (int i = 0; i < pids.size(); i++) {
                    String relpid = pids.get(i);
                    String model = models.get(i);
                    try {
                        byte[] foxmlRecord2 = fedoraOperations.getAndReturnFoxmlFromPid(relpid, repositoryName);
                        InputStream foxmlStream2 = new ByteArrayInputStream(foxmlRecord2);
                        contentDom = getDocument(foxmlStream2);
                        /*
                        javax.xml.transform.Transformer transformer = javax.xml.transform.TransformerFactory.newInstance().newTransformer();
                        transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
                        
                        //initialize StreamResult with File object to save to file
                        javax.xml.transform.stream.StreamResult result = new javax.xml.transform.stream.StreamResult(new java.io.StringWriter());
                        javax.xml.transform.dom.DOMSource source = new javax.xml.transform.dom.DOMSource(contentDom);
                        transformer.transform(source, result);
                        
                        String xmlString = result.getWriter().toString();
                        logger.info(xmlString);
                         */
                        IndexParams childParams = new IndexParams(relpid, model, contentDom);
                        childParams.merge(indexParams);
                        foxmlStream2.reset();
                        num += indexByPid(relpid, repositoryName, indexName, foxmlStream2, requestParams, childParams);
                    } catch (Exception ex) {
                        logger.error("Can't index doc: " + relpid + " Continuing...");
                    }
                }
            }
            // if (logger.isInfoEnabled())
            //     logger.info("indexByPid indexParams.toUrlString(): " + indexParams.toUrlString());
            indexDoc(pid, repositoryName, indexName, foxmlStream, "", indexParams.toArrayList(Integer.toString(num)));

        } catch (Exception e) {
            logger.error("indexByPid error", e);
        }
        return num;
    }
    /* kramerius */

    private void indexDoc(
            String pidOrFilename,
            String repositoryName,
            String indexName,
            InputStream foxmlStream,
            String indexDocXslt,
            ArrayList<String> requestParams)
            throws java.rmi.RemoteException, IOException, Exception {
        foxmlStream.reset();
        String xsltName = indexDocXslt;
        String[] params = new String[12 + requestParams.size()];
        int beginParams = indexDocXslt.indexOf("(");
        if (beginParams > -1) {
            xsltName = indexDocXslt.substring(0, beginParams).trim();
            int endParams = indexDocXslt.indexOf(")");
            if (endParams < beginParams) {
                throw new Exception("Format error (no ending ')') in indexDocXslt=" + indexDocXslt + ": ");
            }
            StringTokenizer st = new StringTokenizer(indexDocXslt.substring(beginParams + 1, endParams), ",");
            params = new String[12 + 2 * st.countTokens() + requestParams.size()];
            int i = 1;
            while (st.hasMoreTokens()) {
                String param = st.nextToken().trim();
                if (param == null || param.length() < 1) {
                    throw new Exception("Format error (empty param) in indexDocXslt=" + indexDocXslt + " params[" + i + "]=" + param);
                }
                int eq = param.indexOf("=");
                if (eq < 0) {
                    throw new Exception("Format error (no '=') in indexDocXslt=" + indexDocXslt + " params[" + i + "]=" + param);
                }
                String pname = param.substring(0, eq).trim();
                String pvalue = param.substring(eq + 1).trim();
                if (pname == null || pname.length() < 1) {
                    throw new Exception("Format error (no param name) in indexDocXslt=" + indexDocXslt + " params[" + i + "]=" + param);
                }
                if (pvalue == null || pvalue.length() < 1) {
                    throw new Exception("Format error (no param value) in indexDocXslt=" + indexDocXslt + " params[" + i + "]=" + param);
                }
                params[10 + 2 * i] = pname;
                params[11 + 2 * i++] = pvalue;
            }
        }
        params[0] = "REPOSITORYNAME";
        params[1] = repositoryName;
        params[2] = "FEDORASOAP";
        params[3] = config.getProperty("FedoraSoap");
        params[4] = "FEDORAUSER";
        params[5] = config.getProperty("FedoraUser");
        params[6] = "FEDORAPASS";
        params[7] = config.getProperty("FedoraPass");
        params[8] = "TRUSTSTOREPATH";
        params[9] = config.getProperty("TrustStorePath");
        params[10] = "TRUSTSTOREPASS";
        params[11] = config.getProperty("TrustStorePass");

        for (int i = 0; i < requestParams.size(); i++) {
            params[i + 12] = requestParams.get(i);
        //logger.info("param: " + requestParams.get(i));
        }
        String xsltPath = config.getProperty("UpdateIndexDocXslt");
        StringBuffer sb = (new GTransformer()).transform(
                xsltPath,
                new StreamSource(foxmlStream),
                getURIResolver(),
                params);
        if (logger.isDebugEnabled()) {
            logger.debug("indexDoc=\n" + sb.toString());
        }
//logger.info("indexDoc=\n" + sb.toString());
        if (sb.indexOf("name=\"" + UNIQUEKEY) > 0) {
            postData(config.getProperty("IndexBase") + "/update", new StringReader(sb.toString()), new StringBuffer());
            updateTotal++;
        }
    }

    private URIResolver getURIResolver() {

        Class uriResolverClass = null;
        String uriResolver = config.getProperty("fgsindex.uriResolver");
        if (!(uriResolver == null || uriResolver.equals(""))) {
            try {
                uriResolverClass = Class.forName(uriResolver);
                try {
                    URIResolverImpl ur = (URIResolverImpl) uriResolverClass.getConstructor(new Class[]{}).newInstance(new Object[]{});
                    if (ur != null) {
                        ur.setConfig(config);
                        return ur;
                    }
                } catch (InstantiationException e) {
                    logger.error(uriResolver + ": instantiation error.\n" + e.toString());
                } catch (IllegalAccessException e) {
                    logger.error(uriResolver + ": instantiation error.\n" + e.toString());
                } catch (InvocationTargetException e) {
                    logger.error(uriResolver + ": instantiation error.\n" + e.toString());
                } catch (NoSuchMethodException e) {
                    logger.error(uriResolver + ": instantiation error:\n" + e.toString());
                }
            } catch (ClassNotFoundException e) {
                logger.error(uriResolver + ": class not found:\n" + e.toString());
            }
        }
        return null;
    }

    public Analyzer getAnalyzer(String analyzerClassName)
            throws Exception {
        Analyzer analyzer = null;
        if (logger.isDebugEnabled()) {
            logger.debug("analyzerClassName=" + analyzerClassName);
        }
        try {
            Class analyzerClass = Class.forName(analyzerClassName);
            if (logger.isDebugEnabled()) {
                logger.debug("analyzerClass=" + analyzerClass.toString());
            }
            analyzer = (Analyzer) analyzerClass.getConstructor(new Class[]{}).newInstance(new Object[]{});
            if (logger.isDebugEnabled()) {
                logger.debug("analyzer=" + analyzer.toString());
            }
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

    /*
    public Analyzer getQueryAnalyzer(String indexName)
    throws Exception {
    Analyzer analyzer = getAnalyzer(config.getAnalyzer(indexName));
    PerFieldAnalyzerWrapper pfanalyzer = new PerFieldAnalyzerWrapper(analyzer);
    StringTokenizer untokenizedFields = new StringTokenizer(config.getUntokenizedFields(indexName));
    while (untokenizedFields.hasMoreElements()) {
    pfanalyzer.addAnalyzer(untokenizedFields.nextToken(), new KeywordAnalyzer());
    }
    if (logger.isDebugEnabled()) {
    logger.debug("getQueryAnalyzer indexName=" + indexName + " untokenizedFields=" + untokenizedFields);
    }
    return pfanalyzer;
    }
     */
    /**
     * Reads data from the data reader and posts it to solr,
     * writes the response to output
     */
    private void postData(String solrUrlString, Reader data, StringBuffer output)
            throws Exception {

        URL solrUrl = null;
        try {
            solrUrl = new URL(solrUrlString);
        } catch (MalformedURLException e) {
            throw new Exception("solrUrl=" + solrUrl.toString() + ": ", e);
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
            StringBuffer errorStream = new StringBuffer();
            try {
                if (status != HttpURLConnection.HTTP_OK) {
                    errorStream.append("postData URL=" + solrUrlString + " HTTP response code=" + status + " ");
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
    private static void pipeString(Reader reader, StringBuffer writer) throws IOException {
        char[] buf = new char[1024];
        int read = 0;
        while ((read = reader.read(buf)) >= 0) {
            if (!(buf[0] == '<' && buf[1] == '?')) {
                writer.append(buf, 0, read);
            }
        }
    }

    private void getIndexReader(String indexName)
            throws Exception {
        IndexReader irreopened = null;
        if (ir != null) {
            try {
                irreopened = ir.reopen();
            } catch (CorruptIndexException e) {
                throw new Exception("IndexReader reopen error indexName=" + indexName + " :\n", e);
            } catch (IOException e) {
                throw new Exception("IndexReader reopen error indexName=" + indexName + " :\n", e);
            }
            if (ir != irreopened) {
                try {
                    ir.close();
                } catch (IOException e) {
                    ir = null;
                    throw new Exception("IndexReader close after reopen error indexName=" + indexName + " :\n", e);
                }
                ir = irreopened;
            }
        } else {
            try {
                ir = IndexReader.open(SimpleFSDirectory.open(new File(config.getProperty("IndexDir"))), true);
            } catch (CorruptIndexException e) {
                throw new Exception("IndexReader open error indexName=" + indexName + " :\n", e);
            } catch (IOException e) {
                throw new Exception("IndexReader open error indexName=" + indexName + " :\n", e);
            }
        }
        docCount = ir.numDocs();
        if (logger.isDebugEnabled()) {
            logger.debug("getIndexReader indexName=" + indexName + " docCount=" + docCount);
        }
    }

    private void closeIndexReader(String indexName)
            throws Exception {
        if (ir != null) {
            docCount = ir.numDocs();
            try {
                ir.close();
            } catch (IOException e) {
                throw new Exception("IndexReader close error indexName=" + indexName + " :\n", e);
            } finally {
                ir = null;
                if (logger.isDebugEnabled()) {
                    logger.debug("closeIndexReader indexName=" + indexName + " docCount=" + docCount);
                }
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
}