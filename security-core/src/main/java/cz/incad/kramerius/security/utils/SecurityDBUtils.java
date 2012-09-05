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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.impl.RoleImpl;
import cz.incad.kramerius.security.impl.UserImpl;
import cz.incad.kramerius.security.jaas.K4LoginModule;

/**
 * Security DB utils
 * @author pavels
 */
public class SecurityDBUtils {

    /**
     * Creates role instance from given {@link ResultSet}
     * @param rs ResultSet
     * @return new role
     * @throws SQLException SQL error has been occurred
     */
    public static Role createRole(ResultSet rs) throws SQLException {
        int id = rs.getInt("group_id");
        String gname = rs.getString("gname");    
        int personalAdminId = rs.getInt("personal_admin_id");
        Role grp = new RoleImpl(id, gname, personalAdminId);
        return grp;
    }

    /**
     * Creates user instance from given {@link ResultSet}
     * @param rs ResultSet
     * @return new User instance
     * @throws SQLException SQL error has been occurred
     */
    public static User createUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("user_id");
        String loginName = rs.getString("loginname");    
        String firstName = rs.getString("name");    
        String surName = rs.getString("surname");
        int personalAdminId = rs.getInt("personal_admin_id");
        User user = new UserImpl(id, firstName, surName, loginName, personalAdminId);
        return user;
    }
    
    /**
     * JNDI key
     */
    public static String JNDI_NAME="java:comp/env/jdbc/kramerius4";

    // without guice because of classloaders
    public static Connection getConnection() {
        try {
            InitialContext ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup(JNDI_NAME);   
            return ds.getConnection();
        } catch (NamingException e) {
            K4LoginModule.LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return null;
        } catch (SQLException e) {
            K4LoginModule.LOGGER.log(Level.SEVERE, e.getMessage(), e);;
            return null;
        }
    }


}
