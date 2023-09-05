package cz.inovatika.folders.jersey;

import org.json.JSONArray;
import org.json.JSONObject;

public class Utils {

    public static String buildErrorJsonStr(String message) {
        JSONObject errorJson = new JSONObject();
        errorJson.put("error", message);
        return errorJson.toString();
    }

    public static boolean isJsonArrayContainingStrings(Object object) {
        if (!(object instanceof JSONArray)) {
            return false;
        }
        JSONArray array = (JSONArray) object;
        for (int i = 0; i < array.length(); i++) {
            if (!(array.get(i) instanceof String)) {
                return false;
            }
        }
        return true;
    }

}
