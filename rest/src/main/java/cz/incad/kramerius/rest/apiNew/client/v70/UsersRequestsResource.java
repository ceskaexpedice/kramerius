package cz.incad.kramerius.rest.apiNew.client.v70;

import com.google.inject.Inject;
import cz.incad.kramerius.processes.client.ProcessManagerClient;
import cz.incad.kramerius.security.User;
import cz.inovatika.dochub.DocumentType;
import cz.inovatika.dochub.UserContentBundle;
import cz.inovatika.dochub.UserContentSpace;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import jakarta.inject.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Path("/client/v7.0/userrequests")
public class UsersRequestsResource extends ClientApiResource {

    public static Logger LOGGER = Logger.getLogger(UsersRequestsResource.class.getName());

    @jakarta.inject.Inject
    @jakarta.inject.Named("forward-client")
    private CloseableHttpClient apacheClient;

    @Inject
    UserContentSpace userContentSpace;

    @jakarta.inject.Inject
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
    @Path("userspace")
    @Produces(MediaType.APPLICATION_JSON)
    public Response userspace() {
        User user = this.userProvider.get();
        try {
            List<UserContentBundle> bundles = this.userContentSpace.listBundles(user.getLoginname());
            JSONArray jsonArray = new JSONArray();
            for (UserContentBundle bundle : bundles) {
                JSONObject item = new JSONObject();
                item.put("token", bundle.getToken());
                item.put("pid", bundle.getPid());
                item.put("type", bundle.getType().name().toLowerCase());
                item.put("created", bundle.getCreated().toString());
                item.put("size", bundle.getSize());
                item.put("available", bundle.isAvailable());
                jsonArray.put(item);
            }
            return Response.ok(jsonArray.toString()).type(MediaType.APPLICATION_JSON).build();
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }

    @GET
    @Path("{source}/userspace")
    @Produces(MediaType.APPLICATION_JSON)
    public Response userspace(@PathParam("source") String source) {
        throw new UnsupportedOperationException("Supported for CDK only");
    }

    @GET
    @Path("userspace/{spacetoken}/{docType}")
    public Response userspace(@PathParam("spacetoken") String token, @PathParam("docType") String docTypeStr) {
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
}
