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
import static cz.incad.kramerius.security.SecuredActions.READ;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

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
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.DCUtils;
import cz.incad.kramerius.utils.params.ParamsLexer;
import cz.incad.kramerius.utils.params.ParamsParser;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

public class DisplayObjectsView extends AbstractRightsView {

    public static final String ACTIONS = "actions";

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
    
    
    
    public String getIdent() {
        String idParam = super.requestProvider.get().getParameter("id");
        return idParam;
    }
    
    
    public List<AffectedObject>getAffectedObjects() throws LexerException {
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
                
                
                if (pid.equals(SpecialObjects.REPOSITORY.getPid())) {
                
                    String kmodelName = SpecialObjects.REPOSITORY.name();
                    AffectedObject affectedObject = new AffectedObject(pid.toString(), SpecialObjects.REPOSITORY.name(),kmodelName, hasRight);
                    objects.add(affectedObject);

                } else {
                    PIDParser pidParser = new PIDParser(pid.toString());
                    pidParser.objectPid();
                    String displayingPid = null;
                    if (pidParser.isDatastreamPid()) {
                        displayingPid = pidParser.getParentObjectPid();
                    } else {
                        displayingPid = pid.toString();
                    }
                    
                    Document dc = this.fedoraAccess.getDC(displayingPid);
                    
                    Locale locale = this.localesProvider.get();
                    String kmodelName = this.fedoraAccess.getKrameriusModelName(displayingPid);
                    String translatedKModelName = resourceBundleService.getResourceBundle("labels", locale).getString("document.type."+kmodelName);
                    
                    AffectedObject affectedObject = new AffectedObject(pid.toString(), DCUtils.titleFromDC(dc),translatedKModelName, hasRight);
                    objects.add(affectedObject);
                    
                }
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

    private List getActionsParam() throws RecognitionException, TokenStreamException {
        HttpServletRequest httpServletRequest = this.requestProvider.get();
        String parameter = httpServletRequest.getParameter(ACTIONS);
        
        ParamsParser params = new ParamsParser(new ParamsLexer(new StringReader(parameter)));
        List paramsList = params.params();
        return paramsList;
    }

    public SecuredActions[] getActions() throws RecognitionException, TokenStreamException {
        String parameter = this.requestProvider.get().getParameter(ACTIONS);
        if (parameter != null) {
            
            List actionsParam = getActionsParam();
            List<SecuredActions> secList = new ArrayList<SecuredActions>();
            for (Object act : actionsParam) {
                SecuredActions action = SecuredActions.findByFormalName(act.toString());
                if (action  == null) {
                    secList.add(SecuredActions.valueOf(act.toString()));
                } else {
                    secList.add(action);
                }
            }   
            return (SecuredActions[]) secList.toArray(new SecuredActions[secList.size()]);
            
        } else {
            return new SecuredActions[] {READ,ADMINISTRATE,EXPORT_K4_REPLICATIONS, SecuredActions.SHOW_CLIENT_PRINT_MENU, SecuredActions.SHOW_CLIENT_PDF_MENU, SecuredActions.PDF_RESOURCE};
        }
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
