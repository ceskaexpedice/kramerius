package cz.incad.kramerius;

import java.sql.Connection;
import java.sql.SQLException;

import com.google.inject.Guice;
import com.google.inject.Injector;

public abstract class AbstractGuiceTestCase {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(AbstractGuiceTestCase.class.getName());
	
	
	protected void dropTables() {

		try {
			Connection connection = connection();
			connection.createStatement().execute("drop table PROCESSES");
		} catch (SQLException e) {
			LOGGER.severe(e.getMessage());
		}
		try {
			Connection connection = connection();
			connection.createStatement().execute("drop table DESIRABLE");
		} catch (SQLException e) {
			LOGGER.severe(e.getMessage());
		}
	}

	public Connection connection() {
		Injector inj = injector();
		Connection connection = inj.getInstance(Connection.class);
		return connection;
	}

	protected abstract Injector injector();
}
