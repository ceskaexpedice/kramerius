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
package cz.incad.kramerius.security.impl;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessOrdering;
import cz.incad.kramerius.processes.database.ProcessDatabaseUtils;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.database.InitSecurityDatabase;
import cz.incad.kramerius.security.database.SecurityDatabaseUtils;
import cz.incad.kramerius.security.database.TypeOfOrdering;
import cz.incad.kramerius.security.utils.PasswordDigest;
import cz.incad.kramerius.security.utils.SecurityDBUtils;
import cz.incad.kramerius.utils.database.JDBCCommand;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.JDBCTransactionTemplate;
import cz.incad.kramerius.utils.database.JDBCUpdateTemplate;
import cz.incad.kramerius.utils.database.Offset;
import cz.incad.kramerius.utils.database.Ordering;
import cz.incad.kramerius.utils.database.SQLFilter;

public class DatabaseUserManager implements UserManager {

    private static final String PUBLIC_USERS_ROLE_NAME = "public_users";

    private static final StringTemplateGroup ST_GROUP = SecurityDatabaseUtils
            .stGroup();
    // private static final StringTemplateGroup RT_GROUP =
    // SecurityDatabaseUtils.stUdateRightGroup();

    static java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(DatabaseUserManager.class.getName());

    @Inject
    @Named("kramerius4")
    Provider<Connection> provider;

    @Inject
    Provider<HttpServletRequest> requestProvider;

    private Role[] rolesForUser(int userId) {
        List<Role> rlist = new JDBCQueryTemplate<Role>(
                SecurityDBUtils.getConnection()) {
            @Override
            public boolean handleRow(ResultSet rs, List<Role> retList)
                    throws SQLException {
                retList.add(SecurityDBUtils.createRole(rs));
                return true;
            }
        }.executeQuery("select * from user_group_mapping where user_id=?",
                userId);
        return rlist.toArray(new Role[rlist.size()]);
    }

    @Override
    @InitSecurityDatabase
    public User validateUser(final String loginName, final String passwd) {
        String sql = ST_GROUP.getInstanceOf("findUser").toString();
        List<User> users = new JDBCQueryTemplate<User>(this.provider.get()) {
            @Override
            public boolean handleRow(ResultSet rs, List<User> returnsList)
                    throws SQLException {
                User user = createUser(rs);
                returnsList.add(user);
                return true;
            }
        }.executeQuery(sql, loginName, passwd);
        return (users != null) && (!users.isEmpty()) ? users.get(0) : null;
    }

    public Provider<Connection> getProvider() {
        return provider;
    }

    public void setProvider(Provider<Connection> provider) {
        this.provider = provider;
    }

    @Override
    public List<Role> filterRoles(Ordering ordering,
            TypeOfOrdering typeOfOrdering, Offset offset, SQLFilter filter) {
        StringBuffer buffer = new StringBuffer("select * from group_entity g ");

        if (filter != null) {
            buffer.append(filter.getSQLOffset());
        }

        if (ordering != null && ordering.getSelected() != null) {
            buffer.append(" order by ").append(ordering.getSelected());
        }

        if (offset != null) {
            buffer.append(offset.getSQLOffset());
        }

        List<Role> roles = new JDBCQueryTemplate<Role>(this.getProvider().get()) {
            @Override
            public boolean handleRow(ResultSet rs, List<Role> returnsList)
                    throws SQLException {
                Role r = SecurityDBUtils.createRole(rs);
                returnsList.add(r);
                return super.handleRow(rs, returnsList);
            }
        }.executeQuery(buffer.toString(), filter != null ? filter
                .getObjectsToPreparedStm().toArray() : new Object[] {});

        return roles;
    }

    @Override
    public List<User> filterUsers(Ordering ordering,
            TypeOfOrdering typeOfOrdering, Offset offset, SQLFilter filter) {
        StringBuffer buffer = new StringBuffer("select * from user_entity u ");

        if (filter != null) {
            buffer.append(filter.getSQLOffset());
        }

        if (ordering != null && ordering.getSelected() != null) {
            buffer.append(" order by ").append(ordering.getSelected());
        }

        if (offset != null) {
            buffer.append(offset.getSQLOffset());
        }

        List<User> users = new JDBCQueryTemplate<User>(this.getProvider().get()) {
            @Override
            public boolean handleRow(ResultSet rs, List<User> returnsList)
                    throws SQLException {
                User user = createUser(rs);
                returnsList.add(user);
                return super.handleRow(rs, returnsList);
            }
        }.executeQuery(buffer.toString(), filter != null ? filter
                .getObjectsToPreparedStm().toArray() : new Object[] {});

        return users;
    }

    @Override
    @InitSecurityDatabase
    public Role[] findRolesForGivenUser(int user_id) {
        String sql = ST_GROUP.getInstanceOf("findAllGroupsByUserId").toString();
        List<Role> users = new JDBCQueryTemplate<Role>(this.provider.get()) {
            @Override
            public boolean handleRow(ResultSet rs, List<Role> returnsList)
                    throws SQLException {
                Role role = SecurityDBUtils.createRole(rs);
                returnsList.add(role);
                return true;
            }
        }.executeQuery(sql, user_id);
        return (users != null) ? (Role[]) users.toArray(new Role[users.size()])
                : new Role[0];
    }

    @Override
    @InitSecurityDatabase
    public User findUser(int user_id) {
        String sql = ST_GROUP.getInstanceOf("findUserByUserId").toString();
        List<User> users = new JDBCQueryTemplate<User>(this.provider.get()) {
            @Override
            public boolean handleRow(ResultSet rs, List<User> returnsList)
                    throws SQLException {
                User user = createUser(rs);
                returnsList.add(user);
                return true;
            }
        }.executeQuery(sql, user_id);
        return (users != null) && (!users.isEmpty()) ? users.get(0) : null;
    }

    @Override
    @InitSecurityDatabase
    public Role findRole(int group_id) {
        String sql = ST_GROUP.getInstanceOf("findGroupByGroupId").toString();
        List<Role> groups = new JDBCQueryTemplate<Role>(this.provider.get()) {
            @Override
            public boolean handleRow(ResultSet rs, List<Role> returnsList)
                    throws SQLException {
                Role role = SecurityDBUtils.createRole(rs);
                returnsList.add(role);
                return true;
            }
        }.executeQuery(sql, group_id);
        return (groups != null) && (!groups.isEmpty()) ? groups.get(0) : null;
    }

    @Override
    @InitSecurityDatabase
    public Role findCommonUsersRole() {
        String sql = ST_GROUP.getInstanceOf("findCommonUsersGroup").toString();
        List<Role> groups = new JDBCQueryTemplate<Role>(this.provider.get()) {
            @Override
            public boolean handleRow(ResultSet rs, List<Role> returnsList)
                    throws SQLException {
                Role role = SecurityDBUtils.createRole(rs);
                returnsList.add(role);
                return true;
            }
        }.executeQuery(sql);
        return (groups != null) && (!groups.isEmpty()) ? groups.get(0) : null;
    }

    @Override
    @InitSecurityDatabase
    public Role[] findRoleWhichIAdministrate(int[] roleIds) {
        StringTemplate template = ST_GROUP
                .getInstanceOf("findGroupsWhichAdministrate");
        template.setAttribute("grps", roleIds);
        String sql = template.toString();
        List<Role> groups = new JDBCQueryTemplate<Role>(this.provider.get()) {
            @Override
            public boolean handleRow(ResultSet rs, List<Role> returnsList)
                    throws SQLException {
                Role role = SecurityDBUtils.createRole(rs);
                returnsList.add(role);
                return true;
            }

        }.executeQuery(sql);
        return (Role[]) groups.toArray(new Role[groups.size()]);
    }

    @Override
    @InitSecurityDatabase
    public Role findGlobalAdminRole() {
        String sql = ST_GROUP.getInstanceOf("findGlobalAdminsGroup").toString();
        List<Role> groups = new JDBCQueryTemplate<Role>(this.provider.get()) {
            @Override
            public boolean handleRow(ResultSet rs, List<Role> returnsList)
                    throws SQLException {
                Role role = SecurityDBUtils.createRole(rs);
                returnsList.add(role);
                return true;
            }
        }.executeQuery(sql);
        return (groups != null) && (!groups.isEmpty()) ? groups.get(0) : null;
    }

    @Override
    @InitSecurityDatabase
    public Role findRoleByName(String gname) {
        String sql = ST_GROUP.getInstanceOf("findGroupByGname").toString();
        List<Role> groups = new JDBCQueryTemplate<Role>(this.provider.get()) {
            @Override
            public boolean handleRow(ResultSet rs, List<Role> returnsList)
                    throws SQLException {
                Role role = SecurityDBUtils.createRole(rs);
                returnsList.add(role);
                return true;
            }
        }.executeQuery(sql, gname);
        return (groups != null) && (!groups.isEmpty()) ? groups.get(0) : null;
    }

    @Override
    @InitSecurityDatabase
    public User findUserByLoginName(String loginName) {
        String sql = ST_GROUP.getInstanceOf("findUserByLoginName").toString();
        List<User> users = new JDBCQueryTemplate<User>(this.provider.get()) {
            @Override
            public boolean handleRow(ResultSet rs, List<User> returnsList)
                    throws SQLException {
                User user = createUser(rs);
                returnsList.add(user);
                return true;
            }
        }.executeQuery(sql, loginName);
        return (users != null) && (!users.isEmpty()) ? users.get(0) : null;
    }

    @Override
    @InitSecurityDatabase
    public User[] findUserByPrefix(String prefix) {
        String sql = ST_GROUP.getInstanceOf("findUserByPrefix").toString();
        List<User> users = new JDBCQueryTemplate<User>(this.provider.get()) {
            @Override
            public boolean handleRow(ResultSet rs, List<User> returnsList)
                    throws SQLException {
                User user = createUser(rs);
                returnsList.add(user);
                return true;
            }
        }.executeQuery(sql, prefix + "%");
        return (User[]) users.toArray(new User[users.size()]);
    }

    @Override
    @InitSecurityDatabase
    public User[] findUserByPrefixForRoles(String prefix, int[] roleIds) {
        StringTemplate template = ST_GROUP
                .getInstanceOf("findUserByPrefixForGroups");
        template.setAttribute("grps", roleIds);
        String sql = template.toString();
        List<User> users = new JDBCQueryTemplate<User>(this.provider.get()) {
            @Override
            public boolean handleRow(ResultSet rs, List<User> returnsList)
                    throws SQLException {
                User user = createUser(rs);
                returnsList.add(user);
                return true;
            }
        }.executeQuery(sql, prefix + "%");
        return (User[]) users.toArray(new User[users.size()]);
    }

    @Override
    @InitSecurityDatabase
    public Role[] findRoleByPrefix(String prefix) {
        String sql = ST_GROUP.getInstanceOf("findGroupByPrefix").toString();
        List<Role> users = new JDBCQueryTemplate<Role>(this.provider.get()) {
            @Override
            public boolean handleRow(ResultSet rs, List<Role> returnsList)
                    throws SQLException {
                Role group = SecurityDBUtils.createRole(rs);
                returnsList.add(group);
                return true;
            }
        }.executeQuery(sql, prefix + "%");
        return (Role[]) users.toArray(new Role[users.size()]);
    }

    @Override
    @InitSecurityDatabase
    public Role[] findRoleByPrefixForRoles(String prefix, int[] roleIds) {
        StringTemplate template = ST_GROUP
                .getInstanceOf("findGroupByPrefixForGroups");
        template.setAttribute("grps", roleIds);
        String sql = template.toString();
        List<Role> users = new JDBCQueryTemplate<Role>(this.provider.get()) {
            @Override
            public boolean handleRow(ResultSet rs, List<Role> returnsList)
                    throws SQLException {
                Role group = SecurityDBUtils.createRole(rs);
                returnsList.add(group);
                return true;
            }
        }.executeQuery(sql, prefix + "%");
        return (Role[]) users.toArray(new Role[users.size()]);
    }

    @Override
    public void saveNewPassword(int userId, String pswd) throws SQLException {
        JDBCUpdateTemplate updateTemplate = new JDBCUpdateTemplate(
                this.provider.get());
        StringTemplate template = ST_GROUP.getInstanceOf("updatePassword");
        updateTemplate.executeUpdate(template.toString(), pswd, userId);
    }
    
    public boolean validatePassword(int userId, String oldPswd)  {
        try {
            String opswd = PasswordDigest.messageDigest(oldPswd);
            List<String> dbPswd = new JDBCQueryTemplate<String>(this.provider.get()) {
                @Override
                public boolean handleRow(ResultSet rs, List<String> returnsList)
                        throws SQLException {
                    returnsList.add(rs.getString("pswd"));
                    return true;
                }
            }.executeQuery(ST_GROUP.getInstanceOf("findUserByUserId").toString(), userId);
            if (!dbPswd.isEmpty()) {
                return dbPswd.get(0).equals(opswd);
            } else return false;
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return false;
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return false;
        }
    }

    @InitSecurityDatabase
    public User[] findAllUsers(String prefix) {
        StringTemplate template = ST_GROUP.getInstanceOf("findAllUsers");
        template.setAttribute("prefix", prefix.trim().equals("") ? null
                : prefix);
        String sql = template.toString();
        List<User> users = null;
        if (prefix.trim().equals("")) {
            users = new JDBCQueryTemplate<User>(this.provider.get()) {
                @Override
                public boolean handleRow(ResultSet rs, List<User> returnsList)
                        throws SQLException {
                    User user = createUser(rs);
                    returnsList.add(user);
                    return true;
                }
            }.executeQuery(sql);
        } else {
            users = new JDBCQueryTemplate<User>(this.provider.get()) {
                @Override
                public boolean handleRow(ResultSet rs, List<User> returnsList)
                        throws SQLException {
                    User user = createUser(rs);
                    returnsList.add(user);
                    return true;
                }
            }.executeQuery(sql, prefix + "%");
        }
        return (User[]) users.toArray(new User[users.size()]);
    }

    @Override
    @InitSecurityDatabase
    public User[] findAllUsers(int[] roleIds) {
        StringTemplate template = ST_GROUP
                .getInstanceOf("findAllUsersForGroups");
        template.setAttribute("grps", roleIds);
        template.setAttribute("prefix", null);
        String sql = template.toString();
        List<User> users = new JDBCQueryTemplate<User>(this.provider.get()) {
            @Override
            public boolean handleRow(ResultSet rs, List<User> returnsList)
                    throws SQLException {
                User user = createUser(rs);
                returnsList.add(user);
                return true;
            }
        }.executeQuery(sql, roleIds);
        return (User[]) users.toArray(new User[users.size()]);
    }

    @Override
    @InitSecurityDatabase
    public User[] findAllUsers(int[] roleIds, String prefix) {
        StringTemplate template = ST_GROUP
                .getInstanceOf("findAllUsersForGroups");
        template.setAttribute("grps", roleIds);
        template.setAttribute("prefix", prefix);
        String sql = template.toString();
        List<User> users = new JDBCQueryTemplate<User>(this.provider.get()) {
            @Override
            public boolean handleRow(ResultSet rs, List<User> returnsList)
                    throws SQLException {
                User user = createUser(rs);
                returnsList.add(user);
                return true;
            }
        }.executeQuery(sql, roleIds, prefix + "%");
        return (User[]) users.toArray(new User[users.size()]);
    }

    @Override
    @InitSecurityDatabase
    public Role[] findAllRoles(int[] roleIds, String prefix) {
        StringTemplate template = ST_GROUP
                .getInstanceOf("findAllGroupsForGroups");
        template.setAttribute("grps", roleIds);
        template.setAttribute("prefix", prefix == null ? "" : prefix);
        String sql = template.toString();
        List<Role> roles = new JDBCQueryTemplate<Role>(this.provider.get()) {
            @Override
            public boolean handleRow(ResultSet rs, List<Role> returnsList)
                    throws SQLException {
                Role grp = SecurityDBUtils.createRole(rs);
                returnsList.add(grp);
                return true;
            }
        }.executeQuery(sql, roleIds, prefix + "%");
        return (Role[]) roles.toArray(new Role[roles.size()]);
    }

    @InitSecurityDatabase
    public Role[] findAllRoles(String prefix) {
        StringTemplate template = ST_GROUP.getInstanceOf("findAllGroups");
        template.setAttribute("prefix", prefix.trim().equals("") ? null
                : prefix);
        String sql = template.toString();
        List<Role> grps = null;
        if (prefix.trim().equals("")) {
            grps = new JDBCQueryTemplate<Role>(this.provider.get()) {
                @Override
                public boolean handleRow(ResultSet rs, List<Role> returnsList)
                        throws SQLException {
                    Role grp = SecurityDBUtils.createRole(rs);
                    returnsList.add(grp);
                    return true;
                }
            }.executeQuery(sql);
        } else {
            grps = new JDBCQueryTemplate<Role>(this.provider.get()) {
                @Override
                public boolean handleRow(ResultSet rs, List<Role> returnsList)
                        throws SQLException {
                    Role grp = SecurityDBUtils.createRole(rs);
                    returnsList.add(grp);
                    return true;
                }
            }.executeQuery(sql, prefix + "%");
        }
        return (Role[]) grps.toArray(new Role[grps.size()]);
    }

    @Override
    @InitSecurityDatabase
    public User[] findUsersForGivenRole(int groupId) {
        StringTemplate template = ST_GROUP
                .getInstanceOf("findUsersForGivenGroup");
        String sql = template.toString();
        List<User> usrs = new JDBCQueryTemplate<User>(this.provider.get()) {
            @Override
            public boolean handleRow(ResultSet rs, List<User> returnsList)
                    throws SQLException {
                User usr = createUser(rs);
                returnsList.add(usr);
                return true;
            }
        }.executeQuery(sql, groupId);
        return (User[]) usrs.toArray(new User[usrs.size()]);
    }

    @Override
    public void insertRole(Role role) throws SQLException {
        StringTemplate template = ST_GROUP.getInstanceOf("insertRole");
        template.setAttribute("role", role);
        JDBCUpdateTemplate jdbcTemplate = new JDBCUpdateTemplate(
                this.provider.get(), true);
        String sql = template.toString();
        LOGGER.fine(sql);
        jdbcTemplate.executeUpdate(sql);
    }

    @Override
    public void removeRole(Role role) throws SQLException {
        StringTemplate template = ST_GROUP.getInstanceOf("deleteRole");
        template.setAttribute("role", role);
        JDBCUpdateTemplate jdbcTemplate = new JDBCUpdateTemplate(
                this.provider.get(), true);
        String sql = template.toString();
        LOGGER.fine(sql);
        jdbcTemplate.executeUpdate(sql);
    }

    @Override
    public void editRole(Role role) throws SQLException {
        StringTemplate template = ST_GROUP.getInstanceOf("updateRole");
        template.setAttribute("role", role);
        JDBCUpdateTemplate jdbcTemplate = new JDBCUpdateTemplate(
                this.provider.get(), true);
        String sql = template.toString();
        LOGGER.fine(sql);
        jdbcTemplate.executeUpdate(sql);
    }

    @Override
    public boolean isLoggedUser(User user) {
        if (this.requestProvider.get().getRemoteUser() != null) {
            if (!user.getLoginname().equals(NOT_LOGGED_USER)) {
                return true;
            } else
                return false;
        } else
            return false;
    }

    @Override
    public void registerLoggedUser(final User user, final String loggedUserKey)
            throws SQLException {
        final StringTemplateGroup stGroup = ST_GROUP;
        final Connection connection = this.provider.get();

        List<JDBCCommand> commands = new ArrayList<JDBCCommand>();
        commands.add(new JDBCCommand() {

            @Override
            public Object executeJDBCCommand(Connection con)
                    throws SQLException {
                StringTemplate template = stGroup
                        .getInstanceOf("registerLoggedUser");
                template.setAttribute("user", user);
                template.setAttribute("userkey", loggedUserKey);

                JDBCUpdateTemplate update = new JDBCUpdateTemplate(connection,
                        false);
                Integer retVal = new Integer(update.executeUpdate(template
                        .toString()));
                return retVal;
            }
        });

        Role[] roles = user.getGroups();
        
        for (final Role role : roles) {
            commands.add(new JDBCCommand() {

                @Override
                public Object executeJDBCCommand(Connection con)
                        throws SQLException {
                    Integer loggedUserID = (Integer) getPreviousResult();

                    StringTemplate template = stGroup
                            .getInstanceOf("registerLoggedUserUpdateRoles");
                    template.setAttribute("loggeduserid", user.getId());
                    template.setAttribute("roleid", role.getId());

                    JDBCUpdateTemplate update = new JDBCUpdateTemplate(
                            connection, false);
                    update.executeUpdate(template.toString());

                    return loggedUserID;
                }
            });

        }

        // update in transaction
        new JDBCTransactionTemplate(connection, true)
                .updateWithTransaction(commands
                        .toArray(new JDBCCommand[commands.size()]));
    }

    @Override
    public void deleteUser(final User user) throws SQLException {

        List<JDBCCommand> commands = new ArrayList<JDBCCommand>();
        // delete all associations
        commands.add(new JDBCCommand() {

            @Override
            public Object executeJDBCCommand(Connection con)
                    throws SQLException {
                StringTemplate template = ST_GROUP
                        .getInstanceOf("disassociateRole");
                String sql = template.toString();
                Role[] roles = user.getGroups();
                for (Role r : roles) {
                    new JDBCUpdateTemplate(con, false).executeUpdate(sql,
                            user.getId(), r.getId());
                }
                return null;
            }
        });
        // delete the user
        commands.add(new JDBCCommand() {

            @Override
            public Object executeJDBCCommand(Connection con)
                    throws SQLException {
                StringTemplate template = ST_GROUP.getInstanceOf("deleteUser");
                String sql = template.toString();
                new JDBCUpdateTemplate(con, false).executeUpdate(sql,
                        user.getId());
                return null;
            }
        });

        // update in transaction
        new JDBCTransactionTemplate(this.provider.get(), true)
                .updateWithTransaction(commands
                        .toArray(new JDBCCommand[commands.size()]));

    }

    @Override
    public void insertUser(final User user, final String pswd)
            throws SQLException {

        final Connection connection = this.provider.get();

        List<JDBCCommand> commands = new ArrayList<JDBCCommand>();
        commands.add(new JDBCCommand() {

            @Override
            public Object executeJDBCCommand(Connection con)
                    throws SQLException {
                StringTemplate template = ST_GROUP
                        .getInstanceOf("insertPublicUser");
                template.setAttribute("user", user);
                // template.setAttribute("curdate", new
                // Date(System.currentTimeMillis()));
                JDBCUpdateTemplate jdbcTemplate = new JDBCUpdateTemplate(con,
                        false);
                String sql = template.toString();
                LOGGER.fine(sql);
                return jdbcTemplate.executeUpdate(sql, new java.sql.Timestamp(
                        System.currentTimeMillis()));
            }
        });

        commands.add(new JDBCCommand() {

            @Override
            public Object executeJDBCCommand(Connection con)
                    throws SQLException {
                Integer nuser = (Integer) getPreviousResult();

                Role prole = findPublicUsersRole();
                StringTemplate template = ST_GROUP
                        .getInstanceOf("insertRoleAssoc");
                JDBCUpdateTemplate jdbcTemplate = new JDBCUpdateTemplate(con,
                        false);
                String sql = template.toString();

                jdbcTemplate.executeUpdate(sql, nuser, prole.getId());
                return nuser;
            }
        });

        commands.add(new JDBCCommand() {

            @Override
            public Object executeJDBCCommand(Connection con)
                    throws SQLException {
                try {
                    Integer nuser = (Integer) getPreviousResult();
                    String digested = PasswordDigest.messageDigest(pswd);
                    StringTemplate template = ST_GROUP
                            .getInstanceOf("updatePassword");
                    String sql = template.toString();
                    JDBCUpdateTemplate jdbcTemplate = new JDBCUpdateTemplate(
                            connection, false);
                    jdbcTemplate.setUseReturningKeys(false);
                    jdbcTemplate.executeUpdate(sql, digested, nuser);
                    return nuser;
                } catch (NoSuchAlgorithmException e) {
                    throw new SQLException(e);
                } catch (UnsupportedEncodingException e) {
                    throw new SQLException(e);
                }
            }
        });

        // update in transaction
        new JDBCTransactionTemplate(connection, true)
                .updateWithTransaction(commands
                        .toArray(new JDBCCommand[commands.size()]));
    }

    @Override
    public void saveUserPassword(User user, String pswd) throws SQLException {
        try {
            final Connection connection = this.provider.get();
            String digested = PasswordDigest.messageDigest(pswd);
            StringTemplate template = ST_GROUP.getInstanceOf("updatePassword");
            String sql = template.toString();
            new JDBCUpdateTemplate(connection, true).executeUpdate(sql,
                     digested,user.getId());
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Override
    public void activateUser(User user) throws SQLException {
        final Connection connection = this.provider.get();
        StringTemplate template = ST_GROUP.getInstanceOf("activateUser");
        String sql = template.toString();
        new JDBCUpdateTemplate(connection, true).executeUpdate(sql,
                user.getLoginname());
    }

    @Override
    public void insertPublicUsersRole() throws SQLException {
        Role role = new RoleImpl(-1, PUBLIC_USERS_ROLE_NAME, -1);
        this.insertRole(role);
    }

    @Override
    public Role findPublicUsersRole() {
        StringTemplate template = ST_GROUP.getInstanceOf("findPublicRole");
        List<Role> roles = new JDBCQueryTemplate<Role>(this.provider.get()) {

            @Override
            public boolean handleRow(ResultSet rs, List<Role> returnsList)
                    throws SQLException {
                Role role = SecurityDBUtils.createRole(rs);
                returnsList.add(role);
                return true;
            }
        }.executeQuery(template.toString(), PUBLIC_USERS_ROLE_NAME);

        return roles.isEmpty() ? null : roles.get(0);
    }

    public void changeRoles(final User user, final List<String> rnames)
            throws SQLException {

        List<JDBCCommand> cmds = new ArrayList<JDBCCommand>();
        for (final String rname : rnames) {

            JDBCCommand command = new JDBCCommand() {
                public Object executeJDBCCommand(Connection con)
                        throws SQLException {

                    List<Role> roles = (List<Role>) getPreviousResult();
                    if (roles == null) {
                        roles = new ArrayList<Role>();
                    }

                    StringTemplate template = ST_GROUP
                            .getInstanceOf("findGroupByGname");
                    String sql = template.toString();

                    List<Role> rr = new JDBCQueryTemplate<Role>(con, false) {
                        @Override
                        public boolean handleRow(ResultSet rs,
                                List<Role> returnsList) throws SQLException {
                            Role grp = SecurityDBUtils.createRole(rs);
                            returnsList.add(grp);
                            return true;
                        }
                    }.executeQuery(sql, rname);

                    if (rr.isEmpty())
                        throw new SQLException("cannot find role '" + rname
                                + "'");
                    roles.add(rr.get(0));
                    return roles;
                }
            };
            cmds.add(command);
        }
        // dissasociate all old roles
        cmds.add(new JDBCCommand() {

            public Object executeJDBCCommand(Connection con)
                    throws SQLException {
                StringTemplate template = ST_GROUP
                        .getInstanceOf("disassociateRole");
                String sql = template.toString();
                Role[] roles = user.getGroups();
                if (roles != null) {
                    for (Role r : roles) {
                        new JDBCUpdateTemplate(con, false).executeUpdate(sql,
                                user.getId(), r.getId());
                    }
                }
                return getPreviousResult();
            }

        });
        // associate new roles
        cmds.add(new JDBCCommand() {

            @Override
            public Object executeJDBCCommand(Connection con)
                    throws SQLException {

                List<Role> roles = (List<Role>) getPreviousResult();
                for (Role r : roles) {
                    StringTemplate template = ST_GROUP
                            .getInstanceOf("associateRole");
                    String sql = template.toString();
                    new JDBCUpdateTemplate(con, false).executeUpdate(sql,
                            user.getId(), r.getId());
                }
                return roles;
            }
        });

        // do in one transaction
        final Connection connection = this.provider.get();
        JDBCTransactionTemplate transaction = new JDBCTransactionTemplate(
                connection, true);
        transaction.updateWithTransaction(cmds);
    }

    @Override
    public void associateRole(User user, Role role) throws SQLException {
        final Connection connection = this.provider.get();
        StringTemplate template = ST_GROUP.getInstanceOf("associateRole");
        String sql = template.toString();
        new JDBCUpdateTemplate(connection, true).executeUpdate(sql,
                user.getId(), role.getId());
    }

    @Override
    public void disAssociateRole(User user, Role role) throws SQLException {
        final Connection connection = this.provider.get();
        StringTemplate template = ST_GROUP.getInstanceOf("disassociateRole");
        String sql = template.toString();
        new JDBCUpdateTemplate(connection, true).executeUpdate(sql,
                user.getId(), role.getId());
    }

    private User createUser(ResultSet rs) throws SQLException {
        User user = SecurityDBUtils.createUser(rs);
        ((UserImpl) user).setGroups(rolesForUser(user.getId()));
        return user;
    }

}
