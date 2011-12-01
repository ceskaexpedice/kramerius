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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

import org.antlr.stringtemplate.StringTemplate;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.security.User;
import cz.incad.kramerius.users.UserProfile;
import cz.incad.kramerius.users.UserProfileManager;
import cz.incad.kramerius.users.utils.ActiveUsersUtils;
import cz.incad.kramerius.utils.DatabaseUtils;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.JDBCUpdateTemplate;

public class UserProfileManagerImpl implements UserProfileManager {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(UserProfileManagerImpl.class.getName());
    
    @Inject
    @Named("kramerius4")
    Provider<Connection> connectionProvider;

    @Override
    public UserProfile getProfile(User user) {
        Connection con = this.connectionProvider.get();
        try {
            Integer activeUser = ActiveUsersUtils.findActiveUser(user, con);
            List<String> list = findProfileText(con, activeUser);
            if (!list.isEmpty()) {
                UserProfileImpl userProfileImpl = new UserProfileImpl(list.get(0));
                return userProfileImpl;
            } else {
                return new UserProfileImpl();
            }
        } finally {
            DatabaseUtils.tryClose(con);
        }
    }

    public List<String> findProfileText(Connection con, Integer activeUser) {
        StringTemplate template = LoggedUsersSingletonImpl.stGroup.getInstanceOf("findUserProfile");
        List<String> list = new JDBCQueryTemplate<String>(con, false) {
            @Override
            public boolean handleRow(ResultSet rs, List<String> returnsList) throws SQLException {
                String profile = rs.getString("profile");
                returnsList.add(profile);
                return true;
            }
        }.executeQuery(template.toString(), activeUser);
        return list;
    }

    @Override
    public synchronized void saveProfile(User user, UserProfile profile) {
        Connection con = this.connectionProvider.get();
        try {
            Integer activeUser = ActiveUsersUtils.findActiveUser(user, con);
            List<String> profileText = findProfileText(con, activeUser);
            if (!profileText.isEmpty()) {
                
                StringTemplate updateTemplate = LoggedUsersSingletonImpl.stGroup.getInstanceOf("updateUserProfile");
                JDBCUpdateTemplate update = new JDBCUpdateTemplate(con, false);
                update.executeUpdate(updateTemplate.toString(), profile.getRawData(),activeUser);
                
            } else {
                StringTemplate updateTemplate = LoggedUsersSingletonImpl.stGroup.getInstanceOf("insertProfile");
                JDBCUpdateTemplate update = new JDBCUpdateTemplate(con, false);
                update.executeUpdate(updateTemplate.toString(), activeUser,profile.getRawData());
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }finally {
            DatabaseUtils.tryClose(con);
        }
    }
    
}
