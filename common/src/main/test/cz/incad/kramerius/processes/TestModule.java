package cz.incad.kramerius.processes;

import java.io.File;
import java.sql.Connection;

import com.google.gwt.benchmarks.client.Setup;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

import cz.incad.kramerius.processes.database.StandardConnectionProvider;
import cz.incad.kramerius.processes.impl.DatabaseProcessManager;
import cz.incad.kramerius.processes.impl.LRProcessDefinitionManagerImpl;
import cz.incad.kramerius.utils.conf.KConfiguration;


public class TestModule extends AbstractModule {

	@Override
	protected void configure() {
		KConfiguration testConf = KConfiguration.getKConfiguration("src/main/test/kk.xml");
		bind(KConfiguration.class).toInstance(testConf);
		bind(DefinitionManager.class).to(LRProcessDefinitionManagerImpl.class).in(Scopes.SINGLETON);
		bind(LRProcessManager.class).to(DatabaseProcessManager.class).in(Scopes.SINGLETON);
		bind(Connection.class).toProvider(StandardConnectionProvider.class);
		
		//jdbc:hsqldb:hsql://localhost/
		//bind(String.class).annotatedWith(Names.named("jdbcUrl")).toInstance("jdbc:hsqldb:file:data/processes");
//		bind(String.class).annotatedWith(Names.named("jdbcUrl")).toInstance("jdbc:hsqldb:hsql://localhost/");
//		bind(String.class).annotatedWith(Names.named("jdbcUrl")).toInstance("jdbc:postgresql://localhost:5432/kramerius_test");
		
//		bind(String.class).annotatedWith(Names.named("jdbcUserName")).toInstance("sa");
//		bind(String.class).annotatedWith(Names.named("jdbcUserPass")).toInstance("");
//		bind(String.class).annotatedWith(Names.named("jdbcUserName")).toInstance("postgres");
//		bind(String.class).annotatedWith(Names.named("jdbcUserPass")).toInstance("postgres");
	}
}
