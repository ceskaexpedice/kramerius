package cz.incad.kramerius.auth.thirdparty.keycloack.cdk;

import cz.incad.kramerius.service.LifeCycleHook;
import cz.incad.kramerius.utils.conf.KConfiguration;
import jakarta.inject.Inject;

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
        boolean apiKeyAuth = KConfiguration.getInstance().getConfiguration().getBoolean("cdk.secured.apikey", false);
        boolean channelEnabled  =  KConfiguration.getInstance().getConfiguration().getBoolean("cdk.secured.channel", false);
        if (channelEnabled || apiKeyAuth) {
            cdkCache.scheduleNextTask();
        }
    }
}
