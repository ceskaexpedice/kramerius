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

import cz.incad.kramerius.security.jaas.K4LoginModule;

/**
 * User's and group's manager
 */
public interface UserManager {

    public static final String NOT_LOGGED_USER="not_logged";
    
	/**
	 * Method can validate given username and password. If username or password is invalid method returns <code>null</code> otherwise  it returns user object. 
	 * <br>
	 * This method is used by login module
	 * @param loginName Login name of user
	 * @param passwd Password of user
	 * 
	 * @see User
	 * @see K4LoginModule
	 */
    public User validateUser(String loginName, String passwd);
    
    /**
     * Find group associated with user represented by given user_id
     * @param user_id User's id
     * 
     * @see Role
     */
    public Role[] findRolesForGivenUser(int user_id);
    
    /**
     * Find user by given user_id
     * @param user_id User's id
     * @return
     */
    public User findUser(int user_id);
    
    
    
    /**
     * Find users which have loginname with given prefix
     * @param prefix prefix
     * 
     * @see User
     */
    public User[] findUserByPrefix(String prefix);
    
    /**
     * Find group by given group_id
     * @param group_id Group's id
     * 
     * @see Role
     */
    public Role findRole(int group_id);
    
    /**
     * Find all roles which can be administered by a user associated with given roles. 
     * (Roles are represented by role ids)
     * @param roleIds 
     * 
     * @see Role
     */
    public Role[] findRoleWhichIAdministrate(int[] roleIds);
    
    /**
     * Find groups which have groupname with given prefix
     * @param prefix prefix
     */
    public Role[] findRoleByPrefix(String prefix);
    
    /**
     * Returns special role for everyone.
     * @return
     */
    public Role findCommonUsersRole();
    
    /**
     * Returns special super admin role == Role can administrate everything. 
     * @return
     */
    public Role findGlobalAdminRole();
    
    /**
     * Find group by given group name
     * @param gname Name of group
     * @return
     */
    public Role findRoleByName(String gname);

    /**
     * Find user by login name
     * @param loginName User's login name
     * @return
     */
    public User findUserByLoginName(String loginName);

    

    /**
     * Find all users associated with given group. 
     * @param groupId Group id. 
     * @return
     */
    public User[] findUsersForGivenRole(int groupId);
    
    /**
     * Returns all users which can be administered by given groups 
     * @param roleIds Master group ids
     * @return
     */
    public User[] findAllUsers(int[] roleIds);

    
    /**
     * Returns all users which can be administered by given groups and starts witgh given prefix
     * @param roleIds Master group ids
     * @param prefix for User
     * @return
     */
    public User[] findAllUsers(int[] roleIds, String prefix);

    /**
     * Returns all groups which can be administered by given groups
     * @param roleIds Master group ids
     * @return
     */
    public Role[] findAllRoles(int[] roleIds, String prefix);

    /**
     * Change password
     * @param userId user id
     * @param pswd new password
     * @throws SQLException
     */
    void saveNewPassword(int userId, String pswd) throws SQLException;

    /**
     * Returns all users which starts with given prefix
     * @param prefix Prefix for user
     * @return
     */
    public User[] findAllUsers(String prefix);
    
    /**
     * Returns all group which starts with given prefix
     * @param prefix
     * @return
     */
    public Role[] findAllRoles(String prefix);
    
    
    public User[] findUserByPrefixForRoles(String prefix, int[] roleIds);
    public Role[] findRoleByPrefixForRoles(String prefix, int[] roleIds);
    

    public void insertRole(Role role) throws SQLException;
    
    public void removeGroup(Role role) throws SQLException;
    
    public void editRole(Role role) throws SQLException;
    
    public boolean isLoggedUser(User user);
    
    //TODO:  Move it !!
    
    public void registerLoggedUser(User user, String loggedUserKey) throws SQLException;
}
