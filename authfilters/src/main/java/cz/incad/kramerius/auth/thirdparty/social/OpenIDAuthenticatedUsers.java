package cz.incad.kramerius.auth.thirdparty.social;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.brickred.socialauth.Profile;
import org.json.JSONArray;
import org.json.JSONObject;

import cz.incad.kramerius.auth.thirdparty.impl.AbstractAuthenticatedUsers;
import cz.incad.kramerius.auth.thirdparty.social.utils.OpenIDFlag;
import cz.incad.kramerius.auth.thirdparty.social.utils.OpenIdUserWrapper;
import cz.incad.kramerius.auth.thirdparty.utils.RemoteUsersUtils;
import cz.incad.kramerius.auth.utils.GeneratePasswordUtils;

public class OpenIDAuthenticatedUsers extends AbstractAuthenticatedUsers<OpenIdUserWrapper> {

    public static final Logger LOGGER = Logger.getLogger(OpenIDAuthenticatedUsers.class.getName());

    public static String OPEN_ID_PREFIX = "_openid_";

    

    @Override
    public String calculateUserName(HttpServletRequest request) {
        try {
            
            OpenIDFlag flag = OpenIDFlag.flagFromRequest(request);
            if (flag.equals(OpenIDFlag.LOGIN_INITIALIZED)) {
                flag = flag.next(request);
            }
            Profile profile = flag.profile(request);
            if (profile != null) {
                return OPEN_ID_PREFIX+profile.getProviderId()+ "_" + profile.getValidatedId();
            } else return null;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public String updateExistingUser(String userName,OpenIdUserWrapper wrapper) throws Exception {
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
    protected String createNewUser(String user, OpenIdUserWrapper w) throws Exception {
        String password = GeneratePasswordUtils.generatePswd();
        JSONObject json = w.toJSON(password);
        RemoteUsersUtils.createUser( json);
        return password;
    }

    @Override
    protected OpenIdUserWrapper createUserWrapper(HttpServletRequest req, String userName) throws Exception {
        Profile profile = OpenIDFlag.flagFromRequest(req).profile(req);
        return new OpenIdUserWrapper(userName,profile);
    }

    @Override
    public String storeUserPropertiesToSession(HttpServletRequest req, String userName) throws Exception {
        String pass = super.storeUserPropertiesToSession(req, userName);
        OpenIDFlag.STORED.next(req);
        return pass;
    }

}
