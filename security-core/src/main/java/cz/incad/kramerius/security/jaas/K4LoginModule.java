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
package cz.incad.kramerius.security.jaas;

import java.io.IOException;
import java.security.Principal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import javax.sql.DataSource;


import cz.incad.kramerius.security.Group;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.impl.UserImpl;
import cz.incad.kramerius.security.utils.SecurityDBUtils;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;

/**
 * JAAS login module for K4
 * @author pavels
 */
public class K4LoginModule implements LoginModule {

    private static String JNDI_NAME="java:comp/env/jdbc/kramerius4";

    private static final String PSWD_COL = "pswd";

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(K4LoginModule.class.getName());
    
    private Subject subject;
    private CallbackHandler callbackhandler;
    private Map<String, ?> options;

    
    private boolean logged = false;

    private User foundUser;
    private String foundPswd;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackhandler = callbackHandler;
        this.options = options;
        

    }

    @Override
    public boolean login() throws LoginException {
        try {
            
            NameCallback nmCallback = new NameCallback("Name");
            PasswordCallback pswdCallback = new PasswordCallback("Password",false);
            this.callbackhandler.handle(new Callback[] {nmCallback, pswdCallback});

            String loginName = nmCallback.getName();
            char[] pswd = pswdCallback.getPassword();
            
            new JDBCQueryTemplate<User>(getConnection()){
                @Override
                public boolean handleRow(ResultSet rs, List<User> retList) throws SQLException {
                    
                    foundUser= SecurityDBUtils.createUser(rs);
                    foundPswd = rs.getString(PSWD_COL);
                    return false;
                }
                
            }.executeQuery("select * from user_entity where loginname=?", loginName);

            if (foundUser != null) {
                
                List<Group> groupsList = new JDBCQueryTemplate<Group>(getConnection()){
                    @Override
                    public boolean handleRow(ResultSet rs, List<Group> retList) throws SQLException {
                        retList.add(SecurityDBUtils.createGroup(rs));
                        return true;
                    }
                    
                }.executeQuery("select * from user_group_mapping where user_id=?", foundUser.getId());
                
                //TODO:Zmenit
                ((UserImpl)foundUser).setGroups((Group[]) groupsList.toArray(new Group[groupsList.size()]));
                
                boolean result = checkPswd(foundUser.getLoginname(),foundPswd,pswd);
                
                this.logged = result;
            } else {
                this.logged = false;
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (UnsupportedCallbackException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return this.logged;
    }

    private boolean checkPswd(String string, String expectedPswd, char[] pswd) {
        return expectedPswd.equals(new String(pswd));
    }

    @Override
    public boolean commit()  throws LoginException {
        if (!this.logged ) return false;

        K4UserPrincipal userPrincipal = new K4UserPrincipal(foundUser);
        K4RolePrincipal rolePrincipal = new K4RolePrincipal("formalRole");
        // vyhodit .. 
        K4RolePrincipal webRole = new K4RolePrincipal("krameriusAdmin");

        assignPrincipal(userPrincipal);
        assignPrincipal(rolePrincipal);
        assignPrincipal(webRole);

        return true;
    }

    public void assignPrincipal(Principal principal) {
        if (!subject.getPrincipals().contains(principal)) {
            subject.getPrincipals().add(principal);
        }
    }

    @Override
    public boolean abort() throws LoginException {
        //throw new IllegalStateException("illegal call");
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        this.subject.getPrincipals().clear();
        return true;
    }
    
    // without guice because of classloaders
    /**
     * 
     * @return
     */
    public Connection getConnection() {
        try {
            InitialContext ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup(JNDI_NAME);   
            return ds.getConnection();
        } catch (NamingException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return null;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);;
            return null;
        }
    }

}
