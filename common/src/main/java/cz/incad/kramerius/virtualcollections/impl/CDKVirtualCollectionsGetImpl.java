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
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.utils.DCUtils;
import cz.incad.kramerius.virtualcollections.CDKVirtualCollectionsGet;
import cz.incad.kramerius.virtualcollections.VirtualCollection;
import cz.incad.kramerius.virtualcollections.VirtualCollection.CollectionDescription;

public class CDKVirtualCollectionsGetImpl implements CDKVirtualCollectionsGet {

    private static Map<String, List<String>> COLLECTIONS_MAPPING = new HashMap<String, List<String>>();

    private static final String URI_PREFIX = "URI";

    public static final Logger LOGGER = Logger
            .getLogger(CDKVirtualCollectionsGetImpl.class.getName());

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    public CDKVirtualCollectionsGetImpl() {
    }

    public List<VirtualCollection> virtualCollectionsFromResource(
            String resource) {
        clearResource(resource);
        List<VirtualCollection> vcs = new ArrayList<VirtualCollection>();
        CDKResourcesFilter filter = new CDKResourcesFilter();
        if (!filter.isHidden(resource)) {
            try {
                Document dc = this.fedoraAccess.getDC(resource);
                String vcpoint = appendVCPoint(disectURL(dc));
                JSONArray jsonArray = virtualCollectionsFromPoint(vcpoint);
                for (int i = 0, ll = jsonArray.length(); i < ll; i++) {
                    VirtualCollection vc = collectionFromJSON(jsonArray
                            .getJSONObject(i));
                    addResourceVC(resource, vc.getPid());
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

    public List<VirtualCollection> virtualCollections() {
        clearAllResources();
        List<VirtualCollection> vcs = new ArrayList<VirtualCollection>();
        CDKResourcesFilter filter = new CDKResourcesFilter();
        List<String> resources = filter.getResources();
        for (String res : resources) {
            List<VirtualCollection> subVCS = virtualCollectionsFromResource(res);
            vcs.addAll(subVCS);
        }
        return vcs;
    }

    @Override
    public VirtualCollection virtualCollectionsFromResource(String vc,
            String res) {
        CDKResourcesFilter filter = new CDKResourcesFilter();
        if (filter.isResource(res)) {
            if (!filter.isHidden(res)) {
                // http://krameriusdemo.mzk.cz/search/api/v5.0/vc/vc:758d7168-d625-4648-911a-9a80473a1717
                try {
                    Document dc = this.fedoraAccess.getDC(res);
                    String vcpoint = appendVCPoint(disectURL(dc)) + "/" + vc;
                    JSONObject jsonObject = virtualCollectionFromPoint(vcpoint,
                            vc);
                    VirtualCollection vcObject = collectionFromJSON(jsonObject);
                    addResourceVC(res, vcObject.getPid());
                    return vcObject;
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
    public String getResource(String vcId) {
        Set<String> keys = COLLECTIONS_MAPPING.keySet();
        for (String resource : keys) {
            List<String> list = COLLECTIONS_MAPPING.get(resource);
            if (list.contains(vcId))
                return resource;
        }
        return null;
    }

    
    public FedoraAccess getFedoraAccess() {
        return fedoraAccess;
    }

    public void setFedoraAccess(FedoraAccess fedoraAccess) {
        this.fedoraAccess = fedoraAccess;
    }

    static VirtualCollection collectionFromJSON(JSONObject jsonObject)
            throws JSONException {
        String pid = jsonObject.getString("pid");
        String label = jsonObject.getString("label");
        boolean cleave = jsonObject.getBoolean("canLeave");
        VirtualCollection col = new VirtualCollection(label, pid, cleave);
        JSONObject descs = jsonObject.getJSONObject("descs");
        Iterator keys = descs.keys();
        while (keys.hasNext()) {
            String k = (String) keys.next();
            col.addDescription(k, descs.getString(k));
        }
        return col;
    }

    static JSONArray virtualCollectionsFromPoint(String point)
            throws JSONException {
        Client c = Client.create();
        WebResource r = c.resource(point);
        String t = r.accept(MediaType.APPLICATION_JSON).get(String.class);
        JSONArray jsonArr = new JSONArray(t);
        return jsonArr;
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

    static synchronized void clearAllResources() {
        COLLECTIONS_MAPPING.clear();
    }

    static synchronized void clearResource(String resource) {
        COLLECTIONS_MAPPING.remove(resource);
    }

    static synchronized void addResourceVC(String resource, String vcid) {
        if (!COLLECTIONS_MAPPING.containsKey(resource)) {
            COLLECTIONS_MAPPING.put(resource, new ArrayList<String>());
        }
        List<String> alist = COLLECTIONS_MAPPING.get(resource);
        if (!alist.contains(vcid)) {
            alist.add(vcid);
        }
    }

    static synchronized void removeResourceVC(String resource, String vcid) {
        if (!COLLECTIONS_MAPPING.containsKey(resource)) {
            COLLECTIONS_MAPPING.put(resource, new ArrayList<String>());
        }
        List<String> alist = COLLECTIONS_MAPPING.get(resource);
        if (alist.contains(vcid)) {
            alist.remove(vcid);
        }
    }

    public static void main(String[] args) throws JSONException {
        List<VirtualCollection> cols = new ArrayList<VirtualCollection>();
        String str = "http://kramerius4.nkp.cz/search/";
        JSONArray jsonArray = virtualCollectionsFromPoint(appendVCPoint(str));
        for (int i = 0, ll = jsonArray.length(); i < ll; i++) {
            cols.add(collectionFromJSON(jsonArray.getJSONObject(i)));
        }

        for (VirtualCollection vc : cols) {
            System.out.println(vc.getLabel());
            System.out.println(vc.getPid());
            System.out.println(vc.getDescriptionLocale("en"));
            System.out.println(vc.getDescriptionLocale("cs"));
        }
    }

}
