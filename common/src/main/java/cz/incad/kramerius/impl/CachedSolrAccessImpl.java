package cz.incad.kramerius.impl;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;
import org.w3c.dom.Document;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * CachedSolrAccessImpl
 *
 * @author Martin Rumanek
 */
public class CachedSolrAccessImpl extends SolrAccessImpl implements SolrAccess {

    private Cache<String, Document> cache;

    private static final String CACHE_ALIAS = "SolrDocumentCache";

    @Inject
    public CachedSolrAccessImpl(CacheManager cacheManager, KConfiguration configuration) {

        cache = cacheManager.getCache(CACHE_ALIAS, String.class, Document.class);
        if (cache == null) {
            cache = cacheManager.createCache(CACHE_ALIAS,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Document.class,
                            ResourcePoolsBuilder.heap(1000).offheap(32, MemoryUnit.MB))
                            .withExpiry(Expirations.timeToLiveExpiration(
                                    Duration.of(configuration.getCacheTimeToLiveExpiration(), TimeUnit.SECONDS))).build());
        }
    }

    @Override
    public Document getSolrDataDocument(String pid) throws IOException {
        Document document = cache.get(pid);

        if (document != null) { //cache hit
            return document;
        } else { //cache miss
            document = super.getSolrDataDocument(pid);
            cache.put(pid, document);
            return document;
        }
    }
}
