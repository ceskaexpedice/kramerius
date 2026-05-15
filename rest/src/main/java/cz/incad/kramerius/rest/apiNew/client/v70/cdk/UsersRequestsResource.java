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
import cz.inovatika.dochub.UserContentSpace;
import cz.inovatika.monitoring.ApiCallEvent;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.ceskaexpedice.akubra.KnownDatastreams;
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

    @Inject
    UserContentSpace userContentSpace;

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
        String[] parts = processId.split("/", 2);
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

    private ProxyItemHandler findRedirectHandler(String source) throws LexerException, IOException {
        OneInstance found = instances.find(source);
        if (found != null) {
            String remoteAddress = IPAddressUtils.getRemoteAddress(this.requestProvider.get(), KConfiguration.getInstance().getConfiguration());
            ProxyItemHandler proxyHandler = found.createProxyItemHandler(this.userProvider.get(), this.apacheClient, null, this.solrAccess, source, null, remoteAddress);
            return proxyHandler;
        } else {
            return null;
        }
    }


}
