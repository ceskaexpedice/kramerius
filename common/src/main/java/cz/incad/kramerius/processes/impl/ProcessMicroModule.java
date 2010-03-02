package cz.incad.kramerius.processes.impl;

import java.sql.Connection;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import cz.incad.kramerius.processes.database.ConfigurationConnectionProvider;
import cz.incad.kramerius.processes.database.PropertyConnectionProvider;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class ProcessMicroModule extends AbstractModule{

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ProcessMicroModule.class.getName());
	
	@Override
	protected void configure() {
		String jdbcUrl = System.getProperty("jdbcUrl");
		LOGGER.info("connection url "+jdbcUrl);
		bind(String.class).annotatedWith(Names.named("jdbcUrl")).toInstance(jdbcUrl);
		String jdbcUserName = System.getProperty("jdbcUserName");
		LOGGER.info("connection jdbcUser "+jdbcUserName);
		bind(String.class).annotatedWith(Names.named("jdbcUserName")).toInstance(jdbcUserName);
		String jdbcUserPass = System.getProperty("jdbcUserPass");;
		LOGGER.info("connection jdbcPass "+jdbcUserPass);
		bind(String.class).annotatedWith(Names.named("jdbcUserPass")).toInstance(jdbcUserPass);
		bind(Connection.class).toProvider(PropertyConnectionProvider.class);
	}
}
