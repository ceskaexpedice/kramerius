package cz.incad.kramerius.processes.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import cz.incad.kramerius.processes.definition.ProcessDefinitionManager;
import cz.incad.kramerius.processes.definition.ProcessDefinitionManagerImpl;
import cz.incad.kramerius.processes.scheduler.SchedulersLifeCycleHook;
import cz.incad.kramerius.service.LifeCycleHook;

/**
 * Modul pro dlouhotrvajici procesy
 * 
 * @author pavels
 */
public class ProcessModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ProcessDefinitionManager.class).to(ProcessDefinitionManagerImpl.class).in(Scopes.SINGLETON);
        Multibinder<LifeCycleHook> lfhooks = Multibinder.newSetBinder(binder(), LifeCycleHook.class);
        lfhooks.addBinding().to(SchedulersLifeCycleHook.class);
    }

}
