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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.IsActionAllowedBase;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.utils.SecurityDBUtils;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;

public class IsActionAllowedBaseImpl implements IsActionAllowedBase {
    
    // rightsadmin
    // rightssubadmin
    
    private static String getGroupIds(User user) {
        StringBuffer buffer = new StringBuffer();
        Role[] grps = user.getGroups();
        for (int i = 0; i < grps.length; i++) {
            Role grp = grps[i];
            buffer.append(grp.getId());
            if (i <= grps.length-1) { buffer.append(","); }
        }
        return buffer.toString();
    }
    
    @Override
    public boolean isActionAllowed(User user, String actionName) {
        String query = "select * from right_entity ent"+
            "left join  user_entity users on  (ent.user_id = users.user_id)"+
            "left join  group_entity groups on  (ent.group_id = groups.group_id)"+
        "where uuid='uuid:1' and \"action\"='" +actionName+"'"+
            " (ent.user_id="+user.getId()+" or ent.group_id in ("+getGroupIds(user)+"))";
        
        List<String> results = new JDBCQueryTemplate<String>(SecurityDBUtils.getConnection()){
            @Override
            public boolean handleRow(ResultSet rs, List<String> retList) throws SQLException {
                retList.add("");
                return false;
            }
        }.executeQuery(query);
        return results.isEmpty();
    }
}
