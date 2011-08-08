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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.http.HttpServletResponse;

import org.antlr.stringtemplate.StringTemplate;
import org.apache.commons.lang.NotImplementedException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.Kramerius.security.ServletCommand;
import cz.incad.Kramerius.security.rightscommands.ServletRightsCommand;
import cz.incad.Kramerius.security.strenderers.AbstractUserWrapper;
import cz.incad.Kramerius.security.strenderers.RightWrapper;
import cz.incad.Kramerius.security.strenderers.SecuredActionWrapper;
import cz.incad.Kramerius.security.utils.UserFieldParser;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.AbstractUser;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.Right;
import cz.incad.kramerius.security.RightsManager;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.utils.SortingRightsUtils;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.users.LoggedUsersSingleton;
import cz.incad.kramerius.utils.pid.LexerException;


/**
 * Zobrazi html formular vsech prav  
 * @author pavels
 */
public class ShowRightsHtml extends ServletRightsCommand{

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ShowRightsHtml.class.getName());

    @Inject
    IsActionAllowed actionAllowed;

    @Inject
    LoggedUsersSingleton loggedUsersSingleton;
    
    @Override
    public void doCommand() {
        try {
            if (this.loggedUsersSingleton.isLoggedUser(this.requestProvider)) {
                String uuid = getUuid();

                throw new NotImplementedException("not implemented");
                
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
            } else {

                responseProvider.get().sendError(HttpServletResponse.SC_FORBIDDEN);
            }
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        }
    }

    private List<Right> findAllRightWithUserWhichIAdministrate(User user, List<Right> foundRights) {
        List<Right> filtered = new ArrayList<Right>();
        for (Right right : foundRights) {
            // uzivatel
            if (right.getUser() instanceof User) {
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
            // skupina
            } else if (user.isAdministratorForGivenGroup(right.getUser().getPersonalAdminId())) {
                filtered.add(right);
            }
        }
        return filtered;
    }

    private List<Right> findAllRightWithUuid(String uuid, List<Right> foundRights) {
        List<Right> rightWithUUID = new ArrayList<Right>();
        for (Right right : foundRights) {
            if (uuid.equals(right.getPid())) {
                rightWithUUID.add(right);
            }
        }
        return rightWithUUID;
    }

    public Right[] allRights(List<String> saturatedPath) {
        return rightsManager.findAllRights(((String[]) saturatedPath.toArray(new String[saturatedPath.size()])), getSecuredAction());
    }

    
    public Right[] filteredRights(List<String> saturatedPath) {
        return rightsManager.findAllRights(((String[]) saturatedPath.toArray(new String[saturatedPath.size()])), getSecuredAction());
    }
    
    public List<AbstractUser> accumulateUsersToCombo(List<Right> foundRights) {
        List<AbstractUser> users = new ArrayList<AbstractUser>();
        for (Right r : foundRights) {
            users.add(r.getUser());
        }
        return users;
    }

    public List<Right> filterRequestedUser(List<Right> foundRights, TypeOfList typeOfList) {
        String requestedUser = requestProvider.get().getParameter("requesteduser");
        List<Right> result = new ArrayList<Right>();
        if ((requestedUser!= null) && (!requestedUser.equals(""))) {
            if (requestedUser.equals("all")) {
                result.addAll(foundRights);
            } else {
                try {
                    UserFieldParser userFieldParser = new UserFieldParser(requestedUser);
                    userFieldParser.parseUser();
                    String parsedUser = userFieldParser.getUserValue();
                    AbstractUser foundUser = null;
                    if (typeOfList.equals(TypeOfList.group)) {
                        foundUser = userManager.findRoleByName(parsedUser);
                    } else if (typeOfList.equals(TypeOfList.user)){
                        foundUser = userManager.findUserByLoginName(parsedUser);
                    } else {
                        foundUser = userManager.findUserByLoginName(parsedUser);
                        if (foundUser == null) {
                            foundUser = userManager.findRoleByName(parsedUser);
                        }
                    }

                    
                    for (int i = 0; i < foundRights.size(); i++) {
                        Right right = foundRights.get(i);
                        boolean equalsId = right.getUser().getId() == foundUser.getId();
                        boolean sameClass = right.getUser().getClass().equals(foundUser.getClass());
                        if (equalsId && sameClass){
                            result.add(right);
                        }
                    }

                } catch (LexerException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
                }
            }
        } else {
            result = new ArrayList<Right>(foundRights);
        }
        return result;
    }

    /** Typ vypisu */
    enum TypeOfList {
        all {
            @Override
            public List<Right> filter(List<Right> allRights) {
                return new ArrayList<Right>(allRights);
            }
        },
        group {
            @Override
            public List<Right> filter(List<Right> allRights) {
                List<Right> result = new ArrayList<Right>();
                for (Right right : allRights) {
                    if (right.getUser() instanceof Role) {
                        result.add(right);
                    }
                }
                return result;
            }
        },
        user {
            @Override
            public List<Right> filter(List<Right> allRights) {
                List<Right> result = new ArrayList<Right>();
                for (Right right : allRights) {
                    if (right.getUser() instanceof User) {
                        result.add(right);
                    }
                }
                return result;
            }
        };
        
        public abstract List<Right> filter(List<Right> allRights);
        
        public static Map<String, Boolean> typeOfListAsMap(TypeOfList selectedTypeOfList) {
            Map<String, Boolean> map = new HashMap<String, Boolean>();
            TypeOfList[] values = TypeOfList.values();
            for (TypeOfList value : values) {
                map.put(value.name(), false);
            }
            map.put(selectedTypeOfList.name(), true);
            return map;
        }
    }
    
}
