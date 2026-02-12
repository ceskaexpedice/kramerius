package cz.incad.kramerius.auth.thirdparty.shibb.internal;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import cz.incad.kramerius.security.Role;
import org.apache.commons.configuration.ConfigurationException;
import org.json.JSONException;

import cz.incad.kramerius.auth.thirdparty.impl.AbstractThirdPartyUser;
import cz.incad.kramerius.auth.thirdparty.shibb.impl.ShibThirdPartyUsersSupport;
import cz.incad.kramerius.auth.thirdparty.shibb.utils.Shibboleth3rdUser;
import cz.incad.kramerius.auth.utils.GeneratePasswordUtils;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.impl.UserImpl;
import cz.incad.kramerius.security.utils.UserUtils;

public class InternalThirdPartyUsersSupportImpl extends ShibThirdPartyUsersSupport {

    @Override
    protected String updateExistingUser(String userName, Shibboleth3rdUser w) throws SQLException {
        User u = this.usersManager.findUserByLoginName(userName);
        
        UserUtils.associateGroups(u, this.usersManager);
        UserUtils.associateCommonGroup(u, this.usersManager);
        UserUtils.associateAuthenticatedGroup(u, this.usersManager);
        String password = GeneratePasswordUtils.generatePswd();

        User userByLoginName = this.usersManager.findUserByLoginName(userName);
        Role[] groups = userByLoginName.getGroups();

        List<String> fromDb = Arrays.stream(groups).map(Role::getName).collect(Collectors.toList());
        List<String> fromSettings = new ArrayList<>(w.getRoles());

        int max = Math.min(fromDb.size(), fromSettings.size());
        for(int i=0;i<max;i++) {
            fromDb.remove(fromSettings.remove(0));
        }
        if (!fromDb.isEmpty() || !fromSettings.isEmpty()) {
            this.usersManager.changeRoles(u, w.getRoles());
        }
        return password;
    }

    @Override
    protected boolean checkIfUserExists(String userName) throws ConfigurationException, JSONException {
        User user = this.usersManager.findUserByLoginName(userName);
        return user != null;
    }

    @Override
    protected String createNewUser(String user, Shibboleth3rdUser w)
            throws JSONException, ConfigurationException, SQLException {
        User u = new UserImpl(-1, w.getProperty(UserUtils.FIRST_NAME_KEY),
                w.getProperty(UserUtils.LAST_NAME_KEY), w.getCalculatedName(), -1);
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

 
    
    
}
