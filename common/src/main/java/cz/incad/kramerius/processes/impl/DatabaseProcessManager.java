package cz.incad.kramerius.processes.impl;

import static cz.incad.kramerius.processes.database.ProcessDatabaseUtils.*;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.processes.LRProcessOffset;
import cz.incad.kramerius.processes.LRProcessOrdering;
import cz.incad.kramerius.processes.NotReadyException;
import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.processes.TypeOfOrdering;
import cz.incad.kramerius.processes.database.InitProcessDatabase;
import cz.incad.kramerius.processes.database.ProcessDatabaseUtils;
import cz.incad.kramerius.processes.utils.ProcessUtils;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.utils.SecurityDBUtils;
import cz.incad.kramerius.users.LoggedUsersSingleton;
import cz.incad.kramerius.utils.DatabaseUtils;
import cz.incad.kramerius.utils.database.JDBCCommand;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.JDBCTransactionTemplate;
import cz.incad.kramerius.utils.database.JDBCUpdateTemplate;

public class DatabaseProcessManager implements LRProcessManager {

    // "DEFID,PID,UUID,STATUS,PLANNED,STARTED,NAME AS PNAME, PARAMS, STARTEDBY"

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(DatabaseProcessManager.class.getName());

    private final Provider<Connection> connectionProvider;
    private final DefinitionManager lrpdm;
    private final Provider<User> userProvider;
    private LoggedUsersSingleton loggedUsersSingleton;

    private final Lock reentrantLock = new ReentrantLock();

    @Inject
    public DatabaseProcessManager(@Named("kramerius4") Provider<Connection> connectionProvider, Provider<User> userProvider, DefinitionManager lrpdm, LoggedUsersSingleton singleton) {
        super();
        this.connectionProvider = connectionProvider;
        this.lrpdm = lrpdm;
        this.userProvider = userProvider;
        this.loggedUsersSingleton = singleton;
    }

    @Override
    public LRProcess getLongRunningProcess(String uuid) {
        Connection connection = null;
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            connection = connectionProvider.get();
            if (connection == null)
                throw new NotReadyException("connection not ready");

            stm = connection.prepareStatement("select p.DEFID,PID,p.UUID,p.STATUS,p.PLANNED,p.STARTED,p.NAME AS PNAME, p.PARAMS, p.STARTEDBY, p.TOKEN, " + "p.loginname,p.surname,p.firstname,p.user_key from PROCESSES p where UUID = ?");
            stm.setString(1, uuid);
            rs = stm.executeQuery();
            if (rs.next()) {
                // CREATE TABLE PROCESSES(DEFID VARCHAR, UUID VARCHAR ,PID
                // VARCHAR,STARTED timestamp, STATUS int
                // String definitionId = rs.getString("DEFID");
                // int pid = rs.getInt("PID");
                // int status = rs.getInt("STATUS");
                // Timestamp started = rs.getTimestamp("STARTED");
                // Timestamp planned = rs.getTimestamp("PLANNED");
                // String name = rs.getString("NAME");
                //
                // LRProcessDefinition definition =
                // this.lrpdm.getLongRunningProcessDefinition(definitionId);
                LRProcess process = processFromResultSet(rs);

                return process;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }

        }
        return null;
    }

    @Override
    @InitProcessDatabase
    public void registerLongRunningProcess(LRProcess lp, String loggedUserKey) {
        Connection connection = null;
        try {

            connection = connectionProvider.get();
            if (connection == null)
                throw new NotReadyException("connection not ready");
            registerProcess(connection, lp, /* this.userProvider.get() */lp.getUser(), lp.getLoggedUserKey());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }
    }

    @InitProcessDatabase
    public void updateLongRunningProcessPID(LRProcess lrProcess) {
        Connection connection = null;
        try {
            connection = connectionProvider.get();
            if (connection == null)
                throw new NotReadyException("connection not ready");
            ProcessDatabaseUtils.updateProcessPID(connection, lrProcess.getPid(), lrProcess.getUUID());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }

        }
    }

    @InitProcessDatabase
    @Override
    public void updateLongRunningProcessName(LRProcess lrProcess) {
        Connection connection = null;
        try {
            connection = connectionProvider.get();
            if (connection == null)
                throw new NotReadyException("connection not ready");
            ProcessDatabaseUtils.updateProcessName(connection, lrProcess.getUUID(), lrProcess.getProcessName());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }

        }
    }

    @Override
    @InitProcessDatabase
    public void deleteLongRunningProcess(LRProcess lrProcess) {
        Connection connection = null;
        try {
            connection = connectionProvider.get();
            if (connection == null)
                throw new NotReadyException("connection not ready");
            ProcessDatabaseUtils.deleteProcess(connection, lrProcess.getUUID());
            File processWorkingDirectory = lrProcess.processWorkingDirectory();
            FileUtils.deleteDirectory(processWorkingDirectory);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }

        }

    }

    @InitProcessDatabase
    public void updateLongRunningProcessStartedDate(LRProcess lrProcess) {
        Connection connection = null;
        try {
            connection = connectionProvider.get();
            if (connection == null)
                throw new NotReadyException("connection not ready");
            ProcessDatabaseUtils.updateProcessStarted(connection, lrProcess.getUUID(), new Timestamp(lrProcess.getStartTime()));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }

        }
    }

    @InitProcessDatabase
    @Override
    public void updateLongRunningProcessState(LRProcess lrProcess) {
        Connection connection = null;
        try {
            connection = connectionProvider.get();
            if (connection == null)
                throw new NotReadyException("connection not ready");
            ProcessDatabaseUtils.updateProcessState(connection, lrProcess.getUUID(), lrProcess.getProcessState());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }

        }
    }

    @InitProcessDatabase
    public List<LRProcess> getPlannedProcess(int howMany) {
        Connection connection = null;
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {

            List<LRProcess> processes = new ArrayList<LRProcess>();
            connection = connectionProvider.get();
            if (connection == null)
                throw new NotReadyException("connection not ready");
            // if (connection != null) {
            // POZN: dotazovanych vet bude vzdycky malo, misto join budu
            // provadet dodatecne selekty.
            // POZN: bude jich v radu jednotek.
            StringBuffer buffer = new StringBuffer("select p.DEFID,PID,p.UUID,p.STATUS,p.PLANNED,p.STARTED,p.NAME AS PNAME, p.PARAMS, p.STARTEDBY,p.TOKEN, " + "p.loginname,p.surname,p.firstname,p.user_key " + "from processes p where status = ?");
            buffer.append(" ORDER BY PLANNED LIMIT ? ");

            stm = connection.prepareStatement(buffer.toString());
            stm.setInt(1, States.PLANNED.getVal());
            stm.setInt(2, howMany);
            rs = stm.executeQuery();
            while (rs.next()) {
                LRProcess processFromResultSet = processFromResultSet(rs);
                processes.add(processFromResultSet);
            }
            return processes;
            // } else return new ArrayList<LRProcess>();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }

        }
        return new ArrayList<LRProcess>();
    }

    @Override
    @InitProcessDatabase
    public int getNumberOfLongRunningProcesses() {
        Connection connection = null;
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {

            connection = connectionProvider.get();
            if (connection == null)
                throw new NotReadyException("connection not ready ");

            StringBuffer buffer = new StringBuffer("select count(*) from process_grouped_view ");

            stm = connection.prepareStatement(buffer.toString());
            rs = stm.executeQuery();
            int count = 0;
            if (rs.next()) {
                count = rs.getInt(1);
            }
            return count;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }

        }
        return 0;
    }

    private LRProcess processFromResultSet(ResultSet rs) throws SQLException {
        // CREATE TABLE PROCESSES(DEFID VARCHAR, UUID VARCHAR ,PID
        // VARCHAR,STARTED timestamp, STATUS int
        String definitionId = rs.getString("DEFID");
        String pid = rs.getString("PID");
        String uuid = rs.getString("UUID");
        int status = rs.getInt("STATUS");
        Timestamp planned = rs.getTimestamp("PLANNED");
        Timestamp started = rs.getTimestamp("STARTED");
        String name = rs.getString("PNAME");
        String params = rs.getString("PARAMS");
        String token = rs.getString("TOKEN");
        int startedBy = rs.getInt("STARTEDBY");
        String loginname = rs.getString("LOGINNAME");
        String firstname = rs.getString("FIRSTNAME");
        String surname = rs.getString("SURNAME");
        String userKey = rs.getString("USER_KEY");

        LRProcessDefinition definition = this.lrpdm.getLongRunningProcessDefinition(definitionId);
        if (definition == null) {
            throw new RuntimeException("cannot find definition '" + definitionId + "'");
        }
        LRProcess process = definition.loadProcess(uuid, pid, planned != null ? planned.getTime() : 0, States.load(status), name);
        process.setToken(token);
        if (started != null)
            process.setStartTime(started.getTime());
        if (params != null) {
            String[] paramsArray = params.split(",");
            process.setParameters(Arrays.asList(paramsArray));
        }

        process.setFirstname(firstname);
        process.setSurname(surname);
        process.setLoginname(loginname);
        process.setLoggedUserKey(userKey);

        return process;
    }

    @Override
    public List<LRProcess> getLongRunningProcessesByToken(String token) {

        try {
            Connection con = this.connectionProvider.get();
            if (con == null)
                throw new NotReadyException("connection not ready");
            List<LRProcess> lpList = new JDBCQueryTemplate<LRProcess>(con) {
                @Override
                public boolean handleRow(ResultSet rs, List<LRProcess> returnsList) throws SQLException {
                    LRProcess process = processFromResultSet(rs);
                    returnsList.add(process);
                    return true;
                }
            }.executeQuery("select p.DEFID,PID,p.UUID,p.STATUS,p.PLANNED,p.STARTED,p.NAME AS PNAME, p.PARAMS, " + "p.STARTEDBY,p.TOKEN, p.loginname,p.surname,p.firstname,p.user_key from processes p where token = ?", token);
            return lpList;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return new ArrayList<LRProcess>();
        }
    }

    @Override
    @InitProcessDatabase
    public List<LRProcess> getLongRunningProcesses(States state) {
        Connection connection = null;
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {

            List<LRProcess> processes = new ArrayList<LRProcess>();
            connection = connectionProvider.get();
            if (connection == null)
                throw new NotReadyException("connection not ready");
            StringBuffer buffer = new StringBuffer("select p.DEFID,PID,p.UUID,p.STATUS,p.PLANNED,p.STARTED,p.NAME AS PNAME, p.PARAMS, p.STARTEDBY,p.TOKEN" + ", p.loginname,p.surname,p.firstname,p.user_key from PROCESSES p  where STATUS = ?");
            stm = connection.prepareStatement(buffer.toString());
            stm.setInt(1, state.getVal());
            rs = stm.executeQuery();
            while (rs.next()) {
                processes.add(processFromResultSet(rs));
            }
            return processes;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }
        return new ArrayList<LRProcess>();
    }

    @Override
    @InitProcessDatabase
    public List<LRProcess> getLongRunningProcesses(LRProcessOrdering ordering, TypeOfOrdering typeOfOrdering, LRProcessOffset offset) {
        Connection connection = null;
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            List<LRProcess> processes = new ArrayList<LRProcess>();
            connection = connectionProvider.get();
            if (connection == null)
                throw new NotReadyException("connection not ready");

            StringBuffer buffer = new StringBuffer("select p.DEFID,PID,p.UUID,p.STATUS,p.PLANNED,p.STARTED,p.NAME AS PNAME, p.PARAMS, p.STARTEDBY,p.TOKEN,p.loginname,p.surname,p.firstname,p.user_key,v.pcount from processes p " + " join process_grouped_view v on (p.process_id=v.process_id)");

            if (ordering != null) {
                buffer.append(" order by ");
                ordering(ordering, typeOfOrdering, buffer);
                if (ordering != LRProcessOrdering.PLANNED) {
                    buffer.append(',');
                    ordering(LRProcessOrdering.PLANNED, typeOfOrdering, buffer);
                }
            }

            if (offset != null) {
                buffer.append(offset.getSQLOffset());
            }

            stm = connection.prepareStatement(buffer.toString());
            rs = stm.executeQuery();
            while (rs.next()) {
                LRProcess lrProcess = processFromResultSet(rs);
                processes.add(lrProcess);
                int processCount = rs.getInt("pcount");
                lrProcess.setMasterProcess(processCount > 1);
            }

            // for (LRProcess lrProcess : processes) {
            // LOGGER.info("process '"+lrProcess.getUUID()+"' state "+lrProcess.getProcessState());
            // if (lrProcess.getProcessState().equals(States.RUNNING)) {
            // if (!lrProcess.isLiveProcess()) {
            // lrProcess.setProcessState(States.FAILED);
            // this.updateLongRunningProcessState(lrProcess);
            // }
            // } else if (lrProcess.getProcessState().equals(States.FAILED)) {
            // if (lrProcess.isLiveProcess()) {
            // lrProcess.setProcessState(States.RUNNING);
            // this.updateLongRunningProcessState(lrProcess);
            // }
            // }
            // }
            return processes;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }

        }
        return new ArrayList<LRProcess>();
    }

    public void ordering(LRProcessOrdering ordering, TypeOfOrdering typeOfOrdering, StringBuffer buffer) {
        if (ordering != null) {
            buffer.append(ordering.getOrdering()).append(' ');
            // if (ordering != LRProcessOrdering.PLANNED) {
            // buffer.append(',').append(LRProcessOrdering.PLANNED).append(' ');
            // }
        }

        if (typeOfOrdering != null) {
            buffer.append(typeOfOrdering.getTypeOfOrdering()).append(' ');

            if (typeOfOrdering == TypeOfOrdering.ASC) {
                buffer.append("NULLS FIRST").append(' ');
            } else {
                buffer.append("NULLS LAST").append(' ');
            }
        }
    }

    
    

    @Override
    public String getSessionKey(String token) {
        List<String> list = new JDBCQueryTemplate<String>(this.connectionProvider.get()) {
            public boolean handleRow(ResultSet rs, List<String> returnsList) throws SQLException {
                returnsList.add(rs.getString("SESSION_KEY"));
                return false;
            }
        }.executeQuery("select m.*,sk.* from PROCESS_2_TOKEN m" +
        		" join session_keys sk on (sk.session_keys_id = m.session_keys_id)" +
        		" where token = ?", token);

        return !list.isEmpty() ? list.get(0) : null;
    }

    @Override
    public void updateTokenMapping(LRProcess lrProcess, String sessionKey) {
        Connection con = null;
        try {
            con = this.connectionProvider.get();
            int sessionKeyId = this.loggedUsersSingleton.getSessionKeyId(sessionKey);
            if (sessionKeyId > -1) {

                List<Integer> list = new JDBCQueryTemplate<Integer>(con, false) {
                    public boolean handleRow(ResultSet rs, List<Integer> returnsList) throws SQLException {
                        returnsList.add(rs.getInt("process_id"));
                        return true;
                    }
                }.executeQuery("select * from processes where token = ?", lrProcess.getToken());

                if (!list.isEmpty()) {
                    new JDBCUpdateTemplate(con, true).executeUpdate("insert into PROCESS_2_TOKEN (PROCESS_2_TOKEN_ID, PROCESS_ID, TOKEN,SESSION_KEYS_ID) " + "values (nextval('PROCESS_2_TOKEN_ID_SEQUENCE')," + "?,?,?)", list.get(0), lrProcess.getToken(), sessionKeyId);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } finally {
            DatabaseUtils.tryClose(con);
        }

    }


    @Override
    public List<LRProcess> getLongRunningProcesses() {
        return getLongRunningProcesses(null, null, null);
    }

    @Override
    public Lock getSynchronizingLock() {
        return this.reentrantLock;
    }

}
