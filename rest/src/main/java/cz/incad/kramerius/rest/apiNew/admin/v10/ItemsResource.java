package cz.incad.kramerius.rest.apiNew.admin.v10;

import cz.incad.kramerius.cdk.ChannelUtils;
import cz.incad.kramerius.repository.RepositoryApi;
import cz.incad.kramerius.repository.utils.Utils;
import cz.incad.kramerius.rest.api.k5.client.utils.UsersUtils;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.OneInstance;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.OneInstance.InstanceType;
import cz.incad.kramerius.rest.apiNew.exceptions.BadRequestException;
import cz.incad.kramerius.rest.apiNew.exceptions.ForbiddenException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.java.Pair;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.sun.jersey.api.client.Client;

import javax.inject.Provider;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Path("/admin/v7.0/items")
public class ItemsResource extends AdminApiResource {

    public static Logger LOGGER = Logger.getLogger(ItemsResource.class.getName());

    private static final Integer DEFAULT_OFFSET = 0;
    private static final Integer DEFAULT_LIMIT = 10;

    //TODO: prejmenovat role podle spravy uctu
    private static final String ROLE_READ_ITEMS = "kramerius_admin";
    private static final String ROLE_READ_FOXML = "kramerius_admin";
    private static final String ROLE_DELETE_OBJECTS = "kramerius_admin";
    private static final String ROLE_EDIT_OBJECTS = "kramerius_admin";



    @javax.inject.Inject
    Provider<User> userProvider;

    @Inject
    private Instances libraries;

    private Client client;
    
    
    public ItemsResource() {
        super();
        this.client = Client.create();
    }

    /**
     * Returns array of pids (with titles) that have given model. Only partial array with offset & limit.
     * All top-level objects without model specification cannot be returned here, because this information (being top-level) is not available from resource index.
     * Instead it is derived during indexation process and stored in search index.
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

            User user1 = this.userProvider.get();
            List<String> roles = Arrays.stream(user1.getGroups()).map(Role::getName).collect(Collectors.toList());
            //authorization
            String role = ROLE_READ_ITEMS;
            if (!roles.contains(role)) {
                                                                                                        // request doesnt contain user principal
                throw new ForbiddenException("user '%s' is not allowed to do this (missing role '%s')", requestProvider.get().getUserPrincipal().getName(), role); //403
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
            User user1 = this.userProvider.get();
            List<String> roles = Arrays.stream(user1.getGroups()).map(Role::getName).collect(Collectors.toList());
            //authorization
            String role = ROLE_READ_ITEMS;
            if (!roles.contains(role)) {
                throw new ForbiddenException("user '%s' is not allowed to do this (missing role '%s')", user1.getLoginname(), role); //403
            }

            //checkObjectExists(pid);
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
        JSONObject obj = new JSONObject();
        List<OneInstance> instances = libraries.enabledInstances();
        for(OneInstance inst:instances) {
            String library = inst.getName();
            boolean channelAccess = KConfiguration.getInstance().getConfiguration().containsKey("cdk.collections.sources." + library + ".licenses") ?  KConfiguration.getInstance().getConfiguration().getBoolean("cdk.collections.sources." + library + ".licenses") : false;
            if(channelAccess) {
                String channel = KConfiguration.getInstance().getConfiguration().getString("cdk.collections.sources." + library + ".forwardurl");
                String solrChannelUrl = ChannelUtils.solrChannelUrl(inst.getInstanceType().name(), channel);
                InstanceType instType = inst.getInstanceType();
                String solrPid = ChannelUtils.solrChannelPid(this.client, channel, solrChannelUrl, instType.name(), pid);
                if (solrPid != null) {
                    obj.put(library, new JSONObject(solrPid));
                }
            }
        }
        return Response.ok(obj.toString()).build();
    }
    
    
    @GET
    @Path("{pid}/foxml")
    @Produces(MediaType.APPLICATION_XML)
    public Response getFoxml(@PathParam("pid") String pid) {
        try {
            checkSupportedObjectPid(pid);
            //authentication
            User user1 = this.userProvider.get();
            List<String> roles = Arrays.stream(user1.getGroups()).map(Role::getName).collect(Collectors.toList());
            //authorization
            String role = ROLE_READ_FOXML;
            if (!roles.contains(role)) {
                throw new ForbiddenException("user '%s' is not allowed to do this (missing role '%s')", user1.getLoginname(), role); //403
            }

            //checkObjectExists(pid);
            Document foxml = krameriusRepositoryApi.getLowLevelApi().getFoxml(pid);
            return Response.ok().entity(foxml.asXML()).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @DELETE
    @Path("{pid}")
    public Response deleteObject(@PathParam("pid") String pid) {
        try {
            checkSupportedObjectPid(pid);
            //authentication
            User user1 = this.userProvider.get();
            List<String> roles = Arrays.stream(user1.getGroups()).map(Role::getName).collect(Collectors.toList());
            //authorization
            String role = ROLE_DELETE_OBJECTS;
            if (!roles.contains(role)) {
                throw new ForbiddenException("user '%s' is not allowed to do this (missing role '%s')", user1.getLoginname(), role); //403
            }

            //checkObjectExists(pid);
            String model = krameriusRepositoryApi.getModel(pid);
            //other objects can reference images belonging to other objects (pages),
            //some of the reference are managed, so deleting for example collection should not include deleting file with thumbnail
            boolean deleteManagedDatastreamsData = "page".equals(model);
            krameriusRepositoryApi.getLowLevelApi().deleteObject(pid, deleteManagedDatastreamsData);
            //TODO: schedule indexation of the affected objects
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
            User user1 = this.userProvider.get();
            List<String> roles = Arrays.stream(user1.getGroups()).map(Role::getName).collect(Collectors.toList());
            //authorization
            String role = ROLE_READ_FOXML;
            if (!roles.contains(role)) {
                throw new ForbiddenException("user '%s' is not allowed to do this (missing role '%s')", user1.getLoginname(), role); //403
            }

            //	"image": {
            //checkObjectAndDatastreamExist(pid, dsid);
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
            User user1 = this.userProvider.get();
            List<String> roles = Arrays.stream(user1.getGroups()).map(Role::getName).collect(Collectors.toList());
            //authorization
            String role = ROLE_READ_FOXML;
            if (!roles.contains(role)) {
                throw new ForbiddenException("user '%s' is not allowed to do this (missing role '%s')", user1.getLoginname(), role); //403
            }

            //checkObjectAndDatastreamExist(pid, dsid);
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
            User user1 = this.userProvider.get();
            List<String> roles = Arrays.stream(user1.getGroups()).map(Role::getName).collect(Collectors.toList());
            //authorization
            String role = ROLE_READ_FOXML;
            if (!roles.contains(role)) {
                throw new ForbiddenException("user '%s' is not allowed to to do this (missing role '%s')", user1.getLoginname(), role); //403
            }

            //checkObjectAndDatastreamExist(pid, dsId);
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
            User user1 = this.userProvider.get();
            List<String> roles = Arrays.stream(user1.getGroups()).map(Role::getName).collect(Collectors.toList());
            //authorization
            String role = ROLE_EDIT_OBJECTS;
            if (!roles.contains(role)) {
                throw new ForbiddenException("user '%s' is not allowed to to do this (missing role '%s')", user1.getLoginname(), role); //403
            }
            //check target object
            checkSupportedObjectPid(targetPid);
            //checkObjectExists(targetPid);
            String targetModel = krameriusRepositoryApi.getModel(targetPid);
            if ("page".equals(targetModel)) {
                throw new BadRequestException("target's model cannot be page (target is %s)", targetPid);
            }
            //check source object
            if (sourcePid == null || sourcePid.isEmpty()) {
                throw new BadRequestException("missing mandatory query param 'srcPid'");
            }
            checkSupportedObjectPid(sourcePid);
            //checkObjectExists(sourcePid);
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
            User user1 = this.userProvider.get();
            List<String> roles = Arrays.stream(user1.getGroups()).map(Role::getName).collect(Collectors.toList());
            //authorization
            String role = ROLE_EDIT_OBJECTS;
            if (!roles.contains(role)) {
                throw new ForbiddenException("user '%s' is not allowed to to do this (missing role '%s')", user1.getLoginname(), role); //403
            }
            //check target object
            checkSupportedObjectPid(pid);
            //checkObjectExists(pid);
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

}
