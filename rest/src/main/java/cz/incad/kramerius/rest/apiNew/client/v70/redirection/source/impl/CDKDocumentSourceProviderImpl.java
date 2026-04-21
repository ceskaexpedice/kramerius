package cz.incad.kramerius.rest.apiNew.client.v70.redirection.source.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.cdk.CDKUtils;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.source.CDKDocumentSourceProvider;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Eagerly initialized singleton providing cached document source lookups.
 * It maps document PIDs to library acronyms using Solr and an internal LRU cache.
 */
@Singleton
public class CDKDocumentSourceProviderImpl implements CDKDocumentSourceProvider {

    private static final Logger LOGGER = Logger.getLogger(CDKDocumentSourceProviderImpl.class.getName());
    private static final int MAX_CACHE_SIZE = 2000;

    private final SolrAccess solrAccess;
    
    /**
     * Thread-safe LRU (Least Recently Used) cache.
     * The 'true' flag in the constructor enables access-order, moving 
     * recently accessed entries to the end of the queue.
     */
    private final Map<String, String> acronymCache = Collections.synchronizedMap(
            new LinkedHashMap<String, String>(MAX_CACHE_SIZE, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                    return size() > MAX_CACHE_SIZE;
                }
            }
    );

    @Inject
    public CDKDocumentSourceProviderImpl(@Named("cachedSolrAccess") SolrAccess solrAccess) {
        this.solrAccess = solrAccess;
        LOGGER.fine("DocumentSourceProviderImpl initialized as Eager Singleton.");
    }

    @Override
    public String getDocumentSource(String pid) throws IOException {
        if (pid == null || pid.isEmpty()) return null;

        String cachedAcronym = acronymCache.get(pid);
        if (cachedAcronym != null) {
            return cachedAcronym;
        }

        String acronym = fetchFromSolr(pid);
        if (acronym != null) {
            acronymCache.put(pid, acronym);
        }
        return acronym;
    }

    /**
     * Queries Solr to determine the source library for the given PID.
     * Prefers 'cdk.leader' if present, otherwise returns the first entry from 'sources'.
     */
    private String fetchFromSolr(String pid) throws IOException {
        try {
            // Retrieve only the 'cdk.collection' field to minimize payload
            Document solrDataByPid = solrAccess.getSolrDataByPid(pid, "cdk.collection");
            if (solrDataByPid == null || solrDataByPid.getDocumentElement() == null) {
                return null;
            }

            List<String> sources = CDKUtils.findSources(solrDataByPid.getDocumentElement());
            if (sources != null && !sources.isEmpty()) {
                return sources.get(0);
            }

            return null;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching document source from Solr for PID: " + pid, e);
            return null;
        }
    }
}