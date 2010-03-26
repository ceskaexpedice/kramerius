package cz.incad.kramerius.processes.impl;

import static cz.incad.kramerius.processes.database.DatabaseUtils.*;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.processes.database.DatabaseUtils;

public class DatabaseProcessManager implements LRProcessManager {
	
	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(DatabaseProcessManager.class.getName());
	
	private final Provider<Connection> provider;
	private final DefinitionManager lrpdm;
	
	@Inject
	public DatabaseProcessManager(Provider<Connection> provider, DefinitionManager lrpdm) {
		super();
		this.provider = provider;
		this.lrpdm = lrpdm;
	}

	@Override
	public LRProcess getLongRunningProcess(String uuid) {
		Connection connection = null;
		try {
			connection = provider.get();
			if (connection != null) {
				PreparedStatement stm = connection.prepareStatement("select * from PROCESSES where UUID = ?");
				stm.setString(1, uuid);
				ResultSet rs = stm.executeQuery();
				if(rs.next()) {
					//CREATE TABLE PROCESSES(DEFID VARCHAR, UUID VARCHAR ,PID VARCHAR,STARTED timestamp, STATUS int
					String definitionId = rs.getString("DEFID");
					String pid = rs.getString("PID");
					int status = rs.getInt("STATUS");
					Timestamp stmp = rs.getTimestamp("STARTED");
					LRProcessDefinition definition = this.lrpdm.getLongRunningProcessDefinition(definitionId);
					LRProcess process = definition.loadProcess(uuid, pid, stmp.getTime(), States.load(status));
					return process;
				} 
			}
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
		return null;
	}

	@Override
	public void registerLongRunningProcess(LRProcess lp) {
		Connection connection = null;
		try {
			connection = provider.get();
			if (connection != null) {
				if (!tableExists(connection)) {
					createTable(connection);
				}
				registerProcess(connection, lp);
			}
		} catch (SQLException e) {
			System.out.println(e.getErrorCode());
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


	public void updateLongRunningProcessPID(LRProcess lrProcess) {
		Connection connection = null;
		try {
			connection = provider.get();
			if (!tableExists(connection)) {
				createTable(connection);
			}
			DatabaseUtils.updateProcessPID(connection,  lrProcess.getPid(),lrProcess.getUUID());
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
	public void updateLongRunningProcessState(LRProcess lrProcess) {
		Connection connection = null;
		try {
			connection = provider.get();
			if (!tableExists(connection)) {
				createTable(connection);
			}
			DatabaseUtils.updateProcessState(connection, lrProcess.getUUID(), lrProcess.getProcessState());
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
	public List<LRProcess> getLongRunningProcesses() {
		Connection connection = null;
		try {
			List<LRProcess> processes = new ArrayList<LRProcess>();
			connection = provider.get();
			if (connection != null) {
				if (!tableExists(connection)) {
					createTable(connection);
				}
				PreparedStatement stm = connection.prepareStatement("select * from PROCESSES");
				ResultSet rs = stm.executeQuery();
				while(rs.next()) {
					//CREATE TABLE PROCESSES(DEFID VARCHAR, UUID VARCHAR ,PID VARCHAR,STARTED timestamp, STATUS int
					String definitionId = rs.getString("DEFID");
					String pid = rs.getString("PID");
					String uuid = rs.getString("UUID");
					int status = rs.getInt("STATUS");
					Timestamp stmp = rs.getTimestamp("STARTED");
					LRProcessDefinition definition = this.lrpdm.getLongRunningProcessDefinition(definitionId);
					LRProcess process = definition.loadProcess(uuid, pid, stmp.getTime(), States.load(status));
					processes.add(process);
				} 
			}
			return processes;
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
		return new ArrayList<LRProcess>();
	}
}
