package cz.inovatika.kramerius.services.iterators;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;

/**
 * Functional interface for enriching Apache HTTP requests.
 * Used to add custom headers (like X-API-KEY), authentication, or tracing IDs.
 */
@FunctionalInterface
public interface ApacheHTTPRequestEnricher {

    void enrich(HttpUriRequestBase request);
    
    ApacheHTTPRequestEnricher NO_OP = (builder) -> {};

}