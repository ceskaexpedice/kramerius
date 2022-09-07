package cz.incad.kramerius.rest.apiNew.admin.v70;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.imaging.ImageStreams;
import cz.incad.kramerius.repository.RepositoryApi;
import cz.incad.kramerius.repository.utils.Utils;
import cz.incad.kramerius.rest.apiNew.exceptions.BadRequestException;
import cz.incad.kramerius.rest.apiNew.exceptions.ForbiddenException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.security.EvaluatingResultState;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.RightsReturnObject;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.Dom4jUtils;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.java.Pair;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Path("/admin/v7.0/items")
public class ItemsResource extends AdminApiResource {

    public static Logger LOGGER = Logger.getLogger(ItemsResource.class.getName());

    private static final Integer DEFAULT_OFFSET = 0;
    private static final Integer DEFAULT_LIMIT = 10;

    //TODO: prejmenovat role podle spravy uctu
    
    /*
    private static final String ROLE_READ_ITEMS = "kramerius_admin";
    private static final String ROLE_READ_FOXML = "kramerius_admin";
    private static final String ROLE_EDIT_OBJECTS = "kramerius_admin";
    private static final String ROLE_DELETE_OBJECTS = "kramerius_admin";
    */
    

    @javax.inject.Inject
    Provider<User> userProvider;

    @Inject
    private FoxmlBuilder foxmlBuilder;

    @Inject
    @Named("new-index")
    private SolrAccess solrAccess;
    

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
                throw new ForbiddenException("user '%s' is not allowed to do this (missing action '%s')", requestProvider.get().getUserPrincipal().getName(), SecuredActions.A_ADMIN_READ.name()); //403
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
            RepositoryApi.TitlePidPairs titlePidPairsByModel = cursor != null ?
                    krameriusRepositoryApi.getLowLevelApi().getPidsOfObjectsWithTitlesByModelWithCursor(model, ascendingOrder, cursor, limitInt) :
                    krameriusRepositoryApi.getLowLevelApi().getPidsOfObjectsWithTitlesByModel(model, ascendingOrder, offsetInt, limitInt);
            JSONObject json = new JSONObject();
            json.put("model", model);
            if (titlePidPairsByModel.nextCursorMark != null) {
                json.put("nextCursor", titlePidPairsByModel.nextCursorMark);
            }
            JSONArray items = new JSONArray();
            for (Pair<String, String> pidAndTitle : titlePidPairsByModel.titlePidPairs) {
                JSONObject item = new JSONObject();
                item.put("title", pidAndTitle.getFirst());
                item.put("pid", pidAndTitle.getSecond());
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
                throw new ForbiddenException("user '%s' is not allowed to do this (missing action '%s')", requestProvider.get().getUserPrincipal().getName(), SecuredActions.A_ADMIN_READ.name()); //403
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
    @Path("{pid}/foxml")
    @Produces(MediaType.APPLICATION_XML)
    public Response getFoxml(@PathParam("pid") String pid) {
        try {
            checkSupportedObjectPid(pid);
            //authentication
            User user = this.userProvider.get();
            List<String> roles = Arrays.stream(user.getGroups()).map(Role::getName).collect(Collectors.toList());
            if (!userIsAllowedToRead(this.rightsResolver, user, pid)) {
                // request doesnt contain user principal
                throw new ForbiddenException("user '%s' is not allowed to do this (missing action '%s')", requestProvider.get().getUserPrincipal().getName(), SecuredActions.A_ADMIN_READ.name()); //403
            }
            checkObjectExists(pid);
            Document foxml = krameriusRepositoryApi.getLowLevelApi().getFoxml(pid);
            return Response.ok().entity(foxml.asXML()).build();
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
                throw new ForbiddenException("user '%s' is not allowed to do this (missing action '%s')", requestProvider.get().getUserPrincipal().getName(), SecuredActions.A_ADMIN_READ.name()); //403
            }

            checkObjectExists(pid);

            Document relsExt = krameriusRepositoryApi.getRelsExt(pid, true);
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
            checkSupportedObjectPid(pid);
            //authentication
            User user = this.userProvider.get();
            List<String> roles = Arrays.stream(user.getGroups()).map(Role::getName).collect(Collectors.toList());
            //authorization
            if (!userIsAllowedToRead(this.rightsResolver, user, pid)) {
                // request doesnt contain user principal
                throw new ForbiddenException("user '%s' is not allowed to do this (missing action '%s')", requestProvider.get().getUserPrincipal().getName(), SecuredActions.A_ADMIN_READ.name()); //403
            }
            checkObjectExists(pid);

            //extract childrens' pids from request
            List<String> newChildrenOrderPids = new ArrayList<>();
            for (int i = 0; i < newChildrenOrder.getJSONArray("childrenPids").length(); i++) {
                newChildrenOrderPids.add(newChildrenOrder.getJSONArray("childrenPids").getString(i));
            }
            //extract childrens' pids an relations from rels-ext
            Map<String, String> foxmlChildrenPidToRelationName = new HashMap<>();
            Document relsExt = krameriusRepositoryApi.getRelsExt(pid, true);
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
            krameriusRepositoryApi.updateRelsExt(pid, relsExt);

            scheduleReindexation(pid, user.getLoginname(), user.getLoginname(), "OBJECT_AND_CHILDREN", true, pid);
            return Response.ok().build();
        } catch (WebApplicationException e) {
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
            checkSupportedObjectPid(pid);
            //authentication
            User user = this.userProvider.get();
            List<String> roles = Arrays.stream(user.getGroups()).map(Role::getName).collect(Collectors.toList());
            //authorization
            if (!userIsAllowedToRead(this.rightsResolver, user, pid)) {
                // request doesnt contain user principal
                throw new ForbiddenException("user '%s' is not allowed to do this (missing action '%s')", requestProvider.get().getUserPrincipal().getName(), SecuredActions.A_ADMIN_READ.name()); //403
            }

            checkObjectExists(pid);
            String model = krameriusRepositoryApi.getModel(pid);
            //other objects can reference images belonging to other objects (pages),
            //some of the reference are managed, so deleting for example collection should not include deleting file with thumbnail
            boolean deleteManagedDatastreamsData = "page".equals(model);
            krameriusRepositoryApi.getLowLevelApi().deleteObject(pid, deleteManagedDatastreamsData);
            //remove object from Search index (directly, without scheduling process)
            deleteFromSearchIndex(pid);
            return Response.ok().build();
        } catch (WebApplicationException e) {
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
                throw new ForbiddenException("user '%s' is not allowed to do this (missing action '%s')", requestProvider.get().getUserPrincipal().getName(), SecuredActions.A_ADMIN_READ.name()); //403
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
                throw new ForbiddenException("user '%s' is not allowed to do this (missing action '%s')", requestProvider.get().getUserPrincipal().getName(), SecuredActions.A_ADMIN_READ.name()); //403
            }

            checkObjectAndDatastreamExist(pid, dsid);
            switch (dsid) {
                case "IMG_FULL": {
                    String mime = krameriusRepositoryApi.getImgFullMimetype(pid);
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
                    String mime = krameriusRepositoryApi.getImgThumbMimetype(pid);
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
                    String mime = krameriusRepositoryApi.getImgPreviewMimetype(pid);
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
                throw new ForbiddenException("user '%s' is not allowed to do this (missing action '%s')", requestProvider.get().getUserPrincipal().getName(), SecuredActions.A_ADMIN_READ.name()); //403
            }

            checkObjectAndDatastreamExist(pid, dsId);
            switch (dsId) {
                case "BIBLIO_MODS":
                    return Response.ok()
                            .type(MediaType.APPLICATION_XML + ";charset=utf-8")
                            .entity(krameriusRepositoryApi.getMods(pid, true).asXML())
                            .build();
                case "DC":
                    return Response.ok()
                            .type(MediaType.APPLICATION_XML + ";charset=utf-8")
                            .entity(krameriusRepositoryApi.getDublinCore(pid, true).asXML())
                            .build();
                case "RELS-EXT":
                    return Response.ok()
                            .type(MediaType.APPLICATION_XML + ";charset=utf-8")
                            .entity(krameriusRepositoryApi.getRelsExt(pid, true).asXML())
                            .build();
                case "TEXT_OCR":
                    return Response.ok()
                            .type(MediaType.TEXT_PLAIN + ";charset=utf-8")
                            .entity(krameriusRepositoryApi.getOcrText(pid))
                            .build();
                case "ALTO":
                    return Response.ok()
                            .type(MediaType.APPLICATION_XML + ";charset=utf-8")
                            .entity(krameriusRepositoryApi.getOcrAlto(pid, true).asXML())
                            .build();
                case "IMG_FULL": {
                    String mimeType = krameriusRepositoryApi.getImgFullMimetype(pid);
                    InputStream is = krameriusRepositoryApi.getImgFull(pid);
                    StreamingOutput stream = output -> {
                        IOUtils.copy(is, output);
                        IOUtils.closeQuietly(is);
                    };
                    return Response.ok().entity(stream).type(mimeType).build();
                }
                case "IMG_THUMB": {
                    String mimeType = krameriusRepositoryApi.getImgThumbMimetype(pid);
                    InputStream is = krameriusRepositoryApi.getImgThumb(pid);
                    StreamingOutput stream = output -> {
                        IOUtils.copy(is, output);
                        IOUtils.closeQuietly(is);
                    };
                    return Response.ok().entity(stream).type(mimeType).build();
                }
                case "IMG_PREVIEW": {
                    String mimeType = krameriusRepositoryApi.getImgPreviewMimetype(pid);
                    InputStream is = krameriusRepositoryApi.getImgPreview(pid);
                    StreamingOutput stream = output -> {
                        IOUtils.copy(is, output);
                        IOUtils.closeQuietly(is);
                    };
                    return Response.ok().entity(stream).type(mimeType).build();
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

    @PUT
    @Path("{pid}/streams/IMG_THUMB")
    public Response setImgThumb(@PathParam("pid") String targetPid, @QueryParam("srcPid") String sourcePid) {
        try {
            //authentication
            User user = this.userProvider.get();
            List<String> roles = Arrays.stream(user.getGroups()).map(Role::getName).collect(Collectors.toList());
            //authorization
            
            if (!userIsAllowedToRead(this.rightsResolver, user, targetPid)) {
                // request doesnt contain user principal
                throw new ForbiddenException("user '%s' is not allowed to do this (missing action '%s')", requestProvider.get().getUserPrincipal().getName(), SecuredActions.A_ADMIN_READ.name()); //403
            }
            
            //check target object
            checkSupportedObjectPid(targetPid);
            checkObjectExists(targetPid);
            String targetModel = krameriusRepositoryApi.getModel(targetPid);
            if ("page".equals(targetModel)) {
                throw new BadRequestException("target's model cannot be page (target is %s)", targetPid);
            }
            //check source object
            if (sourcePid == null || sourcePid.isEmpty()) {
                throw new BadRequestException("missing mandatory query param 'srcPid'");
            }
            checkSupportedObjectPid(sourcePid);
            checkObjectExists(sourcePid);
            String sourceModel = krameriusRepositoryApi.getModel(sourcePid);
            if (!"page".equals(sourceModel)) {
                throw new BadRequestException("source's model must be page (source is %s with model:%s)", targetPid, sourceModel);
            }
            //copy whole datastream xml, with all datastreamVersions; datastreamVersion from repository always contains reference reference:
            //exterenal with CONTROL_GROUP="E" and contentLocation TYPE="URL"
            // or
            // internal with CONTROL_GROUP="M" and contentLocation TYPE="INTERNAL_ID"
            Document srcThumbDs = krameriusRepositoryApi.getLowLevelApi().getDatastreamXml(sourcePid, "IMG_THUMB");
            krameriusRepositoryApi.getLowLevelApi().setDatastreamXml(targetPid, "IMG_THUMB", srcThumbDs);
            return Response.ok().build();
        } catch (WebApplicationException e) {
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
            //authentication
            User user = this.userProvider.get();
            List<String> roles = Arrays.stream(user.getGroups()).map(Role::getName).collect(Collectors.toList());
            //authorization
            if (!userIsAllowedToRead(this.rightsResolver, user, pid)) {
                // request doesnt contain user principal
                throw new ForbiddenException("user '%s' is not allowed to do this (missing action '%s')", requestProvider.get().getUserPrincipal().getName(), SecuredActions.A_ADMIN_READ.name()); //403
            }
            
            //check target object
            checkSupportedObjectPid(pid);
            checkObjectExists(pid);
            Document mods = Utils.inputstreamToDocument(xml, true);
            krameriusRepositoryApi.updateMods(pid, mods);
            return Response.ok().build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }


    private boolean userIsAllowedToRead(RightsResolver rightsResolver, User user, String pid) throws IOException {
        checkSupportedObjectPid(pid);
        ObjectPidsPath[] paths = this.solrAccess.getPidPaths(pid);
        if (paths.length == 0) {
            throw new InternalErrorException("illegal state: no paths for object %s found in search index", pid);
            //or maybe build paths from resource/processing index
            //but user should not access page before it is indexed anyway
            //so eventual consistency vs. "API doesn't (at least seemingly) depend on search index"
        }
        for (int i = 0; i < paths.length; i++) {
            ObjectPidsPath path = paths[i];
            if (rightsResolver.isActionAllowed(user, SecuredActions.A_ADMIN_READ .getFormalName(), pid, null, path.injectRepository()).flag()) {
                return true;
            }
        }
        return false;
    }

}
