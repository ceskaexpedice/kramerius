package cz.incad.kramerius.processes.database;

import static cz.incad.kramerius.Constants.*;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.Constants;

public class DefaultConnectionProvider implements Provider<Connection>{
	
	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(DefaultConnectionProvider.class.getName());

	@Inject
	public DefaultConnectionProvider() {
		super();
	}

	@Override
	public Connection get() {
		try {
			return DatabaseUtils.openConnection();
		} catch (ClassNotFoundException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			return null;
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			return null;
		}
	}
}
