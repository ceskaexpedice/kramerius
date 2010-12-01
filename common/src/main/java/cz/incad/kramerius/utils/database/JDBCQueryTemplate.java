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
package cz.incad.kramerius.utils.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;

import com.google.inject.Provider;
import com.sun.jmx.snmp.Timestamp;

import cz.incad.kramerius.security.database.SecurityDatabaseUtils;
import cz.incad.kramerius.security.impl.UserImpl;

public class JDBCQueryTemplate<T> {
    
    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(JDBCQueryTemplate.class.getName());
    
    private Provider<Connection> provider;
    
    public JDBCQueryTemplate(Provider<Connection> provider) {
        super();
        this.provider = provider;
    }

    public List<T> executeQuery(String sql, Object... params) {
        List<T> result = new ArrayList<T>();
        Connection connection = this.provider.get();
        PreparedStatement pstm = null;
        ResultSet rs=null;
        try {
            pstm = connection.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                setParam(i+1, params[i], pstm);
            }
            rs= pstm.executeQuery();
            while(rs.next()) {
                if(!handleRow(rs, result)) break;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            if (pstm != null ) {
                try {
                    pstm.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }
        return result;
    }
    
    private void setParam(int i, Object object, PreparedStatement pstm) throws SQLException {
        if (object instanceof String) {
            pstm.setString(i, (String) object);
        } else if (object instanceof Integer) {
            pstm.setInt(i, (Integer) object);
        } else if (object instanceof Timestamp) {
            pstm.setTimestamp(i, (java.sql.Timestamp) object);
        } else if (object instanceof Long) {
            pstm.setLong(i, (Long) object);
        } else throw new IllegalArgumentException("unsupported type of argument "+object.getClass().getName());
        
    }

    public boolean handleRow(ResultSet rs, List<T> returnsList) throws SQLException {return true;}

}
