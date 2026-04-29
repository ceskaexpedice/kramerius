package cz.incad.kramerius.processes.cdk;

import com.google.inject.Inject;
import cz.incad.kramerius.service.LifeCycleHook;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class CDKApiKeyCycleHook implements LifeCycleHook {

    @Inject
    CDKAPIKeySupport  cdkapikeysupport;

    @Override
    public void shutdownNotification() {

    }

    @Override
    public void startNotification() {
        boolean apiKeyAuth = KConfiguration.getInstance().getConfiguration().getBoolean("cdk.secured.apikey", false);
        if (apiKeyAuth) {
            cdkapikeysupport.init();
        }
    }
}
