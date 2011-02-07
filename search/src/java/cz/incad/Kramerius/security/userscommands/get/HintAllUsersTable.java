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
import java.text.MessageFormat;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.http.HttpServletResponse;

import org.antlr.stringtemplate.StringTemplate;

import cz.incad.Kramerius.security.userscommands.ServletUsersCommand;
import cz.incad.kramerius.security.User;

public class HintAllUsersTable extends ServletUsersCommand {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(HintAllUsersTable.class.getName());
    
    @Override
    public void doCommand() {
        try {
            User user = this.userProvider.get();
            String prefix = this.requestProvider.get().getParameter("prefix");
            if (prefix == null) prefix ="";
            int[] grps = getUserGroups(user);
            User[] users = null;
            if (hasCurrentUserHasSuperAdminRole(user)) {
                users = userManager.findAllUsers(prefix);
            } else {
                users = userManager.findAllUsers(grps, prefix);
            }
            StringTemplate template = ServletUsersCommand.stFormsGroup().getInstanceOf("allUsersTable");
            template.setAttribute("users", users);
            Map<String, String> bundleToMap = bundleToMap(); 
            template.setAttribute("bundle", bundleToMap);
            String content = template.toString();
            responseProvider.get().getOutputStream().write(content.getBytes("UTF-8"));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            try {
                this.responseProvider.get().sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (IOException e1) {
                LOGGER.log(Level.SEVERE, e.getMessage(),e);
            }
        }
    }

}
