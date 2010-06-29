package cz.incad.kramerius.processes.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import cz.incad.kramerius.processes.LRProcess;

public class MostDesirableDatabaseUtils {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(MostDesirableDatabaseUtils.class.getName());
	
	public static void createTable(Connection con) throws SQLException {
		PreparedStatement prepareStatement = null;
		try {
			prepareStatement = con.prepareStatement("CREATE TABLE DESIRABLE(UUID VARCHAR(64), ACCESS TIMESTAMP)");
			int r = prepareStatement.executeUpdate();
			LOGGER.finest("CREATE TABLE: updated rows "+r);
		} finally {
			if (prepareStatement != null) prepareStatement.close();
		}
	}


	public static void saveAccess(Connection con, String uuid, Date date) throws SQLException {
		PreparedStatement prepareStatement = null;
		try {
			prepareStatement = con.prepareStatement("insert into DESIRABLE(UUID, ACCESS) values(?,?)");
			prepareStatement.setString(1, uuid);
			prepareStatement.setTimestamp(2, new Timestamp(date.getTime()));
			prepareStatement.executeUpdate();
		}finally {
			if (prepareStatement != null) prepareStatement.close();
		}
	}

}
