package cz.incad.kramerius.rest.apiNew.admin.v10;

import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.rest.apiNew.exceptions.BadRequestException;
import cz.incad.kramerius.rest.apiNew.exceptions.ForbiddenException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.dom4j.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

@Path("/admin/v1.0/items")
public class ItemsResource extends AdminApiResource {

    public static Logger LOGGER = Logger.getLogger(ItemsResource.class.getName());

    //TODO: prejmenovat role podle spravy uctu
    private static final String ROLE_READ_ITEMS = "kramerius_admin";
    private static final String ROLE_READ_FOXML = "kramerius_admin";
    private static final String ROLE_DELETE_OBJECTS = "kramerius_admin";

    /**
     * Returns array of pids that have given model.
     * All top-level objects without model specification cannot be returned here, because this information (being top-level) is not available from resource index.
     * Instead it is derived during indexation process and stored in search index.
     * Accessing this information from search index would violate architecture and possibly cause circular dependency.
     *
     * @param model
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getItems(@QueryParam("model") String model) {
        //TODO: offset, limit, nejspis nebude potreba, see https://app.gethido.com/p/posu5sqvet/tasks/24
        try {
            //authentication
            AuthenticatedUser user = getAuthenticatedUserByOauth();
            //authorization
            String role = ROLE_READ_ITEMS;
            if (!user.getRoles().contains(role)) {
                throw new ForbiddenException("user '%s' is not allowed to do this (missing role '%s')", user.getName(), role); //403
            }

            if (model == null || model.isEmpty()) {
                throw new BadRequestException("missing mandatory query param 'model'");
            }
            List<String> pids = krameriusRepositoryApi.getLowLevelApi().getPidsOfObjectsByModel(model);
            JSONObject json = new JSONObject();
            json.put("model", model);
            json.put("items", new JSONArray(pids));
            return Response.ok(json).build();
        } catch (RepositoryException | IOException | SolrServerException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }

    @HEAD
    @Path("{pid}")
    public Response checkItemExists(@PathParam("pid") String pid) {
        //authentication
        AuthenticatedUser user = getAuthenticatedUserByOauth();
        //authorization
        String role = ROLE_READ_ITEMS;
        if (!user.getRoles().contains(role)) {
            throw new ForbiddenException("user '%s' is not allowed to do this (missing role '%s')", user.getName(), role); //403
        }

        checkObjectExists(pid);
        return Response.ok().build();
    }

    @GET
    @Path("{pid}/foxml")
    @Produces(MediaType.APPLICATION_XML)
    public Response getFoxml(@PathParam("pid") String pid) {
        try {
            //authentication
            AuthenticatedUser user = getAuthenticatedUserByOauth();
            //authorization
            String role = ROLE_READ_FOXML;
            if (!user.getRoles().contains(role)) {
                throw new ForbiddenException("user '%s' is not allowed to do this (missing role '%s')", user.getName(), role); //403
            }

            checkObjectExists(pid);
            Document foxml = krameriusRepositoryApi.getLowLevelApi().getFoxml(pid);
            return Response.ok().entity(foxml.asXML()).build();
        } catch (RepositoryException | IOException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }

    @DELETE
    @Path("{pid}")
    public Response deleteObject(@PathParam("pid") String pid) {
        try {
            //authentication
            AuthenticatedUser user = getAuthenticatedUserByOauth();
            //authorization
            String role = ROLE_DELETE_OBJECTS;
            if (!user.getRoles().contains(role)) {
                throw new ForbiddenException("user '%s' is not allowed to do this (missing role '%s')", user.getName(), role); //403
            }

            checkObjectExists(pid);
            krameriusRepositoryApi.getLowLevelApi().deleteObject(pid);
            //TODO: schedule indexation of the affected objects
            return Response.ok().build();
        } catch (RepositoryException | IOException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }

    @HEAD
    @Path("{pid}/streams/{dsid}")
    public Response checkDatastreamExists(@PathParam("pid") String pid, @PathParam("dsid") String dsid) {
        //authentication
        AuthenticatedUser user = getAuthenticatedUserByOauth();
        //authorization
        String role = ROLE_READ_FOXML;
        if (!user.getRoles().contains(role)) {
            throw new ForbiddenException("user '%s' is not allowed to do this (missing role '%s')", user.getName(), role); //403
        }

        checkObjectAndDatastreamExist(pid, dsid);
        return Response.ok().build();
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
            //authentication
            AuthenticatedUser user = getAuthenticatedUserByOauth();
            //authorization
            String role = ROLE_READ_FOXML;
            if (!user.getRoles().contains(role)) {
                throw new ForbiddenException("user '%s' is not allowed to do this (missing role '%s')", user.getName(), role); //403
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
        } catch (RepositoryException | IOException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{pid}/streams/{dsid}")
    public Response getDatastream(@PathParam("pid") String pid, @PathParam("dsid") String dsid) {
        try {
            //authentication
            AuthenticatedUser user = getAuthenticatedUserByOauth();
            //authorization
            String role = ROLE_READ_FOXML;
            if (!user.getRoles().contains(role)) {
                throw new ForbiddenException("user '%s' is not allowed to to do this (missing role '%s')", user.getName(), role); //403
            }

            checkObjectAndDatastreamExist(pid, dsid);
            switch (dsid) {
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
                    //TODO: test http://localhost:8080/search/api/admin/v1.0/items/uuid:d41a05bb-7ec7-474c-adeb-da4cdfeaab3a/streams/TEXT_OCR
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
        } catch (RepositoryException | IOException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }

    //TODO: operace pro nastavení/změnu url pro IMG_THUMB
    //výhledově možná kromě url taky nahrát obrázek samotný
}
