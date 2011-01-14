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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.security.Group;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.database.SecurityDatabaseUtils;
import cz.incad.kramerius.security.utils.SecurityDBUtils;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;

public class DatabaseUserManager implements UserManager{

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(DatabaseUserManager.class.getName());
    
    @Inject
    @Named("kramerius4")
    Provider<Connection> provider;

    
    @Override
    public User validateUser(final String loginName, final String passwd) {
        String sql = SecurityDatabaseUtils.stGroup().getInstanceOf("findUser").toString();
        List<User> users= new JDBCQueryTemplate<User>(this.provider.get()){
            @Override
            public boolean handleRow(ResultSet rs, List<User> returnsList) throws SQLException {
                    returnsList.add(SecurityDBUtils.createUser(rs));
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
    public Group[] findGroups(int user_id) {
        String sql = SecurityDatabaseUtils.stGroup().getInstanceOf("findAllGroupsByUserId").toString();
        List<Group> users= new JDBCQueryTemplate<Group>(this.provider.get()){
            @Override
            public boolean handleRow(ResultSet rs, List<Group> returnsList) throws SQLException {
                    Group grp = SecurityDBUtils.createGroup(rs);
                    returnsList.add(grp);
                    return true;
            }
        }.executeQuery(sql, user_id);
        return (users != null) ? (Group[]) users.toArray(new Group[users.size()]) : new Group[0];
    }

    @Override
    public User findUser(int user_id) {
        String sql = SecurityDatabaseUtils.stGroup().getInstanceOf("findUserByUserId").toString();
        List<User> users= new JDBCQueryTemplate<User>(this.provider.get()){
            @Override
            public boolean handleRow(ResultSet rs, List<User> returnsList) throws SQLException {
                    User user = SecurityDBUtils.createUser(rs);
                    returnsList.add(user);
                    return true;
            }
        }.executeQuery(sql, user_id);
        return (users != null) && (!users.isEmpty()) ? users.get(0) : null;
    }

    @Override
    public Group findGroup(int group_id) {
        String sql = SecurityDatabaseUtils.stGroup().getInstanceOf("findGroupByGroupId").toString();
        List<Group> groups= new JDBCQueryTemplate<Group>(this.provider.get()){
            @Override
            public boolean handleRow(ResultSet rs, List<Group> returnsList) throws SQLException {
                    Group grp = SecurityDBUtils.createGroup(rs);
                    returnsList.add(grp);
                    return true;
            }
        }.executeQuery(sql, group_id);
        return (groups != null) && (!groups.isEmpty()) ? groups.get(0) : null;
    }

    @Override
    public Group findCommonUsersGroup() {
        String sql = SecurityDatabaseUtils.stGroup().getInstanceOf("findCommonUsersGroup").toString();
        List<Group> groups= new JDBCQueryTemplate<Group>(this.provider.get()){
            @Override
            public boolean handleRow(ResultSet rs, List<Group> returnsList) throws SQLException {
                    Group grp = SecurityDBUtils.createGroup(rs);
                    returnsList.add(grp);
                    return true;
            }
        }.executeQuery(sql);
        return (groups != null) && (!groups.isEmpty()) ? groups.get(0) : null;
    }

    @Override
    public Group findGroupByName(String gname) {
        String sql = SecurityDatabaseUtils.stGroup().getInstanceOf("findGroupByGname").toString();
        List<Group> groups= new JDBCQueryTemplate<Group>(this.provider.get()){
            @Override
            public boolean handleRow(ResultSet rs, List<Group> returnsList) throws SQLException {
                    Group grp = SecurityDBUtils.createGroup(rs);
                    returnsList.add(grp);
                    return true;
            }
        }.executeQuery(sql, gname);
        return (groups != null) && (!groups.isEmpty()) ? groups.get(0) : null;
    }

    @Override
    public User findUserByLoginName(String loginName) {
        String sql = SecurityDatabaseUtils.stGroup().getInstanceOf("findGroupByLoginName").toString();
        List<User> users= new JDBCQueryTemplate<User>(this.provider.get()){
            @Override
            public boolean handleRow(ResultSet rs, List<User> returnsList) throws SQLException {
                    User user = SecurityDBUtils.createUser(rs);
                    returnsList.add(user);
                    return true;
            }
        }.executeQuery(sql, loginName);
        return (users != null) && (!users.isEmpty()) ? users.get(0) : null;
    }
    
    
}
