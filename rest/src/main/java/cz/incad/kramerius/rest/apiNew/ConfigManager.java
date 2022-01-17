package cz.incad.kramerius.rest.apiNew;

import cz.incad.kramerius.utils.DatabaseUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Access to database table CONFIG(KEY:STRING,VALUE:STRING)
 * TODO: maybe move to different module
 */
public class ConfigManager {

    public static final Logger LOGGER = Logger.getLogger(ConfigManager.class.getName());

    @Inject
    @Named("kramerius4")
    private Provider<Connection> connectionProvider;

    public String getProperty(String key) {
        Connection conn = connectionProvider.get();
        if (conn == null) {
            throw new RuntimeException("connection not ready");
        }
        try {
            String SQL = "SELECT value FROM config WHERE key=?;";
            PreparedStatement pstmt = conn.prepareStatement(SQL);
            pstmt.setString(1, key);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            } else {
                return null;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            DatabaseUtils.tryClose(conn);
        }
    }

    public void setProperty(String key, String value) {
        Connection conn = connectionProvider.get();
        if (conn == null) {
            throw new RuntimeException("connection not ready");
        }
        try {
            String currentValue = getProperty(key);
            if (currentValue == null) {
                String SQL = "INSERT INTO config(key, value) VALUES (?,?)";
                PreparedStatement pstmt = conn.prepareStatement(SQL);
                int index = 1;
                pstmt.setString(index++, key);
                pstmt.setString(index++, value);
                pstmt.executeUpdate();
            } else {
                String SQL = "UPDATE config SET value=? WHERE key=?;";
                PreparedStatement pstmt = conn.prepareStatement(SQL);
                int index = 1;
                pstmt.setString(index++, value);
                pstmt.setString(index++, key);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            DatabaseUtils.tryClose(conn);
        }
    }

}
