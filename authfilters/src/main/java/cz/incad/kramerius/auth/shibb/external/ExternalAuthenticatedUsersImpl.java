package cz.incad.kramerius.auth.shibb.external;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

import org.apache.commons.configuration.ConfigurationException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.auth.shibb.impl.ShibAuthenticatedUsers;
import cz.incad.kramerius.auth.shibb.utils.ShibbolethUserWrapper;
import cz.incad.kramerius.auth.utils.GeneratePasswordUtils;
import cz.incad.kramerius.security.utils.UserUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.jersey.BasicAuthenticationFilter;

public class ExternalAuthenticatedUsersImpl extends ShibAuthenticatedUsers {

    public static Logger LOGGER = Logger.getLogger(ExternalAuthenticatedUsersImpl.class.getName());

    @Override
    public String updateUser(String userName, ShibbolethUserWrapper wrapper) throws Exception {
        String password = GeneratePasswordUtils.generatePswd();
        JSONArray users = getUser(userName);
        JSONObject jsonObject = users.getJSONObject(0);
        int id = jsonObject.getInt("id");
        newPasswordUser(""+id, password);
        return password;
    }

    public boolean userExists(String userName) throws Exception {
        JSONArray users = getUser(userName);
        return users.length() > 0 ;
        
    }

    void newPasswordUser( String userId,
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
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (ClientHandlerException  e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public String createUser(String user, ShibbolethUserWrapper w) throws Exception {
        String url = KConfiguration.getInstance().getConfiguration()
                .getString("api.point") + "/admin/users";
        Client c = Client.create();
        WebResource r = c.resource(url);

        String password = GeneratePasswordUtils.generatePswd();

        JSONObject object = new JSONObject();
        object.put("lname", w.getCalculatedName());
        object.put("firstname", w.getProperty(UserUtils.FIRST_NAME_KEY));
        object.put("surname", w.getProperty(UserUtils.LAST_NAME_KEY));
        object.put("password", password);
    
        r.addFilter(new BasicAuthenticationFilter(adminUser(),
                adminPass()));
        String t = r.accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .entity(object.toString(), MediaType.APPLICATION_JSON)
                .post(String.class);
        return t;
    }


    
    JSONArray getUser(String userName)
            throws ConfigurationException, JSONException {
        Client c = Client.create();
        String url = KConfiguration.getInstance().getConfiguration()
                .getString("api.point")
                + "/admin/users?lname="
                + userName;
    
        WebResource r = c.resource(url);
        r.addFilter(new BasicAuthenticationFilter(adminUser(),
                adminPass()));
        String t = r.accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON).get(String.class);
        JSONArray jsonArr = new JSONArray(t);
        return jsonArr;
    }

    private String adminPass() {
        String adminPass = KConfiguration.getInstance().getConfiguration()
                .getString("k4.admin.pswd") ;
        return adminPass;
    }


    private String adminUser() {
        String adminUser = KConfiguration.getInstance().getConfiguration()
                .getString("k4.admin.user") ;
        return adminUser;
    }

   
}
