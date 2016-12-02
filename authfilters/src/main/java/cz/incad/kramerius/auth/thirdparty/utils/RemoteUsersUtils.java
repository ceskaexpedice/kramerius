package cz.incad.kramerius.auth.thirdparty.utils;

import java.util.logging.Level;

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
import cz.incad.kramerius.auth.thirdparty.shibb.external.ExternalAuthenticatedUsersImpl;
import cz.incad.kramerius.security.utils.UserUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.jersey.BasicAuthenticationFilter;

public class RemoteUsersUtils {

    public static JSONArray getUser(String userName)
            throws ConfigurationException, JSONException {
        Client c = Client.create();
        String url = KConfiguration.getInstance().getConfiguration()
                .getString("api.point")
                + "/admin/users?lname="
                + userName;
    
        WebResource r = c.resource(url);
        r.addFilter(new BasicAuthenticationFilter(RemoteUsersUtils.adminUser(),
                RemoteUsersUtils.adminPass()));
        String t = r.accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON).get(String.class);
        JSONArray jsonArr = new JSONArray(t);
        return jsonArr;
    }

    public static String adminPass() {
        String adminPass = KConfiguration.getInstance().getConfiguration()
                .getString("k4.admin.pswd") ;
        return adminPass;
    }

    public static String adminUser() {
        String adminUser = KConfiguration.getInstance().getConfiguration()
                .getString("k4.admin.user") ;
        return adminUser;
    }

    public static void newPasswordUser( String userId,
            String pswd) {
        try {
            Client c = Client.create();
            String url = KConfiguration.getInstance().getConfiguration()
                    .getString("api.point")
                    + "/admin/users/"
                    + userId
                    + "/password";
    
            WebResource r = c.resource(url);
            r.addFilter(new BasicAuthenticationFilter(
                    adminUser(), adminPass()));
            JSONObject object = new JSONObject();
            object.put("password", pswd);
    
            String t = r.accept(MediaType.APPLICATION_JSON)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(object.toString(), MediaType.APPLICATION_JSON)
                    .put(String.class);
        } catch (UniformInterfaceException e) {
            ExternalAuthenticatedUsersImpl.LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (ClientHandlerException  e) {
            ExternalAuthenticatedUsersImpl.LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (JSONException e) {
            ExternalAuthenticatedUsersImpl.LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public static String createUser(JSONObject jsonObject) throws Exception {
        String url = KConfiguration.getInstance().getConfiguration()
                .getString("api.point") + "/admin/users";
        Client c = Client.create();
        
        WebResource r = c.resource(url);
    
        r.addFilter(new BasicAuthenticationFilter(adminUser(),
                adminPass()));
        String t = r.accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .entity(jsonObject.toString(), MediaType.APPLICATION_JSON)
                .post(String.class);
        return t;
    }

    public static String deleteUser(String usr) throws Exception {
        String url = KConfiguration.getInstance().getConfiguration()
                .getString("api.point") + "/admin/users/"+usr;
        Client c = Client.create();
        
        WebResource r = c.resource(url);
    
        r.addFilter(new BasicAuthenticationFilter(adminUser(),
                adminPass()));
        String t = r.accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .delete(String.class);
        return t;
    }

    public static void main(String[] args) throws JSONException, Exception {
        String str = "_third_party_googleplus_114387057939312155006";
        JSONArray user = getUser(str);
        System.out.println(user);
        System.out.println(user.length());
        
        
        //        String str = "{\"lname\":\"_tthird_party_googleplus_114387057939312155006\",\"firstname\":\"Pavel\",\"password\":\".dlPGvYJT4iW\",\"surname\":\"Stastny\"}";
//
//        String createUser = createUser(new JSONObject(str));
//        System.out.println(createUser);
        
    }
    
    
}
