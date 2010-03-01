package cz.incad.kramerius.processes.impl;

import java.sql.Connection;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import cz.incad.kramerius.processes.database.StandardConnectionProvider;

public class ProcessMicroModule extends AbstractModule{

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(ProcessMicroModule.class.getName());
	
	@Override
	protected void configure() {
		String jdbcUrl = System.getProperty(ProcessStarter.JDBC_URL);
		LOGGER.info("connection url "+jdbcUrl);
		bind(String.class).annotatedWith(Names.named(ProcessStarter.JDBC_URL)).toInstance(jdbcUrl);
		String jdbcUserName = System.getProperty(ProcessStarter.JDBC_USER_NAME);
		LOGGER.info("connection jdbcUserName "+jdbcUserName);
		bind(String.class).annotatedWith(Names.named(ProcessStarter.JDBC_USER_NAME)).toInstance(jdbcUserName);
		String jdbcUserPass = System.getProperty(ProcessStarter.JDBC_USER_PASS);;
		LOGGER.info("connection jdbcUserPass "+jdbcUserPass);
		bind(String.class).annotatedWith(Names.named(ProcessStarter.JDBC_USER_PASS)).toInstance(jdbcUserPass);
		bind(Connection.class).toProvider(StandardConnectionProvider.class);
	}

	
}
