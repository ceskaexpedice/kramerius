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

import cz.incad.Kramerius.security.RightsServlet;
import cz.incad.Kramerius.security.userscommands.ServletUsersCommand;
import cz.incad.kramerius.security.Right;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.utils.PasswordDigest;

public class SaveNewPassword extends ServletUsersCommand {

    private static final String OLD_PSWD_PARAM = "opswd";
    private static final String PSWD_PARAM = "nswpd";
    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(SaveNewPassword.class.getName());
    
    @Override
    public void doCommand() {
        try {
            HttpServletRequest req = this.requestProvider.get();
            String newPswd = req.getParameter(PSWD_PARAM);
            String oldPswd = req.getParameter(OLD_PSWD_PARAM);
            User user = this.userProvider.get();
            if (user.getId() > 0) {
                if (this.userManager.validatePassword(user.getId(), oldPswd)) {
                    newPswd = PasswordDigest.messageDigest(newPswd);
                    this.userManager.saveNewPassword(this.userProvider.get().getId(), newPswd);
                    
                } else {
                    this.responseProvider.get().sendError(HttpServletResponse.SC_BAD_REQUEST);
                }
            } else {
                this.responseProvider.get().sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            try {
                this.responseProvider.get().sendError(HttpServletResponse.SC_BAD_REQUEST);
            } catch (IOException e1) {
                LOGGER.log(Level.SEVERE, e.getMessage(),e);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            try {
                this.responseProvider.get().sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (IOException e1) {
                LOGGER.log(Level.SEVERE, e.getMessage(),e);
            }
        } catch(Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            try {
                this.responseProvider.get().sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (IOException e1) {
                LOGGER.log(Level.SEVERE, e.getMessage(),e);
            }
        }
    }
}
