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
    CDKDocumentSourceProvider documentSourceProvider;

    @Inject
    Instances instances;

    @Inject
    com.google.inject.Provider<HttpServletRequest> requestProvider;

    @GET
    @Path("requests/{reqid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response requests(@PathParam("processId") String processId) {
        // TODO pepo ziskat source
        // TODO pepo poslat dal orezane
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, source);
            if (redirectHandler != null) {
                return redirectHandler.requestsStatus(reqid);
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
        // TODO pepo ziskat source z tokenu a orezat

        LOGGER.fine("Requesting user space with token: " + token + " and docType: " + docTypeStr);
        DocumentType docType;
        try {
            docType = DocumentType.valueOf(docTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new cz.incad.kramerius.rest.apiNew.exceptions.BadRequestException("Invalid document type: " + docTypeStr);
        }
        User user = this.userProvider.get();

        if (!this.userContentSpace.exists(token, docType)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new JSONObject().put("error", "User content space for token=" + token + " not found").toString())
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        try {
            Optional<InputStream> bundle = this.userContentSpace.getBundle(token, user.getLoginname(), docType);
            if (!bundle.isPresent()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new JSONObject().put("error", "Bundle for token=" + token + ", user=" + user.getLoginname() + " and docType=" + docType + " not found").toString())
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            }

            InputStream is = bundle.get();
            StreamingOutput stream = output -> {
                try (is) {
                    is.transferTo(output);
                } catch (Exception e) {
                    throw new WebApplicationException(e);
                }
            };
            return Response.ok(stream)
                    .header("Content-Disposition", "attachment; filename=\"" +
                            switch (docType) {
                                case PDF -> "export_" + token + ".pdf";
                                case TEXT -> "export_" + token + ".txt";
                                case EPUB -> "export_" + token + ".epub";
                            } + "\"")
                    .type(switch (docType) {
                        case PDF -> "application/pdf";
                        case TEXT -> "text/plain";
                        case EPUB -> "application/epub+zip";
                    })
                    .build();
        } catch (ClientErrorException e) {
            throw e;
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }

    private ProxyItemHandler findRedirectHandler(String pid, String source) throws LexerException, IOException {
        if (source == null) {
            //source = defaultDocumentSource(pid);
            source = documentSourceProvider.getDocumentSource(pid);
        }
        OneInstance found = instances.find(source);
        if (found!= null) {
            String remoteAddress = IPAddressUtils.getRemoteAddress(this.requestProvider.get(), KConfiguration.getInstance().getConfiguration());
            ProxyItemHandler proxyHandler = found.createProxyItemHandler(this.userProvider.get(), this.apacheClient.get(), this.deleteTriggerSupport, this.solrAccess, source, pid, remoteAddress);
            return proxyHandler;
        } else {
            return null;
        }
    }


}
