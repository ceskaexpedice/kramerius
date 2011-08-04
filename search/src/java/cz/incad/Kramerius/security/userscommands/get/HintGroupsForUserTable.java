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

import org.antlr.stringtemplate.StringTemplate;

import cz.incad.Kramerius.security.strenderers.AbstractUserWrapper;
import cz.incad.Kramerius.security.userscommands.ServletUsersCommand;
import cz.incad.Kramerius.security.utils.UserFieldParser;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.pid.LexerException;

public class HintGroupsForUserTable extends ServletUsersCommand {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(HintGroupsForUserTable.class.getName());
    
    @Override
    public void doCommand() {
        try {
            User user = this.userProvider.get();
            String requestedUser = this.requestProvider.get().getParameter("user");
            try {
                UserFieldParser ufieldParser = new UserFieldParser(requestedUser);
                ufieldParser.parseUser();
                requestedUser = ufieldParser.getUserValue();

            } catch (LexerException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(),e);
            }
            User foundByLoginName = userManager.findUserByLoginName(requestedUser);
            Role[] grps = userManager.findRolesForGivenUser(foundByLoginName.getId());
            
            StringTemplate template = ServletUsersCommand.stFormsGroup().getInstanceOf("groupsTableForUser");
            template.setAttribute("groups", grps);
            template.setAttribute("user", new AbstractUserWrapper(foundByLoginName));
            Map<String, String> bundleToMap = bundleToMap(); {
                StringTemplate userTemplate=  new StringTemplate("$user.loginname$ ($user.firstName$ $user.surname$)");
                userTemplate.setAttribute("user", new AbstractUserWrapper(foundByLoginName));
                bundleToMap.put("rights.dialog.hinted.groupselecteduser", MessageFormat.format(bundleToMap.get("rights.dialog.hinted.groupselecteduser"), userTemplate.toString()));
            }
            String content = template.toString();
            responseProvider.get().getOutputStream().write(content.getBytes("UTF-8"));
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        }
    }
}
