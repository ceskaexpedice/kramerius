package cz.incad.kramerius.auth.thirdparty.impl;

import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.impl.UserImpl;
import org.json.JSONObject;

import cz.incad.kramerius.auth.thirdparty.ThirdPartyUser;
import cz.incad.kramerius.security.utils.UserUtils;

public abstract class AbstractThirdPartyUser implements ThirdPartyUser {


    @Override
    public User toUser(UserManager userManager) {
        String calculatedName = this.getCalculatedName();
        String firsname = this.getProperty(UserUtils.FIRST_NAME_KEY);
        String lastname = this.getProperty(UserUtils.LAST_NAME_KEY);
        String email = this.getProperty(UserUtils.EMAIL_KEY);
        UserImpl user = new UserImpl(1, firsname, lastname, calculatedName, -1);
        if (email != null) {
            user.setEmail(email);
        }

        return user;
    }

    public JSONObject toJSON(String pass) {
        JSONObject object = new JSONObject();
        object.put("lname", getCalculatedName());
        object.put("firstname", getProperty(UserUtils.FIRST_NAME_KEY));
        object.put("surname", getProperty(UserUtils.LAST_NAME_KEY));
        object.put("password", pass);
        return object;
    }
}
