package cz.incad.kramerius.rest.apiNew.client.v70.redirection.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.ws.rs.core.Response;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestManager;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.ProxyHandlerException;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.impl.RoleImpl;
import cz.incad.kramerius.security.impl.UserImpl;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class V5ForwardUserHandler extends ProxyUserHandler {

    public V5ForwardUserHandler(ReharvestManager reharvestManager, Instances instances, User user, Client client, SolrAccess solrAccess, String source,
            String remoteAddr) {
        super(reharvestManager,instances, user, client, solrAccess, source, remoteAddr);
    }

    protected String forwardUrl() {
        String baseurl = KConfiguration.getInstance().getConfiguration()
                .getString("cdk.collections.sources." + this.source + ".forwardurl");
        return baseurl;
    }

    @Override
    public Pair<User, List<String>> user() throws ProxyHandlerException {
        try {
            String baseurl = forwardUrl();
            String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/cdk/forward/user";
            ClientResponse fResponse = super.forwardedResponse(url);

            if (fResponse.getStatus() != 200) {
                String errorMessage = "Chyba při volání API: Status code " + fResponse.getStatus() + ", URL: " + url;
                LOGGER.log(Level.SEVERE, errorMessage);
                throw new ProxyHandlerException(errorMessage);
            }

            String entity = fResponse.getEntity(String.class);
            JSONObject jObject = new JSONObject(entity);
            return userFromJSON(jObject);
        } catch (ClientHandlerException | UniformInterfaceException | JSONException | ProxyHandlerException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return null;
        }
    }

    public static Pair<User, List<String>> userFromJSON(JSONObject json) throws JSONException {
        Map<String, String> session = new HashMap<>();
        String loginName = json.optString("lname");
        String firstName = json.optString("firstname");
        String surName = json.optString("surname");
        ;

        List<String> licenses = new ArrayList<>();
        List<Role> roles = new ArrayList<>();
        JSONArray rolesJSONArray = json.optJSONArray("roles");

        if (rolesJSONArray != null) {
            for (int i = 0; i < rolesJSONArray.length(); i++) {
                String optString = rolesJSONArray.getJSONObject(i).optString("name");
                roles.add(new RoleImpl(optString));
            }
        }

        if (json.has("session")) {
            JSONObject sessionAttrs = json.getJSONObject("session");
            sessionAttrs.keySet().forEach(key -> {
                Object object = sessionAttrs.get((String) key);
                session.put(key.toString(), object.toString());
            });
        }

        if (json.has("labels")) {
            JSONArray licensesArray = json.getJSONArray("labels");
            for (int i = 0; i < licensesArray.length(); i++) {
                licenses.add(licensesArray.getString(i));
            }
        }

        UserImpl userImpl = new UserImpl(1, firstName != null ? firstName : "", surName != null ? surName : "",
                loginName, 0);
        userImpl.setGroups(roles.toArray(new Role[roles.size()]));
        session.entrySet().forEach(entry -> {
            userImpl.addSessionAttribute(entry.getKey(), entry.getValue());
        });
        return Pair.of(userImpl, licenses);
    }
}
