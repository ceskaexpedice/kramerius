package cz.incad.kramerius.rest.apiNew.client.v70.redirection.user;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import cz.incad.kramerius.rest.apiNew.client.v70.redirection.DeleteTriggerSupport;
import cz.inovatika.cdk.cache.CDKRequestCacheSupport;
import cz.inovatika.cdk.cache.CDKRequestItem;
import cz.inovatika.cdk.cache.impl.CDKRequestItemFactory;
import cz.inovatika.monitoring.ApiCallEvent;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    public V5ForwardUserHandler(CDKRequestCacheSupport cacheSupport,
                                ReharvestManager reharvestManager,
                                Instances instances,
                                User user,
                                CloseableHttpClient closeableHttpClient,
                                SolrAccess solrAccess,
                                String source,
                                String remoteAddr) {
        super(cacheSupport,reharvestManager,instances, user, closeableHttpClient,  solrAccess, source, remoteAddr);
    }

    protected String forwardUrl() {
        String baseurl = KConfiguration.getInstance().getConfiguration()
                .getString("cdk.collections.sources." + this.source + ".forwardurl");
        return baseurl;
    }

    @Override
    public Pair<User, List<String>> user(ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/cdk/forward/user";

        String userData = super.cacheHit(url, event);
        if (userData != null) {
            return userFromJSON(new JSONObject(userData));
        }

        List<Triple<String, Long, Long>> granularTimeSnapshots = event != null ?  event.getGranularTimeSnapshots() : null;
        long start = System.currentTimeMillis();


        HttpGet httpGet = super.apacheGet(url, true);
        try (CloseableHttpResponse response = apacheClient.execute(httpGet)) {
            int code = response.getCode();
            if (code == 200) {

                long stop = System.currentTimeMillis();
                if (granularTimeSnapshots != null) {
                    granularTimeSnapshots.add(Triple.of(String.format("http/v5/%s", this.getSource()), start, stop));
                }

                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();
                String jsonUser = IOUtils.toString(is, Charset.forName("UTF-8"));
                try {
                    CDKRequestItem<String> cacheItem = (CDKRequestItem<String>)  CDKRequestItemFactory.createCacheItem(
                            jsonUser,
                            "application/json",
                            url,
                            null,
                            source,
                            LocalDateTime.now(),
                            userCacheIdentification()
                    );
                    LOGGER.info( String.format("Cache item is %s", cacheItem.toString()));
                    this.cacheSupport.save(cacheItem);
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                }

                JSONObject jObject = new JSONObject(jsonUser);
                return userFromJSON(jObject);
            } else {
                LOGGER.warning(String.format("Cannot connect %s",url));
                return null;
            }
        }catch (IOException e) {
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
