package cz.incad.kramerius.rest.apiNew.admin.v10.collections;

import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.rest.apiNew.admin.v10.AdminApiResource;
import cz.incad.kramerius.rest.apiNew.admin.v10.AuthenticatedUser;
import cz.incad.kramerius.rest.apiNew.exceptions.BadRequestException;
import cz.incad.kramerius.rest.apiNew.exceptions.ForbiddenException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.utils.Dom4jUtils;
import org.dom4j.Document;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.UUID;

@Path("/admin/v1.0/collections")
public class CollectionsResource extends AdminApiResource {

    //TODO: prejmenovat role podle spravy uctu
    private static final String ROLE_CREATE_COLLECTION = "kramerius_admin";
    private static final String ROLE_READ_COLLECTION = "kramerius_admin";
    private static final String ROLE_EDIT_COLLECTION = "kramerius_admin";
    private static final String ROLE_DELETE_COLLECTION = "kramerius_admin";

    private final FoxmlBuilder foxmlBuilder = new FoxmlBuilder();

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createCollection(JSONObject collectionDefinition) {
        //authentication
        AuthenticatedUser user = getAuthenticatedUser();
        String role = ROLE_CREATE_COLLECTION;
        if (!user.getRoles().contains(role)) {
            throw new ForbiddenException("user '%s' is not allowed to create collections (missing role '%s')", user.getName(), role); //403
        }
        Collection collection = extractCollectionFromJson(collectionDefinition);
        if (collection.name == null || collection.name.isEmpty()) {
            throw new BadRequestException("name can't be empty");
        }
        System.out.println(collection);
        String newPid = "uuid:" + UUID.randomUUID().toString();
        collection.pid = newPid;
        //TODO: vyrobit foxml
        //TODO: ingest
        //TODO: schedule indexing
        //return Response.status(Response.Status.CREATED).entity(collection.toJson().toString()).build();
        //TODO: implement
        throw new InternalErrorException("not implemented yet");
    }

    @GET
    @Path("{pid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCollection(@PathParam("pid") String pid) {
        try {
            //authentication
            AuthenticatedUser user = getAuthenticatedUser();
            String role = ROLE_READ_COLLECTION;
            if (!user.getRoles().contains(role)) {
                throw new ForbiddenException("user '%s' is not allowed to read collections (missing role '%s')", user.getName(), role); //403
            }
            checkObjectExists(pid);
            Collection collection = fetchCollectionFromRepository(pid);
            return Response.ok(collection.toJson()).build();
        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        }
    }


    @PUT
    @Path("{pid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateCollection(@PathParam("pid") String pid, JSONObject collectionDefinition) {
        try {
            //authentication
            AuthenticatedUser user = getAuthenticatedUser();
            String role = ROLE_EDIT_COLLECTION;
            if (!user.getRoles().contains(role)) {
                throw new ForbiddenException("user '%s' is not allowed to edit collections (missing role '%s')", user.getName(), role); //403
            }
            checkObjectExists(pid);
            Collection current = fetchCollectionFromRepository(pid);

            Collection updated = current.withUpdatedTexts(extractCollectionFromJson(collectionDefinition));
            if (updated.name == null || updated.name.isEmpty()) {
                throw new BadRequestException("name can't be empty");
            }
            if (!current.equalsInTexts(updated)) {
                krameriusRepositoryApi.updateMods(pid, foxmlBuilder.buildMods(updated));
                //TODO: update dublin core
                //TODO: schedule indexation
            }
            return Response.ok().build();
        } catch (IOException | RepositoryException e) {
            e.printStackTrace();
            throw new InternalErrorException(e.getMessage());
        }
    }

    @DELETE
    @Path("{pid}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response deleteCollection(@PathParam("pid") String pid) {
        //authentication
        AuthenticatedUser user = getAuthenticatedUser();
        String role = ROLE_DELETE_COLLECTION;
        if (!user.getRoles().contains(role)) {
            throw new ForbiddenException("user '%s' is not allowed to delete collections (missing role '%s')", user.getName(), role); //403
        }
        System.out.println("pid: " + pid);
        //TODO: implement
        throw new InternalErrorException("not implemented yet");
    }

    private Collection fetchCollectionFromRepository(String pid) {
        try {
            Collection collection = new Collection();
            collection.pid = pid;

            //timestamps from Foxml properties
            collection.created = krameriusRepositoryApi.getTimestampCreated(pid);
            collection.modified = krameriusRepositoryApi.getTimestampLastModified(pid);

            //data from MODS
            Document mods = krameriusRepositoryApi.getMods(pid, false);
            collection.name = Dom4jUtils.stringOrNullFromFirstElementByXpath(mods.getRootElement(), "//mods/titleInfo/title");
            collection.description = Dom4jUtils.stringOrNullFromFirstElementByXpath(mods.getRootElement(), "//mods/abstract");
            collection.content = Dom4jUtils.stringOrNullFromFirstElementByXpath(mods.getRootElement(), "//mods/note");

            return collection;
        } catch (IOException | RepositoryException e) {
            e.printStackTrace();
            throw new InternalErrorException(e.getMessage());
        }
    }

    private Collection extractCollectionFromJson(JSONObject collectionDefinition) {
        try {
            return new Collection(collectionDefinition);
        } catch (JSONException e) {
            throw new BadRequestException("error parsing json: " + e);
        }
    }

}
