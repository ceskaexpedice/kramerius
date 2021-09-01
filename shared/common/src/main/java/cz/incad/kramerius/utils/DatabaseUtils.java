package cz.incad.kramerius.utils;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseUtils {

    private static final Logger LOGGER = Logger.getLogger(DatabaseUtils.class.getName());

    public static boolean tableExists(Connection con, String tableName) throws SQLException {
        String[] types = {"TABLE"};
        ResultSet rs = con.getMetaData().getTables(null, null, "%", types);
        try {
            while (rs.next()) {
                if (tableName.equals(rs.getString("TABLE_NAME").toUpperCase())) {
                    return true;
                }
            }
            return false;
        } finally {
            tryClose(rs);
        }
    }

    public static boolean viewExists(Connection con, String tableName) throws SQLException {
        String[] types = {"VIEW"};
        ResultSet rs = con.getMetaData().getTables(null, null, "%", types);
        try {
            while (rs.next()) {
                if (tableName.equals(rs.getString("TABLE_NAME").toUpperCase())) {
                    return true;
                }
            }
            return false;
        } finally {
            tryClose(rs);
        }
    }

    public static boolean columnExists(Connection con, String tableName, String columnName) throws SQLException {
        PreparedStatement pstm = con.prepareStatement("select * from " + tableName + " where 1>2");
        try {
            ResultSetMetaData pstmMetadata = pstm.getMetaData();
            int size = pstmMetadata.getColumnCount();
            for (int i = 0; i < size; i++) {
                String mtdColName = pstmMetadata.getColumnName(i + 1);
                if (mtdColName.toLowerCase().equals(columnName.toLowerCase())) {
                    return true;
                }
            }
            return false;
        } finally {
            tryClose(pstm);
        }
    }

    public static boolean indexExists(Connection con, String tableName, String columnName) throws SQLException {
        String sql = "select t.relname as table_name, i.relname as index_name, a.attname as column_name\n" +
                "from\n" +
                "    pg_class t,\n" +
                "    pg_class i,\n" +
                "    pg_index ix,\n" +
                "    pg_attribute a\n" +
                "where\n" +
                "    t.oid = ix.indrelid\n" +
                "    and i.oid = ix.indexrelid\n" +
                "    and a.attrelid = t.oid\n" +
                "    and a.attnum = ANY(ix.indkey)\n" +
                "    and t.relkind = 'r'\n" +
                "   \tand t.relname = ?\n" +
                "\tand a.attname =?";
        PreparedStatement pstm = con.prepareStatement(sql);
        pstm.setString(1, tableName);
        pstm.setString(2, columnName);
        ResultSet rs = null;
        try {
            rs = pstm.executeQuery();
            return rs.next();
        } finally {
            tryClose(pstm);
            if (rs != null) tryClose(rs);
        }
    }

    public static void tryClose(Connection c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public static void tryClose(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void tryClose(ResultSet rs) {
        try {
            rs.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
}
