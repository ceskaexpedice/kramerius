package cz.incad.kramerius.processes.database;

import cz.incad.kramerius.utils.DatabaseUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class which contains method for manipulation of most desirable objects
 * @author pavels
 */
public class MostDesirableDatabaseUtils {

    public static final Logger LOGGER = Logger.getLogger(MostDesirableDatabaseUtils.class.getName());

    /**
     * Creating mostdesirable table
     * @param con
     * @throws SQLException
     */
    public static void createTable(Connection con) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement(
                "CREATE TABLE DESIRABLE(UUID VARCHAR(64), ACCESS TIMESTAMP)");
        int r = prepareStatement.executeUpdate();
        LOGGER.log(Level.FINEST, "CREATE TABLE: updated rows {0}", r);
    }

    /**
     * Store access to object
     * @param con Database connection
     * @param uuid Uuid of requestiong object
     * @param date When
     * @param Model model
     * @throws SQLException
     */
    public static void saveAccess(Connection con, String uuid, Date date, String model) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement(
                "insert into DESIRABLE(UUID, ACCESS, MODEL) values(?, ?, ?)");
        try {
            prepareStatement.setString(1, uuid);
            prepareStatement.setTimestamp(2, new Timestamp(date.getTime()));
            prepareStatement.setString(3, model);
            prepareStatement.executeUpdate();
        } finally {
            DatabaseUtils.tryClose(prepareStatement);
        }
    }
}
