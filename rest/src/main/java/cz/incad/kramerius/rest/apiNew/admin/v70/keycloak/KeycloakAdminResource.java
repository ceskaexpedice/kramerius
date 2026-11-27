package cz.incad.kramerius.rest.apiNew.admin.v70.keycloak;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Logger;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.DefaultValue;

import org.keycloak.representations.idm.FederatedIdentityRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.EventRepresentation;

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
import cz.incad.kramerius.keycloak.KeycloakUserLoginActivity;
import cz.incad.kramerius.keycloak.KeycloakUserDetails;
import cz.incad.kramerius.rest.api.exceptions.ActionNotAllowed;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.auth.AttributeRoleRulesService;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.Right;
import cz.incad.kramerius.security.RightCriteriumContext;
import cz.incad.kramerius.security.RightCriteriumContextFactory;
import cz.incad.kramerius.security.EvaluatingResultState;
import cz.incad.kramerius.security.DataMockExpectation;
import cz.incad.kramerius.security.impl.DatabaseRightsManager;
import cz.incad.kramerius.security.impl.RoleImpl;
import cz.incad.kramerius.security.impl.UserImpl;
import cz.incad.kramerius.security.licenses.License;
import cz.incad.kramerius.security.licenses.LicensesManager;
import cz.incad.kramerius.security.licenses.LicensesManagerException;
import cz.incad.kramerius.security.licenses.lock.ExclusiveLockMaps;
import cz.incad.kramerius.imaging.ImageStreams;
import cz.incad.kramerius.security.utils.UserUtils;
import cz.incad.kramerius.rest.apiNew.client.v70.ClientUserResource;
import cz.incad.kramerius.keycloak.KeycloakUserFilter;

@Path("/admin/v7.0/keycloak")
public class KeycloakAdminResource {

    public static final Logger LOGGER = Logger.getLogger(KeycloakAdminResource.class.getName());

    private static final List<String> PROTECTED_ROLES = Arrays.asList("offline_access", "uma_authorization");
    private static final String PROTECTED_ROLE_PREFIX = "default-roles-";

    @Inject
    KeycloakAdminService keycloakAdminService;

    @Inject
    AttributeRoleRulesService attributeRoleRulesService;

    @Inject
    Provider<User> userProvider;

    @Inject
    RightsResolver rightsResolver;

    @Inject
    UserManager userManager;

    @Inject
    DatabaseRightsManager databaseRightsManager;

    @Inject
    RightCriteriumContextFactory ctxFactory;

    @Inject
    LicensesManager licensesManager;

    @Inject
    ExclusiveLockMaps exclusiveMaps;

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

    @GET
    @Path("users/{id}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response userDetails(@PathParam("id") String id) {
        checkPermission();
        try {
            KeycloakUserDetails details = this.keycloakAdminService.getUserDetails(id);
            EventRepresentation lastEvent = this.keycloakAdminService.getLastUserEvent(id);
            List<String> realmRoles = new ArrayList<>(details.getRealmRoles());
            List<String> rolesForLicenseEvaluation = new ArrayList<>(realmRoles);
            List<AttributeRoleRulesService.RoleMapping> mappedRoles = mappedRealmRoles(details.getUser(), rolesForLicenseEvaluation);
            List<String> providedLicenses = findProvidedLicenses(details.getUser(), rolesForLicenseEvaluation,
                    lastEvent == null ? null : blankToNull(lastEvent.getIpAddress()));
            KeycloakUserDetails detailsWithMappedRoles = new KeycloakUserDetails(details.getUser(), realmRoles);
            return Response.ok().entity(userDetailsJson(detailsWithMappedRoles, mappedRoles, lastEvent, providedLicenses).toString()).build();
        } catch (WebApplicationException e) {
            return keycloakError(e, "get details for user " + id);
        }
    }

    /** Adds roles implied by Kramerius attribute rules to the roles shown for this Keycloak user. */
    private List<AttributeRoleRulesService.RoleMapping> mappedRealmRoles(UserRepresentation keycloakUser, List<String> realmRoles) {
        UserImpl mockUser = new UserImpl(0, keycloakUser.getFirstName(), keycloakUser.getLastName(),
                keycloakUser.getUsername(), 0);
        Map<String, String> attributes = new java.util.HashMap<>();
        Map<String, List<String>> keycloakAttributes = keycloakUser.getAttributes();
        if (keycloakAttributes != null) {
            keycloakAttributes.forEach((key, values) -> {
                if (values != null && !values.isEmpty()) attributes.put(key, String.join(",", values));
            });
        }
        if (mockUser.getLoginname() != null) {
            attributes.put("username", mockUser.getLoginname());
            attributes.put("loginName", mockUser.getLoginname());
        }
        putIfNotNull(attributes, "firstName", mockUser.getFirstName());
        putIfNotNull(attributes, "lastName", mockUser.getSurname());
        putIfNotNull(attributes, "given_name", mockUser.getFirstName());
        putIfNotNull(attributes, "family_name", mockUser.getSurname());
        putIfNotNull(attributes, "email", keycloakUser.getEmail());
        try {
            return attributeRoleRulesService.applyAndGetRoleMappings(attributes, realmRoles);
        } catch (Exception e) {
            throw new IllegalStateException("Could not apply configured authentication attribute rules to Keycloak user details", e);
        }
    }

    private static void putIfNotNull(Map<String, String> attributes, String key, String value) {
        if (value != null && !value.isEmpty()) attributes.put(key, value);
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

    @GET
    @Path("users/search")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response searchUsers(@QueryParam("search") String search,
                                @QueryParam("username") String username,
                                @QueryParam("firstName") String firstName,
                                @QueryParam("lastName") String lastName,
                                @QueryParam("email") String email,
                                @QueryParam("enabled") Boolean enabled,
                                @QueryParam("idp") String idp,
                                @QueryParam("attr") List<String> attrs,
                                @QueryParam("lastLoginWithinMinutes") Integer lastLoginWithinMinutes,
                                @QueryParam("exact") @DefaultValue("false") boolean exact,
                                @QueryParam("first") @DefaultValue("0") int first,
                                @QueryParam("max") @DefaultValue("20") int max) {
        checkPermission();
        if (first < 0 || max < 1 || max > 500) {
            return error(400, "first must be >= 0 and max between 1 and 500");
        }

        KeycloakUserFilter filter = new KeycloakUserFilter();
        filter.search = search;
        filter.username = username;
        filter.firstName = firstName;
        filter.lastName = lastName;
        filter.email = email;
        filter.enabled = enabled;
        filter.idpAlias = idp;
        filter.exact = exact;
        if (attrs != null) {
            for (String attr : attrs) {
                int colon = attr.indexOf(':');
                String key = colon > 0 ? attr.substring(0, colon).trim() : "";
                String value = colon > 0 ? attr.substring(colon + 1) : "";
                if (key.isEmpty() || value.isEmpty() || key.matches(".*[\\s\"].*")) {
                    return error(400, "attr must be key:value (key without spaces), got '" + attr + "'");
                }
                filter.attributes.put(key, value);
            }
        }

        if (lastLoginWithinMinutes != null
                && (lastLoginWithinMinutes < 1 || lastLoginWithinMinutes > 10080)) {
            return error(400, "lastLoginWithinMinutes must be between 1 and 10080");
        }

        boolean otherFilters = notBlank(username) || notBlank(firstName) || notBlank(lastName)
                || notBlank(email) || notBlank(idp) || !filter.attributes.isEmpty();
        if (notBlank(search) && otherFilters) {
            return error(400, "'search' cannot be combined with other filters (only with 'enabled')");
        }
        if (lastLoginWithinMinutes != null && (notBlank(search) || enabled != null || otherFilters)) {
            return error(400, "'lastLoginWithinMinutes' cannot be combined with other user filters");
        }

        try {
            // one extra record tells us whether there is a next page
            JSONArray users = new JSONArray();
            boolean hasMore;
            if (lastLoginWithinMinutes != null) {
                List<KeycloakUserLoginActivity> found = this.keycloakAdminService.usersLoggedInWithinMinutes(
                        lastLoginWithinMinutes, first, max + 1);
                hasMore = found.size() > max;
                for (KeycloakUserLoginActivity activity : found.subList(0, Math.min(found.size(), max))) {
                    users.put(userJson(activity.getUser())
                            .put("lastLogin", activity.getLastLogin())
                            .put("loginCount", activity.getLoginCount()));
                }
            } else {
                List<UserRepresentation> found = this.keycloakAdminService.searchUsers(filter, first, max + 1);
                hasMore = found.size() > max;
                for (UserRepresentation user : found.subList(0, Math.min(found.size(), max))) {
                    users.put(userJson(user));
                }
            }
            return Response.ok().entity(new JSONObject()
                    .put("first", first)
                    .put("max", max)
                    .put("hasMore", hasMore)
                    .put("users", users).toString()).build();
        } catch (WebApplicationException e) {
            return keycloakError(e, lastLoginWithinMinutes != null
                    ? "search login events"
                    : "search users");
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

    // ------------- identity providers -------------

    @GET
    @Path("identity-providers")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response identityProviders(@QueryParam("search") String search) {
        checkPermission();
        String filter = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        try {
            JSONArray result = new JSONArray();
            for (Map<String, Object> idp : this.keycloakAdminService.listIdentityProviders()) {
                String alias = String.valueOf(idp.get("alias"));
                String displayName = idp.get("displayName") != null ? String.valueOf(idp.get("displayName")) : null;
                if (!filter.isEmpty()
                        && !alias.toLowerCase(Locale.ROOT).contains(filter)
                        && (displayName == null || !displayName.toLowerCase(Locale.ROOT).contains(filter))) {
                    continue;
                }
                Object entityId = null;
                Object config = idp.get("config");
                if (config instanceof Map) {
                    entityId = ((Map<?, ?>) config).get("entityId");
                }
                result.put(new JSONObject()
                        .put("alias", alias)
                        .put("displayName", displayName)
                        .put("providerId", idp.get("providerId"))
                        .put("enabled", idp.get("enabled"))
                        .put("entityId", entityId));
            }
            return Response.ok().entity(result.toString()).build();
        } catch (WebApplicationException e) {
            return keycloakError(e, "list identity providers");
        }
    }

    @GET
    @Path("identity-providers/{alias}/users")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response identityProviderUsers(@PathParam("alias") String alias,
                                          @QueryParam("first") @DefaultValue("0") int first,
                                          @QueryParam("max") @DefaultValue("20") int max) {
        checkPermission();
        if (first < 0 || max < 1 || max > 500) {
            return error(400, "first must be >= 0 and max between 1 and 500");
        }

        try {
            JSONArray users = new JSONArray();
            for (UserRepresentation u : this.keycloakAdminService.identityProviderUsers(alias, first, max)) {
                users.put(userJson(u));
            }
            return Response.ok().entity(users.toString()).build();
        } catch (WebApplicationException e) {
            return keycloakError(e, "list users of identity provider " + alias);
        }
    }

    @GET
    @Path("users/{id}/identities")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response usersIdentities(@PathParam("id") String id) {
        checkPermission();
        try {
            JSONArray result = new JSONArray();
            for (FederatedIdentityRepresentation fi : this.keycloakAdminService.userIdentities(id)) {
                result.put(new JSONObject()
                        .put("identityProvider", fi.getIdentityProvider())
                        .put("userId", fi.getUserId())
                        .put("userName", fi.getUserName()));
            }
            return Response.ok().entity(result.toString()).build();
        } catch (WebApplicationException e) {
            return keycloakError(e, "list identities of user " + id);
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

    private static JSONObject userJson(UserRepresentation u) {
        return new JSONObject()
                .put("id", u.getId())
                .put("username", u.getUsername())
                .put("firstName", u.getFirstName())
                .put("lastName", u.getLastName())
                .put("email", u.getEmail())
                .put("enabled", u.isEnabled())
                .put("createdTimestamp", u.getCreatedTimestamp());
    }

    private static JSONObject userDetailsJson(KeycloakUserDetails details,
                                              List<AttributeRoleRulesService.RoleMapping> mappedRoles,
                                              EventRepresentation lastEvent,
                                              List<String> providedLicenses) {
        UserRepresentation user = details.getUser();
        JSONArray mappedRoleJson = new JSONArray();
        for (AttributeRoleRulesService.RoleMapping mapping : mappedRoles) {
            mappedRoleJson.put(new JSONObject()
                    .put("role", mapping.getRole())
                    .put("ruleName", mapping.getRuleName().isEmpty() ? JSONObject.NULL : mapping.getRuleName()));
        }
        JSONObject json = userJson(user)
                .put("attributes", user.getAttributes() == null ? new JSONObject() : new JSONObject(user.getAttributes()))
                .put("realmRoles", new JSONArray(details.getRealmRoles()))
                .put("mappedRoles", mappedRoleJson)
                .put("licenses", new JSONArray(providedLicenses));
        if (lastEvent == null) {
            json.put("lastEvent", JSONObject.NULL);
        } else {
            json.put("lastEvent", new JSONObject()
                    .put("type", lastEvent.getType())
                    .put("time", lastEvent.getTime())
                    .put("userId", lastEvent.getUserId())
                    .put("clientId", lastEvent.getClientId())
                    .put("ipAddress", lastEvent.getIpAddress())
                    .put("details", lastEvent.getDetails() == null ? new JSONObject() : new JSONObject(lastEvent.getDetails())));
        }
        return json;
    }

    private List<String> findProvidedLicenses(UserRepresentation keycloakUser, List<String> keycloakRealmRoles,
                                              String eventIpAddress) {
        User user = userManager.findUserByLoginName(keycloakUser.getUsername());
        if (user == null) {
            // Users supplied by an external Keycloak do not necessarily have a
            // local user_entity record. Still evaluate group-based licenses
            // using their Keycloak realm roles and the standard implicit groups.
            UserImpl externalUser = new UserImpl(-1, keycloakUser.getFirstName(), keycloakUser.getLastName(),
                    keycloakUser.getUsername(), -1);
            externalUser.setEmail(keycloakUser.getEmail());
            List<Role> groups = new ArrayList<>();
            if (keycloakRealmRoles != null) {
                for (String roleName : keycloakRealmRoles) {
                    if (roleName != null && !roleName.trim().isEmpty()) {
                        groups.add(new RoleImpl(roleName));
                    }
                }
            }
            externalUser.setGroups(groups.toArray(new Role[0]));
            user = externalUser;
        }
        // The logged-in user provider adds these implicit groups before rights
        // evaluation. Do the same so common-user licenses such as "public" apply.
        UserUtils.associateCommonGroup(user, userManager);
        UserUtils.associateAuthenticatedGroup(user, userManager);
        Right[] rights = databaseRightsManager.findAllRightByCriteriumNames(
                SecuredActions.A_READ.getFormalName(), ClientUserResource.LICENSES_CRITERIA, user);
        RightCriteriumContext context = ctxFactory.create(SpecialObjects.REPOSITORY.getPid(),
                ImageStreams.IMG_FULL.name(), user, eventIpAddress, eventIpAddress, null, exclusiveMaps);
        Set<String> names = new LinkedHashSet<>();
        for (Right right : rights) {
            try {
                if (right.mockEvaluate(context, databaseRightsManager, DataMockExpectation.EXPECT_DATA_VAUE_EXISTS)
                        == EvaluatingResultState.TRUE) {
                    String name = right.getCriteriumWrapper().getLicense().getName();
                    if (name != null && !name.isEmpty() && Character.isAlphabetic(name.charAt(0))) {
                        names.add(name);
                    }
                }
            } catch (Exception e) {
                LOGGER.warning("Could not evaluate provided license for user " + keycloakUser.getUsername() + ": " + e.getMessage());
            }
        }
        try {
            List<License> licenses = licensesManager.getLicenses();
            licenses.sort(Comparator.comparingInt(License::getPriority));
            List<String> ordered = new ArrayList<>();
            for (License license : licenses) {
                if (names.contains(license.getName())) {
                    ordered.add(license.getName());
                }
            }
            return ordered;
        } catch (LicensesManagerException e) {
            LOGGER.warning("Could not order provided licenses: " + e.getMessage());
            return new ArrayList<>(names);
        }
    }

    private static boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private static String blankToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value;
    }
}
