package cz.incad.kramerius.processes;

import java.io.File;
import java.sql.Connection;

import com.google.gwt.benchmarks.client.Setup;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

import cz.incad.kramerius.processes.impl.DatabaseProcessManager;
import cz.incad.kramerius.processes.impl.LRProcessDefinitionManagerImpl;
import cz.incad.kramerius.utils.conf.KConfiguration;


public class GuiceModuleForTests extends AbstractModule {

	@Override
	protected void configure() {
		KConfiguration testConf = KConfiguration.getKConfiguration();
		bind(KConfiguration.class).toInstance(testConf);
		bind(DefinitionManager.class).to(LRProcessDefinitionManagerImpl.class).in(Scopes.SINGLETON);
		bind(LRProcessManager.class).to(DatabaseProcessManager.class).in(Scopes.SINGLETON);
		//bind(Connection.class).toProvider(DefaultConnectionProvider.class);
	}
}
