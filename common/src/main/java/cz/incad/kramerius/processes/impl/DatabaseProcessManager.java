/*
 * Copyright (C) 2012 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius.processes.impl;

import static cz.incad.kramerius.processes.database.ProcessDatabaseUtils.registerProcess;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.processes.LRPRocessFilter;
import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.processes.LRProcessOffset;
import cz.incad.kramerius.processes.LRProcessOrdering;
import cz.incad.kramerius.processes.NotReadyException;
import cz.incad.kramerius.processes.ProcessManagerException;
import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.processes.TypeOfOrdering;
import cz.incad.kramerius.processes.database.ProcessDatabaseUtils;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.users.LoggedUsersSingleton;
import cz.incad.kramerius.utils.DatabaseUtils;
import cz.incad.kramerius.utils.database.JDBCCommand;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.JDBCTransactionTemplate;
import cz.incad.kramerius.utils.database.JDBCUpdateTemplate;
import cz.incad.kramerius.utils.properties.PropertiesStoreUtils;

public class DatabaseProcessManager implements LRProcessManager {


    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(DatabaseProcessManager.class.getName());

    @Inject
    @Named("kramerius4")
    private Provider<Connection> connectionProvider;
    
    @Inject
    private DefinitionManager lrpdm;
    
    @Inject
    private Provider<User> userProvider;
    
    @Inject
    private LoggedUsersSingleton loggedUsersSingleton;

    private final Lock reentrantLock = new ReentrantLock();

    public DatabaseProcessManager( ) {
        super();
    }

    @Override
    public LRProcess getLongRunningProcess(String uuid) {
        Connection connection = connectionProvider.get();
        if (connection == null)
            throw new NotReadyException("connection not ready");

        String sql = "select p.DEFID,PID,p.UUID,p.STATUS,p.PLANNED,p.STARTED,p.NAME AS PNAME, p.PARAMS, p.STARTEDBY, p.TOKEN, " 
            + "p.loginname,p.surname,p.firstname,p.user_key,p.params_mapping from PROCESSES p where UUID = ?";
        List<LRProcess> processes = new JDBCQueryTemplate<LRProcess>(connection) {
            @Override
            public boolean handleRow(ResultSet rs, List<LRProcess> returnsList) throws SQLException {
                returnsList.add(processFromResultSet(rs));
                return super.handleRow(rs, returnsList);
            }

        }.executeQuery(sql, uuid);

        return !processes.isEmpty() ? processes.get(0) : null;
    }

    
    @Override
    public void registerLongRunningProcess(LRProcess lp, String loggedUserKey, Properties parametersMapping) {
        Connection connection = null;
        try {
            connection = connectionProvider.get();
            if (connection == null)
                throw new NotReadyException("connection not ready");
            
            registerProcess(connection, lp, /* this.userProvider.get() */lp.getUser(), lp.getLoggedUserKey() , PropertiesStoreUtils.storeProperties(parametersMapping));
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

    public void updateLongRunningProcessPID(LRProcess lrProcess) {
        Connection connection = null;
        try {
            connection = connectionProvider.get();
            if (connection == null)
                throw new NotReadyException("connection not ready");
            int pid = Integer.parseInt(lrProcess.getPid());
            new JDBCUpdateTemplate(connection).executeUpdate("update processes set PID = ? where UUID = ?", pid, lrProcess.getUUID());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Override
    public void updateLongRunningProcessName(LRProcess lrProcess) {
        Connection connection = null;
        try {
            connection = connectionProvider.get();
            if (connection == null)
                throw new NotReadyException("connection not ready");
            new JDBCUpdateTemplate(connection).executeUpdate("update processes set NAME = ? where UUID = ?", lrProcess.getProcessName(), lrProcess.getUUID());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    
    @Override
    public void deleteBatchLongRunningProcess(LRProcess longRunningProcess) {
        try {
            final String token = longRunningProcess.getToken();
            final List<LRProcess> childSubprecesses = getLongRunningProcessesByToken(token);

            JDBCCommand deleteTokensMapping = new JDBCCommand() {
                
                @Override
                public Object executeJDBCCommand(Connection con) throws SQLException {
                    PreparedStatement prepareStatement = con.prepareStatement("delete from PROCESS_2_TOKEN where token = ?");
                    prepareStatement.setString(1, token);
                    return prepareStatement.executeUpdate();
                }
            };

            JDBCCommand deleteProcess = new JDBCCommand() {
                
                @Override
                public Object executeJDBCCommand(Connection con) throws SQLException {
                    PreparedStatement prepareStatement = con.prepareStatement("delete from PROCESSES where token = ?");
                    prepareStatement.setString(1, token);
                    return prepareStatement.executeUpdate();
                }
            };
            JDBCTransactionTemplate.Callbacks callbacks = new JDBCTransactionTemplate.Callbacks() {
                
                @Override
                public void rollbacked() {
                    // do nothing
                }
                
                @Override
                public void commited() {
                    for (int i = 0; i < childSubprecesses.size(); i++) {
                        LRProcess child = childSubprecesses.get(i);
                        File chWDir = child.processWorkingDirectory();
                        try {
                            FileUtils.deleteDirectory(chWDir);
                        } catch (IOException e) {
                            LOGGER.log(Level.SEVERE,e.getMessage(),e);
                        }
                    }
                }
            };
            
            new JDBCTransactionTemplate(connectionProvider.get(),true).updateWithTransaction(callbacks, deleteTokensMapping, deleteProcess);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    @Override
    public void deleteLongRunningProcess(final LRProcess lrProcess) {
        Connection connection = null;
        try {
            connection = connectionProvider.get();
            if (connection == null)
                throw new NotReadyException("connection not ready");

            final int id = ProcessDatabaseUtils.getProcessId(lrProcess, connection);
            final String uuid = lrProcess.getUUID();
            JDBCCommand deleteTokensMapping = new JDBCCommand() {
                
                @Override
                public Object executeJDBCCommand(Connection con) throws SQLException {
                    PreparedStatement prepareStatement = con.prepareStatement("delete from PROCESS_2_TOKEN where process_id = ?");
                    prepareStatement.setInt(1, id);
                    return prepareStatement.executeUpdate();
                }
            };

            JDBCCommand deleteProcess = new JDBCCommand() {
                
                @Override
                public Object executeJDBCCommand(Connection con) throws SQLException {
                    PreparedStatement prepareStatement = con.prepareStatement("delete from processes where UUID = ?");
                    prepareStatement.setString(1, uuid);
                    return prepareStatement.executeUpdate();
                }
            };
            JDBCTransactionTemplate.Callbacks callbacks = new JDBCTransactionTemplate.Callbacks() {
                
                @Override
                public void rollbacked() {
                    // do nothing
                }
                
                @Override
                public void commited() {
                    try {
                        File processWorkingDirectory = lrProcess.processWorkingDirectory();
                        FileUtils.deleteDirectory(processWorkingDirectory);
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    }
                }
            };
            new JDBCTransactionTemplate(connectionProvider.get(),true).updateWithTransaction(callbacks, deleteTokensMapping, deleteProcess);

            // TODO: zruseni associace uzivatel - session -> aby se nehromadili
            // hornici
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            DatabaseUtils.tryClose(connection);
        }

    }

    public void updateLongRunningProcessStartedDate(LRProcess lrProcess) {
        Connection connection = null;
        try {
            connection = connectionProvider.get();
            if (connection == null)
                throw new NotReadyException("connection not ready");
            new JDBCUpdateTemplate(connection).executeUpdate("update processes set STARTED = ? where UUID = ?", new Timestamp(lrProcess.getStartTime()), lrProcess.getUUID());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Override
    public void updateLongRunningProcessState(LRProcess lrProcess) {
        try {
            Connection connection = connectionProvider.get();
            if (connection == null)
                throw new NotReadyException("connection not ready");
            int val = lrProcess.getProcessState().getVal();
            String processUuid = lrProcess.getUUID();
            LOGGER.fine("params is "+val+","+processUuid);
            new JDBCUpdateTemplate(connection).executeUpdate("update processes set STATUS = ? where UUID = ?", val, processUuid);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public List<LRProcess> getPlannedProcess(int howMany) {
        Connection connection = connectionProvider.get();
        if (connection == null)
            throw new NotReadyException("connection not ready");
        
        // if (connection != null) {
        // POZN: dotazovanych vet bude vzdycky malo, misto join budu
        // provadet dodatecne selekty.
        // POZN: bude jich v radu jednotek.
        StringBuffer buffer = new StringBuffer("select p.DEFID,PID,p.UUID,p.STATUS,p.PLANNED,p.STARTED,p.NAME AS PNAME, p.PARAMS, p.STARTEDBY,p.TOKEN, " + "p.loginname,p.surname,p.firstname,p.user_key,p.params_mapping " + "from processes p where status = ?");
        buffer.append(" ORDER BY PLANNED LIMIT ? ");

        List<LRProcess> processes = new JDBCQueryTemplate<LRProcess>(connection){

            @Override
            public boolean handleRow(ResultSet rs, List<LRProcess> returnsList) throws SQLException {
                LRProcess processFromResultSet = processFromResultSet(rs);
                returnsList.add(processFromResultSet);
                return super.handleRow(rs, returnsList);
            }
            
        }.executeQuery(buffer.toString(),  States.PLANNED.getVal(), howMany);
        
        return processes;
    }

    @Override
    public int getNumberOfLongRunningProcesses() {
        Connection connection = connectionProvider.get();
        if (connection == null)
            throw new NotReadyException("connection not ready ");
        List<Integer> countList = new JDBCQueryTemplate<Integer>(connection) {
            @Override
            public boolean handleRow(ResultSet rs, List<Integer> returnsList) throws SQLException {
                returnsList.add(rs.getInt(1));
                return super.handleRow(rs, returnsList);
            }
        }.executeQuery("select count(*) from process_grouped_view ");
        return !countList.isEmpty() ? countList.get(0) : 0;
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

        String paramsMapping = rs.getString("params_mapping");
        
        
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

        if (paramsMapping != null) {
            Properties props = PropertiesStoreUtils.loadProperties(paramsMapping);
            process.setParametersMapping(props);
        }
        
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
            }.executeQuery("select p.DEFID,PID,p.UUID,p.STATUS,p.PLANNED,p.STARTED,p.NAME AS PNAME, p.PARAMS, " + "p.STARTEDBY,p.TOKEN, p.loginname,p.surname,p.firstname,p.user_key,p.params_mapping from processes p where token = ? " + " order by p.process_id ", token);
            return lpList;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return new ArrayList<LRProcess>();
        }
    }

    @Override
    public List<LRProcess> getLongRunningProcesses(States state) {
        Connection connection = connectionProvider.get();
        if (connection == null)
            throw new NotReadyException("connection not ready");
        List<LRProcess> processes = new JDBCQueryTemplate<LRProcess>(connection) {
            @Override
            public boolean handleRow(ResultSet rs, List<LRProcess> returnsList) throws SQLException {
                returnsList.add(processFromResultSet(rs));
                return super.handleRow(rs, returnsList);
            }
        }.executeQuery("select p.DEFID,PID,p.UUID,p.STATUS,p.PLANNED,p.STARTED,p.NAME AS PNAME, p.PARAMS, p.STARTEDBY,p.TOKEN" 
                    + ", p.loginname,p.surname,p.firstname,p.user_key,p.params_mapping from PROCESSES p  where STATUS = ?",
                state.getVal());
        return processes;
    }

    @Override
    public List<LRProcess> getLongRunningProcessesAsFlat(LRProcessOrdering ordering, TypeOfOrdering typeOfOrdering, LRProcessOffset offset) {
        Connection connection = connectionProvider.get();
        if (connection == null)
            throw new NotReadyException("connection not ready");
        StringBuffer buffer = new StringBuffer("select p.DEFID,PID,p.UUID,p.STATUS,p.PLANNED,p.STARTED,p.NAME AS PNAME, p.PARAMS, p.STARTEDBY,p.TOKEN,p.loginname,p.surname,p.firstname,p.user_key,p.params_mapping from processes p ");
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
        List<LRProcess> processes = new JDBCQueryTemplate<LRProcess>(connection) {
            @Override
            public boolean handleRow(ResultSet rs, List<LRProcess> returnsList) throws SQLException {
                returnsList.add(processFromResultSet(rs));
                return super.handleRow(rs, returnsList);
            }
        }.executeQuery(buffer.toString());
        return processes;
    }

    @Override
    public List<LRProcess> getLongRunningProcessesAsGrouped(LRProcessOrdering ordering, TypeOfOrdering typeOfOrdering, LRProcessOffset offset, LRPRocessFilter filter) {
        Connection connection = connectionProvider.get();
        if (connection == null)
            throw new NotReadyException("connection not ready");
        StringBuffer buffer = new StringBuffer("select p.DEFID,PID,p.UUID,p.STATUS,p.PLANNED,p.STARTED,p.NAME AS PNAME, p.PARAMS, p.STARTEDBY,p.TOKEN,p.loginname,p.surname,p.firstname,p.user_key,p.params_mapping,v.pcount as pcount from processes p " + " join process_grouped_view v on (p.process_id=v.process_id)");

        if (filter != null) {
            buffer.append(filter.getSQLOffset());
        }

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

        List<LRProcess> processes = new JDBCQueryTemplate<LRProcess>(connection) {
            @Override
            public boolean handleRow(ResultSet rs, List<LRProcess> returnsList) throws SQLException {
                LRProcess lrProcess = processFromResultSet(rs);
                returnsList.add(lrProcess);
                int processCount = rs.getInt("pcount");
                lrProcess.setMasterProcess(processCount > 1);
                return super.handleRow(rs, returnsList);
            }
        }.executeQuery(buffer.toString(), filter!=null ? filter.getObjectsToPreparedStm().toArray() : new Object[]{});

        return processes;
    }

    public void ordering(LRProcessOrdering ordering, TypeOfOrdering typeOfOrdering, StringBuffer buffer) {
        if (ordering != null) {
            buffer.append(ordering.getOrdering()).append(' ');
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

    public boolean isSessionKeyAssociatedWithProcess(String sessionKey) {
        Connection connection = this.connectionProvider.get();
        try {
            List<String> list = ProcessDatabaseUtils.getAssociatedTokens(sessionKey, connection);
            return !list.isEmpty();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return false;
        } finally {
            DatabaseUtils.tryClose(connection);
        }
    }
    
    

    @Override
    public Properties loadParametersMapping(LRProcess lrProcess)  {
        Properties properties = new Properties();
        try {
            List<String> params = new JDBCQueryTemplate<String>(this.connectionProvider.get(),true) {
                @Override
                public boolean handleRow(ResultSet rs, List<String> returnsList) throws SQLException {
                    String paramsMapping = rs.getString("params_mapping");
                    returnsList.add(paramsMapping);
                    return super.handleRow(rs, returnsList);
                }
                
            }.executeQuery("select params_mapping from processes where uuid = ?", lrProcess.getUUID());
            
            if (params.isEmpty()) {
                properties.load(new StringReader(params.get(0)));
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
        return properties;
        
    }


    @Override
    public String getSessionKey(String token) {
        Connection connection = this.connectionProvider.get();
        try {
            List<String> list = ProcessDatabaseUtils.getAssociatedSessionKeys(token, connection);
            return !list.isEmpty() ? list.get(0) : null;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return null;
        } finally {
            DatabaseUtils.tryClose(connection);
        }
    }

    @Override
    public void updateTokenMapping(LRProcess lrProcess, String sessionKey) {
        Connection con = null;
        try {
            con = this.connectionProvider.get();
            int sessionKeyId = this.loggedUsersSingleton.getSessionKeyId(sessionKey);
            if (sessionKeyId > -1) {
                ProcessDatabaseUtils.updateTokenMapping(lrProcess, con, sessionKeyId);
            } else {
                throw new ProcessManagerException("cannot find session associated with sessionKey '" + sessionKey + "'");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            //DatabaseUtils.tryClose(con);
        }

    }

    @Override
    public List<LRProcess> getLongRunningProcesses() {
        return getLongRunningProcessesAsGrouped(null, null, null,null);
    }

    @Override
    public Lock getSynchronizingLock() {
        return this.reentrantLock;
    }

}
