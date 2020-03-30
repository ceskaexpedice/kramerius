package cz.incad.kramerius.rest.apiNew.admin.v10.collections;

import cz.incad.kramerius.rest.apiNew.admin.v10.AdminApiResource;
import cz.incad.kramerius.rest.apiNew.admin.v10.AuthenticatedUser;
import cz.incad.kramerius.rest.apiNew.exceptions.BadRequestException;
import cz.incad.kramerius.rest.apiNew.exceptions.ForbiddenException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

@Path("/admin/v1.0/collections")
public class CollectionsResource extends AdminApiResource {

    //TODO: prejmenovat role podle spravy uctu
    private static final String ROLE_CREATE_COLLECTION = "kramerius_admin";
    private static final String ROLE_READ_COLLECTION = "kramerius_admin";
    private static final String ROLE_EDIT_COLLECTION = "kramerius_admin";
    private static final String ROLE_DELETE_COLLECTION = "kramerius_admin";


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
        Collection collection = collectionFromJson(collectionDefinition);
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
        //authentication
        AuthenticatedUser user = getAuthenticatedUser();
        String role = ROLE_READ_COLLECTION;
        if (!user.getRoles().contains(role)) {
            throw new ForbiddenException("user '%s' is not allowed to read collections (missing role '%s')", user.getName(), role); //403
        }
        System.out.println("pid: " + pid);
        //TODO: implement
        throw new InternalErrorException("not implemented yet");
    }

    @PUT
    @Path("{pid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateCollection(JSONObject collectionDefinition) {
        //authentication
        AuthenticatedUser user = getAuthenticatedUser();
        String role = ROLE_EDIT_COLLECTION;
        if (!user.getRoles().contains(role)) {
            throw new ForbiddenException("user '%s' is not allowed to edit collections (missing role '%s')", user.getName(), role); //403
        }
        Collection collection = collectionFromJson(collectionDefinition);
        System.out.println(collection);
        //TODO: implement
        throw new InternalErrorException("not impemented yet");
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

    private Collection collectionFromJson(JSONObject collectionDefinition) {
        try {
            return new Collection(collectionDefinition);
        } catch (JSONException e) {
            throw new BadRequestException("error parsing json: " + e);
        }
    }

}
