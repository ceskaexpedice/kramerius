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
import java.sql.SQLException;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.filefilter.RegexFileFilter;

import com.google.inject.Inject;

import cz.incad.Kramerius.security.userscommands.ServletUsersCommand;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.users.NotActivatedUsersSingleton;
import cz.incad.kramerius.utils.ApplicationURL;

public class PublicUserActivation extends ServletUsersCommand {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(PublicUserActivation.class.getName());
    
    public static final String KEY = "key";
    
    @Inject
    NotActivatedUsersSingleton notActivatedUsersSingleton;
    
    @Override
    public void doCommand() throws IOException {
        try {
            HttpServletRequest request = this.requestProvider.get();
            String keyParam = request.getParameter(KEY);
            User user = this.notActivatedUsersSingleton.getNotActivatedUser(keyParam);
            if (user != null) {
                this.userManager.activateUser(user);
            }
            String appContext = ApplicationURL.applicationContextPath(request);
            this.responseProvider.get().sendRedirect("/"+appContext+"/useractivated.jsp");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        }
    }
}
