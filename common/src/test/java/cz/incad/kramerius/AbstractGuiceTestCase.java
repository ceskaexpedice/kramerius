package cz.incad.kramerius;

import java.sql.Connection;
import java.sql.SQLException;

import com.google.inject.Guice;
import com.google.inject.Injector;

public abstract class AbstractGuiceTestCase {

	
	protected void dropTables() {
		try {
			Connection connection = connection();
			connection.createStatement().execute("drop table PROCESSES");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			Connection connection = connection();
			connection.createStatement().execute("drop table DESIRABLE");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Connection connection() {
		Injector inj = injector();
		Connection connection = inj.getInstance(Connection.class);
		return connection;
	}

	protected abstract Injector injector();
}
