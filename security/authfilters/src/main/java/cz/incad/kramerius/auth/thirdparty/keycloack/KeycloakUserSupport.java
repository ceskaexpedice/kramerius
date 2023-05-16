package cz.incad.kramerius.auth.thirdparty.keycloack;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import cz.incad.kramerius.auth.thirdparty.ThirdPartyUsersSupport;
import cz.incad.kramerius.auth.thirdparty.impl.AbstractThirdPartyUser;
import cz.incad.kramerius.auth.thirdparty.impl.AbstractThirdPartyUsersSupport;
import cz.incad.kramerius.auth.thirdparty.keycloack.utils.BaseUsersFunctions;
import cz.incad.kramerius.auth.utils.GeneratePasswordUtils;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.impl.UserImpl;
import cz.incad.kramerius.security.utils.UserUtils;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.spi.KeycloakAccount;
import org.keycloak.representations.AccessToken;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class KeycloakUserSupport extends AbstractThirdPartyUsersSupport<Keycloak3rdUser> {

    public static final Logger LOGGER = Logger.getLogger(KeycloakUserSupport.class.getName());

    @Override
    protected String updateExistingUser(String userName, Keycloak3rdUser kUser) throws Exception {
        User u = this.usersManager.findUserByLoginName(userName);

        UserUtils.associateGroups(u, this.usersManager);
        UserUtils.associateCommonGroup(u, this.usersManager);
        String password = GeneratePasswordUtils.generatePswd();

        User userByLoginName = this.usersManager.findUserByLoginName(userName);
        Role[] groups = userByLoginName.getGroups();

        List<String> fromDb = Arrays.stream(groups).map(Role::getName).collect(Collectors.toList());
        List<String> fromKeycloack = BaseUsersFunctions.checkRolesExists(this.usersManager, kUser).stream().map(Role::getName).collect(Collectors.toList());

        if (!fromKeycloack.isEmpty()) {
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
        return BaseUsersFunctions.createNewUser(usersManager, w);
    }

    @Override
    protected Keycloak3rdUser createUserWrapper(HttpServletRequest req, String userName) throws Exception {
        String name = req.getUserPrincipal().getName();
        // keycloak introspection
        KeycloakAccount kAcc = (KeycloakAccount) req.getAttribute(KeycloakAccount.class.getName());
        Set<String> roleSet = new HashSet<>(kAcc.getRoles());

        Keycloak3rdUser keycloack3rdUser = new Keycloak3rdUser(calculateUserName(req));

        AccessToken token = ((KeycloakPrincipal<KeycloakSecurityContext>) req.getUserPrincipal()).getKeycloakSecurityContext().getToken();
        roleSet.addAll(token.getRealmAccess().getRoles());

        keycloack3rdUser.setRoles(new ArrayList<>(roleSet));
        
        
        keycloack3rdUser.setProperty(UserUtils.FIRST_NAME_KEY, token.getGivenName());
        keycloack3rdUser.setProperty(UserUtils.LAST_NAME_KEY, token.getFamilyName());
        keycloack3rdUser.setProperty(UserUtils.EMAIL_KEY, token.getEmail());
        keycloack3rdUser.setProperty("expiration_time", ""+token.getExp());
        keycloack3rdUser.setProperty("authentication_time", ""+token.getAuth_time());
        keycloack3rdUser.setProperty("preffered_user_name", token.getPreferredUsername());
        if (token.getExp()!= null && token.getAuth_time() != null) {
            keycloack3rdUser.setProperty("expires_in", "" + (token.getExp() - token.getAuth_time()));
        } else if (token.getExp()!= null && token.getIat() != null) {
            keycloack3rdUser.setProperty("expires_in", "" + (token.getExp() - token.getIat()));
        }
        keycloack3rdUser.setProperty("token_id", ""+token.getId());

        
        LOGGER.fine("Token id: "+token.getId() +", and returned claims:"+ token.getOtherClaims());
        Map<String, Object> otherClaims = token.getOtherClaims();
        otherClaims.keySet().forEach(key-> {
            Object object = otherClaims.get(key);
            if (object != null) {
                LOGGER.log(Level.FINE,"Key value  "+key+" = "+object.toString());
                keycloack3rdUser.setProperty(key, object.toString());
            }
        });

        
        return keycloack3rdUser;
    }

    @Override
    public String calculateUserName(HttpServletRequest request) {
        if (request.getUserPrincipal() != null) {
            if (request.getUserPrincipal() instanceof KeycloakPrincipal) {
                AccessToken token = ((KeycloakPrincipal) request.getUserPrincipal()).getKeycloakSecurityContext()
                        .getToken();
                if (token.getEmail() != null) {
                    return token.getEmail();
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
