package cz.incad.kramerius.keycloak;

import java.util.List;
import java.util.Map;
import org.keycloak.representations.idm.FederatedIdentityRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

public interface KeycloakAdminService {

    List<String> listRoles();

    void createRole(String roleName, String description);
    void deleteRole(String roleName);

    String createUser(String username, String firstName, String lastName, String email);

    String findUserId(String username);

    void deleteUser(String userId);

    List<String> userRoles(String userId);

    void assignRole(String userId, String roleName);

    void removeRole(String userId, String roleName);

    List<Map<String, Object>> listIdentityProviders();

    List<UserRepresentation> identityProviderUsers(String alias, int first, int max);

    List<FederatedIdentityRepresentation> userIdentities(String userId);

    List<UserRepresentation> searchUsers(KeycloakUserFilter filter, int first, int max);
}
