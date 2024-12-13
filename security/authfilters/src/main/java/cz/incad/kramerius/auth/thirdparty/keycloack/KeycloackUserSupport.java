package cz.incad.kramerius.auth.thirdparty.keycloack;

import cz.incad.kramerius.auth.thirdparty.keycloack.Keycloak3rdUser;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import cz.incad.kramerius.auth.thirdparty.ThirdPartyUsersSupport;
import cz.incad.kramerius.auth.thirdparty.impl.AbstractThirdPartyUsersSupport;
import cz.incad.kramerius.auth.utils.GeneratePasswordUtils;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.impl.RoleImpl;
import cz.incad.kramerius.security.impl.UserImpl;
import cz.incad.kramerius.security.utils.UserUtils;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.spi.KeycloakAccount;
import org.keycloak.representations.AccessToken;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class KeycloackUserSupport extends AbstractThirdPartyUsersSupport<Keycloak3rdUser> {
																		 
    private static final String EDU_PERSON_UNIQUE_ID = "eduPersonUniqueId";

    public static final Logger LOGGER = Logger.getLogger(KeycloackUserSupport.class.getName());

    public static final String KEYCLOACK_USER_PREFIX = "_keycloack_";

//    public final AuthenticatedUser getAuthenticatedUserByOauth() throws ProxyAuthenticationRequiredException {
//        KeycloakAccount keycloakAccount = null;
//        try {
//            keycloakAccount = (KeycloakAccount) requestProvider.get().getAttribute(KeycloakAccount.class.getName());
//        }catch (Throwable th){
//            LOGGER.log(Level.INFO,"Error retrieving KeycloakAccount", th);
//        }
//        if (keycloakAccount == null){
//            return  ANONYMOUS;
//        }
//        return new AuthenticatedUser(keycloakAccount.getPrincipal().getName(), keycloakAccount.getPrincipal().getName(), new ArrayList<>(keycloakAccount.getRoles()));
//    }


    @Override
    protected String updateExistingUser(String userName, Keycloak3rdUser kUser) throws Exception {
        User u = this.usersManager.findUserByLoginName(userName);

        UserUtils.associateGroups(u, this.usersManager);
        UserUtils.associateCommonGroup(u, this.usersManager);
        String password = GeneratePasswordUtils.generatePswd();

        User userByLoginName = this.usersManager.findUserByLoginName(userName);
        Role[] groups = userByLoginName.getGroups();

        List<String> fromDb = Arrays.stream(groups).map(Role::getName).collect(Collectors.toList());
        List<String> fromKeycloack = checkRolesExists(kUser).stream().map(Role::getName).collect(Collectors.toList());

        // check if change is neeeded
        int max = Math.min(fromDb.size(), fromKeycloack.size());
        for(int i=0;i<max;i++) {
            fromDb.remove(fromKeycloack.remove(0));
        }

        if (!fromDb.isEmpty() || !fromKeycloack.isEmpty()) {
            this.usersManager.changeRoles(u, kUser.getRoles());
        }
        return password;

    }

    @Override
    protected boolean checkIfUserExists(String userName) throws Exception {
        User user = this.usersManager.findUserByLoginName(userName);
        return user != null;
    }

    @Override
    protected String createNewUser(String user, Keycloak3rdUser w) throws Exception {
        User u = new UserImpl(-1,
                w.getProperty(UserUtils.FIRST_NAME_KEY) !=  null ? w.getProperty(UserUtils.FIRST_NAME_KEY) : "" ,
                w.getProperty(UserUtils.LAST_NAME_KEY) != null ? w.getProperty(UserUtils.FIRST_NAME_KEY) : "",
                w.getCalculatedName(), -1);

        String password = GeneratePasswordUtils.generatePswd();
        List<Role> roles = checkRolesExists(w);
        ((UserImpl) u).setGroups(roles.toArray(new Role[roles.size()]));

        this.usersManager.insertUser(u, password);
        this.usersManager.activateUser(u);
        u = this.usersManager.findUserByLoginName(w.getCalculatedName());
        if (roles.size() > 0) {
            this.usersManager.changeRoles(u, roles.stream().map(Role::getName).collect(Collectors.toList()));
        }
        return password;
    }

    private List<Role> checkRolesExists(Keycloak3rdUser w) {
        List<String> roleString = w.getRoles();
        List<Role> roles = new ArrayList<>();
        roleString.stream().forEach(r-> {
            Role roleByName = this.usersManager.findRoleByName(r);
            if (roleByName != null) {  roles.add(roleByName);  }
            else {
                try {
                    Role nr = new RoleImpl(-1, r,-1);
                    this.usersManager.insertRole(nr);
                    Role nCreated = this.usersManager.findRoleByName(r);
                    if (nCreated != null) { roles.add(nCreated); }
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
                }
            }
        });
        return roles;
    }

    @Override
    protected Keycloak3rdUser createUserWrapper(HttpServletRequest req, String userName) throws Exception {
        String name = req.getUserPrincipal().getName();
        // keyclocak introspection
        KeycloakAccount kAcc = (KeycloakAccount) req.getAttribute(KeycloakAccount.class.getName());
        Set<String> roleSet = kAcc.getRoles();

        Keycloak3rdUser keycloack3rdUser = new Keycloak3rdUser(calculateUserName(req));
        keycloack3rdUser.setRoles(new ArrayList<>(roleSet));

        AccessToken token = ((KeycloakPrincipal<KeycloakSecurityContext>) req.getUserPrincipal()).getKeycloakSecurityContext().getToken();
        String hash = token.getAccessTokenHash();
        String codeHash = token.getCodeHash();
        Long auth_time = token.getAuth_time();
        
        
        keycloack3rdUser.setProperty(UserUtils.FIRST_NAME_KEY, token.getGivenName());
        keycloack3rdUser.setProperty(UserUtils.LAST_NAME_KEY, token.getFamilyName());
        keycloack3rdUser.setProperty(UserUtils.EMAIL_KEY, token.getEmail());
        keycloack3rdUser.setProperty("preffered_user_name", token.getPreferredUsername());

        keycloack3rdUser.setProperty("expiration_time", ""+token.getExp());
        keycloack3rdUser.setProperty("authentication_time", ""+token.getAuth_time());
        keycloack3rdUser.setProperty("preffered_user_name", token.getPreferredUsername());
        keycloack3rdUser.setProperty("expires_in", ""+(token.getExp()-token.getAuth_time()));
        keycloack3rdUser.setProperty("token_id", ""+token.getId());
        keycloack3rdUser.setProperty("remote_user", ""+token.getPreferredUsername());

        
        LOGGER.fine("Token id: "+token.getId() +", and returned claims:"+ token.getOtherClaims());
        Map<String, Object> otherClaims = token.getOtherClaims();
        otherClaims.keySet().forEach(key-> {
            Object object = otherClaims.get(key);
            if (object != null) {
                LOGGER.log(Level.FINE,"Key value  "+key+" = "+object.toString());
                keycloack3rdUser.setProperty(key, object.toString());
            }
        });
        
        /** K5 instance */
        Set<String> allKeys = keycloack3rdUser.getPropertyKeys();
        if (allKeys.contains("eduPersonScopedAffiliation") && !allKeys.contains("affiliation")) {
            keycloack3rdUser.setProperty("affiliation", keycloack3rdUser.getProperty("eduPersonScopedAffiliation"));
        }

        if (allKeys.contains("eduPersonEntitlement") && !allKeys.contains("entitlement")) {
            keycloack3rdUser.setProperty("entitlement", keycloack3rdUser.getProperty("eduPersonEntitlement"));
        }

        return keycloack3rdUser;
    }

    @Override
    public String calculateUserName(HttpServletRequest request) {
        if (request.getUserPrincipal() != null) {
            if (request.getUserPrincipal() instanceof KeycloakPrincipal) {
                AccessToken token = ((KeycloakPrincipal) request.getUserPrincipal()).getKeycloakSecurityContext()
                        .getToken();
                // If the user is logged in via federation, the eduPersonUniqueId attribute is used; otherwise, the preferred_username attribute is used. 
                if (token.getOtherClaims().containsKey(EDU_PERSON_UNIQUE_ID)) {
                    return token.getOtherClaims().get(EDU_PERSON_UNIQUE_ID).toString();
                } else {
                    return token.getPreferredUsername();
                }
            } else {
                return request.getUserPrincipal().getName();
            }
        } else {
            return "null";
        }
    }
}
