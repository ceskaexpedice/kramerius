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
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

import org.antlr.stringtemplate.StringTemplate;

import cz.incad.Kramerius.security.userscommands.ServletUsersCommand;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;

public class HintAllGroupsTable extends ServletUsersCommand {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(HintAllGroupsTable.class.getName());
    
    @Override
    public void doCommand() {
        try {
            User user = this.userProvider.get();
            String prefix = this.requestProvider.get().getParameter("prefix");
            if (prefix == null) prefix ="";
            int[] grps = getUserGroups(user);
            Role[] groups = null;
            if (hasCurrentUserHasSuperAdminRole(user)) {
                groups = userManager.findAllRoles(prefix);
            } else {
                groups = userManager.findAllRoles(grps, prefix);
            }
            StringTemplate template = ServletUsersCommand.stFormsGroup().getInstanceOf("allGroupsTable");
            template.setAttribute("groups", groups);
            template.setAttribute("bundle", this.resourceBundleService.getResourceBundle("labels", localesProvider.get()));
            String content = template.toString();
            responseProvider.get().getOutputStream().write(content.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        }
    }

    
}
