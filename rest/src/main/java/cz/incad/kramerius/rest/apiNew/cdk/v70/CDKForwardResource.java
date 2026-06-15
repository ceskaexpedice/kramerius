package cz.incad.kramerius.rest.apiNew.cdk.v70;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.logging.Level;

import javax.xml.xpath.XPathExpressionException;

import com.google.inject.Inject;
import cz.incad.kramerius.pdf.OutOfRangeException;
import cz.incad.kramerius.processes.cdk.CDKAPIKeySupport;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.apiNew.cdk.v70.resources.*;
import cz.incad.kramerius.rest.apiNew.client.v70.ItemsResource;
import cz.incad.kramerius.rest.apiNew.client.v70.UsersRequestsResource;
import cz.incad.kramerius.rest.apiNew.exceptions.BadRequestException;
import cz.incad.kramerius.rest.apiNew.client.v70.pdf.PDFResource;
import cz.incad.kramerius.rest.apiNew.exceptions.ForbiddenException;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.service.ReplicateException;
import cz.incad.kramerius.utils.conf.KConfiguration;
import jakarta.inject.Provider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import cz.inovatika.dochub.UserContentSpace;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * CDK Forward resource
 * <p>
 * Provides endpoints for secured channel between source and CDK instance;
 * The visibility of endpoints must be enabled and configuration by following <a href="https://github.com/ceskaexpedice/ceska-digitalni-knihovna/wiki/Zabezpe%C4%8Den%C3%A1-komunikace-chr%C3%A1n%C4%9Bn%C3%BD-kan%C3%A1l"> insructions </a>
 * </p>
 */
@Path("cdk/v7.0/forward")
public class CDKForwardResource {

    public static final String X_API_KEY = "X-API-KEY";
    private static final Logger LOGGER = Logger.getLogger(CDKForwardResource.class.getName());

    @Inject
    CDKAPIKeySupport cdkAPIKeySupport;

    @Inject
    Provider<HttpServletRequest> requestProvider;

    @Inject
    CDKUsersResource usersResource;

    @Inject
    CDKItemResource itemResource;

    @Inject
    CDKIIIFResource iiifResource;

    @Inject
    CDKZoomifyResource zoomifyResource;

    @Inject
    SOLRResource solrResource;

    @Inject
    PDFResource pdfResource;

    @Inject
    ItemsResource itemsResource;

    @Inject
    UsersRequestsResource usersRequestsResource;

    @Inject
    @Named("forward-client")
    private CloseableHttpClient apacheClient;

    @javax.inject.Inject
    protected Provider<User> userProvider;

    // --------- User's endpoint --------------------
    @GET
    @Path("user")
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    public Response user() {
        if (isAllowedByApiKey() || isAllowedByChannel()) {
            return this.usersResource.user();
        } else {
            throw new ForbiddenException("Access denied: Valid API key or secured channel required.");
        }
    }

    // --------- Public worker requests --------------------
    @GET
    @Path("requests/{processId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response requests(@PathParam("processId") String processId) {
        if (isAllowedByApiKey() || isAllowedByChannel()) {
            return usersRequestsResource.requests(processId);
        } else {
            throw new ForbiddenException("Access denied: Valid API key or secured channel required.");
        }
    }

    @GET
    @Path("userspace")
    @Produces(MediaType.APPLICATION_JSON)
    public Response userspace() {
        if (isAllowedByApiKey() || isAllowedByChannel()) {
            return usersRequestsResource.userspace();
        } else {
            throw new ForbiddenException("Access denied: Valid API key or secured channel required.");
        }
    }

    @GET
    @Path("userspace/{spacetoken}/{docType}")
    public Response userspace(@PathParam("spacetoken") String token, @PathParam("docType") String docTypeStr) {
        if (isAllowedByApiKey() || isAllowedByChannel()) {
            return usersRequestsResource.userspace(token, docTypeStr);
        } else {
            throw new ForbiddenException("Access denied: Valid API key or secured channel required.");
        }
    }


    @POST
    @Path("{pid}/requests/{reqid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response requests(@PathParam("pid") String pid,
                             @PathParam("reqid") String reqid,
                             @QueryParam("lang") String lang,
                             @HeaderParam("Accept-Language") Locale locale, String reqDefinition) {
        if (isAllowedByApiKey() || isAllowedByChannel()) {
            JSONObject safeReqDefinition = parseRequestDefinition(pid, reqid, reqDefinition);
            LOGGER.log(Level.FINE, "requests: " + pid + " " + reqid + " " + safeReqDefinition.toString());
            return itemsResource.requests(pid, reqid, lang, locale, safeReqDefinition);
        } else {
            HttpServletRequest request = this.requestProvider.get();
            LOGGER.log(Level.WARNING, String.format(
                    "CDK forward request denied: pid=%s, reqid=%s, xApiKeyPresent=%s, securedChannel=%s",
                    pid,
                    reqid,
                    request.getHeader(X_API_KEY) != null,
                    KConfiguration.getInstance().getConfiguration().getBoolean("cdk.secured.channel", false)
            ));
            throw new ForbiddenException("Access denied: Valid API key or secured channel required.");
        }
    }

    private JSONObject parseRequestDefinition(String pid, String reqid, String reqDefinition) {
        if (reqDefinition == null || reqDefinition.trim().isEmpty()) {
            return new JSONObject();
        }
        try {
            return new JSONObject(reqDefinition);
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, String.format(
                    "Invalid CDK forward request JSON: pid=%s, reqid=%s, body=%s",
                    pid, reqid, reqDefinition
            ), e);
            throw new BadRequestException("Invalid JSON request definition");
        }
    }

    // =========== Collection specific endpoints

    @GET
    @Path("{pid}/collection/cuttings")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getCollectionClips(@PathParam("pid") String pid) {
        if (isAllowedByApiKey() || isAllowedByChannel()) {
            return itemsResource.getCollectionClips(pid);
        } else {
            throw new ForbiddenException("Access denied: Valid API key or secured channel required.");
        }
    }

    @GET
    @Path("{pid}/collection/cuttings/image/{thumb_id}")
    public Response getCollectionThumb(@PathParam("pid") String pid, @PathParam("thumb_id") String thumbId) {
        if (isAllowedByApiKey() || isAllowedByChannel()) {
            return itemsResource.getCollectionThumb(pid, thumbId);
        } else {
            throw new ForbiddenException("Access denied: Valid API key or secured channel required.");
        }
    }

    // --------------- pdf ---------------
    @GET
    @Path("pdf/selection")
    @Produces({"application/pdf", "application/json"})
    public Response selection(@QueryParam("pids") String pidsParam,
                              @QueryParam("firstPageType") @DefaultValue("TEXT") String firstPageType,
                              @QueryParam("format") String format) throws OutOfRangeException {
        if (isAllowedByApiKey() || isAllowedByChannel()) {

            return pdfResource.selection(pidsParam, firstPageType, format);
        } else {
            throw new ForbiddenException("Access denied: Valid API key or secured channel required.");
        }
    }

    // --------------- Item's endpoint ---------------
    @GET
    @Path("item/{pid}/streams/{dsid}")
    public Response stream(@Context HttpHeaders headers, @PathParam("pid") String pid, @PathParam("dsid") String dsid) {
        if (isAllowedByApiKey() || isAllowedByChannel()) {
            return itemResource.stream(pid, dsid);
        } else {
            throw new ForbiddenException("Access denied: Valid API key or secured channel required.");
        }
    }

    @GET
    @Path("iiif/{pid}/info.json")
    @Produces("application/ld+json")
    public Response iiiFManifest(@PathParam("pid") String pid) {
        if (isAllowedByApiKey() || isAllowedByChannel()) {
            return this.iiifResource.iiifManifest(pid);
        } else {
            throw new ForbiddenException("Access denied: Valid API key or secured channel required.");
        }
    }

    @GET
    //@Produces("image/jpeg")
    @Path("iiif/{pid}/{region}/{size}/{rotation}/{qf}.{format}")
    public Response tile(@PathParam("pid") String pid,
                         @PathParam("region") String region,
                         @PathParam("size") String size,
                         @PathParam("rotation") String rotation,
                         @PathParam("qf") String qf,
                         @PathParam("format") String format) {
        if (isAllowedByApiKey() || isAllowedByChannel()) {
            try {
                return this.iiifResource.iiifTile(pid, region, size, rotation, qf, format);
            } catch (IOException e) {
                throw new GenericApplicationException(e.getMessage());
            }
        } else {
            throw new ForbiddenException("Access denied: Valid API key or secured channel required.");
        }
    }


    @GET
    @Path("zoomify/{pid}/ImageProperties.xml")
    @Produces("application/xml")
    public Response zoomifyManifest(@PathParam("pid") String pid) {
        if (isAllowedByApiKey() || isAllowedByChannel()) {
            return this.zoomifyResource.zoomifyManifest(pid);
        } else {
            throw new ForbiddenException("Access denied: Valid API key or secured channel required.");
        }
    }

    @GET
    @Path("zoomify/{pid}/TileGroup0/{level}-{x}-{y}.jpg")
    @Produces("image/jpeg")
    public Response zoomifyTile(@PathParam("pid") String pid, @PathParam("level") String level,
                                @PathParam("x") String x, @PathParam("y") String y) {
        if (isAllowedByApiKey() || isAllowedByChannel()) {
            try {
                return this.zoomifyResource.renderZoomifyTile(pid, level, x, y, "jpg");
            } catch (IOException | XPathExpressionException | SQLException e) {
                throw new GenericApplicationException(e.getMessage());
            }
        } else {
            throw new ForbiddenException("Access denied: Valid API key or secured channel required.");
        }
    }

    @GET
    @Path("providedBy/{pid}")
    @Produces("appliction/json")
    public Response providedBy(@PathParam("pid") String pid) {
        if (isAllowedByApiKey() || isAllowedByChannel()) {
            return this.itemResource.providedBy(pid);
        } else {
            throw new ForbiddenException("Access denied: Valid API key or secured channel required.");
        }
    }


    @GET
    @Path("info/{pid}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getInfo(@PathParam("pid") String pid) {
        if (isAllowedByApiKey() || isAllowedByChannel()) {
            return this.itemResource.info(pid);
        } else {
            throw new ForbiddenException("Access denied: Valid API key or secured channel required.");
        }

//        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/info",pid), "", "GET", pid);
//        try {
//            checkSupportedObjectPid(pid);
//            checkObjectExists(pid);
//            JSONObject json = new JSONObject();
//            json.put("data", extractAvailableDataInfo(pid));
//
//            //json.put("structure", extractStructureInfo(pid));
//            json.put("image", extractImageSourceInfo(pid));
//
//
//            RightRuntimeInformations.RuntimeInformation extracrtedInformation = RightRuntimeInformations.extractInformations(this.rightsResolver, this.solrAccess, pid);
//
//            json.put(RightRuntimeInformations.PROVIDED_BY_LICENSES, extracrtedInformation.getProvidingLicensesAsJSONArray());
//            json.put(RightRuntimeInformations.ACCESSIBLE_LOCSK, extracrtedInformation.getLockAsJSONArray());
//
//
//            return Response.ok(json).build();
//        } catch (WebApplicationException e) {
//            throw e;
//        } catch (Throwable e) {
//            LOGGER.log(Level.SEVERE, e.getMessage(), e);
//            throw new InternalErrorException(e.getMessage());
//        } finally {
//            if (event != null) {
//                this.apiCallMonitor.stop(event, userProvider.get().getLoginname());
//            }
//        }
    }


    // --------------- CDK Replication endpoint ---------------
    @GET
    @Path("sync/solr/select")
    @Produces({MediaType.APPLICATION_XML + ";charset=utf-8"})
    public Response selectXML(@Context UriInfo uriInfo) throws IOException {
        if (isAllowedByApiKey() || isAllowedByChannel()) {
            return this.solrResource.selectXML(uriInfo);
        } else {
            throw new ForbiddenException("Access denied: Valid API key or secured channel required.");
        }
    }

    @GET
    @Path("sync/solr/select")
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    public Response selectJSON(@Context UriInfo uriInfo) throws IOException {
        if (isAllowedByApiKey() || isAllowedByChannel()) {
            return this.solrResource.selectJSON(uriInfo);
        } else {
            throw new ForbiddenException("Access denied: Valid API key or secured channel required.");
        }
    }

    @GET
    @Path("sync/batch/foxmls")
    @Produces("application/zip")
    public Response batchedFOXL(@QueryParam("pids") String stringPids, @QueryParam("collection") String collection) throws ReplicateException, IOException {
        if (isAllowedByApiKey() || isAllowedByChannel()) {
            return this.solrResource.batchedFOXL(stringPids, collection);
        } else {
            throw new ForbiddenException("Access denied: Valid API key or secured channel required.");
        }
    }

    @GET
    @Path("sync/{pid}/foxml")
    @Produces(MediaType.APPLICATION_XML + ";charset=utf-8")
    public Response getExportedFOXML(@PathParam("pid") String pid,
                                     @QueryParam("collection") String collection)
            throws ReplicateException, UnsupportedEncodingException {
        if (isAllowedByApiKey() || isAllowedByChannel()) {
            return this.solrResource.getExportedFOXML(pid, collection);
        } else {
            throw new ForbiddenException("Access denied: Valid API key or secured channel required.");
        }
    }

    //boolean channel = KConfiguration.getInstance().getConfiguration().getBoolean("cdk.secured.channel", false);
    private boolean isAllowedByApiKey() {
        boolean apiKeyAuth = KConfiguration.getInstance().getConfiguration().getBoolean("cdk.secured.apikey", false);
        if (apiKeyAuth) {
            this.cdkAPIKeySupport.init();
        }
        HttpServletRequest httpServletRequest = this.requestProvider.get();
        String header = httpServletRequest.getHeader(X_API_KEY);
        return this.cdkAPIKeySupport.isValidKey(header);
    }

    private boolean isAllowedByChannel() {
        boolean channel = KConfiguration.getInstance().getConfiguration().getBoolean("cdk.secured.channel", false);
        return channel;
    }
}
