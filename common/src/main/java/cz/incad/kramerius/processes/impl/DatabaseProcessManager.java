package cz.incad.kramerius.processes.impl;

import static cz.incad.kramerius.processes.database.ProcessDatabaseUtils.*;

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
import cz.incad.kramerius.processes.database.DatabaseUtils;
import cz.incad.kramerius.processes.database.ProcessDatabaseUtils;

public class DatabaseProcessManager implements LRProcessManager {
	
	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(DatabaseProcessManager.class.getName());
	
	private final Provider<Connection> provider;
	private final DefinitionManager lrpdm;
	
	private final Lock reentrantLock = new ReentrantLock();
	
	@Inject
	public DatabaseProcessManager(@Named("kramerius4")Provider<Connection> provider, DefinitionManager lrpdm) {
		super();
		this.provider = provider;
		this.lrpdm = lrpdm;
	}

	@Override
	public LRProcess getLongRunningProcess(String uuid) {
		Connection connection = null;
		PreparedStatement stm = null;
		ResultSet rs = null;
		try {
			this.reentrantLock.lock();
			
			connection = provider.get();
			if (connection != null) {
				stm = connection.prepareStatement("select * from PROCESSES where UUID = ?");
				stm.setString(1, uuid);
				rs = stm.executeQuery();
				if(rs.next()) {
					//CREATE TABLE PROCESSES(DEFID VARCHAR, UUID VARCHAR ,PID VARCHAR,STARTED timestamp, STATUS int
					String definitionId = rs.getString("DEFID");
					int pid = rs.getInt("PID");
					int status = rs.getInt("STATUS");
					Timestamp started = rs.getTimestamp("STARTED");
					Timestamp planned = rs.getTimestamp("PLANNED");
					String name = rs.getString("NAME");
					LRProcessDefinition definition = this.lrpdm.getLongRunningProcessDefinition(definitionId);
					LRProcess process = definition.loadProcess(uuid, ""+pid, planned!=null?planned.getTime():0, States.load(status), name);
					if (started != null) process.setStartTime(started.getTime());
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
	public void registerLongRunningProcess(LRProcess lp) {
		Connection connection = null;
		try {
			this.reentrantLock.lock();
			
			connection = provider.get();
			if (connection != null) {
				if (!DatabaseUtils.tableExists(connection,"PROCESSES")) {
					createProcessTable(connection);
				}
				registerProcess(connection, lp);
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


	public void updateLongRunningProcessPID(LRProcess lrProcess) {
		Connection connection = null;
		try {
			this.reentrantLock.lock();
			
			connection = provider.get();
			if (!DatabaseUtils.tableExists(connection,"PROCESSES")) {
				createProcessTable(connection);
			}
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
	
	
	
	
	@Override
	public void updateLongRunningProcessName(LRProcess lrProcess) {
		Connection connection = null;
		try {
			this.reentrantLock.lock();
			
			connection = provider.get();
			if (!DatabaseUtils.tableExists(connection,"PROCESSES")) {
				createProcessTable(connection);
			}
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

	public void updateLongRunningProcessStartedDate(LRProcess lrProcess) {
		Connection connection = null;
		try {
			this.reentrantLock.lock();
			
			connection = provider.get();
			if (!DatabaseUtils.tableExists(connection,"PROCESSES")) {
				createProcessTable(connection);
			}

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
	@Override
	public void updateLongRunningProcessState(LRProcess lrProcess) {
		Connection connection = null;
		try {
			this.reentrantLock.lock();
			
			connection = provider.get();
			if (!DatabaseUtils.tableExists(connection,"PROCESSES")) {
				createProcessTable(connection);
			}

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
	
	
	
	@Override
	public List<LRProcess> getPlannedProcess(int howMany) {
		Connection connection = null;
		PreparedStatement stm = null;
		ResultSet rs = null;
		try {
			
			this.reentrantLock.lock();
			
			List<LRProcess> processes = new ArrayList<LRProcess>();
			connection = provider.get();
			if (connection != null) {
				if (!DatabaseUtils.tableExists(connection,"PROCESSES")) {
					createProcessTable(connection);
				}
				// POZN: dotazovanych vet bude vzdycky malo, misto join budu provadet dodatecne selekty.  
				// POZN: bude jich v radu jednotek. 
				StringBuffer buffer = new StringBuffer("select * from PROCESSES where status = ?");
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
	public int getNumberOfLongRunningProcesses() {
		Connection connection = null;
		PreparedStatement stm = null;
		ResultSet rs = null;
		try {
			this.reentrantLock.lock();
			
			connection = provider.get();
			if (connection != null) {
				if (!DatabaseUtils.tableExists(connection,"PROCESSES")) {
					createProcessTable(connection);
				}
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
		String name = rs.getString("NAME");
		String params = rs.getString("PARAMS");
		LRProcessDefinition definition = this.lrpdm.getLongRunningProcessDefinition(definitionId);
		if (definition == null) {
			throw new RuntimeException("cannot find definition '"+definitionId+"'");
		}
		LRProcess process = definition.loadProcess(uuid, pid, planned!=null?planned.getTime():0, States.load(status), name);
		if (started != null) process.setStartTime(started.getTime());
		if (params != null) {
			String[] paramsArray = params.split(",");
			process.setParameters(Arrays.asList(paramsArray));
		}
		return process;
	}
	
	
	
	@Override
	public List<LRProcess> getLongRunningProcesses(States state) {
		Connection connection = null;
		PreparedStatement stm = null;
		ResultSet rs = null;
		try {
			
			this.reentrantLock.lock();
			
			List<LRProcess> processes = new ArrayList<LRProcess>();
			connection = provider.get();
			if (connection != null) {
				if (!DatabaseUtils.tableExists(connection,"PROCESSES")) {
					createProcessTable(connection);
				}
				StringBuffer buffer = new StringBuffer("select * from PROCESSES where STATUS = ?");
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
	public  List<LRProcess> getLongRunningProcesses(LRProcessOrdering ordering, TypeOfOrdering typeOfOrdering,LRProcessOffset offset) {
		Connection connection = null;
		PreparedStatement stm = null;
		ResultSet rs = null;
		try {
			
			this.reentrantLock.lock();
			
			List<LRProcess> processes = new ArrayList<LRProcess>();
			connection = provider.get();
			if (connection != null) {
				if (!DatabaseUtils.tableExists(connection,"PROCESSES")) {
					createProcessTable(connection);
				}
				StringBuffer buffer = new StringBuffer("select * from PROCESSES ");
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
