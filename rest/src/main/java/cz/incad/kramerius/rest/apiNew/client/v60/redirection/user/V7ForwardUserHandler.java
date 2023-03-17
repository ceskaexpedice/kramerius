package cz.incad.kramerius.rest.apiNew.client.v60.redirection.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v60.redirection.ProxyHandlerException;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.impl.RoleImpl;
import cz.incad.kramerius.security.impl.UserImpl;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class V7ForwardUserHandler extends ProxyUserHandler {

    private static final String NAME_KEY = "name";
    private static final String SESSION_KEY = "session";
    private static final String LICENSES_KEY = "licenses";
    private static final String ROLES_KEY = "roles";
    private static final String AUTHENTICATED_KEY = "authenticated";
    private static final String UID_KEY = "uid";

    public V7ForwardUserHandler(Instances instances, User user, Client client, SolrAccess solrAccess, String source,
            String remoteAddr) {
        super(instances, user, client, solrAccess, source, remoteAddr);
    }

    protected String forwardUrl() {
        String baseurl = KConfiguration.getInstance().getConfiguration()
                .getString("cdk.collections.sources." + this.source + ".forwardurl");
        return baseurl;
    }

    @Override
    public Pair<User, List<String>> user() throws ProxyHandlerException {
        String baseurl = forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/user";
        ClientResponse fResponse = super.forwardedResponse(url);
        String entity = fResponse.getEntity(String.class);
        JSONObject jObject = new JSONObject(entity);
        return userFromJSON(jObject);
    }

    public static Pair<User, List<String>> userFromJSON(JSONObject json) throws JSONException {
        if (json.optBoolean(AUTHENTICATED_KEY, false)) {
            String uid = json.getString(UID_KEY);
            String name = json.optString(NAME_KEY,"");
            String[] names = name.split("\\s");
            String firstName = null;
            String secondName = null;
            if (names.length > 0) firstName = names[0];
            if (names.length > 1) secondName = names[1];
            
            Map<String, String> session = new HashMap<>();
            List<String> licenses = new ArrayList<>();
            List<Role> roles = new ArrayList<>();
            JSONArray rolesJSONArray = json.optJSONArray(ROLES_KEY);

            if (rolesJSONArray != null) {
                for (int i = 0; i < rolesJSONArray.length(); i++) {
                    String optString = rolesJSONArray.getString(i);
                    roles.add(new RoleImpl(optString));
                }
            }

            if (json.has(SESSION_KEY)) {
                JSONObject sessionAttrs = json.getJSONObject(SESSION_KEY);
                sessionAttrs.keySet().forEach(key -> {
                    Object object = sessionAttrs.get((String) key);
                    session.put(key.toString(), object.toString());
                });
            }

            if (json.has(LICENSES_KEY)) {
                JSONArray licensesArray = json.getJSONArray(LICENSES_KEY);
                for (int i = 0; i < licensesArray.length(); i++) {
                    licenses.add(licensesArray.getString(i));
                }
            }

            UserImpl userImpl = new UserImpl(1, firstName != null ? firstName : "", secondName != null ? secondName : "", uid, 0);
            userImpl.setGroups(roles.toArray(new Role[roles.size()]));

            session.entrySet().forEach(entry -> {
                userImpl.addSessionAttribute(entry.getKey(), entry.getValue());
            });

            return Pair.of(userImpl, licenses);

        } else
            return null;
    }
}
