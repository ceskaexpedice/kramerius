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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import org.antlr.stringtemplate.StringTemplate;

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
import cz.incad.kramerius.security.Group;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.Right;
import cz.incad.kramerius.security.RightsManager;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.utils.SortingRightsUtils;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.pid.LexerException;


/**
 * Zobrazi html formular vsech prav  
 * @author pavels
 */
public class ShowRightsHtml extends ServletRightsCommand{

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ShowRightsHtml.class.getName());

    @Inject
    transient IsActionAllowed actionAllowed;

    @Override
    public void doCommand() {
        try {
            String uuid = getUuid();
            
            TypeOfList typeOfList =TypeOfList.all;
            String typeOfListParam = requestProvider.get().getParameter("typeoflist");
            if ((typeOfListParam != null) && (!typeOfListParam.equals(""))) {
                typeOfList = TypeOfList.valueOf(typeOfListParam);
            }

            List<String> saturatedPath = rightsManager.saturatePathAndCreatesPIDs(uuid, getPathOfUUIDs(uuid));
            List<Right> foundRights = new ArrayList<Right>(Arrays.asList(rightsManager.findAllRights(((String[]) saturatedPath.toArray(new String[saturatedPath.size()])), getSecuredAction())));
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
                    } else if (wrappedUser.getWrappedValue() instanceof Group) {
                        if (wrappedUser.getName().equals(requestedParameter)) {
                            wrappedUser.setSelected(true);
                        }
                    }
                    
                }
            }
            
            StringTemplate template = ServletRightsCommand.stFormsGroup().getInstanceOf("rightsTable");
            template.setAttribute("rights", RightWrapper.wrapRights(resultRights));
            template.setAttribute("uuid", uuid);
            template.setAttribute("users", wrapped);
            template.setAttribute("typeOfLists",TypeOfList.typeOfListAsMap(typeOfList));
            template.setAttribute("action",new SecuredActionWrapper(getResourceBundle(), SecuredActions.findByFormalName(getSecuredAction())));
            responseProvider.get().getOutputStream().write(template.toString().getBytes("UTF-8"));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        }
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
                        foundUser = userManager.findGroupByName(parsedUser);
                    } else if (typeOfList.equals(TypeOfList.user)){
                        foundUser = userManager.findUserByLoginName(parsedUser);
                    } else {
                        foundUser = userManager.findUserByLoginName(parsedUser);
                        if (foundUser == null) {
                            foundUser = userManager.findGroupByName(parsedUser);
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
                    if (right.getUser() instanceof Group) {
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
