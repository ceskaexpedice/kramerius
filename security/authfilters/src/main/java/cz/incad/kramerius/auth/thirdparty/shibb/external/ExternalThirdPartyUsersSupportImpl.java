package cz.incad.kramerius.auth.thirdparty.shibb.external;

import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.incad.kramerius.auth.thirdparty.impl.AbstractThirdPartyUser;
import cz.incad.kramerius.auth.thirdparty.shibb.impl.ShibThirdPartyUsersSupport;
import cz.incad.kramerius.auth.thirdparty.shibb.utils.Shibboleth3rdUser;
import cz.incad.kramerius.auth.thirdparty.utils.RemoteUsersUtils;
import cz.incad.kramerius.auth.utils.GeneratePasswordUtils;

public class ExternalThirdPartyUsersSupportImpl extends ShibThirdPartyUsersSupport {

    public static Logger LOGGER = Logger.getLogger(ExternalThirdPartyUsersSupportImpl.class.getName());

    @Override
    public String updateExistingUser(String userName, Shibboleth3rdUser wrapper) throws Exception {
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
    protected String createNewUser(String user, Shibboleth3rdUser w) throws Exception {
        String password = GeneratePasswordUtils.generatePswd();
        JSONObject json = w.toJSON(password);
        RemoteUsersUtils.createUser(json);
        return password;
    }


   
}
