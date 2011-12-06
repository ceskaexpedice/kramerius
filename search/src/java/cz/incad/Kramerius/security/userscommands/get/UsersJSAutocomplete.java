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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.antlr.stringtemplate.StringTemplate;

import cz.incad.Kramerius.security.ServletCommand;
import cz.incad.Kramerius.security.userscommands.ServletUsersCommand;
import cz.incad.Kramerius.security.utils.UserFieldParser;
import cz.incad.kramerius.security.AbstractUser;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.pid.LexerException;

@Deprecated
public class UsersJSAutocomplete extends ServletUsersCommand {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(UsersJSAutocomplete.class.getName());
    
    @Override
    public void doCommand() {
        try {
            List<AbstractUser> ausers = new ArrayList<AbstractUser>();
            String autocompletetype = requestProvider.get().getParameter("autcompletetype");
            String prefix = requestProvider.get().getParameter("t");
            try {
                UserFieldParser fparser = new UserFieldParser(prefix);
                fparser.parseUser();
                prefix = fparser.getUserValue();
            } catch (LexerException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(),e);
            }

            User user = this.userProvider.get();
            if (hasCurrentUserHasSuperAdminRole(user)) {
                if (autocompletetype.equals("group")) {
                    Role[] groups = userManager.findRoleByPrefix(prefix.trim());
                    for (Role grp : groups) {
                        ausers.add(grp);
                    }
                } else {
                    User[] users = userManager.findUserByPrefix(prefix.trim());
                    for (User auser : users) {
                        ausers.add(auser);
                    }
                }
            } else {
                int[] grps = getUserGroups(user);
                if (autocompletetype.equals("group")) {
                    Role[] groups = userManager.findRoleByPrefixForRoles(prefix.trim(),grps );
                    for (Role grp : groups) {
                        ausers.add(grp);
                    }
                } else {
                    User[] users = userManager.findUserByPrefixForRoles(prefix.trim(), grps);
                    for (User auser : users) {
                        ausers.add(auser);
                    }
                }
            }
            
                
            StringTemplate template = ServletUsersCommand.stJSDataGroup().getInstanceOf("userAutocomplete");
            template.setAttribute("type", autocompletetype);
            template.setAttribute("users", ausers);
            
            String content = template.toString();
            responseProvider.get().getOutputStream().write(content.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        }
    }
}
