/*
 * Copyright (C) 2010 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.Kramerius.security;

import java.security.Principal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.management.RuntimeErrorException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.security.Group;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.RightsManager;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.impl.UserImpl;
import cz.incad.kramerius.security.jaas.K4LoginModule;
import cz.incad.kramerius.security.jaas.K4UserPrincipal;


public class CurrentLoggedUserProvider implements Provider<User> {
    
    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(CurrentLoggedUserProvider.class.getName());
    
    // TODO: Presunout jinam!
    public static final String SECURITY_FOR_REPOSITORY_KEY = "securityForRepository";
    
    public static final String USER_NAME_PARAM="userName";
    public static final String PSWD_PARAM = "pswd";
    
    
    @Inject
    Provider<HttpServletRequest> provider;
    
    @Inject
    UserManager userManager;
    
    
    @Inject
    IsActionAllowed isActionAllowed;
    
    @Inject
    @Named("kramerius4")
    Provider<Connection> connectionProvider;

    
    static Group commonUsersGroup = null;
//    static Group globalAdminGroup = null;
    
    @Override
    public User get() {
        try {
            HttpServletRequest httpServletRequest = this.provider.get();
            Principal principal = httpServletRequest.getUserPrincipal();
            if (principal != null) {
                
                K4UserPrincipal k4principal = (K4UserPrincipal) principal;
                User user = k4principal.getUser();
                associateCommonGroup(user);

                HttpServletRequest request = this.provider.get();
                HttpSession session = request.getSession(true);
                if (session.getAttribute(SECURITY_FOR_REPOSITORY_KEY) == null) {
                    saveRightsIntoSession(user);
                }
                
                return user;

            } else if ((httpServletRequest.getParameter(USER_NAME_PARAM)!= null) && (httpServletRequest.getParameter(PSWD_PARAM)!= null)) {
                HashMap<String, Object> foundUser = K4LoginModule.findUser(this.connectionProvider.get(), httpServletRequest.getParameter(USER_NAME_PARAM));
                if (foundUser != null) {
                    User dbUser = (User) foundUser.get("user");
                    String dbPswd = (String) foundUser.get("pswd");
                    if (K4LoginModule.checkPswd(httpServletRequest.getParameter(USER_NAME_PARAM), dbPswd, httpServletRequest.getParameter(PSWD_PARAM).toCharArray())) {
                        Group[] grps = userManager.findGroupsForGivenUser(dbUser.getId());
                        //TODO: Zmenit
                        ((UserImpl)dbUser).setGroups(grps);
                        associateCommonGroup(dbUser);
                        return dbUser;
                    } else return getNotLoggedUser();
                } else {
                    return getNotLoggedUser();
                }
            } else {
                return getNotLoggedUser();
            }
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }


public User getNotLoggedUser() {
    LOGGER.info("PROVIDER ~ noe principal ");
    
    UserImpl user = new UserImpl(-1, "not_logged", "not_logged", "not_logged", -1);
    user.setGroups(new Group[] {});
    associateCommonGroup(user);
    LOGGER.info("PROVIDER user instance 0x"+Integer.toHexString(System.identityHashCode(user)));
    return user;
}


    // ?? Synchronizace !!
    private synchronized void saveRightsIntoSession(User user) {
        List<String> actionsForUser = new ArrayList<String>();
        HttpServletRequest request = this.provider.get();
        HttpSession session = request.getSession(true);
        if (session.getAttribute(SECURITY_FOR_REPOSITORY_KEY) == null) {
            SecuredActions[] values = SecuredActions.values();
            for (SecuredActions securedAction : values) {
                if (isActionAllowed.isActionAllowed(user, securedAction.getFormalName(), SpecialObjects.REPOSITORY.getUuid(), new String[0])) {
                    actionsForUser.add(securedAction.getFormalName());
                }
            }
            session.setAttribute(SECURITY_FOR_REPOSITORY_KEY, actionsForUser);
        }
    }

 
    public void associateCommonGroup(User user) {
        Group commonGroup = null;
        if (commonUsersGroup == null) {
            commonGroup = findCommonGoup();
        } else {
            commonGroup = findCommonGoup();
        }
        boolean containsCommonGroup = false;
        Group[] grps = user.getGroups();
        for (Group group : grps) {
            if (commonGroup.equals(group)) {
                containsCommonGroup = true;
                break;
            }
        }
        if (!containsCommonGroup) {
            Group[] newGroups = new Group[grps.length +1];
            System.arraycopy(grps, 0, newGroups, 0, grps.length);
            newGroups[grps.length] = findCommonGoup();
            ((UserImpl)user).setGroups(newGroups);
        }
    }
    
    
    // synchronizace
    private synchronized Group findCommonGoup() {
        if (commonUsersGroup == null) {
            commonUsersGroup =  userManager.findCommonUsersGroup();
        }
        return commonUsersGroup;
    }

}
