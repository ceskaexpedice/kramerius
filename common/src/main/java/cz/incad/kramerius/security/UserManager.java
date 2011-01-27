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
package cz.incad.kramerius.security;

import java.sql.SQLException;

public interface UserManager {

    /**
     * Validate user and given password
     * @param loginName
     * @param passwd
     * @return
     */
    public User validateUser(String loginName, String passwd);
    
    public Group[] findGroupsForGivenUser(int user_id);
    
    public User findUser(int user_id);
    
    public User[] findUserByPrefix(String prefix);
    
    public Group findGroup(int group_id);
    
    public Group[] findGroupsWhichIAdministrate(int[] grpIds);
    
    public Group[] findGroupByPrefix(String prefix);
    
    public Group findCommonUsersGroup();
    public Group findGlobalAdminGroup();
    
    public Group findGroupByName(String gname);
    
    public User findUserByLoginName(String loginName);

    
    public User[] findUserByPrefixForGroups(String prefix, int[] grpIds);
    public Group[] findGroupByPrefixForGroups(String prefix, int[] grpIds);

    public User[] findUsersForGivenGroup(int groupId);
    
    /**
     * Returs all users which can be administrate by given groups 
     * @param grpIds Master group ids
     * @return
     */
    public User[] findAllUsers(int[] grpIds);
    
    /**
     * Returns all groups wich can be administrate by given groups
     * @param grpIds Master group ids
     * @return
     */
    public Group[] findAllGroups(int[] grpIds);
    
    /**
     * Change password
     * @param userId user id
     * @param pswd new password
     * @throws SQLException
     */
    void saveNewPassword(int userId, String pswd) throws SQLException;


    public User[] findAllUsers(String prefix);
    public Group[] findAllGroups(String prefix);
}
