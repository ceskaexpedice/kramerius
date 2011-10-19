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


import static cz.incad.kramerius.security.SecuredActions.ADMINISTRATE;
import static cz.incad.kramerius.security.SecuredActions.EXPORT_K4_REPLICATIONS;
import static cz.incad.kramerius.security.SecuredActions.IMPORT_K4_REPLICATIONS;
import static cz.incad.kramerius.security.SecuredActions.READ;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import javax.inject.Named;

import org.w3c.dom.Document;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.RightCriteriumWrapperFactory;
import cz.incad.kramerius.security.RightsManager;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.DCUtils;

public class DisplayObjectsView extends AbstractRightsView {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(DisplayObjectsView.class.getName());
    
    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    @Inject
    RightsManager rightsManager;

    @Inject
    IsActionAllowed isActionAllowed;
    
    @Inject
    Provider<User> userProvider;
    
    @Inject
    SolrAccess solrAccess;
    
    @Inject
    RightCriteriumWrapperFactory factory;

    @Inject
    ResourceBundleService resourceBundleService;
    
    @Inject
    Provider<Locale> localesProvider;
    
    

    public DisplayObjectsView() {
        super();
    }
    
    
    
    
    public List<AffectedObject>getAffectedObjects() {
        List<AffectedObject> objects = new ArrayList<DisplayObjectsView.AffectedObject>();
        try {
            List paramsList = getPidsParams();
            for (Object pid : paramsList) {
                boolean hasRight = false;
                ObjectPidsPath[] paths = solrAccess.getPath(pid.toString());
                for (ObjectPidsPath path : paths) {
                    if (this.isActionAllowed.isActionAllowed(this.userProvider.get(),SecuredActions.ADMINISTRATE.getFormalName(),pid.toString(), null, path)) {
                        hasRight = true;
                        break;
                    }
                }
                Document dc = this.fedoraAccess.getDC(pid.toString());
                
                Locale locale = this.localesProvider.get();
                String kmodelName = this.fedoraAccess.getKrameriusModelName(pid.toString());
                String translatedKModelName = resourceBundleService.getResourceBundle("labels", locale).getString("document.type."+kmodelName);
                
                AffectedObject affectedObject = new AffectedObject(pid.toString(), DCUtils.titleFromDC(dc),translatedKModelName, hasRight);
                objects.add(affectedObject);
            }
            
        } catch (RecognitionException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (TokenStreamException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
        return objects;
    }


    public SecuredActions[] getActions() {
        return new SecuredActions[] {READ,ADMINISTRATE,IMPORT_K4_REPLICATIONS,EXPORT_K4_REPLICATIONS};
    }
    
    public class AffectedObject {
        
        private String pid;
        private boolean accessed;
        private String title;
        private String modelName;

        public AffectedObject(String pid, String title, String modelName, boolean accessed) {
            super();
            this.pid = pid;
            this.title = title;
            this.accessed = accessed;
            this.modelName = modelName;
        }
        
        public String getTitle() {
            return this.title;
        }
        
        public String getModelName() {
            return modelName;
        }
        
        
        public String getComment() {
            return this.accessed ? "" : "Nemate pravo zit !";
        }
        
        public String getPid() {
            return this.pid;
        }
        
        public boolean isAccessed() {
            return this.accessed;
        }
        
        
        public String getCheckedAttribute() {
            return this.accessed ? "checked='true'" :"";
        }
    }
}
