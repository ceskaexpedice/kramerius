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
package cz.incad.Kramerius.admins;

import static cz.incad.utils.IKeys.UUID_PARAMETER;

import java.io.IOException;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Injector;

import cz.incad.Kramerius.admins.commands.ChangeVisibililtyFlagHtml;
import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

public class AdministratorsActions extends GuiceServlet {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(AdministratorsActions.class.getName());
    
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uuid = req.getParameter(UUID_PARAMETER);
        try {
            PIDParser pidParser = new PIDParser("uuid:"+uuid);
            pidParser.objectPid();

            String action = req.getParameter("action");
            
            try {
                GetCommandsEnum command = GetCommandsEnum.valueOf(action);
                command.doAction(getInjector());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(),e);
            }
            
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    static enum GetCommandsEnum {

        /** zobrazeni prav */
        changeFlag(ChangeVisibililtyFlagHtml.class);
        
        private Class<? extends AdminCommand> commandClass;
        
        private GetCommandsEnum(Class<? extends AdminCommand> command) {
            this.commandClass = command;
        }
        
        public void doAction(Injector injector) throws InstantiationException, Exception {
            AdminCommand command = commandClass.newInstance();
            injector.injectMembers(command);
            command.doCommand();
        }
    }
    
}
