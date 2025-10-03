package cz.incad.kramerius.processes.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import cz.incad.kramerius.processes.definition.ProcessDefinitionManager;
import cz.incad.kramerius.processes.scheduler.SchedulersLifeCycleHook;
import cz.incad.kramerius.processes.cdk.KeycloakCDKCache;
import cz.incad.kramerius.processes.cdk.KeycloakCDKCycleHook;
import cz.incad.kramerius.processes.definition.ProcessDefinitionManagerImpl;
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
        // Move to keycloak cdk module
        lfhooks.addBinding().to(KeycloakCDKCycleHook.class);
        bind(KeycloakCDKCache.class).in(Scopes.SINGLETON);
    }

}
