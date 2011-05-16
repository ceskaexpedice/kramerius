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
package cz.incad.kramerius.security.impl.http;

import java.security.Principal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.jaas.K4LoginModule;
import cz.incad.kramerius.security.jaas.K4UserPrincipal;
import cz.incad.kramerius.security.utils.UserUtils;


public class CurrentLoggedUserProvider implements Provider<User> {
    
    public static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(CurrentLoggedUserProvider.class.getName());
    
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

    
    @Override
    public User get() {
        try {
            HttpServletRequest httpServletRequest = this.provider.get();
            Principal principal = httpServletRequest.getUserPrincipal();
            if (principal != null) {
                
                K4UserPrincipal k4principal = (K4UserPrincipal) principal;
                User user = k4principal.getUser();
                cz.incad.kramerius.security.utils.UserUtils.associateCommonGroup(user, userManager);

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
                        UserUtils.associateGroups(dbUser, userManager);
                        UserUtils.associateCommonGroup(dbUser, userManager);
                        return dbUser;
                    } else return UserUtils.getNotLoggedUser(userManager);
                } else {
                    return UserUtils.getNotLoggedUser(userManager);
                }
            } else {
                return UserUtils.getNotLoggedUser(userManager);
            }
        } catch(Exception e){
            throw new RuntimeException(e);
        }
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

}
