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
package cz.incad.Kramerius.security.rightscommands.get;

import java.io.IOException;
import java.util.logging.Level;

import org.antlr.stringtemplate.StringTemplate;

import com.google.inject.Inject;

import cz.incad.Kramerius.security.rightscommands.ServletRightsCommand;
import cz.incad.kramerius.security.Group;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.User;

public class ShowRolesHtml extends ServletRightsCommand {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ShowRightsHtml.class.getName());

    @Inject
    transient IsActionAllowed actionAllowed;

    @Override
    public void doCommand() {
        try {
            StringTemplate template = ServletRightsCommand.stFormsGroup().getInstanceOf("rolesTable");
            
            User user = this.userProvider.get();
            String prefix = this.requestProvider.get().getParameter("prefix");
            if (prefix == null) prefix ="";
            int[] grps = getUserGroups(user);
            Group[] groups = null;
            if (hasCurrentUserHasSuperAdminRole(user)) {
                groups = userManager.findAllGroups(prefix);
            } else {
                groups = userManager.findAllGroups(grps, prefix);
            }

            template.setAttribute("groups", groups);
            template.setAttribute("bundle", bundleToMap());

            /*
            template.setAttribute("rights", RightWrapper.wrapRights(fedoraAccess, resultRights));
            template.setAttribute("uuid", uuid);
            template.setAttribute("users", wrapped);
            template.setAttribute("typeOfLists", TypeOfList.typeOfListAsMap(typeOfList));
            template.setAttribute("action", new SecuredActionWrapper(getResourceBundle(), SecuredActions.findByFormalName(getSecuredAction())));
            template.setAttribute("canhandlecommongroup", userProvider.get().hasSuperAdministratorRole());
            */
            responseProvider.get().getOutputStream().write(template.toString().getBytes("UTF-8"));

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
