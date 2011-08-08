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

import static cz.incad.utils.IKeys.UUID_PARAMETER;

import java.io.IOException;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.logging.Level;

import javax.servlet.http.HttpServletResponse;

import org.antlr.stringtemplate.StringTemplate;

import com.google.inject.Inject;

import cz.incad.Kramerius.security.ServletCommand;
import cz.incad.Kramerius.security.rightscommands.ServletRightsCommand;
import cz.incad.Kramerius.security.strenderers.SecuredActionWrapper;
import cz.incad.Kramerius.security.strenderers.TitlesForObjects;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.users.LoggedUsersSingleton;

public class ShowsActionsTableHtml extends ServletRightsCommand {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ShowsActionsTableHtml.class.getName());

    @Inject
    LoggedUsersSingleton loggedUsersSingleton;
    
    @Override
    public void doCommand() {
        String uuid = this.requestProvider.get().getParameter(UUID_PARAMETER);
        try {
            if (this.loggedUsersSingleton.isLoggedUser(this.requestProvider)) {
                /*
                String[] path = getPathOfUUIDs(uuid);
                String[] models = getModels(uuid);

                ResourceBundle resourceBundle = getResourceBundle();
                SecuredActionWrapper[] wrappedActions = SecuredActionWrapper.wrap(resourceBundle, SecuredActions.values());
                String actionsPattern = this.requestProvider.get().getParameter("actions");
                if (actionsPattern != null) {
                    String[] actions = actionsPattern.split(",");
                    wrappedActions = new SecuredActionWrapper[actions.length];
                    for (int i = 0; i < actions.length; i++) {
                        SecuredActions secAct = SecuredActions.findByFormalName(actions[i]);
                        wrappedActions[i] = new SecuredActionWrapper(resourceBundle, secAct);
                    }
                }
                StringTemplate template = ServletRightsCommand.stFormsGroup().getInstanceOf("securedActionsTable");
                template.setAttribute("actions", wrappedActions);
                template.setAttribute("uuid", uuid);
                template.setAttribute("bundle", bundleToMap());

                HashMap<String, String> titles = TitlesForObjects.createFinerTitles(fedoraAccess,rightsManager, uuid, path, models, resourceBundle);
                template.setAttribute("titles", titles);
                
                String content = template.toString();
                this.responseProvider.get().getOutputStream().write(content.getBytes("UTF-8"));
                */
                
            } else {
                
                this.responseProvider.get().sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        }
    }

    
}
