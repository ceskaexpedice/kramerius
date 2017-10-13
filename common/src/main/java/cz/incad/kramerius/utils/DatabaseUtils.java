package cz.incad.kramerius.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import java.util.logging.Logger;

import cz.incad.kramerius.utils.conf.KConfiguration;

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
    
    public static void tryClose(Connection c) {
        try {
            c.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public static void tryClose(Statement stmt) {
        try {
            stmt.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public static void tryClose(ResultSet rs) {
        try {
            rs.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");
        Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5433/kramerius4","fedoraAdmin","fedoraAdmin");
        boolean tableExists = viewExists(con,"MONOGRAPH_T");
        System.out.println(tableExists);
        con.close();
    }
}
