package cz.inovatika.kramerius.auth.jwt;

import java.util.Map;
import java.util.Set;

/**
 * JwtUser
 * @author ppodsednik
 */
public class JwtAccount {
    private String username;
    private Set<String> roles;
    private Map<String,String> claims;

    public JwtAccount(String username, Set<String> roles, Map<String,String> claims) {
        this.username = username;
        this.roles = roles;
        this.claims = claims;
    }

    public String getUsername() {
        return username;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public Map<String, String> getClaims() {
        return claims;
    }
}