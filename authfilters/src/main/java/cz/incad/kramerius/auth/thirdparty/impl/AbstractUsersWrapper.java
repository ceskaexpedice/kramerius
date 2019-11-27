package cz.incad.kramerius.auth.thirdparty.impl;

import org.json.JSONObject;

import cz.incad.kramerius.auth.thirdparty.UsersWrapper;
import cz.incad.kramerius.security.utils.UserUtils;

public abstract class AbstractUsersWrapper implements UsersWrapper {

    public JSONObject toJSON(String pass) {
        JSONObject object = new JSONObject();
        object.put("lname", getCalculatedName());
        object.put("firstname", getProperty(UserUtils.FIRST_NAME_KEY));
        object.put("surname", getProperty(UserUtils.LAST_NAME_KEY));
        object.put("password", pass);
        return object;
    }
}
