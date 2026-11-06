package cz.incad.kramerius.keycloak;

import java.util.List;

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
}
