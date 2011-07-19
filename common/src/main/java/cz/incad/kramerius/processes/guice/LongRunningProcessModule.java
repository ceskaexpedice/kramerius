package cz.incad.kramerius.processes.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;

import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.processes.database.InitProcessDatabase;
import cz.incad.kramerius.processes.database.InitProcessDatabaseMethodInterceptor;
import cz.incad.kramerius.processes.impl.DatabaseProcessManager;
import cz.incad.kramerius.processes.impl.LRProcessDefinitionManagerImpl;

/**
 * Modul pro dlouhotrvajici procesy
 * @author pavels
 */
public class LongRunningProcessModule extends AbstractModule {

	public static final String DEFAULT_LIBS_KEY = "LIBS";
	
	@Override
	protected void configure() {
		// long running process modul
		bind(DefinitionManager.class).to(LRProcessDefinitionManagerImpl.class).in(Scopes.SINGLETON);
		bind(LRProcessManager.class).to(DatabaseProcessManager.class).in(Scopes.SINGLETON);
		
		bind(String.class).annotatedWith(Names.named("LIBS")).toInstance(System.getProperty(DEFAULT_LIBS_KEY));

		/*
		InitProcessDatabaseMethodInterceptor initDb = new InitProcessDatabaseMethodInterceptor();
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(InitProcessDatabase.class), 
                initDb);
        requestInjection(initDb);
        */
	}

}
