package cz.incad.kramerius.audio;

import java.util.logging.Logger;
import javax.inject.Inject;

import org.ehcache.CacheManager;
import cz.incad.kramerius.service.LifeCycleHook;


public class CacheLifeCycleHook implements LifeCycleHook {

    private static final Logger LOGGER = Logger.getLogger(CacheLifeCycleHook.class.getName());

    @Inject
    private CacheManager cacheManager;

    @Override
    public void shutdownNotification() {
        LOGGER.info("shutting down Ehcache Manager");
        cacheManager.close();
    }

    @Override
    public void startNotification() {
    }

}
