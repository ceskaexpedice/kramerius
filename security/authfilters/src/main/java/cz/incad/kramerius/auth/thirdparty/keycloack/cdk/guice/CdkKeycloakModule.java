package cz.incad.kramerius.auth.thirdparty.keycloack.cdk.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import cz.incad.kramerius.auth.thirdparty.keycloack.cdk.CDKApiKeyCycleHook;
import cz.incad.kramerius.auth.thirdparty.keycloack.cdk.KeycloakCDKCache;
import cz.incad.kramerius.auth.thirdparty.keycloack.cdk.KeycloakCDKCycleHook;
import cz.incad.kramerius.service.LifeCycleHook;

/**
 * Modul pro dlouhotrvajici procesy
 * 
 * @author pavels
 */
public class CdkKeycloakModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder<LifeCycleHook> lfhooks = Multibinder.newSetBinder(binder(), LifeCycleHook.class);
        lfhooks.addBinding().to(KeycloakCDKCycleHook.class);
        bind(KeycloakCDKCache.class).in(Scopes.SINGLETON);
        lfhooks.addBinding().to(CDKApiKeyCycleHook.class);
    }

}
