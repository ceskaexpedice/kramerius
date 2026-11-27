package cz.incad.kramerius.keycloak;

import org.keycloak.representations.idm.UserRepresentation;

import java.util.Collections;
import java.util.List;

/** User representation and realm roles retrieved together from Keycloak. */
public final class KeycloakUserDetails {
    private final UserRepresentation user;
    private final List<String> realmRoles;

    public KeycloakUserDetails(UserRepresentation user, List<String> realmRoles) {
        this.user = user;
        this.realmRoles = realmRoles == null ? Collections.emptyList() : realmRoles;
    }

    public UserRepresentation getUser() {
        return user;
    }

    public List<String> getRealmRoles() {
        return realmRoles;
    }
}
