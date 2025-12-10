package cz.incad.kramerius.rest.apiNew.admin.v70;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.utils.IntrospectUtils;
import cz.incad.kramerius.rest.apiNew.exceptions.BadRequestException;
import cz.incad.kramerius.rest.apiNew.exceptions.ForbiddenException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.ceskaexpedice.akubra.DistributedLocksException;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.ceskaexpedice.akubra.LockOperation;
import org.ceskaexpedice.akubra.processingindex.CursorItemsPair;
import org.ceskaexpedice.akubra.processingindex.ProcessingIndexItem;
import org.ceskaexpedice.akubra.utils.Dom4jUtils;
import org.ceskaexpedice.fedoramodel.DigitalObject;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Path("/admin/v7.0/items")
public class ItemsResource extends AdminApiResource {

    public static Logger LOGGER = Logger.getLogger(ItemsResource.class.getName());

    private static final Integer DEFAULT_OFFSET = 0;
    private static final Integer DEFAULT_LIMIT = 10;


    @javax.inject.Inject
    Provider<User> userProvider;

    @Inject
    private FoxmlBuilder foxmlBuilder;

    @Inject
    @Named("new-index")
    private SolrAccess solrAccess;


    @Inject
    RightsResolver rightsResolver;

    @Inject
    Provider<HttpServletRequest> requestProvider;

    //TODO: Do it better; divide into two classes
    @Inject
    private Instances libraries;

    @Inject
    @Named("forward-client")
    private CloseableHttpClient apacheClient;

    public ItemsResource() {
        super();
        //this.client = Client.create();
    }


    /**
     * Returns array of pids (with titles) that have given model. Only partial array with offset & limit.
     * All top-level objects without model specification cannot be returned here, because this information (being top-level) is not available from resource index.
     * Instead, it is derived during indexation process and stored in search index.
     * Accessing this information from search index would violate architecture and possibly cause circular dependency.
     *
     * @param model
     * @param order  resulting objects are sorted by title, you can specify ASC or DESC
     * @param offset ignored, if cursor is also present
     * @param cursor cursorMark for cursor query
     * @param limit
     * @return array of {pid,title} objects
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getItems(@QueryParam("model") String model,
                             @QueryParam("offset") String offset,
                             @QueryParam("cursor") String cursor,
                             @QueryParam("limit") String limit,
                             @QueryParam("order") @DefaultValue("ASC") String order) {
        try {

            User user = this.userProvider.get();
            List<String> roles = Arrays.stream(user.getGroups()).map(Role::getName).collect(Collectors.toList());
            //authorization

            if (!userIsAllowedToRead(this.rightsResolver, user, SpecialObjects.REPOSITORY.getPid())) {
                // request doesnt contain user principal
                throw new ForbiddenException("user '%s' is not allowed to do this (missing action '%s')", user, SecuredActions.A_ADMIN_READ.name()); //403
            }


            //model
            if (model == null || model.isEmpty()) {
                throw new BadRequestException("missing mandatory query param 'model'");
            }
            //order
            boolean ascendingOrder;
            switch (order) {
                case "ASC":
                    ascendingOrder = true;
                    break;
                case "DESC":
                    ascendingOrder = false;
                    break;
                default:
                    throw new BadRequestException("invalid value of query param 'order': '%s'; valid values are 'ASC' or 'DESC'", order);
            }
            //offset/cursor & limit
            int offsetInt = DEFAULT_OFFSET;
            if (StringUtils.isAnyString(offset)) {
                try {
                    offsetInt = Integer.valueOf(offset);
                    if (offsetInt < 0) {
                        throw new BadRequestException("offset must be zero or a positive number, '%s' is not", offset);
                    }
                } catch (NumberFormatException e) {
                    throw new BadRequestException("offset must be a number, '%s' is not", offset);
                }
            }
            int limitInt = DEFAULT_LIMIT;
            if (StringUtils.isAnyString(limit)) {
                try {
                    limitInt = Integer.valueOf(limit);
                    if (limitInt < 1) {
                        throw new BadRequestException("limit must be a positive number, '%s' is not", limit);
                    }
                } catch (NumberFormatException e) {
                    throw new BadRequestException("limit must be a number, '%s' is not", limit);
                }
            }
            String nextCursorMark = null;
            List<ProcessingIndexItem> titlePidPairs;
            if (cursor != null) {
                CursorItemsPair pair = akubraRepository.pi().getByModelWithCursor(model, ascendingOrder, cursor, limitInt);
                nextCursorMark = pair.nextCursor();
                titlePidPairs = pair.items();
            }else{
                akubraRepository.pi().getByModel(model, ascendingOrder, offsetInt, limitInt);
                titlePidPairs = akubraRepository.pi().getByModel(model, ascendingOrder, offsetInt, limitInt);
            }
            JSONObject json = new JSONObject();
            json.put("model", model);
            if (nextCursorMark != null) {
                json.put("nextCursor", nextCursorMark);
            }
            JSONArray items = new JSONArray();
            for (ProcessingIndexItem pidAndTitle : titlePidPairs) {
                JSONObject item = new JSONObject();
                item.put("title", pidAndTitle.dcTitle());
                item.put("pid", pidAndTitle.source());
                items.put(item);
            }
            json.put("items", items);
            return Response.ok(json).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }


    @SuppressWarnings("deprecation")
    @GET
    @Path("/models")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCollections(@QueryParam("withItem") String itemPid) {
        try {
            User user = this.userProvider.get();
            List<String> roles = Arrays.stream(user.getGroups()).map(Role::getName).collect(Collectors.toList());
            // TODO: check if it is necessary
//            if (!permitPocessingIndexAcceess(this.rightsResolver, user1)) {
//                throw new ForbiddenException("user '%s' is not allowed to read processing index (missing action '%s')", user1.getLoginname(), SecuredActions.A_INDEX); //403
//            }

            if (!userIsAllowedToRead(this.rightsResolver, user, SpecialObjects.REPOSITORY.getPid())) {
                // request doesnt contain user principal
                throw new ForbiddenException("user '%s' is not allowed to do this (missing action '%s')", user, SecuredActions.A_ADMIN_READ.name()); //403
            }
            List<Pair<String, Long>> allFedoraModelsAsList = akubraRepository.pi().getModelsCount();
            JSONObject object = new JSONObject();
            for (Pair<String, Long> pair : allFedoraModelsAsList) {
                object.put(pair.getKey(), pair.getRight());
            }
            return Response.ok(object.toString()).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @HEAD
    @Path("{pid}")
    public Response checkItemExists(@PathParam("pid") String pid) {
        try {
            checkSupportedObjectPid(pid);
            //authentication
            User user = this.userProvider.get();
            List<String> roles = Arrays.stream(user.getGroups()).map(Role::getName).collect(Collectors.toList());
            //authorization
//            String role = ROLE_READ_ITEMS;
//            if (!roles.contains(role)) {
//                throw new ForbiddenException("user '%s' is not allowed to do this (missing role '%s')", user.getLoginname(), role); //403
//            }

            if (!userIsAllowedToRead(this.rightsResolver, user, pid)) {
                // request doesnt contain user principal
                throw new ForbiddenException("user '%s' is not allowed to do this (missing action '%s')", user, SecuredActions.A_ADMIN_READ.name()); //403
            }

            checkObjectExists(pid);
            return Response.ok().build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{pid}/solr/instintrospect")
    @Produces(MediaType.APPLICATION_JSON)
    public Response introspectPidInInstances(@PathParam("pid") String pid) {
        try {
            boolean cdkServerMode = KConfiguration.getInstance().getConfiguration().getBoolean("cdk.server.mode");
            if (cdkServerMode) {
                User user = this.userProvider.get();

                if (!userIsAllowedToRead(this.rightsResolver, user, SpecialObjects.REPOSITORY.getPid())) {
                    throw new ForbiddenException("user '%s' is not allowed to do this (missing action '%s')", user, SecuredActions.A_ADMIN_READ.name()); //403
                }
                try {
                    JSONObject obj = IntrospectUtils.introspectSolr(this.apacheClient, this.libraries, pid, true);
                    return Response.ok(obj.toString()).build();
                } catch (UnsupportedEncodingException | JSONException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    throw new InternalErrorException(e.getMessage());
                }
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }


    @GET
    @Path("{pid}/foxml")
    @Produces(MediaType.APPLICATION_XML)
    public Response getFoxml(@PathParam("pid") String pid) {
        try {
            checkSupportedObjectPid(pid);
            //authentication
            User user = this.userProvider.get();
            List<String> roles = Arrays.stream(user.getGroups()).map(Role::getName).collect(Collectors.toList());
            if (!userIsAllowedToReadOrObjectEdit(this.rightsResolver, user, pid)) {
                // request doesnt contain user principal
                throw new ForbiddenException("user '%s' is not allowed to do this (missing action '%s')", user, SecuredActions.A_ADMIN_READ.name()); //403
            }
            checkObjectExists(pid);
            return Response.ok(akubraRepository.get(pid).asInputStream()).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{pid}/licenses")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLicenses(@PathParam("pid") String pid) {
        try {
            checkSupportedObjectPid(pid);
            //authentication
            User user = this.userProvider.get();

            List<String> roles = Arrays.stream(user.getGroups()).map(Role::getName).collect(Collectors.toList());
            if (!userIsAllowedToRead(this.rightsResolver, user, pid)) {
                // request doesnt contain user principal
                throw new ForbiddenException("user '%s' is not allowed to do this (missing action '%s')", user, SecuredActions.A_ADMIN_READ.name()); //403
            }

            checkObjectExists(pid);

            Document relsExt = akubraRepository.re().get(pid).asDom4j(true);
            List<Node> licenseEls = Dom4jUtils.buildXpath("/rdf:RDF/rdf:Description/rel:license").selectNodes(relsExt);
            JSONArray licenseArray = new JSONArray();
            for (Node relationEl : licenseEls) {
                licenseArray.put(relationEl.getText());
            }
            List<Node> containsLicenseEls = Dom4jUtils.buildXpath("/rdf:RDF/rdf:Description/rel:containsLicense").selectNodes(relsExt);
            JSONArray containsLicenseArray = new JSONArray();
            for (Node relationEl : containsLicenseEls) {
                containsLicenseArray.put(relationEl.getText());
            }
            Element policyEl = (Element) Dom4jUtils.buildXpath("/rdf:RDF/rdf:Description/rel:policy").selectSingleNode(relsExt);
            String policy = null;
            if (policyEl != null) {
                policy = policyEl.getText().substring("policy:".length()).trim();
            }
            JSONObject result = new JSONObject();
            result.put("pid", pid);
            result.put("licenses", licenseArray);
            result.put("policy", policy);
            //result.put("contains_licenses", containsLicenseArray); //tady irelevantní až matoucí
            return Response.ok().entity(result.toString()).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @PUT
    @Path("{pid}/children_order")
    @Produces(MediaType.APPLICATION_JSON)
    public Response setChildrenOrder(@PathParam("pid") String pid, JSONObject newChildrenOrder) {
        try {
            checkReadOnlyWorkMode();
            checkSupportedObjectPid(pid);
            //authentication
            User user = this.userProvider.get();
            List<String> roles = Arrays.stream(user.getGroups()).map(Role::getName).collect(Collectors.toList());
            //authorization
            if (!userIsAllowedToRead(this.rightsResolver, user, pid)) {
                // request doesnt contain user principal
                throw new ForbiddenException("user '%s' is not allowed to do this (missing action '%s')", user, SecuredActions.A_ADMIN_READ.name()); //403
            }
            checkObjectExists(pid);

            //extract childrens' pids from request
            List<String> newChildrenOrderPids = new ArrayList<>();
            for (int i = 0; i < newChildrenOrder.getJSONArray("childrenPids").length(); i++) {
                newChildrenOrderPids.add(newChildrenOrder.getJSONArray("childrenPids").getString(i));
            }
            //extract childrens' pids an relations from rels-ext
            Map<String, String> foxmlChildrenPidToRelationName = new HashMap<>();
            akubraRepository.doWithWriteLock(pid, new LockOperation<Object>() {
                @Override
                public Object execute() {
                    Document relsExt = akubraRepository.re().get(pid).asDom4j(true);
                    List<Node> childrenEls = Dom4jUtils.buildXpath("/rdf:RDF/rdf:Description/*[starts-with(@rdf:resource, 'info:fedora/uuid:')]").selectNodes(relsExt);
                    for (Node childrenEl : childrenEls) {
                        String relationName = childrenEl.getName();
                        String childPid = ((Element) childrenEl).attributeValue("resource").substring("info:fedora/".length());
                        foxmlChildrenPidToRelationName.put(childPid, relationName);
                    }
                    //check that all pids from request are in rels-ext
                    for (String childPid : newChildrenOrderPids) {
                        if (!foxmlChildrenPidToRelationName.containsKey(childPid)) {
                            throw new BadRequestException("child %s from reorder-data not found in RELS-EXT ", childPid);
                        }
                    }
                    //check that all pids from rels-ext are in request
                    for (String childPid : foxmlChildrenPidToRelationName.keySet()) {
                        if (!newChildrenOrderPids.contains(childPid)) {
                            throw new BadRequestException("child %s from RELS-EXT is missing in reorder-data", childPid);
                        }
                    }
                    //update & save rels-ext
                    for (Node node : childrenEls) {
                        node.detach();
                    }
                    for (String childPid : newChildrenOrderPids) {
                        foxmlBuilder.appendRelationToRelsExt(pid, relsExt, foxmlChildrenPidToRelationName.get(childPid), childPid);
                    }
                    ByteArrayInputStream bis = new ByteArrayInputStream(relsExt.asXML().getBytes(Charset.forName("UTF-8")));
                    akubraRepository.re().update(pid, bis);
                    return null;
                }
            });

            // TODO pepo scheduleSub scheduleReindexation(pid, user.getLoginname(), user.getLoginname(), "OBJECT_AND_CHILDREN", false, pid);
            return Response.ok().build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (DistributedLocksException e) {
            handleWorkMode(e);
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    /**
     * Low level deletion from Repository and Search index.
     * Relations pointing to this object from another objects will be NOT automatically removed.
     * I.e. this operation can cause incorrect state of data in Repository (according to Kramerius) and Search index.
     * <p>
     * For example:
     * <p>
     * before deletion:
     * Periodical1 --hasVolume--> Volume1 --hasItem--> Issue1 --hasPage--> Page1
     * <p>
     * after deletion of issue1:
     * - Issue1 will be removed from Akubra (FOXML, managed datastreams)
     * - Issue1 will be removed from Processing index
     * - relations to and from Issue1 will be removed from Processing index
     * - Volume1 will still reference Issue1 in it's FOXML (with hasItem)
     * - Page1 will have incorrect data in Search Index (pid paths etc). But after reindexation this will be corrected will lose any connection to Periodical1, Volume1, Issue!1.
     *
     * @param pid
     * @return
     */
    @DELETE
    @Path("{pid}")
    public Response deleteObject(@PathParam("pid") String pid) {
        try {
            checkReadOnlyWorkMode();
            checkSupportedObjectPid(pid);
            //authentication
            User user = this.userProvider.get();
            List<String> roles = Arrays.stream(user.getGroups()).map(Role::getName).collect(Collectors.toList());
            //authorization
            if (!userIsAllowedToRead(this.rightsResolver, user, pid)) {
                // request doesnt contain user principal
                throw new ForbiddenException("user '%s' is not allowed to do this (missing action '%s')", user, SecuredActions.A_ADMIN_READ.name()); //403
            }

            checkObjectExists(pid);
            String model = akubraRepository.pi().getModel(pid);
            //other objects can reference images belonging to other objects (pages),
            //some of the reference are managed, so deleting for example collection should not include deleting file with thumbnail
            boolean deleteManagedDatastreamsData = "page".equals(model);
            akubraRepository.delete(pid, deleteManagedDatastreamsData, true);
            //remove object from Search index (directly, without scheduling process)
            deleteFromSearchIndex(pid);
            return Response.ok().build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (DistributedLocksException e) {
            handleWorkMode(e);
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @HEAD
    @Path("{pid}/streams/{dsid}")
    public Response checkDatastreamExists(@PathParam("pid") String pid, @PathParam("dsid") String dsid) {
        try {
            checkSupportedObjectPid(pid);
            //authentication
            User user = this.userProvider.get();
            List<String> roles = Arrays.stream(user.getGroups()).map(Role::getName).collect(Collectors.toList());
            //authorization
            if (!userIsAllowedToRead(this.rightsResolver, user, pid)) {
                // request doesnt contain user principal
                throw new ForbiddenException("user '%s' is not allowed to do this (missing action '%s')", user, SecuredActions.A_ADMIN_READ.name()); //403
            }

            checkObjectAndDatastreamExist(pid, dsid);
            return Response.ok().build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    /**
     * Returns mimetype of given datastream, if the object and datastream exist
     *
     * @param pid
     * @param dsid
     * @return
     */
    @GET
    @Path("{pid}/streams/{dsid}/mime")
    public Response getDatastreamMime(@PathParam("pid") String pid, @PathParam("dsid") String dsid) {
        try {
            checkSupportedObjectPid(pid);
            //authentication
            User user = this.userProvider.get();
            List<String> roles = Arrays.stream(user.getGroups()).map(Role::getName).collect(Collectors.toList());
            //authorization
            if (!userIsAllowedToRead(this.rightsResolver, user, pid)) {
                // request doesnt contain user principal
                throw new ForbiddenException("user '%s' is not allowed to do this (missing action '%s')", user, SecuredActions.A_ADMIN_READ.name()); //403
            }

            checkObjectAndDatastreamExist(pid, dsid);
            switch (dsid) {
                case "IMG_FULL": {
                    String mime = akubraRepository.getDatastreamMetadata(pid, dsid).getMimetype();
                    if (mime == null) {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    } else {
                        return Response.ok()
                                .type(MediaType.TEXT_PLAIN + ";charset=utf-8")
                                .entity(mime)
                                .build();
                    }
                }
                case "IMG_THUMB": {
                    String mime = akubraRepository.getDatastreamMetadata(pid, dsid).getMimetype();
                    if (mime == null) {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    } else {
                        return Response.ok()
                                .type(MediaType.TEXT_PLAIN + ";charset=utf-8")
                                .entity(mime)
                                .build();
                    }
                }
                case "IMG_PREVIEW": {
                    String mime = akubraRepository.getDatastreamMetadata(pid, dsid).getMimetype();
                    if (mime == null) {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    } else {
                        return Response.ok()
                                .type(MediaType.TEXT_PLAIN + ";charset=utf-8")
                                .entity(mime)
                                .build();
                    }
                }
                //TODO: other streams where it makes sense
                default:
                    return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{pid}/streams/{dsid}")
    public Response getDatastream(@PathParam("pid") String pid, @PathParam("dsid") String dsid) {
        return getDatastreamImpl(pid, dsid);
    }

    /**
     * This method is necessary only because Jersey doesn't recognize getDatastream for BIBLIO_MODS when setMODS also exist.
     * I.e. strange collision between {pid}/streams/{dsid} and {pid}/streams/BIBLIO_MODS
     *
     * @param pid
     * @return
     */
    @GET
    @Path("{pid}/streams/BIBLIO_MODS")
    public Response getDatastreamMods(@PathParam("pid") String pid) {
        return getDatastreamImpl(pid, "BIBLIO_MODS");
    }

    private Response getDatastreamImpl(String pid, String dsId) {
        try {
            checkSupportedObjectPid(pid);
            //authentication
            User user = this.userProvider.get();
            List<String> roles = Arrays.stream(user.getGroups()).map(Role::getName).collect(Collectors.toList());
            //authorization
            if (!userIsAllowedToRead(this.rightsResolver, user, pid)) {
                // request doesnt contain user principal
                throw new ForbiddenException("user '%s' is not allowed to do this (missing action '%s')", user, SecuredActions.A_ADMIN_READ.name()); //403
            }

            checkObjectAndDatastreamExist(pid, dsId);
            switch (dsId) {
                case "BIBLIO_MODS":
                    if (akubraRepository.datastreamExists(pid, KnownDatastreams.BIBLIO_MODS)) {
                        Document document = akubraRepository.getDatastreamContent(pid, KnownDatastreams.BIBLIO_MODS).asDom4j(true);
                        return Response.ok()
                                .type(MediaType.APPLICATION_XML + ";charset=utf-8")
                                .entity(document.asXML())
                                .build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                case "DC":
                    if (akubraRepository.datastreamExists(pid, KnownDatastreams.BIBLIO_DC)) {
                        Document document = akubraRepository.getDatastreamContent(pid, KnownDatastreams.BIBLIO_DC).asDom4j(true);
                        return Response.ok()
                                .type(MediaType.APPLICATION_XML + ";charset=utf-8")
                                .entity(document.asXML())
                                .build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }

                case "RELS-EXT":
                    Document relsext = akubraRepository.re().get(pid).asDom4j(true);
                    return Response.ok()
                            .type(MediaType.APPLICATION_XML + ";charset=utf-8")
                            .entity(relsext.asXML())
                            .build();
                case "TEXT_OCR":
                    if (akubraRepository.datastreamExists(pid, KnownDatastreams.OCR_TEXT)) {
                        String ocr = akubraRepository.getDatastreamContent(pid, KnownDatastreams.OCR_TEXT).asString();
                        return Response.ok()
                                .type(MediaType.TEXT_PLAIN + ";charset=utf-8")
                                .entity(ocr)
                                .build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }

                case "ALTO":
                    if (akubraRepository.datastreamExists(pid, KnownDatastreams.OCR_ALTO)) {
                        Document altoDocument = akubraRepository.getDatastreamContent(pid, KnownDatastreams.OCR_ALTO).asDom4j(true);
                        return Response.ok()
                                .type(MediaType.APPLICATION_XML + ";charset=utf-8")
                                .entity(altoDocument.asXML())
                                .build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                case "IMG_FULL": {
                    if (akubraRepository.datastreamExists(pid, KnownDatastreams.IMG_FULL)) {
                        String mimeType = akubraRepository.getDatastreamMetadata(pid, KnownDatastreams.IMG_FULL).getMimetype();
                        InputStream is = akubraRepository.getDatastreamContent(pid, KnownDatastreams.IMG_FULL).asInputStream();
                        StreamingOutput stream = output -> {
                            IOUtils.copy(is, output);
                            IOUtils.closeQuietly(is);
                        };
                        return Response.ok().entity(stream).type(mimeType).build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                }
                case "IMG_THUMB": {
                    if (akubraRepository.datastreamExists(pid, KnownDatastreams.IMG_THUMB)) {
                        String mimeType = akubraRepository.getDatastreamMetadata(pid, KnownDatastreams.IMG_THUMB).getMimetype();
                        InputStream is = akubraRepository.getDatastreamContent(pid, KnownDatastreams.IMG_THUMB).asInputStream();
                        StreamingOutput stream = output -> {
                            IOUtils.copy(is, output);
                            IOUtils.closeQuietly(is);
                        };
                        return Response.ok().entity(stream).type(mimeType).build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                }
                case "IMG_PREVIEW": {
                    if (akubraRepository.datastreamExists(pid, KnownDatastreams.IMG_PREVIEW)) {
                        String mimeType = akubraRepository.getDatastreamMetadata(pid, KnownDatastreams.IMG_PREVIEW).getMimetype();
                        InputStream is = akubraRepository.getDatastreamContent(pid, KnownDatastreams.IMG_PREVIEW).asInputStream();
                        StreamingOutput stream = output -> {
                            IOUtils.copy(is, output);
                            IOUtils.closeQuietly(is);
                        };
                        return Response.ok().entity(stream).type(mimeType).build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                }
                //TODO: MP3, OGG, WAV
                //TODO: POLICY
                //TODO: MIGRATION
                default:
                    return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }


    // TODO AK_NEW nahradit novym REST API - zeptat se Lukase
    @PUT
    @Path("{pid}/akubra/updateManaged/{dsid}")
    @Consumes({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public Response streamHead(@PathParam("pid") String pid,
                               @PathParam("dsid") String dsid,
                               @QueryParam("namespace") String xmlnamespace,
                               InputStream inputStream,
                               @Context HttpHeaders headers) throws IOException {

        //authentication
        User user = this.userProvider.get();

        if (!userIsAllowedToReadOrObjectEdit(this.rightsResolver, user, pid)) {
            // request doesnt contain user principal
            throw new ForbiddenException("user '%s' is not allowed to do this (missing actions '%s', '%s')", user, SecuredActions.A_ADMIN_READ.name(), SecuredActions.A_OBJECT_EDIT.name()); //403
        }

        try {
            checkReadOnlyWorkMode();
            List<String> requestHeader = headers.getRequestHeader("Content-Type");
            if (requestHeader.size() > 0) {
                String mimeType = requestHeader.get(0);
                if (mimeType.equals(MediaType.APPLICATION_XML)) {
                    Document dom4j = Dom4jUtils.streamToDocument(inputStream, true);
                    ByteArrayInputStream bis = new ByteArrayInputStream(dom4j.asXML().getBytes(Charset.forName("UTF-8")));
                    akubraRepository.updateXMLDatastream(pid, dsid, "text/xml", bis);
                    return Response.status(Response.Status.OK).build();

                } else if (mimeType.equals(MediaType.APPLICATION_OCTET_STREAM)) {
                    byte[] stream = IOUtils.toByteArray(inputStream);
                    ByteArrayInputStream bis = new ByteArrayInputStream(stream);
                    akubraRepository.updateManagedDatastream(pid, dsid, mimeType, bis);
                    return Response.status(Response.Status.OK).build();
                } else if (mimeType.equals(MediaType.APPLICATION_JSON)) {
                    byte[] stream = IOUtils.toByteArray(inputStream);
                    ByteArrayInputStream bis = new ByteArrayInputStream(stream);
                    akubraRepository.updateManagedDatastream(pid, dsid, mimeType, bis);
                    return Response.status(Response.Status.OK).build();
                } else {
                    byte[] stream = IOUtils.toByteArray(inputStream);
                    ByteArrayInputStream bis = new ByteArrayInputStream(stream);
                    akubraRepository.updateManagedDatastream(pid, dsid, mimeType, bis);
                    return Response.status(Response.Status.OK).build();
                }
            } else {
                byte[] stream = IOUtils.toByteArray(inputStream);
                ByteArrayInputStream bis = new ByteArrayInputStream(stream);
                akubraRepository.updateManagedDatastream(pid, dsid, MediaType.APPLICATION_OCTET_STREAM.toString(), bis);
                return Response.status(Response.Status.OK).build();
            }
        } catch (DistributedLocksException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            handleWorkMode(e);
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new BadRequestException(e.getMessage());
        }
    }


    // TODO AK_NEW: Delete a zjistit, zda to nekdo pouziva
    @PUT
    @Path("{pid}/streams/IMG_THUMB")
    public Response setImgThumb(@PathParam("pid") String targetPid, @QueryParam("srcPid") String sourcePid) {
        try {
            checkReadOnlyWorkMode();
            //authentication
            User user = this.userProvider.get();
            //authorization
            if (!userIsAllowedToReadOrObjectEdit(this.rightsResolver, user, targetPid)) {
                // request doesnt contain user principal
                throw new ForbiddenException("user '%s' is not allowed to do this (missing actions '%s', '%s' )", user, SecuredActions.A_ADMIN_READ.name(), SecuredActions.A_OBJECT_EDIT.name()); //403
            }

            //check target object
            checkSupportedObjectPid(targetPid);
            checkObjectExists(targetPid);
            String targetModel = akubraRepository.pi().getModel(targetPid);
            if ("page".equals(targetModel)) {
                throw new BadRequestException("target's model cannot be page (target is %s)", targetPid);
            }
            //check source object
            if (sourcePid == null || sourcePid.isEmpty()) {
                throw new BadRequestException("missing mandatory query param 'srcPid'");
            }
            checkSupportedObjectPid(sourcePid);
            checkObjectExists(sourcePid);
            boolean thumbExists = akubraRepository.datastreamExists(sourcePid, KnownDatastreams.IMG_THUMB);
            if (!thumbExists) {
                throw new BadRequestException("src pid (%s) must contain IMG_THUMB stream", sourcePid);
            }

            //copy whole datastream xml, with all datastreamVersions; datastreamVersion from repository always contains reference reference:
            //exterenal with CONTROL_GROUP="E" and contentLocation TYPE="URL"
            // or
            // internal with CONTROL_GROUP="M" and contentLocation TYPE="INTERNAL_ID"
            Document srcThumbDs = akubraRepository.doWithReadLock(sourcePid, () -> {
                DigitalObject object = akubraRepository.get(sourcePid).asDigitalObject();
                if (object.getDatastream().stream().anyMatch(dataStreamType -> dataStreamType.getID().equals(KnownDatastreams.IMG_THUMB.toString()))) {
                    Document foxml = Dom4jUtils.streamToDocument(akubraRepository.marshall(object), true);
                    Element dcEl = (Element) Dom4jUtils.buildXpath(String.format("/foxml:digitalObject/foxml:datastream[@ID='%s']", KnownDatastreams.IMG_THUMB)).selectSingleNode(foxml);
                    Element detached = (Element) dcEl.detach();
                    Document result = DocumentHelper.createDocument();
                    result.add(detached);
                    return result;
                } else {
                    return null;
                }
            });
            akubraRepository.doWithWriteLock(targetPid, () -> {
                Document foxml = akubraRepository.get(targetPid).asDom4j(true);
                Element originalDsEl = (Element) Dom4jUtils.buildXpath(String.format("/foxml:digitalObject/foxml:datastream[@ID='%s']", KnownDatastreams.IMG_THUMB)).selectSingleNode(foxml);
                if (originalDsEl != null) {
                    originalDsEl.detach();
                }
                foxml.getRootElement().add(srcThumbDs.getRootElement().detach());
                Dom4jUtils.updateLastModifiedTimestamp(foxml);
                DigitalObject updatedDigitalObject = akubraRepository.unmarshall(new ByteArrayInputStream(foxml.asXML().getBytes(StandardCharsets.UTF_8)));
                akubraRepository.delete(targetPid, false, false);
                akubraRepository.ingest(updatedDigitalObject);
                akubraRepository.pi().commit();;
                return null;
            });
            return Response.ok().build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (DistributedLocksException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            handleWorkMode(e);
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @PUT
    @Path("{pid}/streams/BIBLIO_MODS")
    @Consumes(MediaType.APPLICATION_XML)
    public Response setMODS(@PathParam("pid") String pid, InputStream xml) {
        try {
            checkReadOnlyWorkMode();
            //authentication
            User user = this.userProvider.get();
            List<String> roles = Arrays.stream(user.getGroups()).map(Role::getName).collect(Collectors.toList());
            //authorization
            if (!userIsAllowedToRead(this.rightsResolver, user, pid)) {
                // request doesnt contain user principal
                throw new ForbiddenException("user '%s' is not allowed to do this (missing action '%s')", user, SecuredActions.A_ADMIN_READ.name()); //403
            }

            //check target object
            checkSupportedObjectPid(pid);
            checkObjectExists(pid);
            Document mods = Dom4jUtils.streamToDocument(xml, true);
            ByteArrayInputStream bis = new ByteArrayInputStream(mods.asXML().getBytes(Charset.forName("UTF-8")));
            akubraRepository.updateXMLDatastream(pid, KnownDatastreams.BIBLIO_MODS.name(), "text/xml", bis);
            return Response.ok().build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (DistributedLocksException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            handleWorkMode(e);
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }


    private boolean userIsAllowedToReadOrObjectEdit(RightsResolver rightsResolver, User user, String pid) throws IOException {
        checkSupportedObjectPid(pid);
        ObjectPidsPath[] paths = this.solrAccess.getPidPaths(pid);
        if (paths.length == 0) {
            throw new InternalErrorException("illegal state: no paths for object %s found in search index", pid);
        }
        for (int i = 0; i < paths.length; i++) {
            ObjectPidsPath path = paths[i];
            boolean adminReadResult = rightsResolver.isActionAllowed(user, SecuredActions.A_ADMIN_READ.getFormalName(), pid, null, path.injectRepository()).flag();
            boolean objectEditResult = false;
            if (!adminReadResult) {
                objectEditResult = rightsResolver.isActionAllowed(user, SecuredActions.A_OBJECT_EDIT.getFormalName(), pid, null, path.injectRepository()).flag();
            }

            if (adminReadResult || objectEditResult) {
                return true;
            }
        }
        return false;
    }

    private boolean userIsAllowedToRead(RightsResolver rightsResolver, User user, String pid) throws IOException {
        checkSupportedObjectPid(pid);
        ObjectPidsPath[] paths = this.solrAccess.getPidPaths(pid);
        if (paths.length == 0) {
            throw new InternalErrorException("illegal state: no paths for object %s found in search index", pid);
        }
        for (int i = 0; i < paths.length; i++) {
            ObjectPidsPath path = paths[i];
            if (rightsResolver.isActionAllowed(user, SecuredActions.A_ADMIN_READ.getFormalName(), pid, null, path.injectRepository()).flag()) {
                return true;
            }
        }
        return false;
    }

}
