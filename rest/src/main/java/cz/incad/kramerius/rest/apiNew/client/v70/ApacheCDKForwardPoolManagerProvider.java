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

import com.google.inject.Provider;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.apache.http.client.config.CookieSpecs;

public class ApacheCDKForwardPoolManagerProvider implements Provider<PoolingHttpClientConnectionManager> {

    /** Maximum total connections allowed in the connection pool. */
    public static final int MAX_CONNECTIONS_IN = 170;

    /** Maximum number of connections per route. */
    public static final int MAX_PER_ROUTE = 160;

    private PoolingHttpClientConnectionManager connectionManager = null;

    public ApacheCDKForwardPoolManagerProvider() {
        int maxConnections = KConfiguration.getInstance().getConfiguration().getInt("cdk.forward.apache.client.max_connections", ApacheCDKForwardPoolManagerProvider.MAX_CONNECTIONS_IN);
        int maxRoute = KConfiguration.getInstance().getConfiguration().getInt("cdk.forward.apache.client.max_per_route", ApacheCDKForwardPoolManagerProvider.MAX_PER_ROUTE);
        this.connectionManager = new PoolingHttpClientConnectionManager();
        this.connectionManager.setMaxTotal(maxConnections);
        this.connectionManager.setDefaultMaxPerRoute(maxRoute);
    }

    @Override
    public PoolingHttpClientConnectionManager get() {
        return this.connectionManager;
    }
}
