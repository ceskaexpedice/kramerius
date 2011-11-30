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
package cz.incad.Kramerius.security.userscommands.post;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.impl.RoleImpl;
import cz.incad.kramerius.security.impl.UserImpl;

public class RegisterPublicUser extends AbstractPostUser{

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(CreateRole.class.getName());
    
    @Override
    public void doCommand() throws IOException {
        try {
            HttpServletRequest req = this.requestProvider.get();
            String loginName = req.getParameter(LOGIN_NAME);
            String surname = req.getParameter(SURNAME);
            String firstName = req.getParameter(FIRSTNAME);
            String pswd = req.getParameter(PASSWORD);

            UserImpl user = new UserImpl(-1, firstName, surname, loginName, -1);
            this.userManager.insertPublicUser(user);
            //this.userManager.insertRole(role);
        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            this.responseProvider.get().sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    
}