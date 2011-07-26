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
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.http.HttpServletResponse;

import org.antlr.stringtemplate.StringTemplate;

import com.google.inject.Inject;

import cz.incad.Kramerius.security.userscommands.ServletUsersCommand;
import cz.incad.kramerius.users.LoggedUsersSingleton;

public class ChangePassword extends ServletUsersCommand {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ChangePassword.class.getName());

    @Inject
    LoggedUsersSingleton loggedUsersSingleton;
    
    @Override
    public void doCommand() {
        try {
            if (this.loggedUsersSingleton.isLoggedUser(this.requestProvider)) {
                StringTemplate template = ServletUsersCommand.stFormsGroup().getInstanceOf("changePswd");
                Map<String, String> bundleToMap = bundleToMap(); 
                template.setAttribute("bundle", bundleToMap);
                String content = template.toString();
                responseProvider.get().setContentType("text/html");
                responseProvider.get().getOutputStream().write(content.getBytes("UTF-8"));
            } else {
                responseProvider.get().sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        
    }

    
}
