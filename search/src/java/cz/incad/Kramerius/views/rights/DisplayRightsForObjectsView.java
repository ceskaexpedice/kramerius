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
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.utils.SortingRightsUtils;

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
    
    public DisplayRightsForObjectsView() {
        super();
    }

    
    public Right[] allRights(ObjectPidsPath path) {
        return rightsManager.findAllRights(path.getPathFromRootToLeaf(), getSecuredAction());
    }

    private List<Right> findAllRightWithPid(String pid, List<Right> foundRights) {
        List<Right> rightsWithPID = new ArrayList<Right>();
        for (Right right : foundRights) {
            if (pid.equals(right.getPid())) {
                rightsWithPID.add(right);
            }
        }
        return rightsWithPID;
    }


    
    private List<Right> findAllRightWithUserWhichIAdministrate(User user, List<Right> foundRights) {
        List<Right> filtered = new ArrayList<Right>();
        for (Right right : foundRights) {
            // prava s uzivatelem jdou do kytek
            // uzivatel
            if (right.getUser() instanceof User) {
                /*
                User rightUsr = (User) right.getUser();
                // administruje primo uzivatele
                if (user.isAdministratorForGivenGroup(rightUsr.getPersonalAdminId())) {
                    filtered.add(right);
                } else {
                    // administruje nekterou ze skupin
                    Role[] grps = userManager.findRolesForGivenUser(rightUsr.getId());
                    for (Role group : grps) {
                        if (user.isAdministratorForGivenGroup(group.getPersonalAdminId())) {
                            filtered.add(right);
                            break;
                        }                        
                    }
                }
                */
                filtered.add(right);
           // skupina
            } else if (user.isAdministratorForGivenGroup(right.getUser().getPersonalAdminId())) {
                filtered.add(right);
            }
        }
        return filtered;
    }

    /*
    public String getTitleOfSelected() {
        TitlesForObjects.createFinerTitles(this.fedoraAccess, this.rightsManager, this., path, models, bundle)
    }*/
    
    
    public List<RightsForPath> getRightsPath() {
        try {
            List params = getPidsParams();
            List<DisplayRightsForObjectsView.RightsForPath> rightsForPaths = new ArrayList<DisplayRightsForObjectsView.RightsForPath>();

            for (Object pid : params) {
                ObjectPidsPath[] paths = solrAccess.getPath(pid.toString());
                
                
                for (ObjectPidsPath path : paths) {
                    path = path.injectRepository();
                    List<Right> pathRights = new ArrayList<Right>(Arrays.asList(allRights(path)));
                    
                    if (!hasSuperAdminRole(this.userProvider.get())) {
                        boolean[] booleans = actionAllowed.isActionAllowedForAllPath(SecuredActions.ADMINISTRATE.getFormalName(), pid.toString(), path);
                        List<Right> filtered = new ArrayList<Right>();
                        for (int i = 0; i < booleans.length; i++) {
                            if (booleans[i]) {
                                String curPid = path.getNodeFromLeafToRoot(i);
                                //ObjectPidsPath cuttedTail = path.cutTail(i);
                                
                                List<Right> rights = findAllRightWithPid(curPid, pathRights);
                                rights = findAllRightWithUserWhichIAdministrate(userProvider.get(), rights);
                                filtered.addAll(rights);
                            }
                        }
                        pathRights = filtered;
                    }
                    
                    Right[] resultRights = SortingRightsUtils.sortRights(pathRights.toArray(new Right[pathRights.size()]), path);
                    if (resultRights.length > 0) {
                        rightsForPaths.add(new RightsForPath(this.fedoraAccess, path, resultRights));
                    }
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
        return new ArrayList<RightsForPath>();
    }


    
    public static class RightsForPath {

        static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(DisplayRightsForObjectsView.RightsForPath.class.getName());
        
        private ObjectPidsPath path;
        private RightWrapper[] wrappers;
        private HashMap<String, String> titles;    
        private HashMap<String, String> models;    
        
        public RightsForPath(FedoraAccess fedoraAccess, ObjectPidsPath path, Right[] rights) {
            super();
            try {
                this.path = path;
                this.wrappers = RightWrapper.wrapRights(fedoraAccess, rights);
                this.titles = TitlesForObjects.createTitlesForPaths(fedoraAccess,  this.path);
                this.models = TitlesForObjects.createModelsForPaths(fedoraAccess,  this.path);
                
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

        
        
    }
    
    
    /*
    TypeOfList typeOfList =TypeOfList.all;
    String typeOfListParam = requestProvider.get().getParameter("typeoflist");
    if ((typeOfListParam != null) && (!typeOfListParam.equals(""))) {
        typeOfList = TypeOfList.valueOf(typeOfListParam);
    }

    List<String> saturatedPath = rightsManager.saturatePathAndCreatesPIDs(uuid, getPathOfUUIDs(uuid));
    List<Right> foundRights = new ArrayList<Right>(Arrays.asList(allRights(saturatedPath)));
    // filtrovani objektu na ktere nemam pravo TODO: jeste uzviatele na ktere nemam pravo
    if (!ServletRightsCommand.hasSuperAdminRole(this.userProvider.get())) {
        boolean[] booleans = actionAllowed.isActionAllowedForAllPath(SecuredActions.ADMINISTRATE.getFormalName(), uuid, getPathOfUUIDs(uuid));
        List<Right> filtered = new ArrayList<Right>();
        for (int i = 0; i < booleans.length; i++) {
            if (booleans[i]) {
                String allowedUUID =  saturatedPath.get(i);
                List<Right> rights = findAllRightWithUserWhichIAdministrate(userProvider.get(), findAllRightWithUuid(allowedUUID,foundRights)) ;
                findAllRightWithUserWhichIAdministrate(userProvider.get(), foundRights);
                
                filtered.addAll(rights);
            }
        }
        foundRights = filtered;
    }
    
    foundRights = typeOfList.filter(foundRights);

        
    // acumulate users
    List<AbstractUser> users = accumulateUsersToCombo(foundRights);

    foundRights = filterRequestedUser(foundRights, typeOfList);

    Right[] resultRights = SortingRightsUtils.sortRights(foundRights.toArray(new Right[foundRights.size()]), saturatedPath);

    List<AbstractUserWrapper> wrapped = AbstractUserWrapper.wrap(users, true);
    String requestedParameter = this.requestProvider.get().getParameter("requesteduser");
    if ((requestedParameter != null) && (!requestedParameter.equals(""))) {
        for (AbstractUserWrapper wrappedUser : wrapped) {
            if (wrappedUser.getWrappedValue() instanceof User) {
                if (wrappedUser.getLoginname().equals(requestedParameter)) {
                    wrappedUser.setSelected(true);
                }
            } else if (wrappedUser.getWrappedValue() instanceof Role) {
                if (wrappedUser.getName().equals(requestedParameter)) {
                    wrappedUser.setSelected(true);
                }
            }
        }
    }
    
    StringTemplate template = ServletRightsCommand.stFormsGroup().getInstanceOf("rightsTable");
    template.setAttribute("rights", RightWrapper.wrapRights(fedoraAccess, resultRights));
    template.setAttribute("uuid", uuid);
    template.setAttribute("bundle", bundleToMap());
    template.setAttribute("users", wrapped);
    template.setAttribute("typeOfLists",TypeOfList.typeOfListAsMap(typeOfList));
    template.setAttribute("action",new SecuredActionWrapper(getResourceBundle(), SecuredActions.findByFormalName(getSecuredAction())));
    template.setAttribute("canhandlecommongroup", userProvider.get().hasSuperAdministratorRole());
    responseProvider.get().getOutputStream().write(template.toString().getBytes("UTF-8"));
    */

    
}
