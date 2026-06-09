package cz.incad.kramerius.rest.apiNew.client.v70.cdk;

import com.google.inject.Inject;
import cz.incad.kramerius.processes.client.ProcessManagerClient;
import cz.incad.kramerius.rest.apiNew.client.v70.ClientApiResource;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.OneInstance;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.item.ProxyItemHandler;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.IPAddressUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Path("/client/v7.0/userrequests")
public class UsersRequestsResource extends ClientApiResource {

    public static final String DELIMITER = ":";


    public static Logger LOGGER = Logger.getLogger(UsersRequestsResource.class.getName());

    @javax.inject.Inject
    @javax.inject.Named("forward-client")
    private CloseableHttpClient apacheClient;

    @javax.inject.Inject
    protected Provider<User> userProvider;

    @Inject
    Instances instances;

    @Inject
    com.google.inject.Provider<HttpServletRequest> requestProvider;

    @GET
    @Path("requests/{processId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response requests(@PathParam("processId") String processId) {
        String[] parts = processId.split(DELIMITER, 2);
        String source = parts[0];
        String processIdWithoutSource = parts[1];
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(source);
            if (redirectHandler != null) {
                return redirectHandler.requestsStatus(processIdWithoutSource);
            } else {
                return Response.ok().build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{source}/userspace")
    @Produces(MediaType.APPLICATION_JSON)
    public Response userspace(@PathParam("source") String source) {
        try {
            OneInstance oneInstance = instances.find(source);
            if (oneInstance == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            JSONArray aggregated = new JSONArray();
            ProxyItemHandler redirectHandler = findRedirectHandler(oneInstance);
            if (redirectHandler == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            Response response = redirectHandler.requestsUserSpace();
            String json = (String) response.getEntity();
            JSONArray responseArray = new JSONArray(json);
            for (int i = 0; i < responseArray.length(); i++) {
                JSONObject item = responseArray.getJSONObject(i);
                item.put("source", oneInstance.getName());
                aggregated.put(item);
            }
            return Response.ok(aggregated.toString()).type(MediaType.APPLICATION_JSON).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("userspace}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response userspace() {
        try {
            List<OneInstance> oneInstances = instances.allInstances();
            JSONArray aggregated = new JSONArray();
            for (OneInstance instance : oneInstances) {
                ProxyItemHandler redirectHandler = findRedirectHandler(instance);
                if (redirectHandler == null) {
                    continue;
                }
                Response response = redirectHandler.requestsUserSpace();
                String json = (String) response.getEntity();
                JSONArray responseArray = new JSONArray(json);
                for (int i = 0; i < responseArray.length(); i++) {
                    JSONObject item = responseArray.getJSONObject(i);
                    item.put("source", instance.getName());
                    aggregated.put(item);
                }
            }
            return Response.ok(aggregated.toString()).type(MediaType.APPLICATION_JSON).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("userspace")
    @Produces(MediaType.APPLICATION_JSON)
    public Response userspace() {
        JSONArray merged = new JSONArray();
        for (OneInstance instance : this.instances.enabledInstances()) {
            String source = instance.getName();
            if (!instance.hasFullAccess()) {
                continue;
            }
            try {
                JSONArray sourceItems = requestUserspace(source);
                for (int i = 0; i < sourceItems.length(); i++) {
                    JSONObject item = sourceItems.getJSONObject(i);
                    item.put("source", source);
                    if (item.has("token")) {
                        item.put("token", source + DELIMITER + item.getString("token"));
                    }
                    merged.put(item);
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, String.format("Cannot fetch userspace list from source=%s", source), e);
            }
        }
        return Response.ok(merged.toString()).type(MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("userspace/{spacetoken}/{docType}")
    public Response userspace(@PathParam("spacetoken") String token, @PathParam("docType") String docTypeStr) {
        String[] parts = token.split(DELIMITER, 2);
        String source = parts[0];
        String tokenWithoutSource = parts[1];
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(source);
            if (redirectHandler != null) {
                return redirectHandler.requestsUserSpace(tokenWithoutSource, docTypeStr);
            } else {
                return Response.ok().build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    private ProxyItemHandler findRedirectHandler(String source) throws LexerException, IOException {
        OneInstance found = instances.find(source);
        if (found != null) {
            return findRedirectHandler(found);
        } else {
            return null;
        }
    }

    private ProxyItemHandler findRedirectHandler(OneInstance oneInstance) {
        String remoteAddress = IPAddressUtils.getRemoteAddress(this.requestProvider.get(), KConfiguration.getInstance().getConfiguration());
        ProxyItemHandler proxyHandler = oneInstance.createProxyItemHandler(this.userProvider.get(), this.apacheClient, null, this.solrAccess, oneInstance.getName(), null, remoteAddress);
        return proxyHandler;
    }

    private JSONArray requestUserspace(String source) throws IOException {
        String baseurl = forwardUrl(source);
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/userspace";
        HttpGet get = new HttpGet(url);
        get.setHeader("User-Agent", "CDK/1.0");
        addCdkHeaders(get, source);

        try (CloseableHttpResponse response = apacheClient.execute(get)) {
            int code = response.getCode();
            if (code == Response.Status.OK.getStatusCode()) {
                String json = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                return new JSONArray(json);
            } else {
                LOGGER.log(Level.WARNING, String.format("Userspace list request failed: source=%s, status=%d, url=%s", source, code, url));
                return new JSONArray();
            }
        }
    }

    private void addCdkHeaders(HttpGet get, String source) {
        User user = this.userProvider.get();
        if (user != null && user.getId() != -1) {
            String header = prepareForwardHeader(source);
            get.setHeader("CDK-TOKEN-PARAMETERS", header);
            get.setHeader("CDK_TOKEN_PARAMETERS", header);
        }
        String apiKey = apiKey(source);
        if (apiKey != null && !apiKey.isEmpty()) {
            get.addHeader("X-API-KEY", apiKey);
        }
    }

    private String prepareForwardHeader(String source) {
        KConfiguration configuration = KConfiguration.getInstance();
        String prefixHeaders = configuration.getConfiguration().getString("cdk.shibboleth.forward.headers");
        boolean shibbolethAttributes = configuration.getConfiguration()
                .getBoolean("cdk.collections.sources." + source + ".shibboleth_attributes", true);

        String header = "";
        User user = this.userProvider.get();
        if (shibbolethAttributes && user != null) {
            header = header + user.getSessionAttributes().keySet().stream()
                    .map(key -> "header_" + key + "=" + user.getSessionAttributes().get(key))
                    .collect(Collectors.joining("|"));
        }

        String remoteAddress = IPAddressUtils.getRemoteAddress(this.requestProvider.get(), configuration.getConfiguration());
        if (remoteAddress != null) {
            header = header + (header.isEmpty() ? "" : "|") + "header_ip_address=" + remoteAddress;
        }
        if (prefixHeaders != null && !prefixHeaders.isEmpty()) {
            header = prefixHeaders + header;
        }
        return header;
    }

    private String forwardUrl(String source) {
        return KConfiguration.getInstance().getConfiguration()
                .getString("cdk.collections.sources." + source + ".forwardurl");
    }

    private String apiKey(String source) {
        return KConfiguration.getInstance().getConfiguration()
                .getString("cdk.collections.sources." + source + ".apikey");
    }

}
