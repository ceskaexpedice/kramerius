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
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

import org.antlr.stringtemplate.StringTemplate;

import cz.incad.kramerius.security.User;

public class EditRoleHtml extends AbstractRoleCommand {

    private static final String ROLENAME = "rolename";

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(EditRoleHtml.class.getName());
    
    @Override
    public void doCommand() {
        try {
            User user = this.userProvider.get();
            HttpServletRequest req = this.requestProvider.get();
            String rolename = req.getParameter(ROLENAME);
            StringTemplate template = htmlTemplate(user, rolename);
            
            responseProvider.get().getOutputStream().write(template.toString().getBytes("UTF-8"));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }
}
