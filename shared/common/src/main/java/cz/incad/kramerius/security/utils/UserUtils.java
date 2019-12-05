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
package cz.incad.kramerius.security.utils;

import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.impl.UserImpl;

public class UserUtils {

    public static Role commonUsersGroup = null;
    //    static Group globalAdminGroup = null;

    public  static User getNotLoggedUser(UserManager userManager) {
        UserImpl user = new UserImpl(-1, "not_logged", "not_logged", "not_logged", -1);
        user.setGroups(new Role[] {});
        UserUtils.associateCommonGroup(user, userManager);
        return user;
    }

    
    
    // synchronizace
    public static synchronized Role findCommonGoup(UserManager userManager) {
        if (commonUsersGroup == null) {
            commonUsersGroup =  userManager.findCommonUsersRole();
        }
        return commonUsersGroup;
    }

    public static void associateCommonGroup(User user, UserManager userManager) {
        Role commonGroup = findCommonGoup(userManager);
        boolean containsCommonGroup = false;
        Role[] grps = user.getGroups();
        if (grps == null) grps = new Role[0];
        
        for (Role group : grps) {
            if (commonGroup.equals(group)) {
                containsCommonGroup = true;
                break;
            }
        }
            
        if (!containsCommonGroup) {
            Role[] newGroups = new Role[grps.length +1];
            System.arraycopy(grps, 0, newGroups, 0, grps.length);
            newGroups[grps.length] = findCommonGoup(userManager);
            ((UserImpl)user).setGroups(newGroups);
        }
    }

    public static void associateGroups(User dbUser, UserManager userManager) {
        Role[] grps = userManager.findRolesForGivenUser(dbUser.getId());
        //TODO: Zmenit
        ((UserImpl)dbUser).setGroups(grps);
    }
    
    /** Params to request's header and session */
    public static final String USER_NAME_PARAM = "userName";
    public static final String PSWD_PARAM = "pswd";
 
    public static final String FIRST_NAME_KEY="firstName";
    public static final String LAST_NAME_KEY="lastName";
    
    
    
    /** Params to http session */
    public static final String LOGGED_USER_PARAM = "loggedUser";
    public static final String LOGGED_USER_KEY_PARAM ="loggedUserKey";
 
    
}
