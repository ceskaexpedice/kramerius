package cz.incad.kramerius.processes.cdk;

import javax.inject.Inject;

import cz.incad.kramerius.service.LifeCycleHook;
import cz.incad.kramerius.utils.conf.KConfiguration;

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
        boolean channelEnabled  =  KConfiguration.getInstance().getConfiguration().getBoolean("cdk.secured.channel");
        if (channelEnabled) {
            cdkCache.scheduleNextTask();
        }
    }
}
