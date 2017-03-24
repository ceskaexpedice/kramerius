package cz.incad.kramerius.impl;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;
import org.w3c.dom.Document;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;


/**
 * CachedFedoraAccessImpl
 *
 * @author Martin Rumanek
 */
public class CachedFedoraAccessImpl extends FedoraAccessImpl implements FedoraAccess {

    private static Cache<String, Document> cache;

    private static final String CACHE_ALIAS = "FedoraRelsExtCache";

    @Inject
    public CachedFedoraAccessImpl(KConfiguration configuration, @Nullable StatisticsAccessLog accessLog,
                                  CacheManager cacheManager) throws IOException {
        super(configuration, accessLog);

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
    public Document getRelsExt(String pid) throws IOException {
        Document relsExt = cache.get(pid);

        if (relsExt != null) { //cache hit
            return relsExt;
        } else { //cache miss
            relsExt = super.getRelsExt(pid);
            cache.put(pid, relsExt);
            return relsExt;
        }
    }
}
