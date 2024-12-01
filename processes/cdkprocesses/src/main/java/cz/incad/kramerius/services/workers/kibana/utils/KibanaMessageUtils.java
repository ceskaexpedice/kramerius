package cz.incad.kramerius.services.workers.kibana.utils;

import cz.incad.kramerius.services.iterators.IterationItem;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Collections;

public class KibanaMessageUtils {

    private KibanaMessageUtils() {}

    public static JSONObject existsMessage(String col, String key, boolean aBoolean) {
        JSONObject jObject = basicPIDMessage(key);
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(col);
        jObject.put("collections", jsonArray);
        jObject.put("exists",aBoolean);
        jObject.put("type","exists");
        return jObject;
    }


    public static JSONObject basicPIDMessage(String pid) {
        JSONObject jObject = new JSONObject();
        jObject.put("pid",pid);
        return jObject;
    }

    public static JSONObject basicPIDMessage(IterationItem item) {
        JSONObject jObject = new JSONObject();
        jObject.put("pid",item.getPid());
        jObject.put("source",item.getSource());
        return jObject;
    }


    public static JSONObject expand(JSONObject confObject, JSONObject logMessage) {
        confObject.keySet().stream().forEach(key-> {
            Object val = confObject.get(key.toString());
            if (val instanceof Boolean) {
                logMessage.put(key.toString(), (Boolean) val);
            } else if (val instanceof Long) {
                logMessage.put(key.toString(), (Long) val);
            } else if (val instanceof Integer) {
                logMessage.put(key.toString(), (Integer) val);
            } else if (val instanceof String) {
                logMessage.put(key.toString(), (String) val);
            } else if (val instanceof Collections) {
                logMessage.put(key.toString(), (Collection) val);
            } else if (val instanceof Double) {
                logMessage.put(key.toString(), (Double) val);
            } else throw new IllegalStateException(String.format("Cannot put value %s %s", key.toString(), val.toString()));
        });
        return logMessage;
    }
}
