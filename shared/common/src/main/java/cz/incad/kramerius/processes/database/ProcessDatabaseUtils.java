package cz.incad.kramerius.processes.database;

import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.ProcessManagerException;
import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.DatabaseUtils;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.JDBCUpdateTemplate;
import org.antlr.stringtemplate.StringTemplate;

import java.sql.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class which contains methods for manipulation of processes
 *
 * @author pavels
 */
public class ProcessDatabaseUtils {

    public static final Logger LOGGER = Logger.getLogger(ProcessDatabaseUtils.class.getName());


//    public static void insertProcessMapping2RolesNotClosingOP(Connection con, String token, Role role) throws SQLException {
//        PreparedStatement prepareStatement = con.prepareStatement(
//            "insert into processes_2_role (token,role_id) values (?,?)");
//        prepareStatement.setString(1, token);
//        prepareStatement.setInt(2, role.getId());
//        
//        int r = prepareStatement.executeUpdate();
//        LOGGER.log(Level.FINEST, "CREATE TABLE: inserted rows {0}", r);
//    }


    public static void insertProcessMapping2SessionId(Connection con, String authToken, int processId, int sessionKeyId) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement(
                "insert into PROCESS_2_TOKEN (PROCESS_2_TOKEN_ID, " +
                        "PROCESS_ID, " +
                        "AUTH_TOKEN," +
                        "SESSION_KEYS_ID) " +
                        " " +
                        "values (nextval('PROCESS_2_TOKEN_ID_SEQUENCE')," +
                        "?," +
                        "?," +
                        "?)");
        prepareStatement.setInt(1, processId);
        prepareStatement.setString(2, authToken);
        prepareStatement.setInt(3, sessionKeyId);


        int r = prepareStatement.executeUpdate();
        LOGGER.log(Level.FINEST, "CREATE TABLE: inserted rows {0}", r);
    }


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

    /**
     * @param con
     * @param lp
     * @param storedParams
     * @return process_id of registered process
     * @throws SQLException
     */
    public static int registerProcess(Connection con, LRProcess lp, String storedParams) throws SQLException {
        PreparedStatement insertStatement = null;

        try {
            //insert
            insertStatement = con.prepareStatement(
                    "INSERT INTO processes(" +
                            "defid," + //1
                            "uuid," + //2
                            "planned," + //3
                            "status," + //4
                            "params," + //5
                            "token," +  //6
                            "process_id," + //
                            "params_mapping," + //7
                            "batch_status," + //8
                            "token_active, " + //9
                            "auth_token," + //10
                            "ip_addr," + //11
                            "owner_id," + //12
                            "owner_name," + //13,
                            "name" + //14
                            ")" +
                            "values " +
                            "  (" +
                            "    ?," + //1 - DEFID
                            "    ?," + //2 - UUID
                            "    ?," + //3 - PLANNED
                            "    ?," + //4 - STATUS
                            "    ?," + //5 - PARAMS
                            "    ?," + //6 - TOKEN
                            "    nextval('PROCESS_ID_SEQUENCE')," +
                            "    ?," + //7 PARAMS_MAPPING
                            "    ?," + //8 BATCH_STATUS
                            "    ?," + //9 TOKEN_ACTIVE
                            "    ?," + //10 AUTH_TOKEN
                            "    ?," + //11 IP_ADDR
                            "    ?," + //12 OWNER_ID
                            "    ?," + //13 OWNER_NAME
                            "    ?" + //14 NAME
                            
                            "  )");
            insertStatement.setString(1, lp.getDefinitionId());
            insertStatement.setString(2, lp.getUUID());
            insertStatement.setTimestamp(3, new Timestamp(lp.getPlannedTime()));
            insertStatement.setInt(4, lp.getProcessState().getVal());
            insertStatement.setString(5, parametersToString(lp.getParameters(), lp.getUUID()));
            insertStatement.setString(6, lp.getGroupToken());
            insertStatement.setString(7, storedParams);
            insertStatement.setInt(8, lp.getBatchState().getVal());
            insertStatement.setBoolean(9, true);
            insertStatement.setString(10, lp.getAuthToken());
            insertStatement.setString(11, lp.getPlannedIPAddress());
            insertStatement.setString(12, lp.getOwnerId());
            insertStatement.setString(13, lp.getOwnerName());
            insertStatement.setString(14, lp.getProcessName());
            insertStatement.executeUpdate();

            //get new process_id
            List<Integer> list = new JDBCQueryTemplate<Integer>(con, false) {
                public boolean handleRow(ResultSet rs, List<Integer> returnsList) throws SQLException {
                    returnsList.add(rs.getInt("process_id"));
                    return true;
                }
            }.executeQuery("SELECT process_id FROM processes WHERE uuid = ?", lp.getUUID());
            return !list.isEmpty() ? list.get(0) : -1;
        } finally {
            DatabaseUtils.tryClose(insertStatement);
        }
    }

    private static String parametersToString(List<String> processParameters, String processUuid) {
        StringBuilder buffer = new StringBuilder();
        if (!processParameters.isEmpty()) {
            for (int i = 0, ll = processParameters.size(); i < ll; i++) {
                buffer.append(processParameters.get(i));
                buffer.append((i == ll - 1) ? "" : ",");
            }
            if (buffer.length() > 4096) {
                LOGGER.log(Level.SEVERE, "Parameters of process " + processUuid + " are too long and won't fit into the database.");
            }
            return buffer.toString();
        } else {
            return null;
        }
    }

    /**
     * @param con
     * @param lp
     * @param user
     * @param loggedUserKey
     * @param storedParams
     * @return process_id of registered process
     * @throws SQLException
     */
    @Deprecated
    public static int registerProcess(Connection con, LRProcess lp, User user, String loggedUserKey, String storedParams) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement(
                "insert into processes(" +
                        "   DEFID, " + //1
                        "   UUID, " + //2
                        "   PLANNED, " + //3
                        "   STATUS, " + //4
                        "   PARAMS, " + //5
                        "   TOKEN, " +  //6
                        "   PROCESS_ID, " + //
                        "   LOGINNAME," + //7
                        "   FIRSTNAME," + //8
                        "   SURNAME," +  // 9
                        "   USER_KEY," + //10
                        "   PARAMS_MAPPING , " + //11
                        "   BATCH_STATUS ," + //12
                        "   TOKEN_ACTIVE, " + //
                        "   AUTH_TOKEN," + //13
                        "   IP_ADDR," + // 14
                        "   OWNER_ID," + // 15
                        "   OWNER_NAME" + // 16
                        "   ) " +
                        "   values " +
                        "   (" +
                        "       ?," + //1 - DEFID
                        "       ?," + //2 - UUID
                        "       ?," + //3 - PLANNED
                        "       ?," + //4 - STATUS
                        "       ?," + //5 - PARAMS
                        "       ?," + //6 - TOKEN
                        "       nextval('PROCESS_ID_SEQUENCE')," + //
                        "       ?," + //7 LOGINNAME
                        "       ?," + //8 FIRSTNAME
                        "       ?," + //9 SURNAME
                        "       ?," + //10 USERKEY
                        "       ?," + //11 PARAMS_MAPPING
                        "       ?," + //12 BATCH_STATUS
                        "       TRUE," + //
                        "       ?," + //13 AUTH_TOKEN
                        "       ?," + //14 IP_ADDR
                        "       ?," + //15 OWNER_ID
                        "       ?" + //16 OWNER_NAME
                        "   )");
        try {
            prepareStatement.setString(1, lp.getDefinitionId());
            prepareStatement.setString(2, lp.getUUID());
            prepareStatement.setTimestamp(3, new Timestamp(lp.getPlannedTime()));
            prepareStatement.setInt(4, lp.getProcessState().getVal());
            prepareStatement.setString(5, parametersToString(lp.getParameters(), lp.getUUID()));
            prepareStatement.setString(6, lp.getGroupToken());
            prepareStatement.setString(7, user.getLoginname());
            prepareStatement.setString(8, user.getFirstName());
            prepareStatement.setString(9, user.getSurname());
            prepareStatement.setString(10, loggedUserKey);
            prepareStatement.setString(11, storedParams);
            prepareStatement.setInt(12, lp.getBatchState().getVal());
            prepareStatement.setString(13, lp.getAuthToken());
            prepareStatement.setString(14, lp.getPlannedIPAddress());
            prepareStatement.setString(15, lp.getOwnerId());
            prepareStatement.setString(16, lp.getOwnerName());

            prepareStatement.executeUpdate();

            //get new process_id
            List<Integer> list = new JDBCQueryTemplate<Integer>(con, false) {
                public boolean handleRow(ResultSet rs, List<Integer> returnsList) throws SQLException {
                    returnsList.add(rs.getInt("process_id"));
                    return true;
                }
            }.executeQuery("SELECT process_id FROM processes WHERE uuid = ?", lp.getUUID());
            return !list.isEmpty() ? list.get(0) : -1;
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

    /*
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
    }*/


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


    public static List<String> getAssociatedTokens(String sessionKey, Connection connection) {
        List<String> list = new JDBCQueryTemplate<String>(connection, false) {
            public boolean handleRow(ResultSet rs, List<String> returnsList) throws SQLException {
                returnsList.add(rs.getString("auth_token"));
                return true;
            }
        }.executeQuery("select m.*,sk.* from PROCESS_2_TOKEN m" +
                " join session_keys sk on (sk.session_keys_id = m.session_keys_id)" +
                " where SESSION_KEY = ?", sessionKey);
        return list;
    }


    public static List<String> getAssociatedSessionKeys(String token, Connection connection) {
        List<String> list = new JDBCQueryTemplate<String>(connection, false) {
            public boolean handleRow(ResultSet rs, List<String> returnsList) throws SQLException {
                returnsList.add(rs.getString("SESSION_KEY"));
                return true;
            }
        }.executeQuery("select m.*,sk.* from PROCESS_2_TOKEN m" +
                " join session_keys sk on (sk.session_keys_id = m.session_keys_id)" +
                " where auth_token = ?", token);
        return list;
    }


    public static int getProcessId(LRProcess lrProcess, Connection con) {
        List<Integer> list = new JDBCQueryTemplate<Integer>(con, false) {
            public boolean handleRow(ResultSet rs, List<Integer> returnsList) throws SQLException {
                returnsList.add(rs.getInt("process_id"));
                return true;
            }
        }.executeQuery("select * from processes where token = ?", lrProcess.getGroupToken());
        return !list.isEmpty() ? list.get(0) : -1;
    }


    public static void updateAuthTokenMapping(LRProcess lrProcess, Connection con, int sessionKeyId) throws SQLException {
        int id = getProcessId(lrProcess, con);
        if (id > -1) {
            new JDBCUpdateTemplate(con, true).executeUpdate("insert into PROCESS_2_TOKEN (PROCESS_2_TOKEN_ID, PROCESS_ID, AUTH_TOKEN,SESSION_KEYS_ID) " + "values (nextval('PROCESS_2_TOKEN_ID_SEQUENCE')," + "?,?,?)", id, lrProcess.getAuthToken(), sessionKeyId);
        } else {
            throw new ProcessManagerException("cannot find process id associated with token " + lrProcess.getGroupToken());
        }
    }


    public static void updateTokenActive(Connection con, String token, boolean activityFlag) {
        try {
            new JDBCUpdateTemplate(con, true).executeUpdate("update PROCESSES set TOKEN_ACTIVE=? where auth_token=?", activityFlag, token);
        } catch (SQLException e) {
            throw new ProcessManagerException("change token " + token);
        }
    }


    public static void deleteTokenMappings(LRProcess lrProcess, Connection con) throws SQLException {
        int id = getProcessId(lrProcess, con);
        if (id > -1) {
            new JDBCUpdateTemplate(con, true).executeUpdate("delete from PROCESS_2_TOKEN where process_id = ?", id);
        } else {
            throw new ProcessManagerException("cannot find process id associated with token " + lrProcess.getGroupToken());
        }
    }


    public static String[] QUERY_PROCESS_COLUMNS = {
            "p.DEFID,PID", "p.UUID", "p.STATUS", "p.PLANNED", "p.STARTED",
            "p.NAME AS PNAME", "p.PARAMS", "p.STARTEDBY", "p.TOKEN", "p.FINISHED",
            "p.loginname", "p.surname", "p.firstname", "p.user_key", "p.params_mapping", "p.batch_status", "p.AUTH_TOKEN", "p.IP_ADDR"
    };


    public static String printQueryProcessColumns() {
        StringTemplate template = new StringTemplate("$columns;separator=\",\"$");
        template.setAttribute("columns", QUERY_PROCESS_COLUMNS);
        return template.toString();
    }
}
