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
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import cz.incad.kramerius.utils.DatabaseUtils;

/**
 * JDBC update template. Suitable for JDBC updates.<br>
 * Typical usecase:
 * <pre>
 *    new JDBCUpdateTemplate(this.connectionProvider.get(), true)
 *      .executeUpdate("update sometable set name=? where id=?","karlos",1);
 * </pre>
 * @author pavels
 */
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

    
    /**
     * Perfrom JDBC update
     * @param sql SQL command
     * @param params Parameters to prepared statement
     * @return Update result
     * @throws SQLException SQL exception has been occurred
     */
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

    /**
     * Create PreparedStatement instance
     * @param con JDBC connection 
     * @param sql SQL command 
     * @return new PreparedStatement instance
     * @throws SQLException SQL exception has been occurred
     */
    public PreparedStatement createPreparedStatement(Connection con, String sql) throws SQLException {
        return this.useReturningKeys ? 
                con.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS) : con.prepareStatement(sql);
    }
    
    /**
     * Flag Statement.RETURN_GENERATED_KEYS.  Used in  {@link #createPreparedStatement(Connection, String)}
     * @return current flag
     */
    public boolean isUseReturningKeys() {
        return useReturningKeys;
    }
    
    /**
     * Sets flag Statement.RETURN_GENERATED_KEYS.  Used in  {@link #createPreparedStatement(Connection, String)}
     * @param useReturningKeys new flag
     */
    public void setUseReturningKeys(boolean useReturningKeys) {
        this.useReturningKeys = useReturningKeys;
    }

    private void setNullParam(int i, NullObject nObj, PreparedStatement pstm) throws SQLException {
        Class clz = nObj.getClz();
        if (clz.equals(String.class)) {
            pstm.setString(i, null);
        } else if (clz.equals(Integer.class)) {
            pstm.setInt(i, (Integer)null);
        } else if (clz.equals(Timestamp.class)) {
            pstm.setTimestamp(i, (java.sql.Timestamp) null);
        } else if (clz.equals(Time.class)) {
            pstm.setTime(i, (java.sql.Time)  null);
        } else if (clz.equals(Date.class)) {
            pstm.setDate(i, (java.sql.Date)  null);
        } else if (clz.equals(Long.class)) {
            pstm.setLong(i, (Long)  null);
        } else if (clz.equals(Boolean.class)) {
            pstm.setBoolean(i, false);
        } else throw new IllegalArgumentException("unsupported type of argument "+clz);
    }
    
    private void setParam(int i, Object object, PreparedStatement pstm) throws SQLException {
        if (object instanceof String) {
            String string = (String) object;
            pstm.setString(i, string);
        } else if (object instanceof Integer) {
            pstm.setInt(i, (Integer) object);
        } else if (object instanceof Timestamp) {
            pstm.setTimestamp(i, (java.sql.Timestamp) object);
        } else if (object instanceof Time) {
            pstm.setTime(i, (java.sql.Time) object);
        } else if (object instanceof Date) {
            pstm.setDate(i, (java.sql.Date) object);
        } else if (object instanceof Long) {
            pstm.setLong(i, (Long) object);
        } else if (object instanceof Boolean) {
            pstm.setBoolean(i, ((Boolean) object).booleanValue());
        } else if (object instanceof NullObject) {
            setNullParam(i, (NullObject)object, pstm);
        } else if (object.getClass().isArray()) {
            int length = Array.getLength(object);
            for (int j = 0; j < length; j++) {
                setParam(i+j, Array.get(object, j), pstm);
            }
        } else throw new IllegalArgumentException("unsupported type of argument "+object.getClass().getName());
    }
    
    /**
     * Represents null object
     * @author pavels
     */
    public static class NullObject {
        
        private Class clz;
        
        public NullObject(Class clz) {
            super();
            this.clz = clz;
        }
        
        /**
         * @return the clz
         */
        public Class getClz() {
            return clz;
        }
        
        
    }
}
