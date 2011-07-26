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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.database.SecurityDatabaseUtils;
import cz.incad.kramerius.security.utils.UserUtils;
import cz.incad.kramerius.users.LoggedUsersSingleton;
import cz.incad.kramerius.users.database.LoggedUserDatabaseInitializator;
import cz.incad.kramerius.utils.database.JDBCCommand;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.JDBCTransactionTemplate;
import cz.incad.kramerius.utils.database.JDBCUpdateTemplate;

public class LoggedUsersSingletonImpl implements LoggedUsersSingleton {

    private HashMap<String, User> loggedUsers = new HashMap<String, User>();

    private static StringTemplateGroup stGroup;
    static {
        InputStream is = LoggedUserDatabaseInitializator.class.getResourceAsStream("res/database.stg");
        stGroup = new StringTemplateGroup(new InputStreamReader(is), DefaultTemplateLexer.class);
    }
    
    //@Inject
    @Inject
    @Named("kramerius4")
    Provider<Connection> provider;
    
    
    
    @Override
    public synchronized String registerLoggedUser(User user) {
        String randomUUID = UUID.randomUUID().toString();
        this.loggedUsers.put(randomUUID, user);
        return randomUUID;
    }

    @Override
    public synchronized void deregisterLoggedUser(String key) {
        this.loggedUsers.remove(key);
    }

    @Override
    public synchronized boolean isLoggedUser(String key)  {
        return this.loggedUsers.containsKey(key);
    }

    @Override
    public synchronized boolean isLoggedUser(Provider<HttpServletRequest> provider) {
        HttpSession session = provider.get().getSession(true);
        String userKey = (String) session.getAttribute(UserUtils.LOGGED_USER_KEY_PARAM);
        return userKey != null ? isLoggedUser(userKey) : false;
    }

    @Override
    public User getLoggedUser(String key) {
        return this.loggedUsers.get(key);
    }
    
    

    /*
    public void removeRowFromLoggedUser(final User user, final String loggedUserKey) {
        StringTemplate sql = stGroup.getInstanceOf("selectLoggedUser");
        Connection connection = this.provider.get();
        final List<Integer> row = new JDBCQueryTemplate<Integer>(connection,false) {
            @Override
            public boolean handleRow(ResultSet rs, List<Integer> returnsList) throws SQLException {
                returnsList.add(rs.getInt("logged_users_id"));
                return false;
            }
        }.executeQuery(sql.toString(),loggedUserKey);

        
        if (!row.isEmpty()) {
            List<JDBCCommand> commands = new ArrayList<JDBCCommand>();
            commands.add(new JDBCCommand() {
                
                @Override
                public Object executeJDBCCommand() throws SQLException {

                    StringTemplate template = stGroup.getInstanceOf("registerLoggedUser");
                    template.setAttribute("user", user);
                    template.setAttribute("userkey", loggedUserKey);

                    return null;
                }
            });
        }
        
        
    }
    
    public void insertRowToLoggedUser(final User user, final String loggedUserKey) throws SQLException {
        final StringTemplateGroup stGroup = SecurityDatabaseUtils.stGroup();
        final Connection connection = this.provider.get();
        
        List<JDBCCommand> commands = new ArrayList<JDBCCommand>();
        commands.add(new JDBCCommand() {
            
            @Override
            public Object executeJDBCCommand() throws SQLException {
                StringTemplate template = stGroup.getInstanceOf("registerLoggedUser");
                template.setAttribute("user", user);
                template.setAttribute("userkey", loggedUserKey);

                JDBCUpdateTemplate update = new JDBCUpdateTemplate(connection, false);
                Integer retVal = new Integer(update.executeUpdate(template.toString()));
                return retVal;
            }
        });
        
        Role[] roles = user.getGroups();
        for (final Role role : roles) {
            commands.add(new JDBCCommand() {
                
                @Override
                public Object executeJDBCCommand() throws SQLException {
                    Integer loggedUserID = (Integer) getPreviousResult();
                    
                    StringTemplate template = stGroup.getInstanceOf("registerLoggedUserUpdateRoles");
                    template.setAttribute("loggeduserid", user.getId());
                    template.setAttribute("roleid", role.getId());
                    
                    JDBCUpdateTemplate update = new JDBCUpdateTemplate(connection, false);
                    update.executeUpdate(template.toString());
                    
                    return loggedUserID;
                }
            });
            
        }
        
        // update in transaction
        new JDBCTransactionTemplate(connection, true).
            updateWithTransaction(commands.toArray(new JDBCCommand[commands.size()]));
    }
     */
}
