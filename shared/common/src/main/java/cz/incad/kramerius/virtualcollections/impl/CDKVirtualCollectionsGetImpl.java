package cz.incad.kramerius.virtualcollections.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;
import javax.xml.xpath.XPathExpressionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.utils.DCUtils;
import cz.incad.kramerius.virtualcollections.CDKVirtualCollectionsGet;
import cz.incad.kramerius.virtualcollections.Collection;

public class CDKVirtualCollectionsGetImpl implements CDKVirtualCollectionsGet {
    
    // TODO: Synchronized together 
//    private static Map<String, List<String>> COLLECTIONS_MAPPING = new HashMap<String, List<String>>();
//    private static Map<String, JSONObject> COLLECTIONS_CACHE = new HashMap<String, JSONObject>(); 
    
    private static SimpleCollectionsCache _CACHE = new SimpleCollectionsCache();
    
    private static final String URI_PREFIX = "URI";

    public static final Logger LOGGER = Logger
            .getLogger(CDKVirtualCollectionsGetImpl.class.getName());

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    public CDKVirtualCollectionsGetImpl() {
    }

    
    public CDKVirtualCollectionsGetImpl(SimpleCollectionsCache cache) {
        _CACHE = cache;
    }
    
    public synchronized List<Collection> virtualCollectionsFromResource(
            String resource) {
        List<Collection> vcs = new ArrayList<Collection>();
        CDKResourcesFilter filter = createResourceFilter();
        if (!filter.isHidden(resource)) {
            try {
                if (!_CACHE.containsResource(resource)) {
                    Document dc = this.fedoraAccess.getDC(resource);
                    String vcpoint = appendVCPoint(disectURL(dc));
                    _CACHE.fillCacheFromResoure(resource, vcpoint);
                }
                JSONArray jsonArray = _CACHE.getResourceJSONArray(resource);
                for (int i = 0, ll = jsonArray.length(); i < ll; i++) {
                    Collection vc = collectionFromJSON(jsonArray
                            .getJSONObject(i));
                    vcs.add(vc);
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            } catch (XPathExpressionException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            } catch (JSONException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        return vcs;
    }

    CDKResourcesFilter createResourceFilter() {
        return new CDKResourcesFilter();
    }

    public List<Collection> virtualCollections() {
        //clearAllResources();
        List<Collection> vcs = new ArrayList<Collection>();
        CDKResourcesFilter filter = createResourceFilter();
        List<String> resources = filter.getResources();
        for (String res : resources) {
            List<Collection> subVCS = virtualCollectionsFromResource(res);
            vcs.addAll(subVCS);
        }
        return vcs;
    }

    @Override
    public Collection virtualCollectionsFromResource(String vc,
            String resource) {
        CDKResourcesFilter filter = createResourceFilter();
        if (filter.isResource(resource)) {
            if (!filter.isHidden(resource)) {
                try {
                    
                    if (!_CACHE.containsResource(resource)) {
                        Document dc = this.fedoraAccess.getDC(resource);
                        String vcpoint = appendVCPoint(disectURL(dc));
                        _CACHE.fillCacheFromResoure(resource, vcpoint);
                    }
                    JSONArray jsonArray = _CACHE.getResourceJSONArray(resource);
                    for (int i = 0,ll=jsonArray.length(); i < ll; i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        Collection vcObject = collectionFromJSON(jsonObject);
                        if (vc.equals(vcObject.getPid())) return vcObject;
                        
                    }
                    
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                } catch (XPathExpressionException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                } catch (JSONException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }
        return null;
    }

    @Override
    public String getResource(String vcId)  {
        try {
            CDKResourcesFilter filter = createResourceFilter();
            List<String> resources = filter.getResources();
            for (String res : resources) {
                boolean containsResource = _CACHE.containsResource(res);
                if (!containsResource) {
                    Document dc = this.fedoraAccess.getDC(res);
                    String vcpoint = appendVCPoint(disectURL(dc));
                    _CACHE.fillCacheFromResoure(res, vcpoint);
                }
                JSONArray jsonArray = _CACHE.getResourceJSONArray(res);
                for (int i = 0,ll=jsonArray.length(); i < ll; i++) {
                    JSONObject jsonObj = jsonArray.getJSONObject(i);
                    String pid = jsonObj.getString("pid");
                    if (pid.equals(vcId)) return res;
                }
            }
        } catch (XPathExpressionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    
    public FedoraAccess getFedoraAccess() {
        return fedoraAccess;
    }

    public void setFedoraAccess(FedoraAccess fedoraAccess) {
        this.fedoraAccess = fedoraAccess;
    }

    static Collection collectionFromJSON(JSONObject jsonObject)
            throws JSONException {

        String pid = jsonObject.getString("pid");
        String label = jsonObject.getString("label");
        boolean cleave = jsonObject.getBoolean("canLeave");
        String url = jsonObject.has("url") ? jsonObject.getString("url") : "";
        Collection col = new Collection(pid,label,url ,cleave);
        JSONObject descs = jsonObject.getJSONObject("descs");
        Iterator keys = descs.keys();
        while (keys.hasNext()) {
            String k = (String) keys.next();
            String string = descs.getString(k);
            Collection.Description desc = new Collection.Description(k, string, string);
            col.addDescription(desc);
        }
        return col;
    }

    
    static JSONArray virtualCollectionsFromPoint(String point)
            throws JSONException {
        try {
            Client c = Client.create();
            WebResource r = c.resource(point);
            r.setProperty(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, new Boolean(true));
            String t = r.accept(MediaType.APPLICATION_JSON).get(String.class);
            JSONArray jsonArr = new JSONArray(t);
            return jsonArr;
        } catch (UniformInterfaceException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            
        } catch (ClientHandlerException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
        return new JSONArray();
    }

    static JSONObject virtualCollectionFromPoint(String point, String pid)
            throws JSONException {
        Client c = Client.create();
        WebResource r = c.resource(point + "/" + pid);
        String t = r.accept(MediaType.APPLICATION_JSON).get(String.class);
        JSONObject json = new JSONObject(t);
        return json;
    }

    static String appendVCPoint(String baseUrl) {
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        return baseUrl + "api/v5.0/vc";
    }

   public static String disectURL(Document dc) throws XPathExpressionException {
       String src = DCUtils.sourceFromDC(dc);
       return src;
    }

   
   static class SimpleCollectionsCache {
       
       private Map<String, JSONArray> collectionsResults = new HashMap<String, JSONArray>();
       private Map<String, JSONObject> collectionObject = new HashMap<String, JSONObject>();
       
       
       public synchronized  void fillCacheFromResoure(String resourcePid, String point) {
           JSONArray jsonArray = virtualCollectionsFromPoint(point);
           this.collectionsResults.put(resourcePid, jsonArray);
           for (int i = 0,ll=jsonArray.length(); i < ll; i++) {
               JSONObject jsonObject = jsonArray.getJSONObject(i);
               this.collectionObject.put(jsonObject.getString("pid"), jsonObject);
               
           }
       }
       
       synchronized JSONArray virtualCollectionsFromPoint(String point) {
           return CDKVirtualCollectionsGetImpl.virtualCollectionsFromPoint(point);
       }

       public synchronized boolean containsResource(String resourcePid) {
           return collectionsResults.containsKey(resourcePid);
       }
       
       public synchronized boolean containsCache(String colPid) {
           return this.collectionObject.containsKey(colPid);
       }
       
       public synchronized JSONArray getResourceJSONArray(String resourcePid) {
           return this.collectionsResults.get(resourcePid);
       }
       
       public synchronized JSONObject getJSONCollection(String colPid) {
           return this.collectionObject.get(colPid);
       }
      
       public synchronized boolean isEmpty() {
           return this.collectionsResults.isEmpty();
       }
   }
   
}
