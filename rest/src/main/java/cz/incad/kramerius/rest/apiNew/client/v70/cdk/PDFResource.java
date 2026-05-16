package cz.incad.kramerius.rest.apiNew.client.v70.cdk;

import com.google.inject.Inject;
import cz.incad.kramerius.pdf.OutOfRangeException;
import cz.incad.kramerius.rest.apiNew.client.v70.ClientApiResource;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.OneInstance;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.item.ProxyItemHandler;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.IPAddressUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/client/v7.0/pdf")
public class PDFResource extends ClientApiResource {

    public static Logger LOGGER = Logger.getLogger(PDFResource.class.getName());

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
    @Path("selection")
    @Produces({"application/pdf", "application/json"})
    public Response selection(@QueryParam("pids") String pidsParam,
                              @QueryParam("firstPageType") @DefaultValue("TEXT") String firstPageType,
                              @QueryParam("format") String format) throws OutOfRangeException {
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pidsParam); // TODO pepo
            if (redirectHandler != null) {
                return redirectHandler.pdfSelection(pidsParam, firstPageType, format);
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
