package cz.incad.kramerius;

import java.sql.Connection;
import java.sql.SQLException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.name.Names;

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
		Provider<Connection> kramerius4Provider = inj.getProvider(Key.get(Connection.class, Names.named("kramerius4")));
		Connection connection = kramerius4Provider.get();
		return connection;
	}

	protected abstract Injector injector();
}
