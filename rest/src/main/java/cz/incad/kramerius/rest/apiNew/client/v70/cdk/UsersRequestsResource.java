package cz.incad.kramerius.rest.apiNew.client.v70.cdk;

import com.google.inject.Inject;
import cz.incad.kramerius.processes.client.ProcessManagerClient;
import cz.incad.kramerius.rest.apiNew.client.v70.ClientApiResource;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.OneInstance;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.item.ProxyItemHandler;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.source.CDKDocumentSourceProvider;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.IPAddressUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.inovatika.dochub.DocumentType;
import cz.inovatika.dochub.UserContentBundle;
import cz.inovatika.dochub.UserContentSpace;
import cz.inovatika.monitoring.ApiCallEvent;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static cz.incad.kramerius.rest.apiNew.client.v70.redirection.item.ProxyItemHandler.RequestMethodName.get;

@Path("/client/v7.0/userrequests")
public class UsersRequestsResource extends ClientApiResource {

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
    @Path("requests/{reqId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response requests(@PathParam("reqId") String reqId) {
        String[] parts = reqId.split("/", 2);
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
    @Path("userspace/{spacetoken}/{docType}")
    public Response userspace(@PathParam("spacetoken") String token, @PathParam("docType") String docTypeStr) {
        String[] parts = token.split("/", 2);
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

    private ProxyItemHandler findRedirectHandler(String source) {
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


}
