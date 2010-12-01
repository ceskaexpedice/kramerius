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

import static cz.incad.kramerius.security.database.SecurityDatabaseUtils.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;


import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.processes.database.DatabaseUtils;
import cz.incad.kramerius.security.AbstractUser;
import cz.incad.kramerius.security.Right;
import cz.incad.kramerius.security.RightParam;
import cz.incad.kramerius.security.RightParamFactory;
import cz.incad.kramerius.security.RightParamType;
import cz.incad.kramerius.security.RightsManager;
import cz.incad.kramerius.security.database.SecurityDatabaseUtils;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;

public class DatabaseRightsManager extends RightsManager {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(DatabaseRightsManager.class.getName());


    @Inject
    @Named("kramerius4")
    Provider<Connection> provider;
    
    @Inject
    RightParamFactory paramFactory;
    
    @Override
    public Right findRight(final String uuid, final String action, final AbstractUser user) {
        String command = SecurityDatabaseUtils.stGroup().getInstanceOf("findRight").toString();
        List<Right> rights = new JDBCQueryTemplate<Right>(this.provider){
            @Override
            public boolean handleRow(ResultSet rs, List<Right> returnsList) throws SQLException {
                String actionVal = rs.getString("action");
                String uuidVal = rs.getString("uuid");
                String qname = rs.getString("qname");
                int rParId = rs.getInt("right_att_id");
                if (qname!=null) {
                    int type = rs.getInt("type");
                    if (type >= 0) {
                        RightParam param = paramFactory.create(rParId, qname, type);
                        RightImpl rightImpl = new RightImpl(param, uuidVal, actionVal, user);
                        returnsList.add(rightImpl);
                    } else {
                        returnsList.add(new RightImpl(null, uuidVal, actionVal, user));
                    }
                }
                returnsList.add(new RightImpl(null, uuidVal, actionVal, user));
                return false;
            }
        }.executeQuery(command, action, user.getId(), uuid.startsWith("uuid:") ? uuid: "uuid:"+uuid);
        return ((rights != null) && (!rights.isEmpty())) ? rights.get(0) : null;
    }

    public Provider<Connection> getProvider() {
        return provider;
    }

    public void setProvider(Provider<Connection> provider) {
        this.provider = provider;
    }
    
}
