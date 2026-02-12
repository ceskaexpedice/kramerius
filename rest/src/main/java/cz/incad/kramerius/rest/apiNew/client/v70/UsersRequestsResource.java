package cz.incad.kramerius.rest.apiNew.client.v70;

import com.google.inject.Inject;
import cz.incad.kramerius.processes.client.ProcessManagerClient;
import cz.incad.kramerius.security.User;
import cz.inovatika.dochub.UserContentSpace;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.json.JSONObject;

import javax.inject.Provider;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.logging.Logger;

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


    @GET
    @Path("requests/{reqid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response requests(@PathParam("reqid") String reqid) {

        ProcessManagerClient processManagerClient = new ProcessManagerClient(apacheClient);
        JSONObject process = processManagerClient.getProcess(reqid);
        if (process != null) {
            return Response.ok().entity(process.toString()).build();
        } else {
            throw new NotFoundException("there's no process with process_id=" + reqid);
        }
    }

    private static StreamingOutput streamingOutput(final InputStream is) {
        return new StreamingOutput() {
            public void write(OutputStream output) throws IOException, WebApplicationException {
                try {
                    IOUtils.copy(is, output);
                } catch (Exception e) {
                    throw new WebApplicationException(e);
                }
            }
        };
    }


    @GET
    @Path("userspace/{spacetoken}")
    @Produces("application/pdf")
    public Response userspace(@PathParam("spacetoken") String token) {
        User user = this.userProvider.get();
        boolean exists = this.userContentSpace.exists(token);
        if (exists) {
            try {
                Optional<InputStream> bundle = this.userContentSpace.getBundle(token, user.getLoginname());
                if (bundle.isPresent()) {
                    InputStream is = bundle.get();
                    StreamingOutput stream = output -> {
                        try (is) {
                            is.transferTo(output);
                        } catch (Exception e) {
                            throw new WebApplicationException(e);
                        }
                    };

                    return Response.ok(stream)
                            .header("Content-Disposition", "attachment; filename=\"" + token + ".pdf\"")
                            //.header("Content-Disposition", "inline; filename=\"" + token + ".pdf\"")
                            .type("application/pdf")
                            .build();
                } else {
                    throw new cz.incad.kramerius.rest.apiNew.exceptions.NotFoundException();
                }
            } catch (ClientErrorException e) {
                throw e;
            } catch (Exception e) {
                throw new WebApplicationException(e);
            }
        } else {
            throw new cz.incad.kramerius.rest.apiNew.exceptions.NotFoundException();
        }

    }
}
