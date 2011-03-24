package cz.incad.kramerius.impl;

import static cz.incad.kramerius.processes.database.MostDesirableDatabaseUtils.LOGGER;
import static cz.incad.kramerius.processes.database.MostDesirableDatabaseUtils.createTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.MostDesirable;
import cz.incad.kramerius.processes.database.MostDesirableDatabaseUtils;
import cz.incad.kramerius.utils.DatabaseUtils;

public class MostDesirableImpl implements MostDesirable {

	private Provider<Connection> provider;
	
	@Inject
	public MostDesirableImpl(@Named("kramerius4")Provider<Connection> provider) {
		super();
		this.provider = provider;
	}

	@Override
	public List<String> getMostDesirable(int count) {
	    
	    Connection connection = null;
		PreparedStatement stm = null;
		ResultSet rs = null;

		try {
			List<String> uuids = new ArrayList<String>();
			connection = provider.get();
			if (connection != null) {
				if (!DatabaseUtils.tableExists(connection,"DESIRABLE")) {
					createTable(connection);
				}
				
				stm = connection.prepareStatement("SELECT count(*) as count ,  uuid FROM desirable group by uuid order by count DESC  LIMIT ?");
				stm.setInt(1, count);
				rs = stm.executeQuery();
				while(rs.next()) {
					String uuid = rs.getString("uuid");
					uuids.add(uuid);
				} 
			}
			return uuids;
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
	public void saveAccess(String uuid, Date date) {
		Connection connection = null;
		try {
			connection = provider.get();
			if (connection != null) {
				if (!DatabaseUtils.tableExists(connection,"DESIRABLE")) {
					createTable(connection);
				}
				MostDesirableDatabaseUtils.saveAccess(connection, uuid, date);
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
	}
}
