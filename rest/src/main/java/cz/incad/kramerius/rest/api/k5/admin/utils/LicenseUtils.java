package cz.incad.kramerius.rest.api.k5.admin.utils;

import cz.incad.kramerius.security.licenses.License;
import cz.incad.kramerius.security.licenses.LicensesManager;
import cz.incad.kramerius.security.licenses.impl.LicenseImpl;
import org.json.JSONObject;

public class LicenseUtils {

    private LicenseUtils() {}

    public static JSONObject licenseToJSON(License l) {
        JSONObject labelObject = new JSONObject();
        labelObject.put("id", l.getId());
        labelObject.put("name", l.getName());
        labelObject.put("description", l.getDescription());
        labelObject.put("priority", l.getPriority());
        labelObject.put("group", l.getGroup());
        return labelObject;
    }

    public static License licenseFromJSON(JSONObject jsonObject) {
        int id = jsonObject.optInt("id");
        return licenseFromJSON(id, jsonObject);
    }

    public static License licenseFromJSON(int id, JSONObject jsonObject) {
        //int id = jsonObject.optInt("id");
        String name = jsonObject.getString("name");
        String description = jsonObject.optString("description");
        if (jsonObject.has("priority")) {
            return new LicenseImpl(id, name, description, LicensesManager.LOCAL_GROUP_NAME, jsonObject.optInt("priority"));
        } else {
            return new LicenseImpl(id, name, description, LicensesManager.LOCAL_GROUP_NAME);
        }
    }

}
