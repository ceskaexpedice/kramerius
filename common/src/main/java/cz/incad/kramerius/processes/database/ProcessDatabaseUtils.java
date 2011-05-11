package cz.incad.kramerius.processes.database;

import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.DatabaseUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class which contains methods for manipulation of processes
 * @author pavels
 *
 */
public class ProcessDatabaseUtils {

    public static final Logger LOGGER = Logger.getLogger(ProcessDatabaseUtils.class.getName());

    public static void createProcessTable(Connection con) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement(
                "CREATE TABLE PROCESSES("
                + "DEFID VARCHAR(255), UUID VARCHAR(255) PRIMARY KEY, PID int,"
                + " STARTED timestamp, PLANNED timestamp, STATUS int,"
                + " NAME VARCHAR(1024), PARAMS VARCHAR(4096))");
        try {
            int r = prepareStatement.executeUpdate();
            LOGGER.log(Level.FINEST, "CREATE TABLE: updated rows {0}", r);
        } finally {
            DatabaseUtils.tryClose(prepareStatement);
        }
    }

    public static void addColumn(Connection con, String tableName, String columnName, String def) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement(
                "alter table " + tableName + " add column " + columnName + " " + def);
        try {
            int r = prepareStatement.executeUpdate();
            LOGGER.log(Level.FINEST, "alter table {0}", r);
        } finally {
            DatabaseUtils.tryClose(prepareStatement);
        }

    }

//    public static void create
//    public static void createRuntimeParametersTable(Connection con) throws SQLException {
//        PreparedStatement prepareStatement = null;
//        try {
//            prepareStatement = con.prepareStatement("CREATE TABLE RUNTIME_PARAMS(PARAM VARCHAR(1024), UUID VARCHAR(255) REFERENCES PROCESSES(UUID))");
//            int r = prepareStatement.executeUpdate();
//            LOGGER.finest("CREATE TABLE: updated rows " + r);
//        } finally {
//            if (prepareStatement != null) {
//                prepareStatement.close();
//            }
//        }
//    }
    
    public static void registerProcess(Connection con, LRProcess lp, User user) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement(
                "insert into processes(DEFID, UUID, PLANNED, STATUS, PARAMS, STARTEDBY, TOKEN) values(?,?,?,?,?,?,?)");
        try {
            prepareStatement.setString(1, lp.getDefinitionId());
            prepareStatement.setString(2, lp.getUUID());
            prepareStatement.setTimestamp(3, new Timestamp(lp.getPlannedTime()));
            prepareStatement.setInt(4, lp.getProcessState().getVal());
            StringBuilder buffer = new StringBuilder();
            List<String> parameters = lp.getParameters();
            if (!parameters.isEmpty()) {
                for (int i = 0, ll = parameters.size(); i < ll; i++) {
                    buffer.append(parameters.get(i));
                    buffer.append((i == ll - 1) ? "" : ",");
                }
                prepareStatement.setString(5, buffer.toString());
            } else {
                prepareStatement.setString(5, null);
            }
            prepareStatement.setInt(6, user.getId());
            prepareStatement.setString(7, lp.getToken());
            prepareStatement.executeUpdate();
        } finally {
            DatabaseUtils.tryClose(prepareStatement);
        }
    }

    public static void updateProcessStarted(Connection con, String uuid, Timestamp timestamp) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement(
                "update processes set STARTED = ? where UUID = ?");
        try {
            prepareStatement.setTimestamp(1, timestamp);
            prepareStatement.setString(2, uuid);
            prepareStatement.executeUpdate();
        } finally {
            DatabaseUtils.tryClose(prepareStatement);
        }
    }

    public static void updateProcessState(Connection con, String uuid, States state) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement(
                "update processes set STATUS = ? where UUID = ?");
        try {
            prepareStatement.setInt(1, state.getVal());
            prepareStatement.setString(2, uuid);
            prepareStatement.executeUpdate();
        } finally {
            DatabaseUtils.tryClose(prepareStatement);
        }
    }

    public static void updateProcessName(Connection con, String uuid, String name) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement(
                "update processes set NAME = ? where UUID = ?");
        try {
            prepareStatement.setString(1, name);
            prepareStatement.setString(2, uuid);
            prepareStatement.executeUpdate();
        } finally {
            DatabaseUtils.tryClose(prepareStatement);
        }
    }

    public static void updateProcessPID(Connection con, String pid, String uuid) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement(
                "update processes set PID = ? where UUID = ?");
        try {
            prepareStatement.setInt(1, Integer.parseInt(pid));
            prepareStatement.setString(2, uuid);
            prepareStatement.executeUpdate();
        } finally {
            DatabaseUtils.tryClose(prepareStatement);
        }
    }

    public static void deleteProcess(Connection con, String uuid) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement(
                "delete from processes where UUID = ?");
        try {
            prepareStatement.setString(1, uuid);
            prepareStatement.executeUpdate();
        } finally {
            DatabaseUtils.tryClose(prepareStatement);
        }
    }
}
