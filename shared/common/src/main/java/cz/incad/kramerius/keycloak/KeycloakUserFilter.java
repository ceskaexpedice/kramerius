package cz.incad.kramerius.keycloak;

import java.util.LinkedHashMap;
import java.util.Map;

public class KeycloakUserFilter {
    public String search;

    public String username;
    public String firstName;
    public String lastName;
    public String email;
    public Boolean enabled;

    public String idpAlias;
    public Map<String, String> attributes = new LinkedHashMap<>();

    public boolean exact;
}
