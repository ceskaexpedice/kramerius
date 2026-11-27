package cz.incad.kramerius.keycloak;

import org.keycloak.representations.idm.UserRepresentation;

/** A user with the latest successful LOGIN event within the requested window. */
public final class KeycloakUserLoginActivity {
    private final UserRepresentation user;
    private final long lastLogin;
    private final long loginCount;

    public KeycloakUserLoginActivity(UserRepresentation user, long lastLogin, long loginCount) {
        this.user = user;
        this.lastLogin = lastLogin;
        this.loginCount = loginCount;
    }

    public UserRepresentation getUser() {
        return user;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public long getLoginCount() {
        return loginCount;
    }
}
