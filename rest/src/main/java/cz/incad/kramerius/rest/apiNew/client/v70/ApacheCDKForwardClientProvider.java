 /*
  * Copyright (C) 2025  Inovatika
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package cz.incad.kramerius.rest.apiNew.client.v70;


 import cz.incad.kramerius.utils.conf.KConfiguration;
 import org.apache.hc.client5.http.config.RequestConfig;
 import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
 import org.apache.hc.client5.http.impl.classic.HttpClients;
 import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
 import org.apache.hc.core5.util.Timeout;
 import org.apache.http.client.config.CookieSpecs;

 import javax.inject.Provider;

 /**
  * Provides an instance of {@link CloseableHttpClient} configured for CDK forwarding.
  * This class initializes an HTTP client with configurable connection settings,
  * allowing communication with remote digital libraries.
  */
 public class ApacheCDKForwardClientProvider implements Provider<CloseableHttpClient> {

     /** Maximum total connections allowed in the connection pool. */
     public static final int MAX_CONNECTIONS_IN = 70;
     /** Maximum number of connections per route. */
     public static final int MAX_PER_ROUTE = 10;
     /** Timeout for establishing a connection (in seconds). */
     public static final int CONNECT_TIMEOUT = 5;
     /** Timeout for receiving a response (in seconds). */
     public static final int RESPONSE_TIMEOUT = 10;

     private CloseableHttpClient closeableHttpClient = null;

     /**
      * Constructs an instance of ApacheCDKForwardClientProvider.
      * Reads configuration values and initializes the HTTP client with custom settings.
      */
     public ApacheCDKForwardClientProvider() {

         int maxConnections = KConfiguration.getInstance().getConfiguration().getInt("cdk.forward.apache.client.max_connections", MAX_CONNECTIONS_IN);
         int maxRoute = KConfiguration.getInstance().getConfiguration().getInt("cdk.forward.apache.client.max_per_route", MAX_PER_ROUTE);
         int connectTimeout = KConfiguration.getInstance().getConfiguration().getInt("cdk.forward.apache.client.connect_timeout", CONNECT_TIMEOUT);
         int responseTimeout = KConfiguration.getInstance().getConfiguration().getInt("cdk.forward.apache.client.response_timeout", RESPONSE_TIMEOUT);

         PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
         connManager.setMaxTotal(maxConnections); // Maximální počet připojení v poolu
         connManager.setDefaultMaxPerRoute(maxRoute);
         RequestConfig requestConfig = RequestConfig.custom()
                 .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                 .setConnectTimeout(Timeout.ofSeconds(connectTimeout))
                 .setResponseTimeout(Timeout.ofSeconds(responseTimeout))
                 .build();

         this.closeableHttpClient = HttpClients.custom()
                 .setConnectionManager(connManager)
                 .disableAuthCaching()
                 .disableCookieManagement()
                 .setDefaultRequestConfig(requestConfig)
                 .build();
     }


     /**
      * Provides the configured instance of {@link CloseableHttpClient}.
      *
      * @return a configured HTTP client instance.
      */
     @Override
     public CloseableHttpClient get() {
         return this.closeableHttpClient;
     }
 }
