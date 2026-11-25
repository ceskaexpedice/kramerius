package cz.incad.kramerius.keycloak;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.configuration.Configuration;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.FederatedIdentityRepresentation;

import com.google.inject.Singleton;

import cz.incad.kramerius.utils.conf.KConfiguration;


@Singleton
public class KeycloakAdminServiceImpl implements KeycloakAdminService {

    private static final int IDP_PAGE = 100;

    public static final Logger LOGGER = Logger.getLogger(KeycloakAdminServiceImpl.class.getName());

    private <T> T withRealm(Function<RealmResource, T> operation) {
        return withKeycloak((keycloak, realmName) -> operation.apply(keycloak.realm(realmName)));
    }

    private <T> T withKeycloak(BiFunction<Keycloak, String, T> operation) {
        Configuration conf = KConfiguration.getInstance().getConfiguration();
        String url = conf.getString("keycloak.admin.url");
        String clientId = conf.getString("keycloak.admin.clientId");
        String secret = conf.getString("keycloak.admin.secret");
        String realmName = conf.getString("keycloak.admin.realm", "kramerius");

        if (isEmpty(url) || isEmpty(clientId) || isEmpty(secret)) {
            throw new IllegalStateException("Keycloak admin is not configured, " +
                    "set keycloak.admin.url, keycloak.admin.clientId and keycloak.admin.secret");
        }

        ResteasyClientBuilder clientBuilder = (ResteasyClientBuilder) ResteasyClientBuilder.newBuilder();

        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(url)
                .realm(realmName)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(clientId)
                .clientSecret(secret)
                .resteasyClient(clientBuilder.build())
                .build();

        try {
            return operation.apply(keycloak, realmName);
        } finally {
            keycloak.close();
        }
    }

    private static String adminServerUrl() {
        String url = KConfiguration.getInstance().getConfiguration().getString("keycloak.admin.url");
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    @Override
    public List<String> listRoles() {
        return withRealm(realm -> realm.roles().list().stream()
                .map(RoleRepresentation::getName).collect(Collectors.toList()));
    }

    @Override
    public void createRole(String roleName, String description) {
        RoleRepresentation role = new RoleRepresentation();
        role.setName(roleName);
        role.setDescription(description);
        withRealm(realm -> {
            realm.roles().create(role);
            return null;
        });
        LOGGER.info("Keycloak role created: " + roleName);
    }

    @Override
    public void deleteRole(String roleName) {
        withRealm(realm -> {
            realm.roles().deleteRole(roleName);
            return null;
        });
        LOGGER.info("Keycloak role deleted: " + roleName);
    }

    @Override
    public String createUser(String username, String firstName, String lastName, String email) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setEnabled(true);

        String id = withRealm(realm ->  {
            Response response = realm.users().create(user);
            if (response.getStatus() != 201) {
                LOGGER.warning("Keycloak refused to create user " + username + ", status " + response.getStatus());
                throw new WebApplicationException(response.getStatus());
            }

            String location = response.getMetadata().getFirst("Location").toString();
            return location.substring(location.lastIndexOf("/") + 1);
        });

        LOGGER.info("Keycloak user created: " + username + " (" + id + ")");
        return id;
    }

    @Override
    public String findUserId(String username) {
        List<UserRepresentation> found = withRealm(realm -> realm.users().searchByUsername(username, true));
        return found.isEmpty() ? null : found.get(0).getId();
    }

    @Override
    public void deleteUser(String userId) {
        withRealm(realm -> {
            Response response = realm.users().delete(userId);
            if (response.getStatus() >= 300) {
                LOGGER.warning("Keycloak refused to delete user " + userId + ", status " + response.getStatus());
                throw new WebApplicationException(response.getStatus());
            }
            return null;
        });
        LOGGER.info("Keycloak user deleted: " + userId);
    }

    @Override
    public List<String> userRoles(String userId) {
        return withRealm(realm -> realm.users().get(userId).roles().realmLevel().listAll().stream()
                .map(RoleRepresentation::getName).collect(Collectors.toList()));
    }

    @Override
    public void assignRole(String userId, String roleName) {
        withRealm(realm -> {
           RoleRepresentation role = realm.roles().get(roleName).toRepresentation();
           realm.users().get(userId).roles().realmLevel().add(Collections.singletonList(role));
           return null;
        });
        LOGGER.info("Keycloak role " + roleName + " assigned to user " + userId);
    }

    @Override
    public void removeRole(String userId, String roleName) {
        withRealm(realm -> {
            RoleRepresentation role = realm.roles().get(roleName).toRepresentation();
            realm.users().get(userId).roles().realmLevel().remove(Collections.singletonList(role));
            return null;
        });

        LOGGER.info("Keycloak role " + roleName + " removed from user " + userId);
    }

    @Override
    public List<Map<String, Object>> listIdentityProviders() {
        return withKeycloak((keycloak, realmName) -> {
            IdentityProviderInstances api = keycloak.proxy(IdentityProviderInstances.class,
                    URI.create(adminServerUrl() + "/admin/realms/" + realmName + "/identity-provider/instances"));
            Map<String, Map<String, Object>> byAlias = new LinkedHashMap<>();
            for (int first = 0; ; first += IDP_PAGE) {
                List<Map<String, Object>> page = api.list(first, IDP_PAGE, false);
                int before = byAlias.size();
                for (Map<String, Object> idp : page) {
                    byAlias.put(String.valueOf(idp.get("alias")), idp);
                }
                if (page.size() < IDP_PAGE || byAlias.size() == before) {
                    break;
                }
            }
            return new ArrayList<>(byAlias.values());
        });
    }

    @Override
    public List<UserRepresentation> identityProviderUsers(String alias, int first, int max) {
        return withRealm(realm -> realm.users().search(null, null, null, null, null, alias, null, first, max, null, true));
    }

    @Override
    public List<FederatedIdentityRepresentation> userIdentities(String userId) {
        return withRealm(realm -> realm.users().get(userId).getFederatedIdentity());
    }

    @Override
    public List<UserRepresentation> searchUsers(KeycloakUserFilter filter, int first, int max) {
        String q = attributeQuery(filter.attributes);
        return withKeycloak(((keycloak, realmName) -> {
            UsersQuery api = keycloak.proxy(UsersQuery.class, URI.create(adminServerUrl() + "/admin/realms/" + realmName + "/users"));
            return api.search(blankToNull(filter.search), blankToNull(filter.username),
                    blankToNull(filter.firstName), blankToNull(filter.lastName), blankToNull(filter.email),
                    filter.enabled, blankToNull(filter.idpAlias), q,
                    filter.exact ? Boolean.TRUE : null, first, max, true);
        }));
    }

    private static String attributeQuery(Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            if (sb.length() > 0) {
                sb.append(' ');
            }

            String value = entry.getValue().replace("\\", "\\\\").replace("\"", "\\\"");
            sb.append(entry.getKey()).append(":\"").append(value).append('"');
        }
        return sb.toString();
    }

    private static String blankToNull(String s) {
        return isEmpty(s) ? null : s.trim();
    }

    private static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}
