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

import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import cz.incad.kramerius.utils.DatabaseUtils;

public class JDBCUpdateTemplate {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(JDBCQueryTemplate.class.getName());
    
    private Connection connection;
    private boolean closeConnectionFlag = true;
    private boolean useReturningKeys = true;
    
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
            pstm = createPreparedStatement(connection,sql);
            for (int i = 0; i < params.length; i++) {
                setParam(i+1, params[i], pstm);
            }
            pstm.executeUpdate();
            rs = pstm.getGeneratedKeys();
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            if (columnCount >=1) {
                String clzName = metaData.getColumnClassName(1);
                if (clzName.equals(Integer.class.getName())) {
                    if (rs.next()) {
                        result = rs.getInt(1);
                    }
                }
            }

        } finally {
            if (rs != null) {
                DatabaseUtils.tryClose(rs);
            }
            if (pstm != null ) {
                DatabaseUtils.tryClose(pstm);
            }
            if (closeConnectionFlag && connection != null) {
                DatabaseUtils.tryClose(connection);
            }
        }
        return result;
    }

    public PreparedStatement createPreparedStatement(Connection con, String sql) throws SQLException {
        return this.useReturningKeys ? 
                con.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS) : con.prepareStatement(sql);
    }
    
    public boolean isUseReturningKeys() {
        return useReturningKeys;
    }
    
    public void setUseReturningKeys(boolean useReturningKeys) {
        this.useReturningKeys = useReturningKeys;
    }
    
    private void setParam(int i, Object object, PreparedStatement pstm) throws SQLException {
        if (object instanceof String) {
            String string = (String) object;
            pstm.setString(i, string);
        } else if (object instanceof Integer) {
            pstm.setInt(i, (Integer) object);
        } else if (object instanceof Timestamp) {
            pstm.setTimestamp(i, (java.sql.Timestamp) object);
        } else if (object instanceof Long) {
            pstm.setLong(i, (Long) object);
        } else if (object.getClass().isArray()) {
            int length = Array.getLength(object);
            for (int j = 0; j < length; j++) {
                setParam(i+j, Array.get(object, j), pstm);
            }
        } else throw new IllegalArgumentException("unsupported type of argument "+object.getClass().getName());
    }
}
