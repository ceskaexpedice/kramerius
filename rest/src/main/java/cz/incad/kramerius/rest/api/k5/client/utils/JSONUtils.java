package cz.incad.kramerius.rest.api.k5.client.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;

import cz.incad.kramerius.rest.api.k5.client.JSONDecorator;
import cz.incad.kramerius.rest.api.k5.client.JSONDecoratorsAggregate;
import cz.incad.kramerius.rest.api.k5.client.SolrMemoization;

public class JSONUtils {

    public static final Logger LOGGER = Logger.getLogger(JSONUtils.class
            .getName());

    public static JSONObject link(JSONObject obj, String key, String link) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("href", link);
        obj.put(key, json);
        return obj;
    }

    public static JSONObject pidAndModelDesc(String pid, JSONObject jsonObject,
            String callContext,SolrMemoization soMemo,
            JSONDecoratorsAggregate decoratorsAggregate, String baseLink)
            throws IOException, JSONException {

        Map<String, Object> m = new HashMap<String, Object>();
        jsonObject.put("pid", pid);
        if (PIDSupport.isComposedPID(pid)) {
            // page model
            jsonObject.put("model", "page");
        } else {
            Element indexDoc = soMemo.getRememberedIndexedDoc(pid);
            if (indexDoc ==  null) {
                indexDoc = soMemo.askForIndexDocument(pid);
            }
            if (indexDoc != null) {
                String fedoraModel = SOLRUtils.value(indexDoc, "fedora.model", String.class);
                jsonObject.put("model",fedoraModel);
            }
        }

        // apply decorator
        if (callContext != null && decoratorsAggregate != null) {
            List<JSONDecorator> ldecs = decoratorsAggregate.getDecorators();
            for (JSONDecorator d : ldecs) {
                d.before(m);
            }
            for (JSONDecorator d : ldecs) {
                if (d.apply(jsonObject, callContext)) {
                    d.decorate(jsonObject, m);
                }
            }
            for (JSONDecorator d : ldecs) {
                d.after();
            }
        }
        return jsonObject;
    }

    public static JSONObject pidAndModelDesc(String pid,String callContext,SolrMemoization solrMemoization,
            JSONDecoratorsAggregate decoratorsAggregate, String baseUrl)
            throws IOException, JSONException {
        return pidAndModelDesc(pid, new JSONObject(),
                callContext, solrMemoization, decoratorsAggregate, baseUrl);
    }


}
