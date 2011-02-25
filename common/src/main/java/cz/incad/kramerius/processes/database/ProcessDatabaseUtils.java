package cz.incad.kramerius.processes.database;

import static cz.incad.kramerius.Constants.*;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Level;

import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.security.User;

/**
 * Utility class which contains methods for manipulation of processes
 * @author pavels
 *
 */
public class ProcessDatabaseUtils {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(ProcessDatabaseUtils.class.getName());

	public static void createProcessTable(Connection con) throws SQLException {
		PreparedStatement prepareStatement = null;
		try {
			prepareStatement = con.prepareStatement("CREATE TABLE PROCESSES(DEFID VARCHAR(255), UUID VARCHAR(255) PRIMARY KEY,PID int,STARTED timestamp, PLANNED timestamp, STATUS int, NAME VARCHAR(1024), PARAMS VARCHAR(4096))");
			int r = prepareStatement.executeUpdate();
			LOGGER.finest("CREATE TABLE: updated rows "+r);
		} finally {
			if (prepareStatement != null) prepareStatement.close();
		}
	}
	
	public static void addColumn(Connection con, String tableName, String columnName, String def) throws SQLException {
        PreparedStatement prepareStatement = null;
        try {
            prepareStatement = con.prepareStatement("alter table "+tableName+" add column "+columnName +" "+def);
            int r = prepareStatement.executeUpdate();
            LOGGER.finest("alter table "+r);
        } finally {
            if (prepareStatement != null) prepareStatement.close();
        }
	    
	}

//	public static void create
	
//	public static void createRuntimeParametersTable(Connection con) throws SQLException {
//		PreparedStatement prepareStatement = null;
//		try {
//			prepareStatement = con.prepareStatement("CREATE TABLE RUNTIME_PARAMS(PARAM VARCHAR(1024), UUID VARCHAR(255) REFERENCES PROCESSES(UUID))");
//			int r = prepareStatement.executeUpdate();
//			LOGGER.finest("CREATE TABLE: updated rows "+r);
//		} finally {
//			if (prepareStatement != null) prepareStatement.close();
//		}
//	}

	
	
	public static void registerProcess(Connection con, LRProcess lp, User user) throws SQLException {
		PreparedStatement prepareStatement = null;
		try {
			prepareStatement = con.prepareStatement("insert into processes(DEFID, UUID,PLANNED, STATUS,PARAMS,STARTEDBY, TOKEN) values(?,?,?,?,?,?,?)");
			prepareStatement.setString(1, lp.getDefinitionId());
			prepareStatement.setString(2, lp.getUUID());
			prepareStatement.setTimestamp(3, new Timestamp(lp.getPlannedTime()));
			prepareStatement.setInt(4, lp.getProcessState().getVal());
			StringBuffer buffer = new StringBuffer();
			List<String> parameters = lp.getParameters();
			if (!parameters.isEmpty()) {
				for (int i = 0, ll = parameters.size(); i < ll; i++) {
					buffer.append(parameters.get(i));
					buffer.append((i==ll-1) ? "":",");
				}
				prepareStatement.setString(5, buffer.toString());
			} else {
				prepareStatement.setString(5, null);
			}
            prepareStatement.setInt(6, user.getId());
            prepareStatement.setString(7, lp.getToken());
			prepareStatement.executeUpdate();
		}finally {
			if (prepareStatement != null) prepareStatement.close();
		}
	}

	public static void updateProcessStarted(Connection con, String uuid, Timestamp timestamp) throws SQLException {
		PreparedStatement prepareStatement = null;
		try {
			prepareStatement = con.prepareStatement("update processes set STARTED = ? where UUID=?");
			prepareStatement.setTimestamp(1, timestamp);
			prepareStatement.setString(2, uuid);
			prepareStatement.executeUpdate();
		} finally {
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
	
	public static void deleteProcess(Connection con, String uuid) throws SQLException {
        PreparedStatement prepareStatement =  null;
        try {
            prepareStatement = con.prepareStatement("delete from processes where UUID=?");
            prepareStatement.setString(1, uuid);
            prepareStatement.executeUpdate();
        } finally {
            if (prepareStatement != null) prepareStatement.close();
        }
	}
}
