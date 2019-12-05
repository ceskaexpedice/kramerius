package cz.incad.kramerius.auth.thirdparty.shibb.external;

import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.incad.kramerius.auth.thirdparty.shibb.impl.ShibAuthenticatedUsers;
import cz.incad.kramerius.auth.thirdparty.shibb.utils.ShibbolethUserWrapper;
import cz.incad.kramerius.auth.thirdparty.utils.RemoteUsersUtils;
import cz.incad.kramerius.auth.utils.GeneratePasswordUtils;

public class ExternalAuthenticatedUsersImpl extends ShibAuthenticatedUsers {

    public static Logger LOGGER = Logger.getLogger(ExternalAuthenticatedUsersImpl.class.getName());

    @Override
    public String updateExistingUser(String userName,ShibbolethUserWrapper wrapper) throws Exception {
        String password = GeneratePasswordUtils.generatePswd();
        JSONArray users = RemoteUsersUtils.getUser(userName);
        JSONObject jsonObject = users.getJSONObject(0);
        int id = jsonObject.getInt("id");
        RemoteUsersUtils.newPasswordUser(""+id, password);
        return password;
    }

    public boolean checkIfUserExists(String userName) throws Exception {
        JSONArray users = RemoteUsersUtils.getUser(userName);
        return users.length() > 0 ;
        
    }

    @Override
    protected String createNewUser(String user, ShibbolethUserWrapper w) throws Exception {
        String password = GeneratePasswordUtils.generatePswd();
        JSONObject json = w.toJSON(password);
        RemoteUsersUtils.createUser(json);
        return password;
    }


   
}
