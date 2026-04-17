package cz.incad.kramerius.auth.thirdparty.utils;

import java.util.logging.Level;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.incad.kramerius.auth.thirdparty.shibb.external.ExternalThirdPartyUsersSupportImpl;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class RemoteUsersUtils {

    public static JSONArray getUser(String userName) throws JSONException {

        Client client = ClientBuilder.newClient();
        try {

            String url = KConfiguration.getInstance()
                    .getConfiguration()
                    .getString("api.point")
                    + "/admin/users?lname=" + userName;

            HttpAuthenticationFeature authFeature =
                    HttpAuthenticationFeature.basic(adminUser(), adminPass());

            client.register(authFeature);

            WebTarget target = client.target(url);

            String response = target
                    .request(MediaType.APPLICATION_JSON)
                    .get(String.class);

            return new JSONArray(response);

        } finally {
            client.close();
        }
    }

    public static String adminPass() {
        return KConfiguration.getInstance()
                .getConfiguration()
                .getString("k4.admin.pswd");
    }

    public static String adminUser() {
        return KConfiguration.getInstance()
                .getConfiguration()
                .getString("k4.admin.user");
    }

    public static void newPasswordUser(String userId, String pswd) {

        Client client = ClientBuilder.newClient();

        try {

            String url = KConfiguration.getInstance()
                    .getConfiguration()
                    .getString("api.point")
                    + "/admin/users/"
                    + userId
                    + "/password";

            HttpAuthenticationFeature authFeature =
                    HttpAuthenticationFeature.basic(adminUser(), adminPass());

            client.register(authFeature);

            WebTarget target = client.target(url);

            JSONObject object = new JSONObject();
            object.put("password", pswd);

            target.request(MediaType.APPLICATION_JSON)
                    .put(Entity.entity(
                            object.toString(),
                            MediaType.APPLICATION_JSON));

        } catch (Exception e) {
            ExternalThirdPartyUsersSupportImpl.LOGGER
                    .log(Level.SEVERE, e.getMessage(), e);
        } finally {
            client.close();
        }
    }

    public static String createUser(JSONObject jsonObject) {

        Client client = ClientBuilder.newClient();

        try {

            String url = KConfiguration.getInstance()
                    .getConfiguration()
                    .getString("api.point")
                    + "/admin/users";

            HttpAuthenticationFeature authFeature =
                    HttpAuthenticationFeature.basic(adminUser(), adminPass());

            client.register(authFeature);

            WebTarget target = client.target(url);

            return target.request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(
                                    jsonObject.toString(),
                                    MediaType.APPLICATION_JSON),
                            String.class);

        } finally {
            client.close();
        }
    }

    public static String deleteUser(String usr) {

        Client client = ClientBuilder.newClient();

        try {

            String url = KConfiguration.getInstance()
                    .getConfiguration()
                    .getString("api.point")
                    + "/admin/users/" + usr;

            HttpAuthenticationFeature authFeature =
                    HttpAuthenticationFeature.basic(adminUser(), adminPass());

            client.register(authFeature);

            WebTarget target = client.target(url);

            return target.request(MediaType.APPLICATION_JSON)
                    .delete(String.class);

        } finally {
            client.close();
        }
    }
}