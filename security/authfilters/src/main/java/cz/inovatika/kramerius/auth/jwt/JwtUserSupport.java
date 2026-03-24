package cz.inovatika.kramerius.auth.jwt;

import cz.incad.kramerius.auth.thirdparty.impl.AbstractThirdPartyUsersSupport;
import cz.incad.kramerius.auth.thirdparty.keycloack.dnnt.StandardDNNTUsersSupport;
import cz.incad.kramerius.auth.thirdparty.keycloack.utils.BaseUsersFunctions;
import cz.incad.kramerius.auth.utils.GeneratePasswordUtils;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.utils.UserUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * JwtUserSupport
 *
 * @author ppodsednik
 */
public class JwtUserSupport extends AbstractThirdPartyUsersSupport<JwtUserWrapper> {

    private static final Logger LOGGER = Logger.getLogger(JwtUserSupport.class.getName());
    private static final String EDU_PERSON_UNIQUE_ID = "eduPersonUniqueId";

    @Override
    protected String updateExistingUser(String userName, JwtUserWrapper user) throws Exception {
        User u = this.usersManager.findUserByLoginName(userName);

        UserUtils.associateGroups(u, this.usersManager);
        UserUtils.associateCommonGroup(u, this.usersManager);
        UserUtils.associateAuthenticatedGroup(u, this.usersManager);

        String password = GeneratePasswordUtils.generatePswd();

        List<String> rolesFromJwt =
                BaseUsersFunctions.checkRolesExists(this.usersManager, user)
                        .stream()
                        .map(Role::getName)
                        .collect(Collectors.toList());
        if (!rolesFromJwt.isEmpty()) {
            this.usersManager.changeRoles(u, user.getRoles());
        }
        return password;
    }

    @Override
    protected boolean checkIfUserExists(String userName) throws Exception {
        User user = this.usersManager.findUserByLoginName(userName);
        return user != null;
    }

    @Override
    protected String createNewUser(String user, JwtUserWrapper w) throws Exception {
        return BaseUsersFunctions.createNewUser(usersManager, w);
    }

    @Override
    protected JwtUserWrapper createUserWrapper(HttpServletRequest req, String userName) {
        JwtAccount jwtAccount = (JwtAccount) req.getAttribute(JwtAccount.class.getName());
        Map<String, String> claims = jwtAccount.getClaims();

        JwtUserWrapper jwtUserWrapper = new JwtUserWrapper(userName);
        jwtUserWrapper.setRoles(new ArrayList<>(jwtAccount.getRoles()));
        setIfExists(jwtUserWrapper, UserUtils.FIRST_NAME_KEY, claims.get("given_name"));
        setIfExists(jwtUserWrapper, UserUtils.LAST_NAME_KEY, claims.get("family_name"));
        setIfExists(jwtUserWrapper, UserUtils.EMAIL_KEY, claims.get("email"));
        setIfExists(jwtUserWrapper, "expiration_time", claims.get("exp"));
        setIfExists(jwtUserWrapper, "authentication_time", claims.get("auth_time"));
        if (claims.get("exp")!= null && claims.get("auth_time") != null) {
            jwtUserWrapper.setProperty("expires_in",  "" + (Long.valueOf(claims.get("exp")) - Long.valueOf(claims.get("auth_time"))));
        } else if (claims.get("exp")!= null && claims.get("iat") != null) {
            jwtUserWrapper.setProperty("expires_in",  "" + (Long.valueOf(claims.get("exp")) - Long.valueOf(claims.get("iat"))));
        }
        setIfExists(jwtUserWrapper, "token_id", claims.get("jti"));
        claims.forEach((k, v) -> {
            if (v != null) {
                jwtUserWrapper.setProperty(k, v.toString());
            }
        });
        boolean cdkServerMode = KConfiguration.getInstance().getConfiguration().getBoolean("cdk.server.mode", false);
        if (cdkServerMode) {
            Set<String> allKeys = jwtUserWrapper.getPropertyKeys();
            if (allKeys.contains("eduPersonScopedAffiliation") && !allKeys.contains("affiliation")) {
                jwtUserWrapper.setProperty("affiliation", jwtUserWrapper.getProperty("eduPersonScopedAffiliation"));
            }
            if (allKeys.contains("eduPersonEntitlement") && !allKeys.contains("entitlement")) {
                jwtUserWrapper.setProperty("entitlement", jwtUserWrapper.getProperty("eduPersonEntitlement")
                );
            }
            if (allKeys.contains("preferred_username") && !allKeys.contains("remote_user")) {
                jwtUserWrapper.setProperty("remote_user", jwtUserWrapper.getProperty("preferred_username"));
            }
        }
        StandardDNNTUsersSupport.makeSureDNNTUsersRole(jwtUserWrapper);
        return jwtUserWrapper;
    }

    @Override
    public String calculateUserName(HttpServletRequest request) {
        JwtAccount principal = (JwtAccount) request.getAttribute(JwtAccount.class.getName());
        if (principal == null) {
            return null;
        }
        Map<String, String> claims = principal.getClaims();
        if (claims.containsKey(EDU_PERSON_UNIQUE_ID)) {
            return claims.get(EDU_PERSON_UNIQUE_ID).toString();
        }
        return principal.getUsername();
    }

    private void setIfExists(JwtUserWrapper user, String key, Object value) {
        if (value != null) {
            user.setProperty(key, value.toString());
        }
    }
}