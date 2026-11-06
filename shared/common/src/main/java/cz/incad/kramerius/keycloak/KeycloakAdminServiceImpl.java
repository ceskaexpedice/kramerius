package cz.incad.kramerius.keycloak;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

import com.google.inject.Singleton;

import cz.incad.kramerius.utils.conf.KConfiguration;


@Singleton
public class KeycloakAdminServiceImpl implements KeycloakAdminService {

    public static final Logger LOGGER = Logger.getLogger(KeycloakAdminServiceImpl.class.getName());


    private <T> T withRealm(Function<RealmResource, T> operation) {
        Configuration conf = KConfiguration.getInstance().getConfiguration();
        String url = conf.getString("keycloak.admin.url");
        String clientId = conf.getString("keycloak.admin.clientId");
        String secret = conf.getString("keycloak.admin.secret");
        String realmName = conf.getString("keycloak.admin.realm", "kramerius");

        if (isEmpty(url) ||  isEmpty(clientId) || isEmpty(secret)) {
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
            return operation.apply(keycloak.realm(realmName));
        } finally {
            keycloak.close();
        }

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

    private static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}
