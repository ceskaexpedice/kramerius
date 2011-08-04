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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.antlr.stringtemplate.StringTemplate;

import cz.incad.Kramerius.security.strenderers.AbstractUserWrapper;
import cz.incad.Kramerius.security.userscommands.ServletUsersCommand;
import cz.incad.Kramerius.security.utils.UserFieldParser;
import cz.incad.kramerius.security.AbstractUser;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.pid.LexerException;

public class HintUsersForGroup extends ServletUsersCommand {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(HintUsersForGroup.class.getName());
    
    @Override
    public void doCommand() {
        try {
            User user = this.userProvider.get();
            String requestedGroup = this.requestProvider.get().getParameter("group");
            try {
                UserFieldParser ufieldParser = new UserFieldParser(requestedGroup);
                ufieldParser.parseUser();
                requestedGroup = ufieldParser.getUserValue();
            } catch (LexerException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(),e);
            }
            Role grp = userManager.findRoleByName(requestedGroup);
            User[] users = userManager.findUsersForGivenRole(grp.getId());
            StringTemplate template = ServletUsersCommand.stFormsGroup().getInstanceOf("usersTableForGroup");
            List<AbstractUserWrapper> ausers = new ArrayList<AbstractUserWrapper>();
            for (int i = 0; i < users.length; i++) {
                AbstractUserWrapper awrapper = new AbstractUserWrapper(users[i]);
                ausers.add(awrapper);
            }
            template.setAttribute("grp", grp);
            template.setAttribute("users", ausers);
            Map<String, String> bundleToMap = bundleToMap(); {
                StringTemplate groupTemplate=  new StringTemplate("$grp.name$");
                groupTemplate.setAttribute("grp", new AbstractUserWrapper(grp));
                bundleToMap.put("rights.dialog.hinted.usersselectedgroup", MessageFormat.format(bundleToMap.get("rights.dialog.hinted.usersselectedgroup"), groupTemplate.toString()));
            }
            template.setAttribute("bundle", bundleToMap);
            String content = template.toString();
            responseProvider.get().getOutputStream().write(content.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        }
    }
}
