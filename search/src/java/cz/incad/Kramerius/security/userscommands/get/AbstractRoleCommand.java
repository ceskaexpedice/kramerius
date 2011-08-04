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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.antlr.stringtemplate.StringTemplate;

import cz.incad.Kramerius.security.strenderers.RoleWrapper;
import cz.incad.Kramerius.security.userscommands.ServletUsersCommand;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;
import cz.incad.utils.K4Collections;

public abstract class AbstractRoleCommand extends ServletUsersCommand {

    public Role[] filterCommonGroup(Role[] grps) {
        Role commonGroup = this.userManager.findCommonUsersRole();
        List<Role> grpList = new ArrayList<Role>();
        for (Role group : grps) {
            if (group.getId() != commonGroup.getId()) {
                grpList.add(group);
            }
        }
        return (Role[]) grpList.toArray(new Role[grpList.size()]);
    }

    public StringTemplate htmlTemplate(User user, String rolename) throws IOException {
        Role[] admGroups = null;
        final int personalAdminRoleId = -1;
        
        StringTemplate template = ServletUsersCommand.stFormsGroup().getInstanceOf("oneRole");
        Role role = this.userManager.findRoleByName(rolename);
        
        if (hasCurrentUserHasSuperAdminRole(user)) {
            admGroups = filterCommonGroup(this.userManager.findAllRoles(""));
        } else {
            admGroups = filterCommonGroup(this.userManager.findRoleWhichIAdministrate(getUserGroups(user)));
        }
        
        template.setAttribute("role", role);
        List<RoleWrapper> mappedList = K4Collections.map(RoleWrapper.wrap(Arrays.asList(admGroups),true), 
            new K4Collections.Mapper<RoleWrapper>() {
                @Override
                public RoleWrapper process(RoleWrapper t, int index) {
                    t.setSelected(t.getId() == personalAdminRoleId);
                    return t;
                }
            });
    
        template.setAttribute("admroles", mappedList);
        template.setAttribute("bundle", bundleToMap());
        template.setAttribute("allroles", this.userManager.findAllRoles(""));
        return template;
    }

    
}
