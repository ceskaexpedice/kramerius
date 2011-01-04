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

import java.sql.ResultSet;
import java.sql.SQLException;

import cz.incad.kramerius.security.Group;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.impl.GroupImpl;
import cz.incad.kramerius.security.impl.UserImpl;

public class SecurityDBUtils {

    public static Group createGroup(ResultSet rs) throws SQLException {
        int id = rs.getInt("group_id");
        String gname = rs.getString("gname");    
        Group grp = new GroupImpl(id, gname);
        return grp;
    }

    public static User createUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("user_id");
        String loginName = rs.getString("loginname");    
        String firstName = rs.getString("name");    
        String surName = rs.getString("surname");
        User user = new UserImpl(id, firstName, surName, loginName);
        return user;
    }

}
