package cz.incad.kramerius.processes.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
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
        PreparedStatement prepareStatement = null;
        try {
            prepareStatement = con.prepareStatement("CREATE TABLE DESIRABLE(UUID VARCHAR(64), ACCESS TIMESTAMP)");
            int r = prepareStatement.executeUpdate();
            LOGGER.finest("CREATE TABLE: updated rows " + r);
        } finally {
            if (prepareStatement != null) {
                prepareStatement.close();
            }
        }
    }

    /**
     * Store access to object
     * @param con Database connection
     * @param uuid Uuid of requestiong object
     * @param date When
     * @throws SQLException
     */
    public static void saveAccess(Connection con, String uuid, Date date) throws SQLException {
        PreparedStatement prepareStatement = null;
        try {
            prepareStatement = con.prepareStatement("insert into DESIRABLE(UUID, ACCESS) values(?,?)");
            prepareStatement.setString(1, uuid);
            prepareStatement.setTimestamp(2, new Timestamp(date.getTime()));
            prepareStatement.executeUpdate();
        } finally {
            if (prepareStatement != null) {
                prepareStatement.close();
            }
        }
    }
}
