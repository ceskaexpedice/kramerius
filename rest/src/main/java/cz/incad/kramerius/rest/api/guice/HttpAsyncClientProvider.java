package cz.incad.kramerius.rest.api.guice;

import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import javax.inject.Provider;

/**
 * HttpAsyncClientProvider
 *
 * @author Martin Rumanek
 */
public class HttpAsyncClientProvider implements Provider<CloseableHttpAsyncClient> {

    @Override
    public CloseableHttpAsyncClient get() {
        int httptimeout = new Integer(KConfiguration.getInstance().getProperty("http.timeout", "10000"));
        int httpConnections = new Integer(KConfiguration.getInstance().getProperty("http.connections", "50"));

        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(httptimeout)
                .setConnectTimeout(httptimeout).build();
        CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setMaxConnPerRoute(httpConnections)
                .setMaxConnTotal(httpConnections)
                .build();

        return httpclient;
    }
}
