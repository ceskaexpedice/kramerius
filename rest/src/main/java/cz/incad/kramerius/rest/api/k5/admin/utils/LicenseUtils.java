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
        int id = jsonObject.getInt("id");
        String name = jsonObject.getString("name");
        String description = jsonObject.getString("description");
        int priority = jsonObject.getInt("priority");
        Label label = new LabelImpl(id, name, description, LabelsManager.LOCAL_GROUP_NAME, priority);
        return label;
    }
}
