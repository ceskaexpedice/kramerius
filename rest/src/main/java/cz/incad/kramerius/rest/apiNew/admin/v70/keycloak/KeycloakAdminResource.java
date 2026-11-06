package cz.incad.kramerius.rest.apiNew.admin.v70.keycloak;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.inject.Provider;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.keycloak.KeycloakAdminService;
import cz.incad.kramerius.rest.api.exceptions.ActionNotAllowed;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;

@Path("/admin/v7.0/keycloak")
public class KeycloakAdminResource {

    public static final Logger LOGGER = Logger.getLogger(KeycloakAdminResource.class.getName());

    private static final List<String> PROTECTED_ROLES = Arrays.asList("offline_access", "uma_authorization");
    private static final String PROTECTED_ROLE_PREFIX = "default-roles-";

    @Inject
    KeycloakAdminService keycloakAdminService;

    @Inject
    Provider<User> userProvider;

    @Inject
    RightsResolver rightsResolver;

    // ------------- roles -------------

    @GET
    @Path("roles")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response roles() {
        checkPermission();
        try {
            return Response.ok().entity(new JSONArray(this.keycloakAdminService.listRoles()).toString()).build();
        }
        catch (WebApplicationException e) {
            return keycloakError(e, "list roles");
        }
    }

    @POST
    @Path("roles")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response createRole(JSONObject body) {
        checkPermission();
        String roleName = body.optString("name", "").trim();
        if (roleName.isEmpty()) {
            return error(400, "missing 'name'");
        }
        if (isProtectedRole(roleName)) {
            return error(400, "role '" + roleName + "' is reserved by Keycloak");
        }
        try {
            this.keycloakAdminService.createRole(roleName,
                    body.optString("description", null));
            return Response.status(201).entity(new JSONObject().put("name", roleName).toString()).build();
        }
        catch (WebApplicationException e) {
            return keycloakError(e, "create role " +  roleName);
        }

    }

    @DELETE
    @Path("roles/{name}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response deleteRole(@PathParam("name") String roleName) {
        checkPermission();
        if (isProtectedRole(roleName)) {
            return error(400, "role '" + roleName + "' is reserved by Keycloak");
        }

        try {
            this.keycloakAdminService.deleteRole(roleName);
            return Response.status(204).build();
        } catch (WebApplicationException e) {
            return keycloakError(e, "delete role " +  roleName);
        }
    }

    // ------------- users -------------

    @GET
    @Path("users")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response findUser(@QueryParam("username") String username) {
        checkPermission();
        if (username == null || username.trim().isEmpty()) {
            return error(400, "missing query parameter 'username'");
        }
        try {
            String id = this.keycloakAdminService.findUserId(username);
            if (id == null) {
                return error(404, "username '" + username + "' not found");
            }
            return Response.ok().entity(new JSONObject().put("id", id).put("username", username).toString()).build();
        } catch (WebApplicationException e) {
            return keycloakError(e, "find user " + username);
        }
    }

    @POST
    @Path("users")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response createUser(JSONObject body) {
        checkPermission();
        String username = body.optString("username", "").trim();
        if (username.isEmpty()) {
            return error(400, "missing 'username'");
        }
        try {
            String id = this.keycloakAdminService.createUser(username,
                    body.optString("firstName", null),
                    body.optString("lastName", null),
                    body.optString("email", null));

            return Response.status(201).entity(new JSONObject().put("id", id).put("username", username).toString()).build();
        } catch (WebApplicationException e) {
            return keycloakError(e, "create user " +  username);
        }
    }

    @DELETE
    @Path("users/{id}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response deleteUser(@PathParam("id") String id) {
        checkPermission();
        try {
            this.keycloakAdminService.deleteUser(id);
            return Response.status(204).build();
        }  catch (WebApplicationException e) {
            return keycloakError(e, "delete user " +  id);
        }
    }

    // ------------- user`s roles -------------

    @GET
    @Path("users/{id}/roles")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response userRoles(@PathParam("id") String id) {
        checkPermission();
        try {
            return Response.ok().entity(new JSONArray(this.keycloakAdminService.userRoles(id)).toString()).build();
        } catch (WebApplicationException e) {
            return keycloakError(e, "list roles of user " +  id);
        }
    }

    @PUT
    @Path("users/{id}/roles/{name}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response assignRole(@PathParam("id") String id, @PathParam("name") String name) {
        checkPermission();
        if (isProtectedRole(name)) {
            return error(400, "role '" + name + "' is reserved by Keycloak");
        }

        try {
            this.keycloakAdminService.assignRole(id, name);
            return Response.status(204).build();
        }  catch (WebApplicationException e) {
            return keycloakError(e, "assign role " +  name + " to user " +  id);
        }
    }



    @DELETE
    @Path("users/{id}/roles/{name}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response removeRole(@PathParam("id") String id, @PathParam("name") String name) {
        checkPermission();
        if (isProtectedRole(name)) {
            return error(400, "role '" + name + "' is reserved by Keycloak");
        }
        try {
            this.keycloakAdminService.removeRole(id, name);
            return Response.status(204).build();
        }  catch (WebApplicationException e) {
            return keycloakError(e, "remove role " +  name + " from user " + id);
        }
    }

    // ------------- util -------------

    private void checkPermission() {
        User user = this.userProvider.get();
        boolean allowed = user != null && this.rightsResolver.isActionAllowed(user, SecuredActions.A_ROLES_EDIT.getFormalName(),
                SpecialObjects.REPOSITORY.getPid(), null, ObjectPidsPath.REPOSITORY_PATH).flag();
        if (!allowed) {
            throw new ActionNotAllowed("not allowed");
        }
    }

    private static boolean isProtectedRole(String name) {
        return PROTECTED_ROLES.contains(name) || name.startsWith(PROTECTED_ROLE_PREFIX);
    }

    /** Passes 404 and 409 from Keycloak through; anything else means Keycloak or our service account has a problem (502) */
    private Response keycloakError(WebApplicationException e, String what) {
        int status = e.getResponse() != null ? e.getResponse().getStatus() : 500;
        LOGGER.warning("Keycloak refused to " + what + ", status " + status);
        switch (status) {
            case 404: return error(404, "not found in Keycloak: " + what);
            case 409: return error(409, "already exists in Keycloak: " + what);
            case 401:
            case 403: return error(502, "Kramerius service account was refused by Keycloak (" + status + "), check keycloak.admin.* and its realm-management roles");
            default:  return error(502, "Keycloak error " + status + " on: " + what);
        }
    }

    private static Response error(int status, String message) {
        return Response.status(status).type(MediaType.APPLICATION_JSON)
                .entity(new JSONObject().put("message", message).toString()).build();
    }
}
