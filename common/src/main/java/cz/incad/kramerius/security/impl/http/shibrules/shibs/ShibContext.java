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
package cz.incad.kramerius.security.impl.http.shibrules.shibs;

import javax.servlet.http.HttpServletRequest;

import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;

/**
 * Shibboleth runtime context
 * @author pavels
 */
public class ShibContext {
    
    private HttpServletRequest httpServletRequest;
    private User user;
    private UserManager userManager;

    public ShibContext(HttpServletRequest httpServletRequest, User user, UserManager userManager) {
        super();
        this.httpServletRequest = httpServletRequest;
        this.user = user;
        this.userManager = userManager;
    }

    /**
     * Returns actual http servlet request
     * @return
     */
    public HttpServletRequest getHttpServletRequest() {
        return this.httpServletRequest;
    }
    
    /**
     * Returns logged user
     * @return
     */
    public User getUser() {
        return this.user;
    }

    
    /**
     * Returns user manager
     * @return
     */
    public UserManager getUserManager() {
        return this.userManager;
    }
}
