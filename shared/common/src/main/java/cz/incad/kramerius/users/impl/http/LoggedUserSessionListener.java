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
package cz.incad.kramerius.users.impl.http;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.google.inject.Injector;

import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.utils.UserUtils;
import cz.incad.kramerius.users.LoggedUsersSingleton;

public class LoggedUserSessionListener implements HttpSessionListener {

    @Override
    public void sessionCreated(HttpSessionEvent evt) {
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent evt) {
        HttpSession session = evt.getSession();
        Injector injector = (Injector) session.getServletContext().getAttribute(Injector.class.getName());
        LoggedUsersSingleton loggedUsersSingleton = injector.getInstance(LoggedUsersSingleton.class);
        
        String userKey = (String) session.getAttribute(UserUtils.LOGGED_USER_KEY_PARAM);
        if (userKey != null) {
            loggedUsersSingleton.deregisterLoggedUser(userKey);
        }
    }
}
