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
package cz.incad.Kramerius.security.rightscommands.post;

import java.sql.SQLException;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.NotImplementedException;

import cz.incad.Kramerius.security.RightsServlet;
import cz.incad.Kramerius.security.ServletCommand;
import cz.incad.Kramerius.security.rightscommands.ServletRightsCommand;
import cz.incad.kramerius.security.Right;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SecurityException;

public class Create extends ServletRightsCommand {
    
    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(Create.class.getName());

    
    
    @Override
    public void doCommand() {
        try {
            HttpServletRequest req = this.requestProvider.get();
            Right right = RightsServlet.createRightFromPost(req, rightsManager, userManager, criteriumWrapperFactory);

            /*
            String uuid = right.getPid().substring("uuid:".length());
            String[] pathOfUUIDs = this.solrAccess.getPath(uuid);

            if (this.actionAllowed.isActionAllowed(SecuredActions.ADMINISTRATE.getFormalName(), uuid, pathOfUUIDs)) {
                rightsManager.insertRight(right);
            } else {
                throw new SecurityException("operation is not permited");
            }
            */
            throw new NotImplementedException("not implemented");

        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
//        } catch (SQLException e) {
//            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        } catch(Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        }
    }
}
