package cz.incad.kramerius.rest.api.k5.client.item.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.core.UriBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.api.k5.client.JSONDecorator;
import cz.incad.kramerius.rest.api.k5.client.JSONDecoratorsAggregate;
import cz.incad.kramerius.rest.api.k5.client.SolrMemoization;
import cz.incad.kramerius.rest.api.k5.client.SolrResultsAware;
import cz.incad.kramerius.rest.api.k5.client.item.ItemResource;
import cz.incad.kramerius.rest.api.k5.client.utils.JSONUtils;
import cz.incad.kramerius.rest.api.k5.client.utils.SOLRUtils;
import cz.incad.kramerius.utils.XMLUtils;

public class ItemResourceUtils {

    public static final Logger LOGGER = Logger.getLogger(ItemResourceUtils.class.getName());

 
    public static List<String> solrChildrenPids(String parentPid, List<String> fList, SolrAccess sa, SolrMemoization memo) throws IOException {
        List<Document> docs = new  ArrayList<Document>();
        List<Map<String, String>> ll = new ArrayList<Map<String, String>>();
        int rows = 10000;
        int size = 1; // 1 for the first iteration
        int offset = 0;
        while (offset < size) {
            // request
            String request = "q=parent_pid:\"" + parentPid
                    + "\"&rows=" + rows + "&start=" + offset;
            if (!fList.isEmpty()) {
                request+="&fl=";
                for (int i = 0,bl=fList.size(); i < bl; i++) {
                    if (i >= 0) request += ",";
                    request += fList.get(i);
                }
            }
            
            Document resp = sa.request(request);
            docs.add(resp);
    
            Element resultelm = XMLUtils.findElement(resp.getDocumentElement(), "result");
            // define size
            size = Integer.parseInt(resultelm.getAttribute("numFound"));
            List<Element> elms = XMLUtils.getElements(resultelm,
                    new XMLUtils.ElementsFilter() {
                        @Override
                        public boolean acceptElement(Element element) {
                            if (element.getNodeName().equals("doc")) {
                                return true;
                            } else
                                return false;
                        }
                    });
            
            for (Element docelm : elms) {
                String docpid = SOLRUtils.value(docelm, "PID", String.class);
                if (docpid.equals(parentPid)) continue;
                Map<String, String> m = new HashMap<String, String>();
                m.put("pid", docpid);
                m.put("index", ItemResourceUtils.relsExtIndex(parentPid, docelm));
                memo.rememberIndexedDoc(docpid, docelm);
    
                ll.add(m);
            }
            offset = offset + rows;
        }
    
    
        Collections.sort(ll, new Comparator<Map<String, String>>() {
    
            @Override
            public int compare(Map<String, String> o1, Map<String, String> o2) {
                Integer i1 = new Integer(o1.get("index"));
                Integer i2 = new Integer(o2.get("index"));
                return i1.compareTo(i2);
            }
    
        });
        
        List<String> values = new ArrayList<String>();
        for (Map<String, String> m : ll) {
            values.add(m.get("pid"));
        }
        return values;
    }

    /**
     * Finds correct rels ext position
     * @param parentPid 
     * @param docelm
     * @return
     */
    public static String relsExtIndex(String parentPid, Element docelm) {
        List<Integer> docindexes =  SOLRUtils.narray(docelm, "rels_ext_index", Integer.class);
        
        if (docindexes.isEmpty()) return "0";
        List<String> parentPids = SOLRUtils.narray(docelm, "parent_pid", String.class);
        int index = 0;
        for (int i = 0, length = parentPids.size(); i < length; i++) {
            if (parentPids.get(i).endsWith(parentPid)) {
                index =  i;
                break;
            }
        }
        if (docindexes.size() > index) {
            return ""+docindexes.get(index);
        } else {
            LOGGER.warning("bad solr document for parent_pid:"+parentPid);
            return "0";
        }
    }

    public static  JSONArray decoratedJSONChildren(String pid, SolrAccess solrAccess, SolrMemoization solrMemoization,JSONDecoratorsAggregate decoratorsAggregate) throws IOException, JSONException {
        JSONArray jsonArray = new JSONArray();
        solrMemoization.clearMemo();
        List<String> fieldList = new ArrayList<String>();
        List<JSONDecorator> decs = decoratorsAggregate.getDecorators();
        for (JSONDecorator jsonDec : decs) {
            if (jsonDec instanceof SolrResultsAware) {
                SolrResultsAware saware = (SolrResultsAware) jsonDec;
                List<String> fList = saware.getFieldList();
                fieldList.addAll(fList);
            }
        }
    
        List<String> children = solrChildrenPids(pid, fieldList, solrAccess, solrMemoization);
        for (String p : children) {
            String repPid = p.replace("/", "");
            // vrchni ma odkaz sam na sebe
            if (repPid.equals(pid))
                continue;
            String uri = UriBuilder.fromResource(ItemResource.class)
                    .path("{pid}/children").build(pid).toString();
            JSONObject jsonObject = JSONUtils.pidAndModelDesc(repPid,
                    uri.toString(),solrMemoization,
                    decoratorsAggregate, uri);
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }


    
    private static boolean array(String value) {
        return ((value.startsWith("[")) && (value.endsWith("]")));
    }
    
    private static boolean isNumber(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /** 
     * Prevent JSONObject automatic conversion
     * @param value
     * @return
     */
    public static String preventAutomaticConversion(String value) {
        return value;
    }

}
