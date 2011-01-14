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
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class JDBCUpdateTemplate {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(JDBCQueryTemplate.class.getName());
    
    private Connection connection;
    private boolean closeConnectionFlag = true;
    
    public JDBCUpdateTemplate(Connection connection) {
        super();
        this.connection = connection;
    }

    public JDBCUpdateTemplate(Connection connection, boolean closeConnectionFlag) {
        super();
        this.connection = connection;
        this.closeConnectionFlag = closeConnectionFlag;
    }




    public int executeUpdate(String sql, Object... params) throws SQLException {
        PreparedStatement pstm = null;
        ResultSet rs=null;
        int result = 0;
        try {
            
            pstm = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < params.length; i++) {
                setParam(i+1, params[i], pstm);
            }
            pstm.executeUpdate();
            rs = pstm.getGeneratedKeys();
            if (rs.next()) {
                result = rs.getInt(1);
            }
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
            
            if (closeConnectionFlag && connection != null) {
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
}
