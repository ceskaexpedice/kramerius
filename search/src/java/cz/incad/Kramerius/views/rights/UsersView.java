/*
 * Copyright (C) 2012 Pavel Stastny
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
/**
 * 
 */
package cz.incad.Kramerius.views.rights;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.Kramerius.security.strenderers.RoleWrapper;
import cz.incad.kramerius.Initializable;
import cz.incad.kramerius.security.DefaultRoles;
import cz.incad.kramerius.security.RightsManager;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.utils.K4Collections;
import cz.incad.utils.K4Collections.Combinator;

/**
 * @author pavels
 *
 */
public class UsersView extends AbstractRightsView implements Initializable {

    private static final String ROLENAME_PARAM = "rolename";

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(RolesView.class.getName());
    
    @Inject
    UserManager userManager;
    
    @Inject
    Provider<User> userProvider;
    
    @Inject
    RightsManager rightsManager;

    @Inject
    Provider<HttpServletRequest> requestProvider;

    private Role role;

    
    
    @Override
    public void init() {
        String rname = this.requestProvider.get().getParameter(ROLENAME_PARAM);
        if (rname != null) {
            this.role = this.userManager.findRoleByName(rname);
        }
    }


    public List<RoleWrapper> getRolesWithNull() {
        
        List<RoleWrapper> retList;
        User user = userProvider.get();
        if (super.hasSuperAdminRole(user)) {
            retList = RoleWrapper.wrap(Arrays.asList(userManager.findAllRoles("")),true);
        } else {
            Role[] roles = user.getGroups();
            int[] roleIds = new int[roles.length];
            for (int i = 0; i < roleIds.length; i++) {
                roleIds[i] = roles[i].getId();
            }
            retList = RoleWrapper.wrap(Arrays.asList(userManager.findAllRoles(roleIds,"")),true);
        }
        retList = saturateAdministratorRoles(retList);
        return saturateFlags(retList);
    }
    
    
    
    private List<RoleWrapper> saturateAdministratorRoles(List<RoleWrapper> retList) {
        List<RoleWrapper> allList = RoleWrapper.wrap(Arrays.asList(userManager.findAllRoles("")),true);
        for (RoleWrapper roleWrapper : retList) {
            final int personalAdminId = roleWrapper.getPersonalAdminId();
            Combinator<RoleWrapper> combinator = new Combinator<RoleWrapper>() {

                @Override
                public RoleWrapper process(RoleWrapper base, RoleWrapper object) {
                    if (base != null) return base;
                    else if (object.getId() == personalAdminId) return object;
                    return null;
                }
            };
            RoleWrapper admin = K4Collections.reduce(combinator, null, allList);
            roleWrapper.setRoleAdministrator(admin);
        }
        
        return retList;
    }


    public List<RoleWrapper> getRoles() {
        List<RoleWrapper> retList;
        User user = userProvider.get();
        if (super.hasSuperAdminRole(user)) {
            retList = RoleWrapper.wrap(Arrays.asList(userManager.findAllRoles("")),false);
        } else {
            Role[] roles = user.getGroups();
            int[] roleIds = new int[roles.length];
            for (int i = 0; i < roleIds.length; i++) {
                roleIds[i] = roles[i].getId();
            }
            retList = RoleWrapper.wrap(Arrays.asList(userManager.findAllRoles(roleIds,"")),false);
        }
        retList = saturateAdministratorRoles(retList);
        return saturateFlags(retList);
    }

    public int getRoleId() {
        return this.role != null ? this.role.getId() : -1;
    }
    
    public String getRoleName() {
        return this.role != null ? this.role.getName() : "";
    }


    public List<RoleWrapper> saturateFlags(List<RoleWrapper> retList) {
        final int[] usedRoles = this.rightsManager.findUsedRoleIDs();
        Arrays.sort(usedRoles);
        List<RoleWrapper> rwraps =  K4Collections.map(retList, 
            new K4Collections.Mapper<RoleWrapper>() {
                @Override
                public RoleWrapper process(RoleWrapper t, int index) {
                    if (DefaultRoles.findByName(t.getName()) != null) {
                        t.setCanbedeleted(false);
                    } else {
                        int indx = Arrays.binarySearch(usedRoles, t.getId());
                        boolean canbedeleted = indx < 0;
                        t.setCanbedeleted(canbedeleted);
                    }
                    return t;
                }
            });
        
        
        if (this.role != null) {
            final int  personalAdminId = role.getPersonalAdminId();
            if (personalAdminId >= 0) {
                return  K4Collections.map(rwraps, 
                        new K4Collections.Mapper<RoleWrapper>() {
                            @Override
                            public RoleWrapper process(RoleWrapper t, int index) {
                                if (t.getId()== personalAdminId) {
                                    t.setSelected(true);
                                }
                                return t;
                            }
                        });
            } else {
                return rwraps;
            }
            
        } else {
            return rwraps;
        }
    }
}