package cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.impl;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.AlreadyRegistedPidsException;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestItem;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestManager;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class SolrReharvestManagerImpl implements ReharvestManager {

    private static final Logger LOGGER = Logger.getLogger(SolrReharvestManagerImpl.class.getName());

    private final Client client;

    public SolrReharvestManagerImpl() {
        this.client = ClientBuilder.newClient();
    }

    @Override
    public void register(ReharvestItem item) throws AlreadyRegistedPidsException {
        this.register(item, true);
    }

    @Override
    public void register(ReharvestItem item, boolean preventMultipleRegistrationFlag) throws AlreadyRegistedPidsException {
        try {
            String reharvest = KConfiguration.getInstance().getSolrReharvestHost();
            if (preventMultipleRegistrationFlag) {
                List<String> pids = findAllRegistredPids(reharvest);
                if (pids.contains(item.getPid())) {
                    throw new AlreadyRegistedPidsException(Collections.singletonList(item.getPid()));
                }
            }
            WebTarget target = client.target(reharvest + "/update/json/docs")
                    .queryParam("split", "/")
                    .queryParam("commit", "true");

            String updated = target.request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(item.toJSON().toString(), MediaType.APPLICATION_JSON), String.class);

            LOGGER.info("Register response: " + updated);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Override
    public ReharvestItem update(ReharvestItem item) {
        try {
            String reharvest = KConfiguration.getInstance().getSolrReharvestHost();

            WebTarget target = client.target(reharvest + "/update/json/docs")
                    .queryParam("split", "/")
                    .queryParam("commit", "true");

            String updated = target.request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(item.toJSON().toString(), MediaType.APPLICATION_JSON), String.class);

            LOGGER.info("Update response: " + updated);

            return findItem(item.getId());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    private ReharvestItem findItem(String id) throws UnsupportedEncodingException, JSONException, ParseException {
        String reharvest = KConfiguration.getInstance().getSolrReharvestHost();
        String fullUrl = String.format("%s/select?q=id:%s&rows=1", reharvest, URLEncoder.encode(id, "UTF-8"));
        String response = solrGet(fullUrl);
        JSONArray docs = solrDocs(new JSONObject(response));
        if (docs.length() > 0) {
            return ReharvestItem.fromJSON(docs.getJSONObject(0));
        }
        return null;
    }

    private List<String> findAllRegistredPids(String reharvest) throws UnsupportedEncodingException {
        List<String> pids = new ArrayList<>();
        String query = URLEncoder.encode("state:(open OR waiting_for)", "UTF-8");
        String fullUrl = String.format("%s/select?q=%s&rows=10000&fl=pid", reharvest, query);
        String response = solrGet(fullUrl);
        JSONArray docs = solrDocs(new JSONObject(response));
        for (int i = 0; i < docs.length(); i++) {
            JSONObject doc = docs.getJSONObject(i);
            if (doc.has("pid")) {
                pids.add(doc.getString("pid"));
            }
        }
        return pids;
    }

    private String solrGet(String fullUrl) {
        WebTarget target = client.target(fullUrl);
        return target.request(MediaType.APPLICATION_JSON).get(String.class);
    }

    @Override
    public String searchItems(int start, int rows, List<String> filters) {
        try {
            String reharvest = KConfiguration.getInstance().getSolrReharvestHost();
            String sort = URLEncoder.encode("indexed desc", "UTF-8");

            String fullUrl = String.format("%s/select?q=*&rows=%d&sort=%s&start=%d", reharvest, rows, sort, start);

            if (!filters.isEmpty()) {
                String fq = filters.stream()
                        .map(f -> {
                            try {
                                return "fq=" + URLEncoder.encode(f, "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .collect(Collectors.joining("&"));
                fullUrl = fullUrl + "&" + fq;
            }

            LOGGER.info("Requesting url " + fullUrl);
            return solrGet(fullUrl);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return new JSONObject().toString();
    }

    @Override
    public List<ReharvestItem> getAllItems() {
        List<ReharvestItem> items = new ArrayList<>();
        try {
            String reharvest = KConfiguration.getInstance().getSolrReharvestHost();
            String fullUrl = String.format("%s/select?q=*&rows=10000&sort=%s", reharvest,
                    URLEncoder.encode("indexed asc", "UTF-8"));
            String response = solrGet(fullUrl);
            JSONArray docs = solrDocs(new JSONObject(response));
            for (int i = 0; i < docs.length(); i++) {
                ReharvestItem ri = ReharvestItem.fromJSON(docs.getJSONObject(i));
                if (ri != null) items.add(ri);
            }
        } catch (Exception e) {
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
            String response = solrGet(fullUrl);
            JSONArray docs = solrDocs(new JSONObject(response));
            if (docs.length() > 0) {
                return ReharvestItem.fromJSON(docs.getJSONObject(0));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    public List<ReharvestItem> getItemByConflictId(String cid) {
        List<ReharvestItem> items = new ArrayList<>();
        try {
            String reharvest = KConfiguration.getInstance().getSolrReharvestHost();
            String query = String.format("conflict_id:(%s)", cid);
            String sort = URLEncoder.encode("indexed asc", "UTF-8");
            String fullUrl = String.format("%s/select?q=%s&rows=100&sort=%s", reharvest, query, sort);

            LOGGER.info("Requesting url " + fullUrl);
            String response = solrGet(fullUrl);
            LOGGER.info("Solr response: " + response);
            JSONArray docs = solrDocs(new JSONObject(response));
            for (int i = 0; i < docs.length(); i++) {
                items.add(ReharvestItem.fromJSON(docs.getJSONObject(i)));
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return items;
    }

    @Override
    public ReharvestItem getItemById(String id) {
        try {
            String reharvest = KConfiguration.getInstance().getSolrReharvestHost();
            String query = String.format("id:(%s)", id);
            String sort = URLEncoder.encode("indexed asc", "UTF-8");
            String fullUrl = String.format("%s/select?q=%s&rows=1&sort=%s", reharvest, query, sort);
            String response = solrGet(fullUrl);
            JSONArray docs = solrDocs(new JSONObject(response));
            if (docs.length() > 0) {
                return ReharvestItem.fromJSON(docs.getJSONObject(0));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    @Override
    public ReharvestItem getOpenItemByPid(String pid) {
        try {
            String reharvest = KConfiguration.getInstance().getSolrReharvestHost();
            String query = URLEncoder.encode(String.format(
                    "pid:(\"%s\") AND NOT state:(closed OR cancelled OR failed)", pid), "UTF-8");
            String sort = URLEncoder.encode("indexed asc", "UTF-8");
            String fullUrl = String.format("%s/select?q=%s&rows=1&sort=%s", reharvest, query, sort);
            String response = solrGet(fullUrl);
            JSONArray docs = solrDocs(new JSONObject(response));
            if (docs.length() > 0) {
                return ReharvestItem.fromJSON(docs.getJSONObject(0));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    private JSONArray solrDocs(JSONObject solrResp) {
        if (solrResp.has("response")) {
            return solrResp.getJSONObject("response").getJSONArray("docs");
        }
        return new JSONArray();
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
            WebTarget target = client.target(reharvest + "/update").queryParam("commit", "true");

            String response = target.request(MediaType.APPLICATION_XML)
                    .post(Entity.entity(writer.toString(), MediaType.APPLICATION_XML), String.class);

            LOGGER.info("Deregister response: " + response);

        } catch (DOMException | ParserConfigurationException | TransformerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}