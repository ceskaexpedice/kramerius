/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.kramerius.k5indexer;

import cz.incad.kramerius.utils.conf.KConfiguration;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.configuration.Configuration;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;

/**
 *
 * @author alberto
 */
public class IndexDocs implements Iterable<Object> {

    private static final Logger logger = Logger.getLogger(IndexDocs.class.getName());

    public final Configuration config;
    IndexDocsIterator _iterator;
    IndexPagesIterator _pagesIterator;
    ArrayList<Object> docs;
    int index;
    int rows;
    int numDocs;
    int numFound;
    String query;
    String fl;
    String wt = "json";
    String host;
    int initStart = 0;
    boolean _init;
    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

    public IndexDocs(String query) throws Exception {
        config = KConfiguration.getInstance().getConfiguration();
        this.host = config.getString("k5indexer.solr.host") + "/" + config.getString("k5indexer.solr.core");
        this.query = query;
        this.rows = config.getInt("k5indexer.convert.rows", 100);
        
        docs = new ArrayList<Object>();
        _init = false;
        _iterator = new IndexDocsIterator();
        _init = false;
        _pagesIterator = new IndexPagesIterator();
    }

    public void setStart(int start) {
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

    private void getDocs(int start) throws Exception {
        
        _init = true;
        docs.clear();

        String urlStr = host + "/select?wt=" + wt
                + "&q=" + URLEncoder.encode(query, "UTF-8")
                + "&rows=" + rows + "&start=" + (start+initStart);
        if (fl != null) {
            urlStr += "&fl=" + fl;
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
            docs.add(respDoc);
        }

    }

    public Iterator pagesIterator() {
        if (!_init) {
            try {
                getDocs(0);
            } catch (Exception ex) {
                Logger.getLogger(IndexDocs.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return _pagesIterator;
    }

    @Override
    public Iterator iterator() {
        if (!_init) {
            try {
                getDocs(0);
            } catch (Exception ex) {
                Logger.getLogger(IndexDocs.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return _iterator;
    }

    protected class IndexPagesIterator implements Iterator<Object> {

        int offset;

        public IndexPagesIterator() {
            offset = 0;
        }

        @Override
        public boolean hasNext() {
            return offset + numDocs < numFound;
            //return docIndexInArray < numDocs;
        }

        @Override
        public Object next() {
            Object ret = docs.get(0);
            try {
                offset += numDocs;
                getDocs(offset);

            } catch (Exception ex) {
                Logger.getLogger(IndexDocs.class.getName()).log(Level.SEVERE, null, ex);
            }

            return ret;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

    protected class IndexDocsIterator implements Iterator<Object> {

        int index;
        int docIndexInArray;

        public IndexDocsIterator() {
            index = 0;
            docIndexInArray = 0;
        }

        @Override
        public boolean hasNext() {
            return index < numFound;
            //return docIndexInArray < numDocs;
        }

        @Override
        public Object next() {
            if (docIndexInArray >= docs.size()) {
                try {
                    getDocs(index);
                    docIndexInArray = 0;
                } catch (Exception ex) {
                    Logger.getLogger(IndexDocs.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            Object ret = docs.get(docIndexInArray);
            index++;
            docIndexInArray++;
            return ret;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

}
