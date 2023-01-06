package cz.incad.kramerius.auth.thirdparty.keycloack.utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import cz.incad.kramerius.auth.thirdparty.impl.AbstractThirdPartyUser;
import cz.incad.kramerius.auth.thirdparty.keycloack.Keycloak3rdUser;
import cz.incad.kramerius.auth.thirdparty.keycloack.KeycloakUserSupport;
import cz.incad.kramerius.auth.utils.GeneratePasswordUtils;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.impl.RoleImpl;
import cz.incad.kramerius.security.impl.UserImpl;
import cz.incad.kramerius.security.utils.UserUtils;

public class BaseUsersFunctions {

    private BaseUsersFunctions() {}

    public static String updateUser(UserManager usersManager, String userName, AbstractThirdPartyUser kUser) throws SQLException {
        User u = usersManager.findUserByLoginName(userName);

        UserUtils.associateGroups(u, usersManager);
        UserUtils.associateCommonGroup(u, usersManager);
        String password = GeneratePasswordUtils.generatePswd();

        User userByLoginName = usersManager.findUserByLoginName(userName);
        Role[] groups = userByLoginName.getGroups();

        List<String> fromDb = Arrays.stream(groups).map(Role::getName).collect(Collectors.toList());
        List<String> fromKeycloack = BaseUsersFunctions.checkRolesExists(usersManager, kUser).stream().map(Role::getName).collect(Collectors.toList());

        if (!fromKeycloack.isEmpty()) {
            usersManager.changeRoles(u, kUser.getRoles());
        }
        return password;

    }
    
    
    public static String createNewUser(UserManager usersManager, AbstractThirdPartyUser w) throws SQLException {
        User u = new UserImpl(-1,
                w.getProperty(UserUtils.FIRST_NAME_KEY) !=  null ? w.getProperty(UserUtils.FIRST_NAME_KEY) : "" ,
                w.getProperty(UserUtils.LAST_NAME_KEY) != null ? w.getProperty(UserUtils.FIRST_NAME_KEY) : "",
                w.getCalculatedName(), -1);

        String password = GeneratePasswordUtils.generatePswd();
        List<Role> roles = BaseUsersFunctions.checkRolesExists(usersManager,w);
        ((UserImpl) u).setGroups(roles.toArray(new Role[roles.size()]));

        usersManager.insertUser(u, password);
        usersManager.activateUser(u);
        u = usersManager.findUserByLoginName(w.getCalculatedName());
        if (roles.size() > 0) {
            usersManager.changeRoles(u, roles.stream().map(Role::getName).collect(Collectors.toList()));
        }
        return password;

    }
    
    public static List<Role> checkRolesExists(UserManager usersManager, AbstractThirdPartyUser w) {
        List<String> roleString = w.getRoles();
        List<Role> roles = new ArrayList<>();
        roleString.stream().forEach(r-> {
            Role roleByName = usersManager.findRoleByName(r);
            if (roleByName != null) {  roles.add(roleByName);  }
            else {
                try {
                    Role nr = new RoleImpl(-1, r,-1);
                    usersManager.insertRole(nr);
                    Role nCreated = usersManager.findRoleByName(r);
                    if (nCreated != null) { roles.add(nCreated); }
                } catch (SQLException e) {
                    KeycloakUserSupport.LOGGER.log(Level.SEVERE, e.getMessage(),e);
                }
            }
        });
        return roles;
    }

}
