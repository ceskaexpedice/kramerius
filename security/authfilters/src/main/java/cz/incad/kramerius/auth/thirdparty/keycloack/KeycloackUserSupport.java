package cz.incad.kramerius.auth.thirdparty.keycloack;

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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class KeycloackUserSupport extends AbstractThirdPartyUsersSupport<Keycloack3rdUser> {

    public static final Logger LOGGER = Logger.getLogger(KeycloackUserSupport.class.getName());





    @Override
    protected String updateExistingUser(String userName, Keycloack3rdUser kUser) throws Exception {
        User u = this.usersManager.findUserByLoginName(userName);

        UserUtils.associateGroups(u, this.usersManager);
        UserUtils.associateCommonGroup(u, this.usersManager);
        String password = GeneratePasswordUtils.generatePswd();

        User userByLoginName = this.usersManager.findUserByLoginName(userName);
        Role[] groups = userByLoginName.getGroups();

        List<String> fromDb = Arrays.stream(groups).map(Role::getName).collect(Collectors.toList());
        List<String> fromKeycloack = checkRolesExists(kUser).stream().map(Role::getName).collect(Collectors.toList());

// check if change is neeeded
//        int max = Math.min(fromDb.size(), fromKeycloack.size());
//        for(int i=0;i<max;i++) {
//            fromDb.remove(fromKeycloack.remove(0));
//        }

        
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
    protected String createNewUser(String user, Keycloack3rdUser w) throws Exception {
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

    private List<Role> checkRolesExists(Keycloack3rdUser w) {
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
    protected Keycloack3rdUser createUserWrapper(HttpServletRequest req, String userName) throws Exception {
        String name = req.getUserPrincipal().getName();
        // keyclocak introspection
        KeycloakAccount kAcc = (KeycloakAccount) req.getAttribute(KeycloakAccount.class.getName());
        Set<String> roleSet = kAcc.getRoles();

        Keycloack3rdUser keycloack3rdUser = new Keycloack3rdUser(calculateUserName(req));
        keycloack3rdUser.setRoles(new ArrayList<>(roleSet));

        AccessToken token = ((KeycloakPrincipal<KeycloakSecurityContext>) req.getUserPrincipal()).getKeycloakSecurityContext().getToken();
        
        
        keycloack3rdUser.setProperty(UserUtils.FIRST_NAME_KEY, token.getGivenName());
        keycloack3rdUser.setProperty(UserUtils.LAST_NAME_KEY, token.getFamilyName());
        keycloack3rdUser.setProperty(UserUtils.EMAIL_KEY, token.getEmail());
        keycloack3rdUser.setProperty("expiration_time", ""+token.getExp());
        keycloack3rdUser.setProperty("authentication_time", ""+token.getAuth_time());
        keycloack3rdUser.setProperty("preffered_user_name", token.getPreferredUsername());
        keycloack3rdUser.setProperty("expires_in", ""+(token.getExp()-token.getAuth_time()));
        
        
        return keycloack3rdUser;
    }

    @Override
    public String calculateUserName(HttpServletRequest request) {
        if (request.getUserPrincipal()!= null){
            if (request.getUserPrincipal() instanceof KeycloakPrincipal){
            	AccessToken token =  ((KeycloakPrincipal)request.getUserPrincipal()).getKeycloakSecurityContext().getToken();
            	if (token.getEmail() != null) {
            		return token.getEmail();
            	} else {
            		return token.getPreferredUsername();
            	}
            } else {
                return  request.getUserPrincipal().getName();
            }
        }else{
            return "null";
        }
    }
}
