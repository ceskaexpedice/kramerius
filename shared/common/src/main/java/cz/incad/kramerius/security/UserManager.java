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
import java.util.List;

import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessOrdering;
import cz.incad.kramerius.security.database.TypeOfOrdering;
import cz.incad.kramerius.security.jaas.K4LoginModule;
import cz.incad.kramerius.users.UserProfile;
import cz.incad.kramerius.utils.database.Offset;
import cz.incad.kramerius.utils.database.Ordering;
import cz.incad.kramerius.utils.database.SQLFilter;

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
    
    
    public List<User> filterUsers(Ordering ordering,TypeOfOrdering typeOfOrdering, Offset offset,SQLFilter filter);

	public List<Role> filterRoles(Ordering ordering,TypeOfOrdering typeOfOrdering, Offset offset, SQLFilter filter);


    
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
    
    /**
     * Find users by prefix and roles which can administrate users
     * @param prefix prefix
     * @param roleIds admin roles
     * @return 
     */
    @Deprecated
    public User[] findUserByPrefixForRoles(String prefix, int[] roleIds);
    
    /**
     * Find roles by prefix and roles ids
     * @param prefix prefix
     * @param roleIds admin roles
     * @return
     */
    public Role[] findRoleByPrefixForRoles(String prefix, int[] roleIds);
    

    /**
     * Create new roles
     * @param role
     * @throws SQLException
     */
    public void insertRole(Role role) throws SQLException;

    
    /**
     * Remove role
     * @param role
     * @throws SQLException
     */
    public void removeRole(Role role) throws SQLException;
    
    /**
     * Edit role
     * @param role
     * @throws SQLException
     */
    public void editRole(Role role) throws SQLException;
    
    
    /**
     * Associate concrete role to given user
     * @param user User
     * @param role Role
     * @throws SQLException SQL error has been occured
     */
    public void associateRole(User user, Role role) throws SQLException;
    
    /**
     * Remove association role and user
     * @param user User 
     * @param role Role 
     * @throws SQLException SQL error has been occured
     */
    public void disAssociateRole(User user, Role role) throws SQLException;
    
    /**
     * Change roles for given user
     * @param user User
     * @param rnames Role names
     * @throws SQLException
     */
    public void changeRoles(User user, List<String> rnames) throws SQLException;
    
    /**
     * Insert new user
     * @param user New user
     * @param pswd Password
     * @throws SQLException SQL error has been occurred
     * @see UserManager#activateUser(User)
     */
    public void insertUser(User user, String pswd) throws SQLException;

    /**
     * Delete user
     * @param user
     * @throws SQLException
     */
    public void deleteUser(User user) throws SQLException;
    
    
    /**
     * Save user's password
     * @param user User 
     * @param pswd Password
     * @throws SQLException SQL error has been occurred
     */
    public void saveUserPassword(User user, String pswd) throws SQLException;
    
    /**
     * Activate given user
     * @param user User
     * @throws SQLException SQL error has been occurred
     */
    public void activateUser(User user) throws SQLException;
    
    
    public boolean validatePassword(int userId, String pswd);
    
    /**
     * Find public role
     * @return
     */
    public Role findPublicUsersRole();
    
    
    //TODO:  remove
    public boolean isLoggedUser(User user);
    // TODO: remove
    public void insertPublicUsersRole() throws SQLException;
    //TODO : remove
    public void registerLoggedUser(User user, String loggedUserKey) throws SQLException;
    
}
