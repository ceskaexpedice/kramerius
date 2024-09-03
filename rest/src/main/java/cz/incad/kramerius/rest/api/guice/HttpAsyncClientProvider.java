package cz.incad.kramerius.rest.api.guice;

import cz.incad.kramerius.utils.conf.KConfiguration;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.TlsConfig;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.Timeout;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.client5.http.config.RequestConfig;

import javax.inject.Provider;

/**
 * HttpAsyncClientProvider
 *
 * @author Martin Rumanek
 */
public class HttpAsyncClientProvider implements Provider<CloseableHttpAsyncClient> {

    @Override
    public CloseableHttpAsyncClient get() {
        int httptimeout = Integer.parseInt(KConfiguration.getInstance().getProperty("http.timeout", "10000"));
        int httpConnections = Integer.parseInt(KConfiguration.getInstance().getProperty("http.connections", "50"));

        PoolingAsyncClientConnectionManager connectionManager = PoolingAsyncClientConnectionManagerBuilder.create()
                .setTlsStrategy(ClientTlsStrategyBuilder.create()
                        .setSslContext(SSLContexts.createSystemDefault())
                        .setTlsVersions(TLS.V_1_3)
                        .build())
                .setPoolConcurrencyPolicy(PoolConcurrencyPolicy.STRICT)
                .setConnPoolPolicy(PoolReusePolicy.LIFO)
                .setDefaultConnectionConfig(ConnectionConfig.custom()
                        .setSocketTimeout(Timeout.ofMilliseconds(httptimeout))
                        .setConnectTimeout(Timeout.ofMilliseconds(httptimeout))
                        .setTimeToLive(TimeValue.ofMinutes(10))
                        .build())
                .setDefaultTlsConfig(TlsConfig.custom()
                        .setVersionPolicy(HttpVersionPolicy.NEGOTIATE)
                        .setHandshakeTimeout(Timeout.ofMinutes(1))
                        .build())
                .setMaxConnPerRoute(httpConnections)
                .setMaxConnTotal(httpConnections)
                .build();

        RequestConfig requestConfig = RequestConfig.custom()
                .setResponseTimeout(Timeout.ofMilliseconds(httptimeout))
                .build();

        IOReactorConfig reactorConfig = IOReactorConfig.custom()
                .setSoTimeout(Timeout.ofMilliseconds(httptimeout))
                .build();

        return HttpAsyncClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .setIOReactorConfig(reactorConfig)
                .build();
    }
}
