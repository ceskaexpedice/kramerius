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
package cz.incad.kramerius.users.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.antlr.stringtemplate.StringTemplate;

import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.users.impl.LoggedUsersSingletonImpl;
import cz.incad.kramerius.utils.database.JDBCCommand;
import cz.incad.kramerius.utils.database.JDBCPreparedStatementCommand;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.JDBCTransactionTemplate;

/**
 * Active user utility class
 * @author pavels
 */
public class ActiveUsersUtils {


    /**
     * Find active user id 
     * @param user User object
     * @param con Database connection
     * @return Database id
     */
    public static Integer findActiveUser(User user, Connection con) {
        StringTemplate template = LoggedUsersSingletonImpl.stGroup.getInstanceOf("findUserByLoginName");
        List<Integer> list = new JDBCQueryTemplate<Integer>(con, false) {
            @Override
            public boolean handleRow(ResultSet rs, List<Integer> returnsList) throws SQLException {
                int id = rs.getInt("active_users_id");
                returnsList.add(id);
                return true;
            }
        }.executeQuery(template.toString(), user.getLoginname());
        return list.isEmpty() ? -1 : list.get(0);
    }
    

    public static Integer findAndRegisterActiveUser(User user, Connection con) throws SQLException {
        StringTemplate template = LoggedUsersSingletonImpl.stGroup.getInstanceOf("findUserByLoginName");
        List<Integer> list = new JDBCQueryTemplate<Integer>(con, false) {
            @Override
            public boolean handleRow(ResultSet rs, List<Integer> returnsList) throws SQLException {
                int id = rs.getInt("active_users_id");
                returnsList.add(id);
                return true;
            }
    
        }.executeQuery(template.toString(), user.getLoginname());
        if (list.isEmpty()) {
    
            List<JDBCCommand> commands = new ArrayList<JDBCCommand>();
            StringTemplate userTmpl = LoggedUsersSingletonImpl.stGroup.getInstanceOf("registerActiveUser");
            commands.add(new JDBCPreparedStatementCommand(con, userTmpl.toString(), user.getLoginname(), user.getFirstName(), user.getSurname()));
    
            Role[] grps = user.getGroups();
            for (Role role : grps) {
                StringTemplate mappingTmpl = LoggedUsersSingletonImpl.stGroup.getInstanceOf("registerLoggedUserUpdateRoles");
    
                commands.add(new JDBCPreparedStatementCommand(con, mappingTmpl.toString(), role.getId()) {
    
                    @Override
                    public void prepareStatement() throws SQLException {
                        this.preparedStatement.setInt(1, (Integer) getPreviousResult());
                        for (int i = 0, index = 2; i < params.length; i++) {
                            int changedIndex = setParam(index, params[i], this.preparedStatement);
                            index = changedIndex + 1;
                        }
                    }
    
                    @Override
                    public Object executeJDBCCommand(Connection con) throws SQLException {
                        super.executeJDBCCommand(con);
                        return getPreviousResult();
                    }
                });
            }
    
            return (Integer) new JDBCTransactionTemplate(con, false).updateWithTransaction(commands);
        } else {
            final Integer activeUserId = list.get(0);
            List<JDBCCommand> commands = new ArrayList<JDBCCommand>();
    
            StringTemplate updateUserTemplate = LoggedUsersSingletonImpl.stGroup.getInstanceOf("updateActiveUser");
            commands.add(new JDBCPreparedStatementCommand(con, updateUserTemplate.toString(),
                    user.getFirstName(),user.getSurname(), activeUserId));
    
            StringTemplate delTemplate = LoggedUsersSingletonImpl.stGroup.getInstanceOf("deleteRoleActiveId");
            commands.add(new JDBCPreparedStatementCommand(con, delTemplate.toString(), activeUserId));
    
            StringTemplate mappingTmpl = LoggedUsersSingletonImpl.stGroup.getInstanceOf("registerLoggedUserUpdateRoles");
    
            Role[] grps = user.getGroups();
            for (Role role : grps) {
                commands.add(new JDBCPreparedStatementCommand(con, mappingTmpl.toString(), activeUserId, role.getId()));
            }
    
            new JDBCTransactionTemplate(con, false).updateWithTransaction(commands);
            return activeUserId;
        }
    }

}
