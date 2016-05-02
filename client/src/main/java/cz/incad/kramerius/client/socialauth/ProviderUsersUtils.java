package cz.incad.kramerius.client.socialauth;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;

import org.apache.commons.configuration.ConfigurationException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.auth.thirdparty.UsersWrapper;
import cz.incad.kramerius.client.kapi.auth.AdminUser;
import cz.incad.kramerius.client.kapi.auth.CallUserController;
import cz.incad.kramerius.security.utils.UserUtils;
import cz.incad.kramerius.utils.BasicAuthenticationFilter;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class ProviderUsersUtils {

    public static Logger LOGGER = Logger.getLogger(ProviderUsersUtils.class.getName());
    
    
    static JSONArray getUser(HttpServletRequest req, UsersWrapper w)
            throws ConfigurationException, JSONException {
        Client c = Client.create();
        String url = KConfiguration.getInstance().getConfiguration()
                .getString("api.point")
                + "/admin/users?lname="
                + w.getCalculatedName();
    
        CallUserController callUserController = (cz.incad.kramerius.client.kapi.auth.CallUserController) req
                .getSession(true).getAttribute(CallUserController.KEY);
        AdminUser adminCaller = callUserController.getAdminCaller();
    
        WebResource r = c.resource(url);
        r.addFilter(new BasicAuthenticationFilter(adminCaller.getUserName(),
                adminCaller.getPassword()));
        String t = r.accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON).get(String.class);
        JSONArray jsonArr = new JSONArray(t);
        return jsonArr;
    }


    // create user
    public static String createUser(HttpServletRequest req, UsersWrapper w,
            String password) throws JSONException, ConfigurationException {
        String url = KConfiguration.getInstance().getConfiguration()
                .getString("api.point") + "/admin/users";
    
        Client c = Client.create();
        WebResource r = c.resource(url);
    
        CallUserController callUserController = (cz.incad.kramerius.client.kapi.auth.CallUserController) req
                .getSession(true).getAttribute(CallUserController.KEY);
        AdminUser adminCaller = callUserController.getAdminCaller();
    
        JSONObject object = new JSONObject();
        object.put("lname", w.getCalculatedName());
        object.put("firstname", w.getProperty(UserUtils.FIRST_NAME_KEY));
        object.put("surname", w.getProperty(UserUtils.LAST_NAME_KEY));
        object.put("password", password);
    
        r.addFilter(new BasicAuthenticationFilter(adminCaller.getUserName(),
                adminCaller.getPassword()));
        String t = r.accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .entity(object.toString(), MediaType.APPLICATION_JSON)
                .post(String.class);
        return t;
    }

    public static void newPasswordUser(HttpServletRequest req, String userId,
            String pswd) {
        try {
            Client c = Client.create();
            String url = KConfiguration.getInstance().getConfiguration()
                    .getString("api.point")
                    + "/admin/users/"
                    + userId
                    + "/password";
    
            CallUserController callUserController = (cz.incad.kramerius.client.kapi.auth.CallUserController) req
                    .getSession(true).getAttribute(CallUserController.KEY);
            AdminUser adminCaller = callUserController.getAdminCaller();
    
            WebResource r = c.resource(url);
            r.addFilter(new BasicAuthenticationFilter(
                    adminCaller.getUserName(), adminCaller.getPassword()));
            JSONObject object = new JSONObject();
            object.put("password", pswd);
    
            String t = r.accept(MediaType.APPLICATION_JSON)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(object.toString(), MediaType.APPLICATION_JSON)
                    .put(String.class);
        } catch (UniformInterfaceException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (ClientHandlerException  e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public static void deleteUser(HttpServletRequest req, String userId) {
        try {
            String url = KConfiguration.getInstance().getConfiguration()
                    .getString("api.point") + "/admin/users/" + userId;
    
            Client c = Client.create();
    
            CallUserController callUserController = (cz.incad.kramerius.client.kapi.auth.CallUserController) req
                    .getSession(true).getAttribute(CallUserController.KEY);
            AdminUser adminCaller = callUserController.getAdminCaller();
    
            WebResource r = c.resource(url);
    
            r.addFilter(new BasicAuthenticationFilter(
                    adminCaller.getUserName(), adminCaller.getPassword()));
    
            String t = r.accept(MediaType.APPLICATION_JSON)
                    .type(MediaType.APPLICATION_JSON).delete(String.class);
    
        } catch (ClientHandlerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (UniformInterfaceException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
