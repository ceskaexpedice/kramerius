package cz.incad.kramerius.auth.thirdparty;

import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import org.json.JSONObject;

import java.util.Set;

/**
 * Represents third party authenticated user
 * @author pavels
 */
public interface ThirdPartyUser {

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

    // create user instance from wrapper - not store to db
    public User toUser(UserManager userManager);

}

