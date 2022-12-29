package cz.incad.kramerius.processes.cdk;

import javax.inject.Inject;

import cz.incad.kramerius.service.LifeCycleHook;

public class KeycloakCDKCycleHook implements LifeCycleHook{

    @Inject
    KeycloakCDKCache cdkCache;
    
    
    @Override
    public void shutdownNotification() {
        cdkCache.shutdown();
    }

    @Override
    public void startNotification() {
        cdkCache.init();
        cdkCache.scheduleNextTask();
    }
}
