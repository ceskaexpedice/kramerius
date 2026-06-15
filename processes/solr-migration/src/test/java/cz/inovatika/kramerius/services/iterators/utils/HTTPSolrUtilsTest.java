package cz.inovatika.kramerius.services.iterators.utils;

import cz.inovatika.kramerius.services.iterators.ApacheHTTPRequestEnricher;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;

public class HTTPSolrUtilsTest {

    public static void main(String[] args) {

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(30))
                .setResponseTimeout(Timeout.ofSeconds(60))
                .build();

        CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build();

        var enricher = new ApacheHTTPRequestEnricher() {
            @Override
            public void enrich(HttpUriRequestBase request) {
                request.setHeader("X-API-KEY", "niy43HHvTfBz60gdz81jS-Fx3FHbsE1G25Q6SQ-Q-X8");
            }
        };

        HTTPSolrUtils.executeQueryApache(client,enricher, "https://kramerius.kkvysociny.cz/search/api/cdk/v7.0/forward/sync/solr/select", "q=*:*");

    }
}
