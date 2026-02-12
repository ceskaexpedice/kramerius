package cz.incad.kramerius.security.licenses.utils;

import cz.incad.kramerius.security.licenses.License;
import cz.incad.kramerius.security.licenses.LicensesManager;
import cz.incad.kramerius.security.licenses.RuntimeLicenseType;
import cz.incad.kramerius.security.licenses.impl.LicenseImpl;
import cz.incad.kramerius.security.licenses.lock.ExclusiveReadersLock;
import cz.incad.kramerius.security.licenses.lock.ExclusiveReadersLock.ExclusiveLockType;

import org.json.JSONObject;

/**
 * Utility class for converting {@link License} objects to and from JSON representation.
 * <p>
 * This class supports serialization of license attributes including exclusive lock settings and runtime license type.
 * It is primarily used to facilitate data exchange (e.g., over REST APIs or in configuration storage).
 *
 */
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

    private static final String RUNTIME = "runtime";
    private static final String RUNTIME_TYPE="runtime_type";

    private LicenseTOJSONSupport() {}

    /**
     * Serializes a {@link License} object into its JSON representation.
     *
     * @param l the license to serialize
     * @return a JSON object containing all relevant license fields
     */
    public static JSONObject licenseToJSON(License l) {
        JSONObject labelObject = new JSONObject();
        labelObject.put(ID_KEY, l.getId());
        labelObject.put(NAME_KEY, l.getName());
        labelObject.put(DESCRIPTION_KEY, l.getDescription());
        labelObject.put(PRIORITY_KEY, l.getPriority());
        labelObject.put(GROUP_KEY, l.getGroup());
        
        if (l.exclusiveLockPresent()) {
            labelObject.put(EXCLUSIVE_KEY, true);
            ExclusiveReadersLock lock = l.getExclusiveLock();
            labelObject.put(MAXINTERVAL_KEY, lock.getMaxInterval());
            labelObject.put(REFRESHINTERVAL_KEY, lock.getRefreshInterval());
            labelObject.put(MAXREADERS_KEY, lock.getMaxReaders());
            
            labelObject.put(EXCLUSIVE_LOCK_TYPE, lock.getType().name());
        }

        if (l.isRuntimeLicense()) {
            labelObject.put(RUNTIME, true);
            labelObject.put(RUNTIME_TYPE, l.getRuntimeLicenseType().name());
        }
        return labelObject;
    }

    /**
     * Deserializes a {@link License} object from a JSON object.
     * This variant expects the license ID to be part of the JSON.
     *
     * @param jsonObject the JSON object containing license data
     * @return a reconstructed {@link License} instance
     */
    public static License licenseFromJSON(JSONObject jsonObject) {
        int id = jsonObject.optInt(ID_KEY);
        return licenseFromJSON(id, jsonObject);
    }

    /**
     * Deserializes a {@link License} object from a JSON object with an explicit ID.
     * This is useful when the license ID is managed externally or not stored in the JSON.
     *
     * @param id the unique ID of the license
     * @param jsonObject the JSON object containing license data
     * @return a reconstructed {@link License} instance
     */
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

        boolean runtimeLicense = jsonObject.optBoolean(RUNTIME);
        String runtimeLicenseType = jsonObject.optString(RUNTIME_TYPE);
        if (runtimeLicense) {
            lic.initRuntime(RuntimeLicenseType.valueOf(runtimeLicenseType));
        }

        return lic;
    }

}
