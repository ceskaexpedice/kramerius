package cz.incad.kramerius.security.database;

import cz.incad.kramerius.database.VersionService;
import cz.incad.kramerius.utils.DatabaseUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigDBInitializer {

    public static final Logger LOGGER = Logger.getLogger(ConfigDBInitializer.class.getName());

    public static void initDatabase(Connection connection, VersionService versionService) {

        try {
            if (!DatabaseUtils.tableExists(connection, "CONFIG")) {
                createConfigTable(connection);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }

    private static void createConfigTable(Connection con) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement(
                "CREATE TABLE CONFIG(KEY VARCHAR(255) PRIMARY KEY, VALUE TEXT)");
        int r = prepareStatement.executeUpdate();
        LOGGER.log(Level.FINEST, "CREATE TABLE: updated rows {0}", r);
    }
}