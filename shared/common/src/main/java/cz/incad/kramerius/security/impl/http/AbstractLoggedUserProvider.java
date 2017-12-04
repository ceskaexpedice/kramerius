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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.utils.UserUtils;
import cz.incad.kramerius.service.TextsService;
import cz.incad.kramerius.users.LoggedUsersSingleton;
import cz.incad.kramerius.users.UserProfile;
import cz.incad.kramerius.users.UserProfileManager;

public abstract class AbstractLoggedUserProvider implements Provider<User>{
    public static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(AbstractLoggedUserProvider.class.getName());

    public static final String SECURITY_FOR_REPOSITORY_KEY = "securityForRepository";

    @Inject
    Provider<HttpServletRequest> provider;

    @Inject
    UserManager userManager;

    @Inject
    IsActionAllowed isActionAllowed;

    @Inject
    @Named("kramerius4")
    Provider<Connection> connectionProvider;

    @Inject
    LoggedUsersSingleton loggedUsersSingleton;
    
    @Inject
    UserProfileManager userProfileManager;

    @Inject
    TextsService textsService;
    
    
    @Override
    public User get() {
        try {
            HttpServletRequest httpServletRequest = this.provider.get();
            // previous logged user
            User loggedUser = getPreviousLoggedUser(httpServletRequest);
            if (loggedUser != null) return loggedUser;

            // try to log
            tryToLog(httpServletRequest);
            
            // returns user from session
            if (httpServletRequest.getSession() != null) {
                loggedUser = (User) httpServletRequest.getSession().getAttribute(UserUtils.LOGGED_USER_PARAM);
                if (loggedUser != null) {
                    return loggedUser;
                } else return UserUtils.getNotLoggedUser(userManager);
            } else {
                return UserUtils.getNotLoggedUser(userManager);
            }
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    protected abstract void tryToLog(HttpServletRequest httpServletRequest) throws NoSuchAlgorithmException, UnsupportedEncodingException, FileNotFoundException, RecognitionException, TokenStreamException, IOException;

    protected abstract User getPreviousLoggedUser(HttpServletRequest httpServletRequest);

    protected synchronized void clearRightsInSession(User user) {
        HttpServletRequest request = this.provider.get();
        HttpSession session = request.getSession();
        if (session.getAttribute(SECURITY_FOR_REPOSITORY_KEY) != null) {
            session.removeAttribute(SECURITY_FOR_REPOSITORY_KEY);
        }
    }
    
    protected synchronized void saveRightsIntoSession(User user) {
        List<String> actionsForUser = new ArrayList<String>();
        HttpServletRequest request = this.provider.get();
        HttpSession session = request.getSession(true);
        if (session.getAttribute(SECURITY_FOR_REPOSITORY_KEY) == null) {
            SecuredActions[] values = SecuredActions.values();
            for (SecuredActions securedAction : values) {
                if (isActionAllowed.isActionAllowed(user, securedAction.getFormalName(), SpecialObjects.REPOSITORY.getPid(), null, ObjectPidsPath.REPOSITORY_PATH)) {
                    actionsForUser.add(securedAction.getFormalName());
                }
            }
            session.setAttribute(SECURITY_FOR_REPOSITORY_KEY, actionsForUser);
        }
    }
    
}
