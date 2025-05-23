package cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.impl;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.AlreadyRegistedPidsException;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestItem;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestManager;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class SolrReharvestManagerImpl implements ReharvestManager {

    public static Logger LOGGER = Logger.getLogger(SolrReharvestManagerImpl.class.getName());

    private Client client;

    public SolrReharvestManagerImpl() {
        this.client = Client.create();
    }

    @Override
    public void register(ReharvestItem item) throws AlreadyRegistedPidsException {
        try {
            this.register(item, true);
        } catch (UniformInterfaceException | ClientHandlerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Override
    public void register(ReharvestItem item, boolean preventMultipleRegistrationFlag) throws AlreadyRegistedPidsException {
        try {
            String reharvest = KConfiguration.getInstance().getSolrReharvestHost();
            if (preventMultipleRegistrationFlag) {
                List<String> pids = findAllRegistredPids(reharvest);
                if (pids.contains(item.getPid())) {
                    throw new AlreadyRegistedPidsException(Arrays.asList(item.getPid()));
                }
            }
            WebResource updateResource = this.client.resource(reharvest + "/update/json/docs?split=/&commit=true");
            String updated = updateResource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON)
                    .entity(item.toJSON().toString(), MediaType.APPLICATION_JSON).post(String.class);
        } catch (UnsupportedEncodingException | UniformInterfaceException | ClientHandlerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Override
    public ReharvestItem update(ReharvestItem item) {
        try {
            String reharvest = KConfiguration.getInstance().getSolrReharvestHost();
            WebResource updateResource = this.client.resource(reharvest + "/update/json/docs?split=/&commit=true");
            String payload = item.toJSON().toString();
            String updated = updateResource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON)
                    .entity(payload, MediaType.APPLICATION_JSON).post(String.class);
            LOGGER.info(String.format("Update response %s ", updated));
            return findItem(item.getId());
        } catch (UniformInterfaceException | ClientHandlerException | UnsupportedEncodingException | JSONException
                | ParseException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }
    
    private ReharvestItem findItem(String id) throws UnsupportedEncodingException, JSONException, ParseException {
        String reharvest = KConfiguration.getInstance().getSolrReharvestHost();
        List<String> pids = new ArrayList<>();
        String query = URLEncoder.encode("state:(%s)", "UTF-8");
        String fullUrl = String.format("%s/select?q=id:%ss&rows=1", reharvest, id);
        String t = solrGet(fullUrl);
        JSONArray docs = solrDocs(new JSONObject(t));
        if (docs.length() > 0) {
            return ReharvestItem.fromJSON(docs.getJSONObject(0));
        } else return null;
    }
    
    private List<String> findAllRegistredPids(String reharvest) throws UnsupportedEncodingException {
        List<String> pids = new ArrayList<>();
        String query = URLEncoder.encode("state:(open OR waiting_for)", "UTF-8");
        String fullUrl = String.format("%s/select?q=%s&rows=10000&fl=pid", reharvest, query);
        String t = solrGet(fullUrl);
        JSONArray docs = solrDocs(new JSONObject(t));
        for (int i = 0; i < docs.length(); i++) {
            JSONObject doc = docs.getJSONObject(i);
            if (doc.has("pid")) {
                pids.add(doc.getString("pid"));
            }
        }
        return pids;
    }

    private String solrGet(String fullUrl) {
        WebResource resource = this.client.resource(fullUrl);
        String t = resource.accept(MediaType.APPLICATION_JSON).get(String.class);
        return t;
    }


    @Override
    public String searchItems(int start, int rows, List<String> filters) {
        try {
            String reharvest = KConfiguration.getInstance().getSolrReharvestHost();
            String sort = URLEncoder.encode("indexed desc", "UTF-8");

            String fullUrl = String.format("%s/select?q=*&rows=%d&sort=%s&start=%d", reharvest, rows,sort,start);
            if (!filters.isEmpty()) {
                String fq = filters.stream().map(f-> {
                    try {
                        return "fq="+ URLEncoder.encode(f, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.joining("&"));
                fullUrl = fullUrl +"&"+ fq;
            }

            LOGGER.info( String.format("Requesting url %s", fullUrl));
            String t = solrGet(fullUrl);
            return t;
        } catch (UniformInterfaceException | ClientHandlerException | JSONException |  UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return new JSONObject().toString();
    }

    @Override
    public List<ReharvestItem> getAllItems() {
        List<ReharvestItem> items = new ArrayList<>();
        try {
            String reharvest = KConfiguration.getInstance().getSolrReharvestHost();
            String sort = URLEncoder.encode("indexed asc", "UTF-8");
            String fullUrl = String.format("%s/select?q=*&rows=10000&sort=%s", reharvest,sort);
            String t = solrGet(fullUrl);
            JSONArray docs = solrDocs(new JSONObject(t));
            for (int i = 0; i < docs.length(); i++) {
                JSONObject doc = docs.getJSONObject(i);
                ReharvestItem reharvestItem = ReharvestItem.fromJSON(doc);
                if (reharvestItem != null)
                    items.add(reharvestItem);
            }
        } catch (UniformInterfaceException | ClientHandlerException | JSONException | ParseException | UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return items;
    }

    @Override
    public ReharvestItem getTopItem(String status) {
        try {
            String reharvest = KConfiguration.getInstance().getSolrReharvestHost();
            String query = String.format("state:(%s)", status);
            String sort = URLEncoder.encode("indexed asc", "UTF-8");
            
            String fullUrl = String.format("%s/select?q=%s&rows=1&sort=%s", reharvest, query, sort);
            String t = solrGet(fullUrl);
            JSONArray docs = solrDocs(new JSONObject(t));
            if (docs.length() > 0) {
                JSONObject doc = docs.getJSONObject(0);
                ReharvestItem reharvestItem = ReharvestItem.fromJSON(doc);
                return reharvestItem;
            }
        } catch (UniformInterfaceException | ClientHandlerException | JSONException | ParseException | UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    public List<ReharvestItem> getItemByConflictId(String cid) {
        try {
            List<ReharvestItem> items = new ArrayList<>();
            String reharvest = KConfiguration.getInstance().getSolrReharvestHost();
            String query = String.format("conflict_id:(%s)", cid);
            String sort = URLEncoder.encode("indexed asc","UTF-8");
            String fullUrl = String.format("%s/select?q=%s&rows=100&sort=%s", reharvest, query,sort);
            LOGGER.info(String.format("Requesting url %s", fullUrl));
            String t = solrGet(fullUrl);
            LOGGER.info(String.format("Solr response  %s", t));
            JSONObject solrResp = new JSONObject(t);
            JSONArray docs = solrDocs(solrResp);
            if (docs.length() > 0) {
                for (int i = 0; i < docs.length(); i++) {
                    ReharvestItem reharvestItem = ReharvestItem.fromJSON(docs.getJSONObject(i));
                    items.add(reharvestItem);
                }
            }
            return items;
        } catch (UniformInterfaceException | ClientHandlerException | JSONException | ParseException | UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return new ArrayList<>();
   }

    @Override
    public ReharvestItem getItemById(String id) {
        try {
            String reharvest = KConfiguration.getInstance().getSolrReharvestHost();
            String query = String.format("id:(%s)", id);
            String sort = URLEncoder.encode("indexed asc","UTF-8");
            String fullUrl = String.format("%s/select?q=%s&rows=1&sort=%s", reharvest, query,sort);
            String t = solrGet(fullUrl);
            JSONObject solrResp = new JSONObject(t);
            JSONArray docs = solrDocs(solrResp);
            if (docs.length() > 0) {
                JSONObject doc = docs.getJSONObject(0);
                ReharvestItem reharvestItem = ReharvestItem.fromJSON(doc);
                return reharvestItem;
            }
        } catch (UniformInterfaceException | ClientHandlerException | JSONException | ParseException | UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    
    
    
    
    @Override
    public ReharvestItem getOpenItemByPid(String pid) {
        try {
            String reharvest = KConfiguration.getInstance().getSolrReharvestHost();
            String query = URLEncoder.encode( String.format("pid:(\"%s\") AND NOT state:(closed OR cancelled OR failed)", pid),"UTF-8");
            String sort = URLEncoder.encode("indexed asc","UTF-8");
            String fullUrl = String.format("%s/select?q=%s&rows=1&sort=%s", reharvest, query,sort);
            String t = solrGet(fullUrl);
            JSONObject solrResp = new JSONObject(t);
            JSONArray docs = solrDocs(solrResp);
            if (docs.length() > 0) {
                JSONObject doc = docs.getJSONObject(0);
                ReharvestItem reharvestItem = ReharvestItem.fromJSON(doc);
                return reharvestItem;
            }
        } catch (UniformInterfaceException | ClientHandlerException | JSONException | ParseException | UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }


    private JSONArray solrDocs(JSONObject solrResp) {
        JSONArray docs = new JSONArray();
        if (solrResp.has("response")) {
            docs = solrResp.getJSONObject("response").getJSONArray("docs");
        }
        return docs;
    }

    @Override
    public void deregister(String id) {
        try {
            Document deleteDoc = XMLUtils.crateDocument("delete");
            Element idElm = deleteDoc.createElement("id");
            idElm.setTextContent(id);
            deleteDoc.getDocumentElement().appendChild(idElm);
            StringWriter writer = new StringWriter();
            XMLUtils.print(deleteDoc, writer);

            String reharvest = KConfiguration.getInstance().getSolrReharvestHost();

            WebResource updateResource = this.client.resource(reharvest + "/update?commit=true");
            String updated = updateResource.accept(MediaType.APPLICATION_XML).type(MediaType.APPLICATION_XML)
                    .entity(writer.toString(), MediaType.APPLICATION_XML).post(String.class);
        } catch (DOMException | UniformInterfaceException | ClientHandlerException | ParserConfigurationException
                | TransformerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
