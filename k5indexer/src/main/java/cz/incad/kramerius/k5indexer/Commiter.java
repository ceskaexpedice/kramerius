/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.kramerius.k5indexer;

import cz.incad.kramerius.utils.conf.KConfiguration;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.Configuration;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

/**
 *
 * @author alberto
 */
public class Commiter {

    private static final Logger logger = Logger.getLogger(Commiter.class.getName());
    private SolrServer server;
    private  String host;
    private String core;
    private  int batchSize;
    List<SolrInputDocument> insertDocs = new ArrayList<SolrInputDocument>();

    private URL solrUrl;
    private final Configuration config;
    private static Commiter _sharedInstance = null;
    
    public synchronized static Commiter getInstance() throws MalformedURLException {
        if (_sharedInstance == null) {
            _sharedInstance = new Commiter();
        }
        return _sharedInstance;
    }
    
    private void init() throws MalformedURLException{
        solrUrl = new URL(config.getString("k5indexer.solr.host") + "/" + core + "/update");
        
        this.host = config.getString("k5indexer.solr.host");
        this.batchSize = config.getInt("k5indexer.batchSize", 100);
        
        //Http, Cloud,  ConcurrentUpdate,  LBHttp
        String serverType = config.getString("k5indexer.solrServerType", "Http");
        if("Http".equals(serverType)){
            server = new HttpSolrServer(host + "/" + core);
        }else if("LBHttp".equals(serverType)){
            LBHttpSolrServer lb = new LBHttpSolrServer(host + "/" + core);
            
            server = (SolrServer)lb;
        }else if("Cloud".equals(serverType)){
            CloudSolrServer cloud = new CloudSolrServer(host + "/" + core);
            cloud.setIdField("PID");
            server = (SolrServer)cloud;
        }else{
            server = new HttpSolrServer(host + "/" + core);
        }
        
        //server.setMaxRetries(1); // defaults to 0.  > 1 not recommended.
        //server.setConnectionTimeout(1000); // 5 seconds to establish TCP
        // Setting the XML response parser is only required for cross
        // version compatibility and only when one side is 1.4.1 or
        // earlier and the other side is 3.1 or later.
        //server.setParser(new XMLResponseParser()); // binary parser is used by default
        // The following settings are provided here for completeness.
        // They will not normally be required, and should only be used 
        // after consulting javadocs to know whether they are truly required.
        //server.setSoTimeout(1000);  // socket read timeout
        //server.setDefaultMaxConnectionsPerHost(100);
        //server.setMaxTotalConnections(100);
        //server.setFollowRedirects(false);  // defaults to false
        // allowCompression defaults to false.
        // Server side must support gzip or deflate for this to have any effect.
        //server.setAllowCompression(true);
        
    }

    public Commiter() throws MalformedURLException {
        config = KConfiguration.getInstance().getConfiguration();
        this.core = config.getString("k5indexer.solr.core");
        init();
    }

    public Commiter(String core) throws MalformedURLException {
        config = KConfiguration.getInstance().getConfiguration();
        this.core = core;
        init();
    }
    
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        commit();
    }

    public void commit() throws Exception {
        if(!insertDocs.isEmpty()){
            server.add(insertDocs);
            Indexer.success = Indexer.success + insertDocs.size();
            insertDocs.clear();
            //logger.log(Level.INFO, "Indexer.success {0}", Indexer.success);
        }
        server.commit();

//        String s;
//        if (isSoftCommit) {
//            s = "<commit />";
//        } else {
//            s = "<commit softCommit=\"false\" />";
//        }
//        logger.log(Level.FINE, "commit");
//
//        postXml(s);
    }
    
    public void delete(String pid) throws SolrServerException, IOException{
        server.deleteById(pid);
    }
    
    public void deleteByQuery(String query) throws SolrServerException, IOException{
        server.deleteByQuery(query);
    }

    public void add(SolrInputDocument doc) throws SolrServerException, IOException {
        insertDocs.add(doc);
        if (insertDocs.size() >= batchSize) {
            server.add(insertDocs);
            server.commit();
            Indexer.success = Indexer.success + insertDocs.size();
            insertDocs.clear();
            //logger.log(Level.INFO, "Indexer.success {0}", Indexer.success);
        }
    }

    public void add(List<SolrInputDocument> docs) throws SolrServerException, IOException {
        insertDocs.addAll(docs);
        if (insertDocs.size() >= batchSize) {
            server.add(insertDocs);
            server.commit();
            Indexer.success = Indexer.success + insertDocs.size();
            insertDocs.clear();
            //logger.log(Level.INFO, "Indexer.success {0}", Indexer.success);
        }
    }

    public void postXml(String xml)
            throws Exception {
        postData(solrUrl,new StringReader(xml), "text/xml; charset=UTF-8", new StringBuilder());
    }

    public void postXml(String urlStr, String xml)
            throws Exception {
        URL url = new URL(urlStr + "/" + core + "/update");
        postData(url, new StringReader(xml), "text/xml; charset=UTF-8", new StringBuilder());
    }

    public void postJson(String json)
            throws Exception {
        postData(solrUrl, new StringReader(json), "application/json; charset=UTF-8", new StringBuilder());
    }

    /**
     * Reads data from the data reader and posts it to solr, writes the response
     * to output
     */
    private void postData(URL url, Reader data, String contentType, StringBuilder output)
            throws Exception {
        HttpURLConnection urlc = null;

        try {
            urlc = (HttpURLConnection) url.openConnection();
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
            urlc.setRequestProperty("Content-type", contentType);

            OutputStream out = urlc.getOutputStream();

            try {
                Writer writer = new OutputStreamWriter(out, "UTF-8");
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
                    errorStream.append("postData URL=").append(solrUrl).append(" HTTP response code=").append(status).append(" ");
                    throw new Exception("URL=" + solrUrl + " HTTP response code=" + status);
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
     * Pipes everything from the reader to the writer via a buffer except lines
     * starting with '<?'
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
}
