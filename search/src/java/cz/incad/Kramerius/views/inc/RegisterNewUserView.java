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
package cz.incad.Kramerius.views.inc;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;

public class RegisterNewUserView {

    public static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(RegisterNewUserView.class.getName());
    
    @Inject
    UserManager userManager;
    
    @Inject
    Provider<HttpServletRequest> requestProvider;
    
    public RegisterNewUserView() {
        super();
        LOGGER.fine("Register new user instance");
    }

    public List<String> getUserLoginNames() {
        List<String>loginNames = new ArrayList<String>();
        User[] users = this.userManager.findAllUsers("");
        for (User user : users) {
            loginNames.add(user.getLoginname());
        }
        return loginNames;
        
    }

    
}
