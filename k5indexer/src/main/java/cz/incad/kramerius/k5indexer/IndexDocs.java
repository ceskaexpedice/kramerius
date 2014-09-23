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
import org.apache.commons.configuration.Configuration;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author alberto
 */
public class IndexDocs implements Iterable<JSONObject> {

    private static final Logger logger = Logger.getLogger(IndexDocs.class.getName());
    
    public final Configuration config;
    IndexDocsIterator _iterator;
    ArrayList<JSONObject> roots;
    int index;
    int rows;
    int numDocs;
    String query;
    String fl;
    boolean _init;

    public IndexDocs(String query) throws Exception {
        config = KConfiguration.getInstance().getConfiguration();
        this.query = query;
        rows = config.getInt("k5indexer.convert.rows", 100);
        roots = new ArrayList<JSONObject>();
        _init = false;
        _iterator = new IndexDocsIterator();
    }
    
    public void setFl(String fl){
        this.fl = fl;
    }

    private void getDocs(int start) throws Exception {
        _init = true;
        roots.clear();
        String k5Index = config.getString("k5indexer.convert.dest") + "/" + config.getString("k5indexer.solrCollection");
        
        String urlStr = k5Index + "/select?wt=json&q=" + URLEncoder.encode(query, "UTF-8") + "&rows=" + rows + "&start=" + start;
        if(fl != null){
            urlStr += "&fl=" + fl;
        }
        logger.log(Level.INFO, "urlStr: {0}", urlStr);
        JSONObject json;

        java.net.URL url = new java.net.URL(urlStr);
        InputStream is;
        try {
            is = url.openStream();
            StringWriter sw = new StringWriter();
            org.apache.commons.io.IOUtils.copy(is, sw, "UTF-8");
            json = new JSONObject(sw.toString());
        } catch (Exception ex) {
            logger.log(Level.WARNING, "", ex);
            is = url.openStream();
            StringWriter sw = new StringWriter();
            org.apache.commons.io.IOUtils.copy(is, sw, "UTF-8");
            json = new JSONObject(sw.toString());
        }
        JSONObject response = json.getJSONObject("response");
        numDocs = response.getInt("numFound");
        JSONArray docs = response.getJSONArray("docs");
        for (int i = 0; i < docs.length(); i++) {
            roots.add( docs.getJSONObject(i));
        }
    }

    @Override
    public Iterator iterator() {
        if (!_init){
            try {
                getDocs(0);
            } catch (Exception ex) {
                Logger.getLogger(IndexDocs.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return _iterator;
    }

    protected class IndexDocsIterator implements Iterator<JSONObject> {

        int index;
        int docIndexInArray;
        public IndexDocsIterator() {
            index = 0;
            docIndexInArray = 0;
        }

        @Override
        public boolean hasNext() {
            return index < numDocs;
        }

        @Override
        public JSONObject next() {
            if (docIndexInArray >= roots.size()){
                try {
                    getDocs(index);
                    docIndexInArray = 0;
                } catch (Exception ex) {
                    Logger.getLogger(IndexDocs.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            JSONObject jo = roots.get(docIndexInArray);
            index++;
            docIndexInArray++;
            return jo;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

}