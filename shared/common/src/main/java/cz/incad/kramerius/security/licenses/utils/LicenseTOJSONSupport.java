package cz.incad.kramerius.security.licenses.utils;

import cz.incad.kramerius.security.licenses.License;
import cz.incad.kramerius.security.licenses.LicensesManager;
import cz.incad.kramerius.security.licenses.impl.LicenseImpl;
import cz.incad.kramerius.security.licenses.lock.ExclusiveLock;
import cz.incad.kramerius.security.licenses.lock.ExclusiveLock.ExclusiveLockType;

import org.json.JSONObject;

public class LicenseTOJSONSupport {

    private static final String ID_KEY = "id";
    private static final String GROUP_KEY = "group";
    private static final String NAME_KEY = "name";
    private static final String DESCRIPTION_KEY = "description";

    public static final String EXCLUSIVE_LOCK_TYPE = "type";
    
    private static final String PRIORITY_KEY = "priority";
    private static final String MAXREADERS_KEY = "maxreaders";
    private static final String REFRESHINTERVAL_KEY = "refreshinterval";
    private static final String MAXINTERVAL_KEY = "maxinterval";
    private static final String EXCLUSIVE_KEY = "exclusive";

    private LicenseTOJSONSupport() {}

    public static JSONObject licenseToJSON(License l) {
        JSONObject labelObject = new JSONObject();
        labelObject.put(ID_KEY, l.getId());
        labelObject.put(NAME_KEY, l.getName());
        labelObject.put(DESCRIPTION_KEY, l.getDescription());
        labelObject.put(PRIORITY_KEY, l.getPriority());
        labelObject.put(GROUP_KEY, l.getGroup());
        
        if (l.exclusiveLockPresent()) {
            labelObject.put(EXCLUSIVE_KEY, true);
            ExclusiveLock lock = l.getExclusiveLock();
            labelObject.put(MAXINTERVAL_KEY, lock.getMaxInterval());
            labelObject.put(REFRESHINTERVAL_KEY, lock.getRefreshInterval());
            labelObject.put(MAXREADERS_KEY, lock.getMaxReaders());
            
            labelObject.put(EXCLUSIVE_LOCK_TYPE, lock.getType().name());
        }
        return labelObject;
    }

    public static License licenseFromJSON(JSONObject jsonObject) {
        int id = jsonObject.optInt(ID_KEY);
        return licenseFromJSON(id, jsonObject);
    }

    public static License licenseFromJSON(int id, JSONObject jsonObject) {
        String name = jsonObject.getString(NAME_KEY);
        String description = jsonObject.optString(DESCRIPTION_KEY);
        License lic = null;
        if (jsonObject.has(PRIORITY_KEY)) {
            lic = new LicenseImpl(id, name, description, LicensesManager.LOCAL_GROUP_NAME, jsonObject.optInt(PRIORITY_KEY));
        } else {
            lic = new LicenseImpl(id, name, description, LicensesManager.LOCAL_GROUP_NAME);
        }
        boolean exclusiveAccess = jsonObject.optBoolean(EXCLUSIVE_KEY);
        if (exclusiveAccess) {
            int maxTime = jsonObject.optInt(MAXINTERVAL_KEY);
            int refreshTime = jsonObject.optInt(REFRESHINTERVAL_KEY);
            int maxReaders = jsonObject.optInt(MAXREADERS_KEY);
            String type = jsonObject.optString(EXCLUSIVE_LOCK_TYPE);
            
            lic.initExclusiveLock(refreshTime, maxTime, maxReaders, ExclusiveLockType.findByType(type));
        }
        return lic;
    }

}
