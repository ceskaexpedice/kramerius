package cz.incad.kramerius.rest.api.k5.admin.utils;

import cz.incad.kramerius.security.labels.Label;
import cz.incad.kramerius.security.labels.LabelsManager;
import cz.incad.kramerius.security.labels.impl.LabelImpl;
import org.json.JSONObject;

public class LicenseUtils {

    private LicenseUtils() {}

    public static JSONObject licenseToJSON(Label l) {
        JSONObject labelObject = new JSONObject();
        labelObject.put("id", l.getId());
        labelObject.put("name", l.getName());
        labelObject.put("description", l.getDescription());
        labelObject.put("priority", l.getPriority());
        return labelObject;
    }

    public static Label licenseFromJSON(JSONObject jsonObject) {
        int id = jsonObject.optInt("id");
        return licenseFromJSON(id, jsonObject);
    }

    public static Label licenseFromJSON(int id, JSONObject jsonObject) {
        //int id = jsonObject.optInt("id");
        String name = jsonObject.getString("name");
        String description = jsonObject.optString("description");
        if (jsonObject.has("priority")) {
            return new LabelImpl(id, name, description, LabelsManager.LOCAL_GROUP_NAME, jsonObject.optInt("priority"));
        } else {
            return new LabelImpl(id, name, description, LabelsManager.LOCAL_GROUP_NAME);
        }
    }

}
