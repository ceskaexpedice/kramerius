package cz.incad.kramerius.client.cache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.incad.utils.IOUtils;

public class SimpleJSONResultsCache {

    public static final SimpleJSONResultsCache CACHE = new SimpleJSONResultsCache();
    
    public static final int MAX_RESULTS = 4;
    
    private Map<String, byte[]> results = new HashMap<String, byte[]>();
    private List<String> uriLists = new ArrayList<String>();
    
    private SimpleJSONResultsCache() {
        
    }
    
    public synchronized void chacheResult(String uri, byte[] jsonRes) {
        if (uriLists.size() == MAX_RESULTS) {
            String removingURI = uriLists.get(0);
            uriLists.remove(removingURI);
            results.remove(removingURI);
        }
        this.results.put(uri, jsonRes);
        this.uriLists.add(uri);
    }
    
    public synchronized byte[] getJSONResult(String uri) {
        return this.results.get(uri);
    }

    public synchronized boolean isPresent(String uri) {
        return this.results.containsKey(uri);
    }
    
    public synchronized byte[] processThroughCache(String key, HttpURLConnection hcon) throws IOException {
        if (this.results.containsKey(key)) {
            return this.results.get(key);
        } else {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            InputStream is = hcon.getInputStream();
            IOUtils.copyStreams(is, bos);
            byte[] bytes = bos.toByteArray();
            this.chacheResult(key, bytes);
            return bytes;
        }
    }

    public void print() {
        for (String str : this.uriLists) {
            System.out.println(str+":"+this.results.get(str));
        }
        System.out.println(this.uriLists.size() +"=="+this.results.size());
    }
    
    public static void main(String[] args) {
        SimpleJSONResultsCache scache = new SimpleJSONResultsCache();
        for (int i = 0; i < 200; i++) {
            scache.chacheResult(""+i, "jsonres".getBytes());
            if (i%8== 0 ){
                scache.print();
            }
        }
    }
}
