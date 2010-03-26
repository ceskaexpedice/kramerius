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

import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.States;

/**
 * Database helper methods
 * @author pavels
 *
 */
public class DatabaseUtils {

	
	public static Connection openConnection() throws  ClassNotFoundException, SQLException {
		Class.forName(DERBY_DRIVER);
		Connection con = DriverManager.getConnection(WORKING_DB_JDBC_URL);
		return con;
	}

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
		PreparedStatement prepareStatement = con.prepareStatement("CREATE TABLE PROCESSES(DEFID VARCHAR(255), UUID VARCHAR(255) ,PID VARCHAR(255),STARTED timestamp, STATUS int)");
		int r = prepareStatement.executeUpdate();
	}
	
	public static void registerProcess(Connection con, LRProcess lp) throws SQLException {
		PreparedStatement prepareStatement = con.prepareStatement("insert into processes(DEFID, UUID,STARTED, STATUS) values(?,?,?,?)");
		prepareStatement.setString(1, lp.getDefinitionId());
		prepareStatement.setString(2, lp.getUUID());
		prepareStatement.setTimestamp(3, new Timestamp(lp.getStart()));
		prepareStatement.setInt(4, lp.getProcessState().getVal());

		prepareStatement.executeUpdate();
	}

	public static void updateProcessState(Connection con, String uuid, States state) throws SQLException {
		PreparedStatement prepareStatement = con.prepareStatement("update processes set STATUS = ? where UUID=?");
		prepareStatement.setInt(1, state.getVal());
		prepareStatement.setString(2, uuid);
		prepareStatement.executeUpdate();
	}
	
	public static void updateProcessPID(Connection con, String pid, String uuid) throws SQLException {
		PreparedStatement prepareStatement = con.prepareStatement("update processes set PID = ? where UUID=?");
		prepareStatement.setString(1, pid);
		prepareStatement.setString(2, uuid);
		prepareStatement.executeUpdate();
	}
}
