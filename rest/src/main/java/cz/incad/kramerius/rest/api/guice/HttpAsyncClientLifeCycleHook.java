package cz.incad.kramerius.rest.api.guice;

import cz.incad.kramerius.audio.CacheLifeCycleHook;
import cz.incad.kramerius.service.LifeCycleHook;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.nio.client.HttpAsyncClient;
import org.ehcache.CacheManager;

import javax.inject.Inject;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * HttpAsyncClientLifeCycleHook
 *
 * @author Martin Rumanek
 */
public class HttpAsyncClientLifeCycleHook implements LifeCycleHook {

    private static final Logger LOGGER = Logger.getLogger(HttpAsyncClientLifeCycleHook.class.getName());

    @Inject
    private HttpAsyncClient asyncClient;

    @Override
    public void startNotification() {
        LOGGER.info("Starting Http async client");
        ((CloseableHttpAsyncClient)asyncClient).start();

    }

    @Override
    public void shutdownNotification() {
        try {
            LOGGER.info("Shutting down Http async client");
            ((CloseableHttpAsyncClient)asyncClient).close();
        } catch (IOException e) {
            LOGGER.severe("Shutting down Http async client failed");
        }
    }
}
