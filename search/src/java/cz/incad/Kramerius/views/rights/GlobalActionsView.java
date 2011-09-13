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
package cz.incad.Kramerius.views.rights;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.Kramerius.security.strenderers.SecuredActionWrapper;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.service.ResourceBundleService;

public class GlobalActionsView {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(GlobalActionsView.class.getName());
    
    @Inject
    ResourceBundleService bundleService;
    
    @Inject
    Provider<Locale> localesProvider;

    
    public SecuredActionWrapper[] getWrappers()  {
        try {
            Locale locale = this.localesProvider.get();
            ResourceBundle resbundle = bundleService.getResourceBundle("labels", locale);
            return SecuredActionWrapper.wrap(resbundle, SecuredActions.values());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            throw new RuntimeException(e);
        }
    }
}
