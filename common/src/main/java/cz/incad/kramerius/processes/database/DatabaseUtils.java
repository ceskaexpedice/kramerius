package cz.incad.kramerius.processes.database;

import static cz.incad.kramerius.Constants.*;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;

import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.States;

/**
 * Database helper methods
 * @author pavels
 *
 */
public class DatabaseUtils {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(DatabaseUtils.class.getName());

	public static boolean tableExists(Connection con) throws SQLException  {
		ResultSet rs = null;
		try {
			String[] types = {"TABLE"};
			ResultSet tables = con.getMetaData().getTables(null, null, "%", types);
			while(tables.next()) {
				if ("PROCESSES".equals(tables.getString("TABLE_NAME").toUpperCase())) {
					return true;
				}
			}
			return false;
		} finally {
			if (rs != null) rs.close();
		}
	}
	
	public static void createTable(Connection con) throws SQLException {
		PreparedStatement prepareStatement = null;
		try {
			prepareStatement = con.prepareStatement("CREATE TABLE PROCESSES(DEFID VARCHAR(255), UUID VARCHAR(255) ,PID int,STARTED timestamp, STATUS int, NAME VARCHAR(1024))");
			int r = prepareStatement.executeUpdate();
		} finally {
			if (prepareStatement != null) prepareStatement.close();
		}
	}
	
	public static void registerProcess(Connection con, LRProcess lp) throws SQLException {
		PreparedStatement prepareStatement = null;
		try {
			prepareStatement = con.prepareStatement("insert into processes(DEFID, UUID,STARTED, STATUS) values(?,?,?,?)");
			prepareStatement.setString(1, lp.getDefinitionId());
			prepareStatement.setString(2, lp.getUUID());
			prepareStatement.setTimestamp(3, new Timestamp(lp.getStart()));
			prepareStatement.setInt(4, lp.getProcessState().getVal());
			prepareStatement.executeUpdate();
		}finally {
			if (prepareStatement != null) prepareStatement.close();
		}
	}

	public static void updateProcessState(Connection con, String uuid, States state) throws SQLException {
		PreparedStatement prepareStatement = null;
		try {
			prepareStatement = con.prepareStatement("update processes set STATUS = ? where UUID=?");
			prepareStatement.setInt(1, state.getVal());
			prepareStatement.setString(2, uuid);
			prepareStatement.executeUpdate();
		} finally {
			if (prepareStatement != null) prepareStatement.close();
		}
	}

	public static void updateProcessName(Connection con, String uuid, String name) throws SQLException {
		PreparedStatement prepareStatement =  null;
		try {
			prepareStatement = con.prepareStatement("update processes set NAME = ? where UUID=?");
			prepareStatement.setString(1, name);
			prepareStatement.setString(2, uuid);
			prepareStatement.executeUpdate();
		} finally {
			if (prepareStatement != null) prepareStatement.close();
		}
	}

	public static void updateProcessPID(Connection con, String pid, String uuid) throws SQLException {
		PreparedStatement prepareStatement =  null;
		try {
			prepareStatement = con.prepareStatement("update processes set PID = ? where UUID=?");
			prepareStatement.setInt(1, Integer.parseInt(pid));
			prepareStatement.setString(2, uuid);
			prepareStatement.executeUpdate();
		} finally {
			if (prepareStatement != null) prepareStatement.close();
		}
	}
}
