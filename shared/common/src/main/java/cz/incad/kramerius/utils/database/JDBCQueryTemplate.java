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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;


/**
 * JDBC Template pattern. Useful for SQL querying. <br>
 * Typical usecase:
 * <pre>
 *  List<Integer> ids = new JDBCQueryTemplate(connection){
 *      public boolean handleRow(ResultSet rs, List<Integer> returnsList) throws SQLException {
 *          returnsList.add(rs.get("id"));
 *          // should processing continue
 *          return true;
 *      }
 *  }.executeQuery("select id from sometable where name=? and surname=?","karlos","dakos");
 *  .... 
 * </pre>
 * 
 * @author pavels
 */
public class JDBCQueryTemplate<T> {
    
    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(JDBCQueryTemplate.class.getName());
    
    private Connection connection;
    private boolean closeConnection = true;
    
    
    public JDBCQueryTemplate(Connection connection) {
        super();
        this.connection = connection;
    }

    public JDBCQueryTemplate(Connection connection, boolean closeConnection) {
        super();
        this.connection = connection;
        this.closeConnection = closeConnection;
    }


    /**
     * Execute query 
     * @param sql
     * @param params
     * @return
     */
    public List<T> executeQuery(String sql, Object... params) {

        List<T> result = new ArrayList<T>();
        PreparedStatement pstm = null;
        ResultSet rs=null;
        try {
            pstm = connection.prepareStatement(sql);
            for (int i = 0, index = 1; i < params.length; i++) {
                int changedIndex = setParam(index, params[i], pstm);
                index = changedIndex + 1;
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
            if (closeConnection && connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }
        return result;
    }
    
    private int setParam(int i, Object object, PreparedStatement pstm) throws SQLException {
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
        } else if (object.getClass().isArray()) {
            int length = Array.getLength(object);
            for (int j = 0; j < length; j++) {
                setParam(i+j, Array.get(object, j), pstm);
            }
            return i+(length-1);
        } else throw new IllegalArgumentException("unsupported type of argument "+object.getClass().getName());
        
    }

    public boolean handleRow(ResultSet rs, List<T> returnsList) throws SQLException {return true;}

}
