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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import javax.inject.Named;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.Kramerius.processes.ParamsLexer;
import cz.incad.Kramerius.processes.ParamsParser;
import cz.incad.Kramerius.security.strenderers.RightWrapper;
import cz.incad.Kramerius.security.strenderers.TitlesForObjects;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.Right;
import cz.incad.kramerius.security.RightsManager;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.utils.SortingRightsUtils;
import cz.incad.kramerius.service.ResourceBundleService;

public class DisplayRightsForObjectsView extends AbstractRightsView {

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
    Provider<Locale> localesProvider;
    
    @Inject
    ResourceBundleService resourceBundleService;
    
    
    public DisplayRightsForObjectsView() {
        super();
    }

    
    public Right[] allRights(ObjectPidsPath path) {
        return rightsManager.findAllRights(path.getPathFromRootToLeaf(), getSecuredAction());
    }


    
    public List<DialogContent> getRightsPath() {
        try {
            List params = getPidsParams();
            List<DisplayRightsForObjectsView.DialogContent> rightsForPaths = new ArrayList<DisplayRightsForObjectsView.DialogContent>();

            for (Object pid : params) {
                ObjectPidsPath[] paths = solrAccess.getPath(pid.toString());
                
                for (ObjectPidsPath path : paths) {
                    path = path.injectRepository();
                    List<Right> pathRights = new ArrayList<Right>(Arrays.asList(allRights(path)));
                    Map<Integer, Boolean> map = new HashMap<Integer, Boolean>();
                    
                    // ma superadmin roli ?
                    if (!hasSuperAdminRole(this.userProvider.get())) {
                        boolean[] booleans = actionAllowed.isActionAllowedForAllPath(SecuredActions.ADMINISTRATE.getFormalName(), pid.toString(), path);
                        for (int i = 0; i < booleans.length; i++) {
                            // can administrate
                            if (booleans[i]) {
                                
                                for (Right right : pathRights) {
                                    if (pid.equals(right.getPid())) {
                                        map.put(right.getId(), true);
                                    }
                                }
                                
                            } else {
                                for (Right right : pathRights) {
                                    if (pid.equals(right.getPid())) {
                                        map.put(right.getId(), false);
                                    }
                                }
                            }
                        }
                    } else {
                        
                        for (Right right : pathRights) {
                            map.put(right.getId(), true);
                        }
                    }
                    
                    Right[] resultRights = SortingRightsUtils.sortRights(pathRights.toArray(new Right[pathRights.size()]), path);
                    DialogContent dialogContent = new DialogContent(this.fedoraAccess, path, resultRights, this.resourceBundleService, this.localesProvider.get());
                    dialogContent.setRightsAccess(map);
                    rightsForPaths.add(dialogContent);
                }
            }
            

            return rightsForPaths;
            
        } catch (RecognitionException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (TokenStreamException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
        return new ArrayList<DialogContent>();
    }


    
    // reprezentuje jeden dialog prav. Kolik vybranych objektu, tolik dialogu 
    
    public static class DialogContent {

        static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(DisplayRightsForObjectsView.DialogContent.class.getName());
        // cesta k vybranemu objektu
        private ObjectPidsPath path;

        // prava
        private RightWrapper[] wrappers;
        private Map<Integer, Boolean> rightsAccess;
        
        
        private HashMap<String, String> titles;    
        private HashMap<String, String> models;    

        
        
        // muze administrovat vybrany objekt
        private boolean canAdministrate;
        
        public DialogContent(FedoraAccess fedoraAccess, ObjectPidsPath path, Right[] rights, ResourceBundleService resourceBundleService, Locale locale) {
            super();
            try {
                this.path = path;
                this.wrappers = RightWrapper.wrapRights(fedoraAccess, rights);
                this.titles = TitlesForObjects.createTitlesForPaths(fedoraAccess,  this.path);
                this.models = TitlesForObjects.createModelsForPaths(fedoraAccess,  this.path, resourceBundleService, locale);
                
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        public String getRowId() {
            int hashCode = Arrays.hashCode(path.getPathFromRootToLeaf());
            return Integer.toHexString(hashCode);
        }
        
        
        
        public ObjectPidsPath getPath() {
            return path;
        }

        public Right[] getRights() {
            return wrappers;
        }
        
        
        public Map<String, String> getTitles() {
            return this.titles;
        }

        public String getTooltipForPath() {
            String[] pathFromRootToLeaf = this.path.getPathFromRootToLeaf();
            StringBuilder builder = new StringBuilder();
            for (int i = 0,ll= pathFromRootToLeaf.length; i < ll; i++) {
                builder.append(this.titles.get(pathFromRootToLeaf[i]));
                if (SpecialObjects.findSpecialObject(pathFromRootToLeaf[i]) ==null) {
                    builder.append("(").append(this.models.get(pathFromRootToLeaf[i])).append(")");
                }
                if (i < ll -1) {
                    builder.append("::");
                }
            }
            return builder.toString();
        }

        
        public String getTitleForPath() {
            String[] pathFromRootToLeaf = this.path.getPathFromRootToLeaf();
            StringBuilder builder = new StringBuilder();
            for (int i = 0,ll= pathFromRootToLeaf.length; i < ll; i++) {
                builder.append(this.titles.get(pathFromRootToLeaf[i]));
                if (i < ll -1) {
                    builder.append("::");
                }
            }
            return builder.toString();
        }
        

        public HashMap<String, String> getModels() {
            return models;
        }

        public boolean isCanAdministrate() {
            return canAdministrate;
        }

        public void setCanAdministrate(boolean canAdministrate) {
            this.canAdministrate = canAdministrate;
        }

        public Map<Integer, Boolean> getRightsAccess() {
            return rightsAccess;
        }

        public void setRightsAccess(Map<Integer, Boolean> rightsAccess) {
            this.rightsAccess = rightsAccess;
        }
        
        
        
    }
}
