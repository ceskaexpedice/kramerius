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
package cz.incad.kramerius.users.impl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.processes.NotReadyException;
import cz.incad.kramerius.processes.database.ProcessDatabaseUtils;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.database.SecurityDatabaseUtils;
import cz.incad.kramerius.security.impl.RoleImpl;
import cz.incad.kramerius.security.impl.UserImpl;
import cz.incad.kramerius.security.utils.UserUtils;
import cz.incad.kramerius.users.LoggedUsersSingleton;
import cz.incad.kramerius.users.database.LoggedUserDatabaseInitializator;
import cz.incad.kramerius.users.utils.ActiveUsersUtils;
import cz.incad.kramerius.utils.DatabaseUtils;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.JDBCUpdateTemplate;
import cz.incad.kramerius.utils.database.JDBCPreparedStatementCommand.NULLS;

public class LoggedUsersSingletonImpl implements LoggedUsersSingleton {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(LoggedUsersSingletonImpl.class.getName());
    
    public static StringTemplateGroup stGroup;
    static {
        InputStream is = LoggedUserDatabaseInitializator.class.getResourceAsStream("res/database.stg");
        stGroup = new StringTemplateGroup(new InputStreamReader(is), DefaultTemplateLexer.class);
    }

    // @Inject
    @Inject
    @Named("kramerius4")
    Provider<Connection> connectionProvider;
    
    @Inject
    Provider<HttpServletRequest> requeProvider;
    
    
    @Inject
    LRProcessManager lrProcessManager;
    

    @Override
    public synchronized String registerLoggedUser(User user) {
        String randomUUID = UUID.randomUUID().toString();
        Connection con = null;
        try {
            con = this.connectionProvider.get();
            Integer activeUserId = ActiveUsersUtils.findAndRegisterActiveUser(user, con);
            sessionKeyId(con, activeUserId, randomUUID);
            return randomUUID;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            throw new RuntimeException(e.getMessage());
        } finally {
            DatabaseUtils.tryClose(con);
        }
    }

    
    public void sessionKeyId(Connection con, Integer activeUserId, String loggedUserKey) throws SQLException {
        StringTemplate tmpl = stGroup.getInstanceOf("registerSessionKey");
        JDBCUpdateTemplate update = new JDBCUpdateTemplate(con,false);
        HttpServletRequest req = this.requeProvider.get();
        update.executeUpdate(tmpl.toString(), loggedUserKey,activeUserId, req.getRemoteAddr());
    }
    
    @Override
    public synchronized void deregisterLoggedUser(String key) {
        boolean isSessionKeyAssociatedWithProcess = lrProcessManager.isSessionKeyAssociatedWithProcess(key);

        Connection con = null;
        try {
            con = this.connectionProvider.get();
            if (!isSessionKeyAssociatedWithProcess) {
                deleteSessionKey(key, con);
            } else {
                deregisterLoggedUser(key, con);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } finally {
            DatabaseUtils.tryClose(con);
        }
    }


    public void deleteSessionKey(String key, Connection con) throws SQLException {
        StringTemplate deregisterTemplate = stGroup.getInstanceOf("deleteSessionKey");
        new JDBCUpdateTemplate(con,false).executeUpdate(deregisterTemplate.toString(), key);
    }
    
    public void deregisterLoggedUser(String key, Connection con) throws SQLException {
        StringTemplate deregisterTemplate = stGroup.getInstanceOf("deregisterSessionKey");
        new JDBCUpdateTemplate(con,false).executeUpdate(deregisterTemplate.toString(), key);
    }

    @Override
    public synchronized boolean isLoggedUser(String key) {
        Connection con = null;
        try {
            con = this.connectionProvider.get();
            return loggedUser(key, con) > -1;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return false;
        }
    }
    

    public int getSessionKeyId(String key) {
        Connection con = null;
        try {
            con = this.connectionProvider.get();
            return sessionKey(key, con);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return -1;
        }
    }
    
    public int sessionKey(String key, Connection con) throws SQLException {
        StringTemplate islogged = stGroup.getInstanceOf("user");
        List<Integer> list = new JDBCQueryTemplate<Integer>(con) {

            @Override
            public boolean handleRow(ResultSet rs, List<Integer> returnsList) throws SQLException {
                returnsList.add(rs.getInt("session_keys_id"));
                return false;
            }
            
        }.executeQuery(islogged.toString(), key);
        return !list.isEmpty() ? list.get(0) : -1;
    }

    public int user(String key, Connection con) throws SQLException {
        StringTemplate islogged = stGroup.getInstanceOf("user");
        List<Integer> list = new JDBCQueryTemplate<Integer>(con) {

            @Override
            public boolean handleRow(ResultSet rs, List<Integer> returnsList) throws SQLException {
                returnsList.add(rs.getInt("active_users_id"));
                return false;
            }
            
        }.executeQuery(islogged.toString(), key);
        return !list.isEmpty() ? list.get(0) : -1;
    }

    public int loggedUser(String key, Connection con) throws SQLException {
        StringTemplate islogged = stGroup.getInstanceOf("isLoggedUser");
        List<Integer> list = new JDBCQueryTemplate<Integer>(con) {

            @Override
            public boolean handleRow(ResultSet rs, List<Integer> returnsList) throws SQLException {
                returnsList.add(rs.getInt("active_users_id"));
                return false;
            }
            
        }.executeQuery(islogged.toString(), key);
        return !list.isEmpty() ? list.get(0) : -1;
    }

    @Override
    public synchronized boolean isLoggedUser(Provider<HttpServletRequest> provider) {
        HttpSession session = provider.get().getSession(true);
        String userKey = (String) session.getAttribute(UserUtils.LOGGED_USER_KEY_PARAM);
        return userKey != null ? isLoggedUser(userKey) : false;
    }

    @Override
    public User getUser(String key) {
        try {
            if (key == null) return null;
            int activeUserId =  user(key, this.connectionProvider.get());
            if (activeUserId > -1) {
                Connection connection = this.connectionProvider.get();
                return getUser(activeUserId, connection);
            } else {
                return null;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return null;
        }
    }


    @Override
    public User getLoggedUser(String key) {
        try {
            int activeUserId =  loggedUser(key, this.connectionProvider.get());
            if (activeUserId > -1) {
                Connection connection = this.connectionProvider.get();
                return getUser(activeUserId, connection);
            } else {
                return null;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return null;
        }
    }

    public User getUser(int activeUserId, Connection connection) {
        StringTemplate users = stGroup.getInstanceOf("getLoggedUser");
        List<User> list = new JDBCQueryTemplate<User>(connection) {
            @Override
            public boolean handleRow(ResultSet rs, List<User> returnsList) throws SQLException {
                User user = null;
                if (returnsList.isEmpty()) {
                    String loginname = rs.getString("loginname");
                    String firstname = rs.getString("firstname");
                    String surname = rs.getString("surname");
        
                    user = new UserImpl(-1, firstname, surname, loginname,-1);
                    returnsList.add(user);
                } else {
                    user = returnsList.get(0);
                }

                int group_id = rs.getInt("group_id");
                String gname = rs.getString("gname");
                int personaladm = rs.getInt("personal_admin_id");
                Role role = new RoleImpl(group_id, gname, personaladm);
                
                Role[] roles = user.getGroups() != null ? user.getGroups() : new Role[0];
                Role[] nroles = new Role[roles.length+1];
                System.arraycopy(roles, 0, nroles, 0, roles.length);
                nroles[roles.length] = role;
                ((UserImpl)user).setGroups(nroles);
                
                return true;
            }
            
        }.executeQuery(users.toString(), activeUserId);
        return list.get(0);
    }

}
