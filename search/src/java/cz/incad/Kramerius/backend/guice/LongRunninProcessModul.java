package cz.incad.Kramerius.backend.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.processes.impl.DatabaseProcessManager;
import cz.incad.kramerius.processes.impl.LRProcessDefinitionManagerImpl;

/**
 * Modul pro dlouhotrvajici procesy
 * @author pavels
 */
public class LongRunninProcessModul extends AbstractModule {

	@Override
	protected void configure() {
		// long running process modul
		bind(DefinitionManager.class).to(LRProcessDefinitionManagerImpl.class);
		bind(LRProcessManager.class).to(DatabaseProcessManager.class);
	}

}
