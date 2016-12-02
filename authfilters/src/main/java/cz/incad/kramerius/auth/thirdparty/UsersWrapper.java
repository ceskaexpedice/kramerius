package cz.incad.kramerius.auth.thirdparty;

import org.json.JSONObject;

/**
 * Represents third party authenticated user
 * @author pavels
 */
public interface UsersWrapper {

    /**
     * Return calculated name for kramerius
     * @return
     */
    public String getCalculatedName();

    /**
     * Returns specific user property
     * @param key
     * @return
     */
    public String getProperty(String key);

    /**
     * Returns json representation
     * @param pass
     * @return
     */
    public JSONObject toJSON(String pass);
}

