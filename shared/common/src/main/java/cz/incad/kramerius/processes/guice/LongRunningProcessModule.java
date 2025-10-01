package cz.incad.kramerius.processes.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.processes.impl.SchedulersLifeCycleHook;
import cz.incad.kramerius.processes.cdk.KeycloakCDKCache;
import cz.incad.kramerius.processes.cdk.KeycloakCDKCycleHook;
import cz.incad.kramerius.processes.impl.LRProcessDefinitionManagerImpl;
import cz.incad.kramerius.service.LifeCycleHook;

/**
 * Modul pro dlouhotrvajici procesy
 * 
 * @author pavels
 */
public class LongRunningProcessModule extends AbstractModule {

    public static final String DEFAULT_LIBS_KEY = "LIBS";

    @Override
    protected void configure() {
        // long running process modul
        bind(DefinitionManager.class).to(LRProcessDefinitionManagerImpl.class).in(Scopes.SINGLETON);
        bind(String.class).annotatedWith(Names.named("LIBS")).toInstance(System.getProperty(DEFAULT_LIBS_KEY));

        Multibinder<LifeCycleHook> lfhooks = Multibinder.newSetBinder(binder(), LifeCycleHook.class);
        lfhooks.addBinding().to(SchedulersLifeCycleHook.class);
        // Move to keycloak cdk module
        lfhooks.addBinding().to(KeycloakCDKCycleHook.class);
        bind(KeycloakCDKCache.class).in(Scopes.SINGLETON);
        
    }

}
