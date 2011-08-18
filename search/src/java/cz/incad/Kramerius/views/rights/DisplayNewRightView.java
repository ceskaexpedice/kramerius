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

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.security.RightCriterium;
import cz.incad.kramerius.security.RightCriteriumLoader;
import cz.incad.kramerius.security.RightCriteriumParams;
import cz.incad.kramerius.security.RightCriteriumWrapper;
import cz.incad.kramerius.security.RightCriteriumWrapperFactory;
import cz.incad.kramerius.security.RightsManager;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;

public class DisplayNewRightView extends AbstractRightsView {


    @Inject
    UserManager userManager;
    
    @Inject
    Provider<User> userProvider;

    
    @Inject
    RightCriteriumWrapperFactory factory;

    @Inject
    RightsManager rightsManager;
    
    public String[] getRoles() {
        User user = this.userProvider.get();
        Role[] roles = null;
        
        if (hasSuperAdminRole(this.userProvider.get())) {
            roles = userManager.findAllRoles("");
        } else {
            int[] grps = getUserGroups(user);
            roles = userManager.findAllRoles(grps, "");
        }
        return getRoleNames(roles);
    }

    public String[] getRoleNames(Role[] roles) {
        String[] strRoles = new String[roles.length];
        for (int i = 0; i < strRoles.length; i++) {
            strRoles[i] = roles[i].getName();
        }
        return strRoles;
    }


    public int[] getUserGroups(User user) {
        Role[] grps = user.getGroups();
        int[] grpIds = new int[grps.length];
        for (int i = 0; i < grpIds.length; i++) {
            grpIds[i] = grps[i].getId();
        }
        return grpIds;
    }

    
    public List<RightCriteriumWrapper> getCriteriums() {
        List<RightCriteriumWrapper> criteriums = factory.createAllCriteriumWrappers(SecuredActions.findByFormalName(getSecuredAction()));
        return criteriums;
    }

    public List<RightCriteriumParams> getRightCriteriumParams() {
        RightCriteriumParams[] allParams = this.rightsManager.findAllParams();
        return Arrays.asList(allParams);    
    }
    

}
