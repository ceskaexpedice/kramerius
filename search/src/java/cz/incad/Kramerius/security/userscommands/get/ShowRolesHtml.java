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
package cz.incad.Kramerius.security.userscommands.get;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import javax.servlet.http.HttpServletResponse;

import org.antlr.stringtemplate.StringTemplate;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.Kramerius.security.rightscommands.ServletRightsCommand;
import cz.incad.Kramerius.security.strenderers.RoleWrapper;
import cz.incad.Kramerius.security.userscommands.ServletUsersCommand;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.users.LoggedUsersSingleton;
import cz.incad.utils.K4Collections;

public class ShowRolesHtml extends AbstractRoleCommand {

    java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ShowRolesHtml.class.getName());

    @Inject
    IsActionAllowed actionAllowed;
    
    @Inject
    LoggedUsersSingleton loggedUsersSingleton;
    
    @Override
    public void doCommand() {
        try {
            if (this.loggedUsersSingleton.isLoggedUser(this.requestProvider)) {
                StringTemplate template = ServletUsersCommand.stFormsGroup().getInstanceOf("rolesTable");
                
                User user = this.userProvider.get();
                String prefix = this.requestProvider.get().getParameter("prefix");
                if (prefix == null) prefix ="";
                int[] grps = getUserGroups(user);
                final int[] usedRoles = this.rightsManager.findUsedRoleIDs();
                Arrays.sort(usedRoles);
                Role[] groups = null;
                if (hasCurrentUserHasSuperAdminRole(user)) {
                    groups = userManager.findAllRoles(prefix);
                } else {
                    groups = userManager.findAllRoles(grps, prefix);
                }
                
                List<RoleWrapper> mappedList = K4Collections.map(RoleWrapper.wrap(Arrays.asList(groups),false), 
                        new K4Collections.Mapper<RoleWrapper>() {
                            @Override
                            public RoleWrapper process(RoleWrapper t, int index) {
                                int indx = Arrays.binarySearch(usedRoles, t.getId());
                                boolean canbedeleted = indx < 0;
                                t.setCanbedeleted(canbedeleted);
                                return t;
                            }
                        });

                
                template.setAttribute("groups", mappedList);
                template.setAttribute("bundle", bundleToMap());

                responseProvider.get().getOutputStream().write(template.toString().getBytes("UTF-8"));

            } else {
                responseProvider.get().sendError(HttpServletResponse.SC_FORBIDDEN);
                
            }
 
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
