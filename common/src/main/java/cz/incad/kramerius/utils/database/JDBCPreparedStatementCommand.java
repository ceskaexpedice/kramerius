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
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

/**
 * Implementation of JDBCCommand used {@link PreparedStatement}
 * @author pavels
 */
public class JDBCPreparedStatementCommand extends JDBCCommand {

    protected PreparedStatement preparedStatement;
    protected int index=0;
    protected Object[] params;
    
    /**
     * Enum represents null values
     * @author pavels
     */
    public static enum NULLS {
        String, Integer, Timestamp, Long, Array;
    }

    
    public JDBCPreparedStatementCommand(Connection connection,String sql, Object... params) throws SQLException {
        super();
        this.params = params;
        this.preparedStatement = createPrepareStatement(connection, sql);
    }

    /**
     * Creates prepared statement
     * @param con JDBC connection
     * @param sql SQL command 
     * @return return new created prepared statement
     * @throws SQLException Cannot create PreparedStatement
     */
    public PreparedStatement createPrepareStatement(Connection con, String sql) throws SQLException {
        return con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    }
    
    
    /**
     * Prepare PreparedStatement
     * @throws SQLException Cannot prepare
     */
    public void prepareStatement() throws SQLException {
        for (int i = 0, index = 1; i < params.length; i++) {
            int changedIndex = setParam(index, params[i], this.preparedStatement);
            index = changedIndex + 1;
        }
    }
    

    @Override
    public Object executeJDBCCommand(Connection con) throws SQLException {
        this.prepareStatement();
        int executeUpdate = this.preparedStatement.executeUpdate();
        ResultSet rs = this.preparedStatement.getGeneratedKeys();
        if (rs.next()) {
            int result = rs.getInt(1);
            return result;
        } else return null;
    }

    
    /**
     * Set parameter to prepared statement
     * @param i Index
     * @param object Object to set
     * @param pstm PreparedStatement
     * @return index
     * @throws SQLException Cannot set value to preparedStatement
     */
    protected int setParam(int i, Object object, PreparedStatement pstm) throws SQLException {
        if (object instanceof String) {
            pstm.setString(i, (String) object);
            return i;
        } else if (object instanceof Integer) {
            pstm.setInt(i, (Integer) object);
            return i;
        } else if (object instanceof Timestamp) {
            pstm.setTimestamp(i, (java.sql.Timestamp) object);
            return i;
        } else if (object instanceof Long) {
            pstm.setLong(i, (Long) object);
            return i;
        } else if (object instanceof NULLS) {
            NULLS nullObject = (NULLS) object;
            switch(nullObject) {
                case String: {
                    pstm.setString(i, null);
                }
                break;
                case Integer: {
                    pstm.setInt(i, -1);
                }
                break;
                case Long: {
                    pstm.setLong(i, -1);
                }
                break;
                case Timestamp: {
                    pstm.setTimestamp(i, null);
                }
                break;
                case Array: {
                    pstm.setArray(i, null);
                }
                break;
            }
            
            return i;
        } else if (object.getClass().isArray()) {
            int length = Array.getLength(object);
            for (int j = 0; j < length; j++) {
                setParam(i+j, Array.get(object, j), pstm);
            }
            return i+(length-1);
        } else throw new IllegalArgumentException("unsupported type of argument "+object.getClass().getName());
    }
    
}
