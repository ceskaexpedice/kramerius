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

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.security.Group;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.impl.UserImpl;
import cz.incad.kramerius.security.jaas.K4UserPrincipal;


public class CurrentLoggedUserProvider implements Provider<User>{

    @Inject
    Provider<HttpServletRequest> provider;
    
    @Inject
    UserManager userManager;
    
    @Override
    public User get() {
            Principal principal = this.provider.get().getUserPrincipal();
            if (principal != null) {

                K4UserPrincipal k4principal = (K4UserPrincipal) principal;
                User user = k4principal.getUser();
                return user;

            } else {

                UserImpl user = new UserImpl(-1, "not_logged", "not_logged", "not_logged");
                Group commonUsers = userManager.findCommonUsersGroup();
                user.setGroups(new Group[] { commonUsers });
                
                return user;
            }
    }
    
    

}
