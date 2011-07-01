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

import cz.incad.kramerius.security.Group;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.impl.UserImpl;

public class UserUtils {

    public static Group commonUsersGroup = null;
    //    static Group globalAdminGroup = null;

    public  static User getNotLoggedUser(UserManager userManager) {
        UserImpl user = new UserImpl(-1, "not_logged", "not_logged", "not_logged", -1);
        user.setGroups(new Group[] {});
        UserUtils.associateCommonGroup(user, userManager);
        return user;
    }

    
    
    // synchronizace
    public static synchronized Group findCommonGoup(UserManager userManager) {
        if (commonUsersGroup == null) {
            commonUsersGroup =  userManager.findCommonUsersGroup();
        }
        return commonUsersGroup;
    }

    public static void associateCommonGroup(User user, UserManager userManager) {
        Group commonGroup = findCommonGoup(userManager);
        boolean containsCommonGroup = false;
        Group[] grps = user.getGroups();
        for (Group group : grps) {
            if (commonGroup.equals(group)) {
                containsCommonGroup = true;
                break;
            }
        }
        if (!containsCommonGroup) {
            Group[] newGroups = new Group[grps.length +1];
            System.arraycopy(grps, 0, newGroups, 0, grps.length);
            newGroups[grps.length] = findCommonGoup(userManager);
            ((UserImpl)user).setGroups(newGroups);
        }
    }

    public static void associateGroups(User dbUser, UserManager userManager) {
        Group[] grps = userManager.findGroupsForGivenUser(dbUser.getId());
        //TODO: Zmenit
        ((UserImpl)dbUser).setGroups(grps);
    }

    public static final String USER_NAME_PARAM = "userName";
    public static final String PSWD_PARAM = "pswd";
    public static final String LOGGED_USER_KEY = "loggedUser";

}
