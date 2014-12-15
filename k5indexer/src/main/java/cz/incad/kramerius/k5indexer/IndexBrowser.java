
package cz.incad.kramerius.k5indexer;

import cz.incad.kramerius.utils.conf.KConfiguration;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.configuration.Configuration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 *
 * @author alberto
 */
public class IndexBrowser implements Iterable<Object> {

    private static final Logger logger = Logger.getLogger(IndexBrowser.class.getName());

    public final Configuration config;
    IndexPagesIterator _iterator;
    PIDPagesIterator _pidsIterator;
    ArrayList<Object> docs;
    int index;
    int rows;
    int numDocs;
    int numFound;
    String lastBrowseFieldValue;
    String fl;
    String wt = "json";
    String host;
    String initStart = null;
    
    String browseField;
    String browseFieldType;
    boolean browseFieldSort = true;
    boolean byTerms = false;
     
    boolean _init;
    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

    public IndexBrowser() throws Exception {
        config = KConfiguration.getInstance().getConfiguration();
        this.host = config.getString("k5indexer.solr.host") + "/" + config.getString("k5indexer.solr.core");
        this.rows = config.getInt("k5indexer.convert.rows", 100);
        
        this.browseField = config.getString("k5indexer.convert.browse.field", "modified_date");
        this.browseFieldType = config.getString("k5indexer.convert.browse.field.type", "date");
        this.browseFieldSort = config.getBoolean("k5indexer.convert.browse.field.sort", true);
        this.byTerms = config.getBoolean("k5indexer.convert.browse.byTerms", false);
        
        docs = new ArrayList<Object>();
        _init = false;
        _iterator = new IndexPagesIterator();
        _pidsIterator = new PIDPagesIterator();
    }

    public void setStart(String start) {
        this.initStart = start;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setFl(String fl) {
        this.fl = fl;
    }

    public void setWt(String wt) {
        this.wt = wt;
    }
    
    private void getPids(String from) throws Exception{
// terms?terms.fl=PID&terms.sort=index&terms.lower=&terms.lower.incl=false&terms.limit=200
        String urlStr = host + "/terms?wt=json&terms.fl=PID&terms.sort=index&terms.lower=" +
                from + "&terms.lower.incl=false&terms.limit=" + rows;
        
        java.net.URL url = new java.net.URL(urlStr);
        InputStream is;
        StringWriter resp = new StringWriter();
        try {
            is = url.openStream();
        } catch (Exception ex) {
            logger.log(Level.WARNING, "", ex);
            is = url.openStream();
        }
        
            org.apache.commons.io.IOUtils.copy(is, resp, "UTF-8");
            JSONObject json = new JSONObject(resp.toString());
            JSONObject terms = json.getJSONObject("terms");
            JSONArray jpids = terms.getJSONArray("PID");
            int termsFound = jpids.length() / 2;
            docs.clear();
//            if(termsFound>0){
//                String firstPID = jpids.getString(0);
//                lastBrowseFieldValue = jpids.getString(jpids.length() - 2);
//                getDocsByPidRange(firstPID, lastBrowseFieldValue);
//            }
            
            //pid by pid
            StringBuilder sb = new StringBuilder();
            sb.append("<browse>");
            for(int i = 0; i<jpids.length(); i = i+2){
                sb.append(getDocByPid(jpids.getString(i)));
            }
            sb.append("</browse>");
            //logger.log(Level.INFO, "document: {0}", sb.toString());
            Document respDoc = builder.parse(new InputSource(new StringReader(sb.toString())));
            docs.add(respDoc);
    }
    
    private String getDocByPid(String pid) throws Exception {
        String urlStr = host + "/select?wt=" + wt +
                "&q=PID:\"" + pid + "\"";
        
        if (fl != null) {
            urlStr += "&fl=" + fl;
        }
        
        java.net.URL url = new java.net.URL(urlStr);
        InputStream is;
        StringWriter resp = new StringWriter();
        try {
            is = url.openStream();
        } catch (Exception ex) {
            logger.log(Level.WARNING, "", ex);
            is = url.openStream();
        }
        org.apache.commons.io.IOUtils.copy(is, resp, "UTF-8");
        return resp.toString().substring("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".length());
    }
    
    private void getDocsByPidRange(String from, String to) throws Exception {
        
        _init = true;
        docs.clear();
        String urlStr = host + "/select?wt=" + wt +
                "&q=*:*";
        if(from != null){
            urlStr += "&fq=PID:[" + from.replace(":", "\\:").replace(" ", "%20") + "%20TO%20" + 
                    to.replace(":", "\\:").replace(" ", "%20") + "]";
        }
        if (fl != null) {
            urlStr += "&fl=" + fl;
        }
        urlStr += "&rows=" + rows;
        
        logger.log(Level.INFO, "urlStr: {0}", urlStr);

        java.net.URL url = new java.net.URL(urlStr);
        InputStream is;
        StringWriter resp = new StringWriter();
        try {
            is = url.openStream();
        } catch (Exception ex) {
            logger.log(Level.WARNING, "", ex);
            is = url.openStream();
        }
        Document respDoc = builder.parse(is);
        docs.add(respDoc);

    }

    private void getDocs(String from) throws Exception {
        
        _init = true;
        docs.clear();
        
        
//q=foo&start=0&rows=$ROWS&sort=id+asc&fq=id:{$X TO *]
        String urlStr = host + "/select?wt=" + wt +
                "&q=*:*";
        if(from != null){
            urlStr += "&fq="+browseField+":{" + from.replace(":", "\\:").replace(" ", "%20") + "%20TO%20*}";
        }
        if (fl != null) {
            urlStr += "&fl=" + fl;
        }
        urlStr += "&rows=" + rows + "&start=0";
        if(browseFieldSort){
            urlStr += "&sort="+browseField+"+asc";
        }
        logger.log(Level.INFO, "urlStr: {0}", urlStr);

        java.net.URL url = new java.net.URL(urlStr);
        InputStream is;
        StringWriter resp = new StringWriter();
        try {
            is = url.openStream();
        } catch (Exception ex) {
            logger.log(Level.WARNING, "", ex);
            is = url.openStream();
        }

        if (wt.equals("json")) {
            org.apache.commons.io.IOUtils.copy(is, resp, "UTF-8");
            JSONObject json = new JSONObject(resp.toString());
            JSONObject response = json.getJSONObject("response");
            numFound = response.getInt("numFound");
            JSONArray jdocs = response.getJSONArray("docs");
            numDocs = jdocs.length();
            for (int i = 0; i < jdocs.length(); i++) {
                docs.add(jdocs.getJSONObject(i));
            }
        } else {
            
            Document respDoc = builder.parse(is);
            numFound = Integer.parseInt(respDoc.getElementsByTagName("result").item(0).getAttributes().getNamedItem("numFound").getNodeValue());
            numDocs = respDoc.getElementsByTagName("doc").getLength();
            if(numDocs > 0){
                NodeList fields = respDoc.getElementsByTagName("doc").item(numDocs - 1).getChildNodes();
                for(int i = 0; i<fields.getLength(); i++){
                    Node node = fields.item(i);
                    if(browseFieldType.equals(node.getNodeName()) && 
                            node.getAttributes().getNamedItem("name").getNodeValue().equals(browseField)){
                        lastBrowseFieldValue = node.getTextContent();
                        break;
                    }
                }
            }else{
                lastBrowseFieldValue = null;
            }
            docs.add(respDoc);
        }

    }

    @Override
    public Iterator iterator() {
        if(byTerms){
            return pidsIterator();
        }else{
            return pageIterator();
        }
    }
    
    private Iterator pageIterator(){
        if (!_init) {
            try {
                getDocs(initStart);
            } catch (Exception ex) {
                Logger.getLogger(IndexBrowser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return _iterator;
    }
    
    private Iterator pidsIterator() {
        if (!_init) {
            try {
                getPids(initStart);
            } catch (Exception ex) {
                Logger.getLogger(IndexBrowser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return _pidsIterator;
    }
    
    

    protected class PIDPagesIterator implements Iterator<Object> {


        public PIDPagesIterator() {
        }

        @Override
        public boolean hasNext() {
            return docs.size()>0;
        }

        @Override
        public Object next() {
            Object ret = docs.get(0);
            try {
                getPids(lastBrowseFieldValue);

            } catch (Exception ex) {
                Logger.getLogger(IndexBrowser.class.getName()).log(Level.SEVERE, null, ex);
            }

            return ret;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

    protected class IndexPagesIterator implements Iterator<Object> {


        public IndexPagesIterator() {
        }

        @Override
        public boolean hasNext() {
            return docs.size()>0;
        }

        @Override
        public Object next() {
            Object ret = docs.get(0);
            try {
                getDocs(lastBrowseFieldValue);

            } catch (Exception ex) {
                Logger.getLogger(IndexBrowser.class.getName()).log(Level.SEVERE, null, ex);
            }

            return ret;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

    

}
