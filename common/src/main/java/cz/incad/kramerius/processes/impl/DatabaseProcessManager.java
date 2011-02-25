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
import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.processes.TypeOfOrdering;
import cz.incad.kramerius.processes.database.InitProcessDatabase;
import cz.incad.kramerius.processes.database.ProcessDatabaseUtils;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.utils.SecurityDBUtils;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;

public class DatabaseProcessManager implements LRProcessManager {

    //"DEFID,PID,UUID,STATUS,PLANNED,STARTED,NAME AS PNAME, PARAMS, STARTEDBY"

    
	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(DatabaseProcessManager.class.getName());
	
	private final Provider<Connection> connectionProvider;
	private final DefinitionManager lrpdm;
	private final Provider<User> userProvider;
	
	private final Lock reentrantLock = new ReentrantLock();
	
	@Inject
	public DatabaseProcessManager(@Named("kramerius4")Provider<Connection> connectionProvider, Provider<User> userProvider, DefinitionManager lrpdm) {
		super();
		this.connectionProvider = connectionProvider;
		this.lrpdm = lrpdm;
		this.userProvider = userProvider;
	}

	@Override
	public LRProcess getLongRunningProcess(String uuid) {
		Connection connection = null;
		PreparedStatement stm = null;
		ResultSet rs = null;
		try {
			this.reentrantLock.lock();
			
			connection = connectionProvider.get();
			if (connection != null) {
				stm = connection.prepareStatement("select p.DEFID,PID,p.UUID,p.STATUS,p.PLANNED,p.STARTED,p.NAME AS PNAME, p.PARAMS, p.STARTEDBY, p.TOKEN, u.* from PROCESSES p left join user_entity u on (u.user_id=p.startedby) where UUID = ?");
				stm.setString(1, uuid);
				rs = stm.executeQuery();
				if(rs.next()) {
					//CREATE TABLE PROCESSES(DEFID VARCHAR, UUID VARCHAR ,PID VARCHAR,STARTED timestamp, STATUS int
//					String definitionId = rs.getString("DEFID");
//					int pid = rs.getInt("PID");
//					int status = rs.getInt("STATUS");
//					Timestamp started = rs.getTimestamp("STARTED");
//					Timestamp planned = rs.getTimestamp("PLANNED");
//					String name = rs.getString("NAME");
//					
//					LRProcessDefinition definition = this.lrpdm.getLongRunningProcessDefinition(definitionId);
					LRProcess process = processFromResultSet(rs);
					
					return process;
				} 
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
			
			this.reentrantLock.unlock();
		}
		return null;
	}

	@Override
    @InitProcessDatabase
	public void registerLongRunningProcess(LRProcess lp) {
		Connection connection = null;
		try {
			this.reentrantLock.lock();
			
			connection = connectionProvider.get();
			if (connection != null) {
				registerProcess(connection, lp, this.userProvider.get());
			}
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
			
			this.reentrantLock.unlock();
		}
	}


    @InitProcessDatabase
	public void updateLongRunningProcessPID(LRProcess lrProcess) {
		Connection connection = null;
		try {
			this.reentrantLock.lock();
			
			connection = connectionProvider.get();
			ProcessDatabaseUtils.updateProcessPID(connection,  lrProcess.getPid(),lrProcess.getUUID());
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
			
			this.reentrantLock.unlock();
		}
	}
	
	
	
	
    @InitProcessDatabase
    @Override
	public void updateLongRunningProcessName(LRProcess lrProcess) {
		Connection connection = null;
		try {
			this.reentrantLock.lock();
			connection = connectionProvider.get();
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
			
			this.reentrantLock.unlock();
		}
	}

	
	
	@Override
    @InitProcessDatabase
    public void deleteLongRunningProcess(LRProcess lrProcess) {
        Connection connection = null;
        try {
            this.reentrantLock.lock();
            connection = connectionProvider.get();
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
            
            this.reentrantLock.unlock();
        }
        
    }

    @InitProcessDatabase
    public void updateLongRunningProcessStartedDate(LRProcess lrProcess) {
		Connection connection = null;
		try {
			this.reentrantLock.lock();
			
			connection = connectionProvider.get();

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
			
			this.reentrantLock.unlock();
		}
	}	

    @InitProcessDatabase
    @Override
	public void updateLongRunningProcessState(LRProcess lrProcess) {
		Connection connection = null;
		try {
			this.reentrantLock.lock();
			
			connection = connectionProvider.get();

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
			
			this.reentrantLock.unlock();
		}
	}
	
    
	
	
    @InitProcessDatabase
	public List<LRProcess> getPlannedProcess(int howMany) {
		Connection connection = null;
		PreparedStatement stm = null;
		ResultSet rs = null;
		try {
			
			this.reentrantLock.lock();
			
			List<LRProcess> processes = new ArrayList<LRProcess>();
			connection = connectionProvider.get();
			if (connection != null) {
				// POZN: dotazovanych vet bude vzdycky malo, misto join budu provadet dodatecne selekty.  
				// POZN: bude jich v radu jednotek. 
				StringBuffer buffer = new StringBuffer("select p.DEFID,PID,p.UUID,p.STATUS,p.PLANNED,p.STARTED,p.NAME AS PNAME, p.PARAMS, p.STARTEDBY,p.TOKEN, u.* from processes p left join user_entity u on (u.user_id=p.startedby) where status = ?");
				buffer.append(" ORDER BY PLANNED LIMIT ? ");
				
				stm = connection.prepareStatement(buffer.toString());
				stm.setInt(1, States.PLANNED.getVal());
				stm.setInt(2, howMany);
				rs = stm.executeQuery();
				while(rs.next()) {
					LRProcess processFromResultSet = processFromResultSet(rs);
					processes.add(processFromResultSet);
				} 
				return processes;
			} else return new ArrayList<LRProcess>();
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
			
			this.reentrantLock.unlock();
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
			this.reentrantLock.lock();
			
			connection = connectionProvider.get();
			if (connection != null) {
				StringBuffer buffer = new StringBuffer("select count(*) from PROCESSES ");
				stm = connection.prepareStatement(buffer.toString());
				rs = stm.executeQuery();
				int count = 0;
				if(rs.next()) {
					count = rs.getInt(1);
				} 
				return count;
			} else return 0;
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
			
			this.reentrantLock.unlock();
		}
		return 0;
	}
	
	private LRProcess processFromResultSet(ResultSet rs) throws SQLException {
		//CREATE TABLE PROCESSES(DEFID VARCHAR, UUID VARCHAR ,PID VARCHAR,STARTED timestamp, STATUS int
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
		LRProcessDefinition definition = this.lrpdm.getLongRunningProcessDefinition(definitionId);
		if (definition == null) {
			throw new RuntimeException("cannot find definition '"+definitionId+"'");
		}
		LRProcess process = definition.loadProcess(uuid, pid, planned!=null?planned.getTime():0, States.load(status), name);
		process.setToken(token);
		if (started != null) process.setStartTime(started.getTime());
		if (params != null) {
			String[] paramsArray = params.split(",");
			process.setParameters(Arrays.asList(paramsArray));
		}
		process.setUserId(startedBy);
		User user = SecurityDBUtils.createUser(rs);
		if (user.getLoginname() != null) {
	        process.setUser(user);
		} else {
		    process.setUser(null);
		}
		
		return process;
	}
	
	
	@Override
    public List<LRProcess> getLongRunningProcessesByToken(String token) {
	    try {
            List<LRProcess> lpList = new JDBCQueryTemplate<LRProcess>(this.connectionProvider.get()) {
                @Override
                public boolean handleRow(ResultSet rs, List<LRProcess> returnsList) throws SQLException {
                    LRProcess process = processFromResultSet(rs);
                    returnsList.add(process);
                    return true;
                }
            }.executeQuery("select p.DEFID,PID,p.UUID,p.STATUS,p.PLANNED,p.STARTED,p.NAME AS PNAME, p.PARAMS, p.STARTEDBY,p.TOKEN, u.* from processes p left join user_entity u on (u.user_id=p.startedby) where token = ?", token);
            return lpList;
	    } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
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
			
			this.reentrantLock.lock();
			
			List<LRProcess> processes = new ArrayList<LRProcess>();
			connection = connectionProvider.get();
			if (connection != null) {
				StringBuffer buffer = new StringBuffer("select p.DEFID,PID,p.UUID,p.STATUS,p.PLANNED,p.STARTED,p.NAME AS PNAME, p.PARAMS, p.STARTEDBY,p.TOKEN, u.* from PROCESSES p left join user_entity u on (u.user_id=p.startedby) where STATUS = ?");
				stm = connection.prepareStatement(buffer.toString());
				stm.setInt(1, state.getVal());
				rs = stm.executeQuery();
				while(rs.next()) {
					processes.add(processFromResultSet(rs));
				} 
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

			this.reentrantLock.unlock();
		}
		return new ArrayList<LRProcess>();
	}

	@Override
	@InitProcessDatabase
	public  List<LRProcess> getLongRunningProcesses(LRProcessOrdering ordering, TypeOfOrdering typeOfOrdering,LRProcessOffset offset) {
		Connection connection = null;
		PreparedStatement stm = null;
		ResultSet rs = null;
		try {
			
			this.reentrantLock.lock();
			
			List<LRProcess> processes = new ArrayList<LRProcess>();
			connection = connectionProvider.get();
			if (connection != null) {
				StringBuffer buffer = new StringBuffer("select p.DEFID,PID,p.UUID,p.STATUS,p.PLANNED,p.STARTED,p.NAME AS PNAME, p.PARAMS, p.STARTEDBY,p.TOKEN, u.* from PROCESSES p left join user_entity u on (u.user_id=p.startedby)");
				if (ordering  != null) {
					buffer.append(ordering.getOrdering()).append(' ');
				}
				if (typeOfOrdering != null) {
					buffer.append(typeOfOrdering.getTypeOfOrdering()).append(' ');
				}
				if (offset != null) {
					buffer.append(offset.getSQLOffset());
				}
				
				stm = connection.prepareStatement(buffer.toString());
				rs = stm.executeQuery();
				while(rs.next()) {
					processes.add(processFromResultSet(rs));
				} 
			}
//			for (LRProcess lrProcess : processes) {
//				LOGGER.info("process '"+lrProcess.getUUID()+"' state "+lrProcess.getProcessState());
//				if (lrProcess.getProcessState().equals(States.RUNNING)) {
//					if (!lrProcess.isLiveProcess()) {
//						lrProcess.setProcessState(States.FAILED);
//						this.updateLongRunningProcessState(lrProcess);
//					}
//				} else if (lrProcess.getProcessState().equals(States.FAILED)) {
//					if (lrProcess.isLiveProcess()) {
//						lrProcess.setProcessState(States.RUNNING);
//						this.updateLongRunningProcessState(lrProcess);
//					}
//				}
//			}
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

			this.reentrantLock.unlock();
		}
		return new ArrayList<LRProcess>();
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
