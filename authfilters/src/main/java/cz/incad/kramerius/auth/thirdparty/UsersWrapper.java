package cz.incad.kramerius.auth.thirdparty;

import org.json.JSONObject;

import java.util.Set;

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

    public Set<String> getPropertyKeys();



    /**
     * Returns json representation
     * @param pass
     * @return
     */
    public JSONObject toJSON(String pass);
}

