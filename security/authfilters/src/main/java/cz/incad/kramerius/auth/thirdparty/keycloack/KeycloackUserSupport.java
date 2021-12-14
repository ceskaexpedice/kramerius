package cz.incad.kramerius.auth.thirdparty.keycloack;

import cz.incad.kramerius.auth.thirdparty.ThirdPartyUsersSupport;
import cz.incad.kramerius.auth.thirdparty.impl.AbstractThirdPartyUsersSupport;
import cz.incad.kramerius.auth.utils.GeneratePasswordUtils;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.impl.UserImpl;
import cz.incad.kramerius.security.utils.UserUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class KeycloackUserSupport extends AbstractThirdPartyUsersSupport<Keycloack3rdUser> {



    @Override
    protected String updateExistingUser(String userName, Keycloack3rdUser kuser) throws Exception {
        return null;
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
        List<String> roles = w.getRoles();
        this.usersManager.insertUser(u, password);
        this.usersManager.activateUser(u);
        u = this.usersManager.findUserByLoginName(w.getCalculatedName());
        if (roles.size() > 0) {
            this.usersManager.changeRoles(u, roles);
        }
        return password;
    }

    @Override
    protected Keycloack3rdUser createUserWrapper(HttpServletRequest req, String userName) throws Exception {
        String name = req.getUserPrincipal().getName();
        Keycloack3rdUser keycloack3rdUser = new Keycloack3rdUser(name);
        List<String> roles = new ArrayList<>();
        Role[] allRoles = this.usersManager.findAllRoles("");
        for (Role r:  allRoles) {
            if(req.isUserInRole(r.getName())) {
                roles.add(r.getName());
            }
        }
        keycloack3rdUser.setRoles(roles);
        return keycloack3rdUser;
    }

    @Override
    public String calculateUserName(HttpServletRequest request) {
        return request.getUserPrincipal().getName();
    }
}
