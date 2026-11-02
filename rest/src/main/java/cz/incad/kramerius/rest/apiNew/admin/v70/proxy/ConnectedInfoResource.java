package cz.incad.kramerius.rest.apiNew.admin.v70.proxy;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.inject.Provider;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.solr.client.solrj.SolrServerException;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.inject.Inject;

import cz.incad.kramerius.cdk.ChannelUtils;
import cz.incad.kramerius.rest.api.k5.client.utils.UsersUtils;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.OneInstance;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.OneInstance.TypeOfChangedStatus;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.user.V7ForwardUserHandler;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.utils.IntrospectUtils;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.PhysicalLocationMap;
import cz.incad.kramerius.timestamps.Timestamp;
import cz.incad.kramerius.timestamps.TimestampStore;
import cz.incad.kramerius.timestamps.impl.SolrTimestamp;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

@Path("/admin/v7.0/connected")
public class ConnectedInfoResource {

    public static final Logger LOGGER = Logger.getLogger(ConnectedInfoResource.class.getName());

    @Inject
    private Instances libraries;

    @Inject
    private TimestampStore timestampStore;

    @javax.inject.Inject
    @Named("forward-client")
    private CloseableHttpClient apacheClient;

    @javax.inject.Inject
    @Named("forward-client")
    private PoolingHttpClientConnectionManager apachePoolManager;

    @Inject
    private RightsResolver rightsResolver;

    @Inject
    private Provider<User> userProvider;

    @Inject
    @Named("new-index")
    private SolrAccess solrAccess;


    public ConnectedInfoResource() {
        super();
        //this.client = Client.create();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllConnected(/*@QueryParam("health")String health*/) {
        if (this.permit()) {
            JSONObject retval = new JSONObject();
            this.libraries.allInstances().forEach(library -> {
                JSONObject json = libraryJSON(library);
                retval.put(library.getName(), json);
            });
            return Response.ok(retval).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    @GET
    @Path("pool")
    @Produces(MediaType.APPLICATION_JSON)
    public Response forwardPool() {
        if (!this.permit()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        org.apache.hc.core5.pool.PoolStats total = apachePoolManager.getTotalStats();
        JSONObject result = poolStats(total);
        JSONArray routes = new JSONArray();
        apachePoolManager.getRoutes().forEach(route -> {
            org.apache.hc.core5.pool.PoolStats stats = apachePoolManager.getStats(route);
            JSONObject entry = poolStats(stats);
            entry.put("route", route.toString());
            routes.put(entry);
        });
        result.put("routes", routes);
        result.put("sampledAt", System.currentTimeMillis());
        return Response.ok(result.toString()).build();
    }

    private JSONObject poolStats(org.apache.hc.core5.pool.PoolStats stats) {
        JSONObject result = new JSONObject();
        result.put("leased", stats.getLeased());
        result.put("available", stats.getAvailable());
        result.put("pending", stats.getPending());
        result.put("max", stats.getMax());
        return result;
    }

    private JSONObject libraryJSON(OneInstance found) {
        JSONObject retval = new JSONObject();
        retval.put("status", found.isConnected());
        retval.put("type", found.getType().name());
        return retval;
    }

    @GET
    @Path("{library}/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response library(@PathParam("library") String library) {
        if (this.permit()) {
            OneInstance find = this.libraries.find(library);
            if (find != null) {
                return Response.ok(libraryJSON(find)).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }
    
    // RESOLVED ?? 
    
    @GET
    @Path("{library}/associations")
    @Produces(MediaType.APPLICATION_JSON)
    public Response assocations(@PathParam("library") String library) {
        if (this.permit()) {
            PhysicalLocationMap locationMap = new PhysicalLocationMap();
            List<String> assocations = locationMap.getAssocations(library);
            JSONArray restArr = new JSONArray();

            assocations.stream().forEach( sigla-> {
                String desc = locationMap.getDescription(sigla);
                JSONObject obj = new JSONObject();
                obj.put("sigla", sigla);
                if (desc == null) {
                    desc = "";
                }
                obj.put("description", desc);
                restArr.put(obj);
            });
            return Response.ok(restArr.toString()).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    @PUT
    @Path("{library}/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response library(@PathParam("library") String library, @QueryParam("status") String status) {
        if (this.permit()) {
            OneInstance find = this.libraries.find(library);
            if (find != null) {
                find.setConnected(Boolean.parseBoolean(status), TypeOfChangedStatus.user);
                return Response.ok(libraryJSON(find)).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    @GET
    @Path("{library}/timestamp")
    @Produces(MediaType.APPLICATION_JSON)
    public Response timestamp(@PathParam("library") String library) {
        if (this.permit()) {
            try {
                Timestamp latest = this.timestampStore.findLatest(library);
                if (latest != null) {
                    return Response.ok(latest.toJSONObject().toString()).build();
                } else
                    return Response.status(Response.Status.NOT_FOUND).build();
            } catch (SolrServerException | IOException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    @GET
    @Path("refresh")
    @Produces(MediaType.APPLICATION_JSON)
    public Response refresh() {
        if (this.permit()) {
            try {
                this.libraries.cronRefresh();
                return Response.ok(new JSONObject()).build();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    @GET
    @Path("{library}/timestamps")
    @Produces(MediaType.APPLICATION_JSON)
    public Response timestamps(@PathParam("library") String library) {
        if (this.permit()) {
            try {
                JSONArray array = new JSONArray();
                List<Timestamp> retrieveTimestamp = this.timestampStore.retrieveTimestamps(library);
                retrieveTimestamp.forEach(t -> {
                    array.put(t.toJSONObject());
                });
                return Response.ok(array.toString()).build();
            } catch (SolrServerException | IOException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    @GET
    @Path("{library}/config")
    @Produces(MediaType.APPLICATION_JSON)
    public Response config(@PathParam("library") String library) {
        if (this.permit()) {
          JSONObject config = new JSONObject();
          String baseurl = KConfiguration.getInstance().getConfiguration().getString("cdk.collections.sources." + library + ".baseurl");
          String api = KConfiguration.getInstance().getConfiguration().getString("cdk.collections.sources." + library + ".api");
          boolean channelAccess = KConfiguration.getInstance().getConfiguration().containsKey("cdk.collections.sources." + library + ".licenses") ?  KConfiguration.getInstance().getConfiguration().getBoolean("cdk.collections.sources." + library + ".licenses") : false;
          String channel = KConfiguration.getInstance().getConfiguration().getString("cdk.collections.sources." + library + ".forwardurl");
          boolean solrCloud = KConfiguration.getInstance().getConfiguration().getBoolean("cdk.collections.sources." + library + ".cloud", false);

          config.put("baseurl", baseurl);
          config.put("api", api);
          config.put("licenses", channelAccess);
          config.put("forwardurl", channel);
          config.put("solrcloud", solrCloud);
          return Response.ok(config.toString()).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    
    @GET
    @Path("{library}/config/channel/health")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getChannelHealth(@PathParam("library") String library) {
        if (this.permit()) {
            JSONObject healthObject = new JSONObject();
            JSONObject channelObject = new JSONObject();
            JSONObject usersObject = new JSONObject();

            healthObject.put("channel", channelObject);
            healthObject.put("users", usersObject);

            channelHealth(library, channelObject,usersObject);
            return Response.ok(healthObject.toString()).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    @GET
    @Path("{library}/config/index/health")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getIndexHealth(@PathParam("library") String library) {
        if (!this.permit()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        JSONObject result = new JSONObject();
        result.put("sampledAt", System.currentTimeMillis());
        result.put("cdk", queryCdkIndex(library));
        result.put("remote", queryRemoteIndex(library));
        return Response.ok(result.toString()).build();
    }

    private JSONObject queryCdkIndex(String library) {
        try {
            String response = solrAccess.requestWithSelectReturningString(
                    indexFacetQuery(String.format("cdk.collection:%s", library), "cdk.licenses"), "json", null);
            return parseIndexFacetResponse(response, "cdk.licenses", library);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "CDK index health query failed", e);
            return indexError(e);
        }
    }

    private JSONObject queryRemoteIndex(String library) {
        OneInstance instance = libraries.find(library);
        if (instance == null) {
            return indexError("Unknown library: " + library);
        }

        String channel = KConfiguration.getInstance().getConfiguration()
                .getString("cdk.collections.sources." + library + ".forwardurl");
        if (!StringUtils.isAnyString(channel)) {
            return indexError("Remote index channel is not configured");
        }

        try {
            String solrUrl = ChannelUtils.solrChannelUrl(instance.getInstanceType().name(), channel);
            HttpGet get = new HttpGet(solrUrl + "/select?" + indexFacetQuery("*", "licenses"));
            get.setHeader("User-Agent", "CDK/1.0");
            String apiKey = KConfiguration.getInstance().getConfiguration()
                    .getString("cdk.collections.sources." + library + ".apikey");
            if (StringUtils.isAnyString(apiKey)) {
                get.addHeader("X-API-KEY", apiKey);
            }

            try (CloseableHttpResponse response = apacheClient.execute(get)) {
                HttpEntity entity = response.getEntity();
                if (response.getCode() != 200 || entity == null) {
                    return indexError("Remote Solr returned HTTP " + response.getCode());
                }
                return parseIndexFacetResponse(IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8), "licenses", null);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Remote index health query failed for " + library, e);
            return indexError(e);
        }
    }

    private String indexFacetQuery(String fquery, String licensesField) {
        try {
            return "q=" + URLEncoder.encode(fquery, StandardCharsets.UTF_8.name())
                    + "&rows=0&facet=true&facet.mincount=1"
                    + "&facet.field=" + URLEncoder.encode("model", StandardCharsets.UTF_8.name())
                    + "&facet.field=" + URLEncoder.encode(licensesField, StandardCharsets.UTF_8.name())
                    + "&wt=json";
        } catch (UnsupportedEncodingException e) {
            // UTF-8 is required by every Java implementation.
            throw new IllegalStateException(e);
        }
    }

    private JSONObject parseIndexFacetResponse(String response, String licensesField, String licensePrefix) {
        JSONObject solrResponse = new JSONObject(response);
        JSONObject result = new JSONObject();
        result.put("status", "ok");
        JSONObject responseHeader = solrResponse.optJSONObject("response");
        result.put("numFound", responseHeader == null ? 0 : responseHeader.optLong("numFound", 0));

        JSONObject facetCounts = solrResponse.optJSONObject("facet_counts");
        JSONObject facetFields = facetCounts == null ? null : facetCounts.optJSONObject("facet_fields");
        result.put("models", facetValues(facetFields, "model"));
        result.put("licenses", facetValues(facetFields, licensesField, licensePrefix));
        return result;
    }

    private JSONArray facetValues(JSONObject facetFields, String field) {
        return facetValues(facetFields, field, null);
    }

    private JSONArray facetValues(JSONObject facetFields, String field, String licensePrefix) {
        JSONArray values = new JSONArray();
        JSONArray flatFacet = facetFields == null ? null : facetFields.optJSONArray(field);
        if (flatFacet == null) {
            return values;
        }
        Map<String, Long> counts = new LinkedHashMap<>();
        for (int i = 0; i + 1 < flatFacet.length(); i += 2) {
            String facetValue = flatFacet.optString(i);
            if (licensePrefix != null) {
                String prefix = licensePrefix + "_";
                if (!facetValue.startsWith(prefix)) {
                    continue;
                }
                if (facetValue.startsWith(prefix)) {
                    facetValue = facetValue.substring(prefix.length());
                }
            }
            counts.merge(facetValue, flatFacet.optLong(i + 1), Long::sum);
        }
        counts.forEach((facetValue, count) -> {
            JSONObject value = new JSONObject();
            value.put("value", facetValue);
            value.put("count", count);
            values.put(value);
        });
        return values;
    }

    private JSONObject indexError(Exception e) {
        return indexError(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
    }

    private JSONObject indexError(String message) {
        JSONObject error = new JSONObject();
        error.put("status", "error");
        error.put("message", message);
        return error;
    }


    @GET
    @Path("introspect/{pid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response introspectPid(@PathParam("pid") String pid) {
        //TODO: Delete
        if (this.permit()) {

        try {
            Pair<List<String>, List<String>> intropsected = IntrospectUtils.introspectPid(this.apacheClient, this.libraries, pid);
            JSONObject retval = new JSONObject();
            JSONArray modelsArr = new JSONArray();
            JSONArray libsArr = new JSONArray();
            intropsected.getLeft().forEach(modelsArr::put);
            intropsected.getRight().forEach(libsArr::put);
            retval.put("models", modelsArr);
            retval.put("libraries", libsArr);
            JSONObject clientUrls = new JSONObject();
            for (String library : intropsected.getRight()) {
                String clientUrl = KConfiguration.getInstance().getConfiguration()
                        .getString("cdk.collections.sources." + library + ".client");
                if (StringUtils.isAnyString(clientUrl)) {
                    String normalizedClientUrl = clientUrl.endsWith("/")
                            ? clientUrl.substring(0, clientUrl.length() - 1)
                            : clientUrl;
                    clientUrls.put(library, normalizedClientUrl + "/uuid/" + pid);
                }
            }
            retval.put("clientUrls", clientUrls);
            return Response.ok(retval.toString()).build();
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }
    
    
    

        

    private void channelHealth(String library, JSONObject channelObject, JSONObject usersObject) {
        boolean channelAccess = KConfiguration.getInstance().getConfiguration().containsKey("cdk.collections.sources." + library + ".licenses") ?  KConfiguration.getInstance().getConfiguration().getBoolean("cdk.collections.sources." + library + ".licenses") : false;
        String channel = KConfiguration.getInstance().getConfiguration().getString("cdk.collections.sources." + library + ".forwardurl");
        if (channelAccess) {
            OneInstance inst = this.libraries.find(library);
            if (inst.isConnected() && StringUtils.isAnyString(channel)) {
                channelObject.put("enabled", true);
                // solr
                try {
                    String solrChannelUrl = ChannelUtils.solrChannelUrl(inst.getInstanceType().name(), channel);
                    ChannelUtils.checkSolrChannelEndpoint(this.apacheClient, library, solrChannelUrl);
                    channelObject.put("solr", true);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    channelObject.put("solr", false);
                    channelObject.put("solr_message", e.getMessage());
                }

                // user
                try {
                    String fullChannelUrl =  ChannelUtils.userChannelUrl(inst.getInstanceType().name(), channel);

                    JSONObject notLoggedJSON = ChannelUtils.checkUserChannelEndpoint(this.apacheClient, library, fullChannelUrl, false);
                    Pair<User, List<String>> notLogged = parsedUsers(inst, notLoggedJSON);
                    
                    channelObject.put("user", true);

                    usersObject.put("notLogged", UsersUtils.userToJSON(notLogged.getLeft(),notLogged.getRight(),false));

                    JSONObject dnntJSON = ChannelUtils.checkUserChannelEndpoint(this.apacheClient, library, fullChannelUrl, true);
                    Pair<User, List<String>> dnntUser = parsedUsers(inst, dnntJSON);

                    usersObject.put("dnnt", UsersUtils.userToJSON(dnntUser.getLeft(),dnntUser.getRight(),false));

                    
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    channelObject.put("user", false);
                    channelObject.put("user_message", e.getMessage());
                }
            } else {
                channelObject.put("enabled", false);
            }
        }
    }

    private Pair<User, List<String>> parsedUsers(OneInstance inst, JSONObject notLoggedJSON) {
        switch(inst.getInstanceType()) {
            case V5:
                throw new UnsupportedOperationException("V5 instances are not supported");
            case V7:
                return V7ForwardUserHandler.userFromJSON(notLoggedJSON);
            default:
                return V7ForwardUserHandler.userFromJSON(notLoggedJSON);
        }
    }

//    private void usersDetail(JSONObject notLogged, JSONObject userJson) {
//        //JSONArray rolesDestArray = new JSONArray();
//        JSONArray rolesSourceArray = userJson.optJSONArray("roles");
//        JSONArray nRolesSourceArray = new JSONArray();
//        if (rolesSourceArray != null) {
//            for (int i = 0; i < rolesSourceArray.length(); i++) {
//                Object obj = rolesSourceArray.get(i);
//                if (obj instanceof String) {
//                    nRolesSourceArray.put(obj.toString());
//                } else {
//                    JSONObject roleObj = (JSONObject) obj;
//                    String rname = roleObj.optString("name");
//                    if (rname != null) {
//                        nRolesSourceArray.put(rname);
//                    }
//                }
//                
//            }
//        }
//        
//        notLogged.put("roles", nRolesSourceArray);
//
//        if (userJson.has("labels")) {
//            JSONArray licensesArray = userJson.getJSONArray("labels");
//            notLogged.put("licenses", licensesArray);
//        } else if (userJson.has("licenses")){
//            JSONArray licensesArray = userJson.getJSONArray("licenses");
//            notLogged.put("licenses", licensesArray);
//        }
//    }

    
    @PUT
    @Path("{library}/timestamp")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response timestamp(@PathParam("library") String library, JSONObject jsonObject) {
        if (this.permit()) {
            try {
                Timestamp timestamp = SolrTimestamp.fromJSONDoc(library, jsonObject);
                if (timestamp.getDate() == null) {
                    timestamp.updateDate(new Date());
                }
                timestamp.updateName(library);
                this.timestampStore.storeTimestamp(timestamp);
                return Response.ok(timestamp.toJSONObject().toString()).build();
            } catch (SolrServerException | IOException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    boolean permit() {
        User user = this.userProvider.get();
        if (user != null)
            return this.rightsResolver.isActionAllowed(user,
                    SecuredActions.A_ADMIN_READ.getFormalName(),
                    SpecialObjects.REPOSITORY.getPid(), null,
                    ObjectPidsPath.REPOSITORY_PATH).flag();
        else return false;
    }

}
