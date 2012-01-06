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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import javax.inject.Named;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.Kramerius.security.strenderers.RightWrapper;
import cz.incad.Kramerius.views.rights.DisplayRightsForObjectsView.DialogContent;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.Right;
import cz.incad.kramerius.security.RightCriteriumWrapper;
import cz.incad.kramerius.security.RightCriteriumWrapperFactory;
import cz.incad.kramerius.security.RightsManager;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.utils.SortingRightsUtils;

public class DisplaySelectionRightsViewObject extends AbstractRightsView {
    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(DisplayRightsForObjectsView.class.getName());
    
    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    @Inject
    RightsManager rightsManager;

    @Inject
    IsActionAllowed actionAllowed;
    
    @Inject
    Provider<User> userProvider;
    
    @Inject
    SolrAccess solrAccess;

    @Inject
    UserManager userManager;

    @Inject
    RightCriteriumWrapperFactory factory;

    
    public RightWrapper[] getRights() {
        try {
            List params = getPidsParams();
            String securedAction = getSecuredAction();
            String[] pids = (String[]) params.toArray(new String[params.size()]);
            Right[] foundrights = this.rightsManager.findAllRights(pids, securedAction);
            RightWrapper[] wrappers = RightWrapper.wrapRights(this.fedoraAccess, foundrights);
            return wrappers;
        } catch (RecognitionException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (TokenStreamException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
        return new RightWrapper[0];
    }



}
