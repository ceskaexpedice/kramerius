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
package cz.incad.Kramerius.security.strenderers;

import java.util.ResourceBundle;
import java.util.logging.Level;

import cz.incad.kramerius.security.SecuredActions;

public class SecuredActionWrapper {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(SecuredActionWrapper.class.getName());
    
    private ResourceBundle resBundle;
    private SecuredActions action;

    public SecuredActionWrapper(ResourceBundle resBundle, SecuredActions action) {
        super();
        this.resBundle = resBundle;
        this.action = action;
    }

    public String getFormalName() {
        return action.getFormalName();
    }
    
    
    public String getDescription() {
        try {
            return resBundle.getString("rights.action."+action.getFormalName());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            return "rights.action."+action.getFormalName();
        }
    }
    public String getName() {
        return action.name();
    }
    
    public static  SecuredActionWrapper[] wrap(ResourceBundle bundle, SecuredActions... actions) {
        SecuredActionWrapper[] wrappers = new SecuredActionWrapper[actions.length];
        for (int i = 0; i < wrappers.length; i++) {
            wrappers[i] = new SecuredActionWrapper(bundle, actions[i]);
        }
        return wrappers;
    }
    
    
}
