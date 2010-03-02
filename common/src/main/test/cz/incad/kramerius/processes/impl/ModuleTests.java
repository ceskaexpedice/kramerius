package cz.incad.kramerius.processes.impl;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;

import cz.incad.kramerius.processes.GuiceModuleForTests;
import cz.incad.kramerius.processes.database.PropertyConnectionProvider;

public class ModuleTests {

	@Test
	public void testMicroModule() throws SQLException {
		System.setProperty(PropertyConnectionProvider.JDBC_URL, "jdbc:postgresql://localhost:5432/kramerius_test");
		System.setProperty(PropertyConnectionProvider.JDBC_USER_NAME, "postgres");
		System.setProperty(PropertyConnectionProvider.JDBC_USER_PASS, "postgres");
		
		Injector microProcessModule = ProcessStarter.microProcessModule();
		Provider<Connection> provider = microProcessModule.getProvider(Connection.class);
		Assert.assertNotNull(provider);
		Connection connection = provider.get();
		Assert.assertNotNull(connection);
		connection.close();
	}
	
	@Test
	public void testConfigurationModule() throws SQLException {
		Injector injector = Guice.createInjector(new GuiceModuleForTests());
		Assert.assertNotNull(injector);
		Provider<Connection> provider = injector.getProvider(Connection.class);
		Assert.assertNotNull(provider);
		Connection connection = provider.get();
		Assert.assertNotNull(connection);
		connection.close();
	}
}
