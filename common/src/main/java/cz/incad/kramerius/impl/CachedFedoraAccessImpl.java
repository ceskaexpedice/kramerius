package cz.incad.kramerius.impl;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.statistics.accesslogs.AggregatedAccessLogs;
import cz.incad.kramerius.utils.FedoraUtils;
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


    // as configuration parameters
    private static final int NUMBER_OF_CACHE_ENTRIES = 3000;
    private static final int SIZE_OF_CACHE = 32;

    private static Cache<String, Document> xmlscache;

    private static final String XMLS_CACHE_ALIAS = "FedoraXMLSCache";

    @Inject
    public CachedFedoraAccessImpl(KConfiguration configuration, @Nullable AggregatedAccessLogs accessLog,
                                  CacheManager cacheManager) throws IOException {
        super(configuration, accessLog);


        xmlscache = cacheManager.getCache(XMLS_CACHE_ALIAS, String.class, Document.class);
        if (xmlscache == null) {
            xmlscache = cacheManager.createCache(XMLS_CACHE_ALIAS,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Document.class,
                            ResourcePoolsBuilder.heap(NUMBER_OF_CACHE_ENTRIES).offheap(SIZE_OF_CACHE, MemoryUnit.MB))
                            .withExpiry(Expirations.timeToLiveExpiration(
                                    Duration.of(configuration.getCacheTimeToLiveExpiration(), TimeUnit.SECONDS))).build());
        }


    }


    private String cacheKey(String pid, String stream) {
        return pid +"/"+stream;
    }

    @Override
    public Document getRelsExt(String pid) throws IOException {
        Document relsExt = xmlscache.get(cacheKey(pid, FedoraUtils.RELS_EXT_STREAM));
        if (relsExt != null) { //cache hit
            return relsExt;
        } else { //cache miss
            relsExt = super.getRelsExt(pid);
            xmlscache.put(cacheKey(pid, FedoraUtils.RELS_EXT_STREAM), relsExt);
            return relsExt;
        }
    }

    @Override
    public Document getBiblioMods(String pid) throws IOException {
        Document mods = xmlscache.get(cacheKey(pid, FedoraUtils.BIBLIO_MODS_STREAM));
        if (mods != null) { //cache hit
            return mods;
        } else { //cache miss
            mods = super.getBiblioMods(pid);
            xmlscache.put(cacheKey(pid, FedoraUtils.BIBLIO_MODS_STREAM), mods);
            return mods;
        }
    }

    @Override
    public Document getDC(String pid) throws IOException {
        Document dc = xmlscache.get(cacheKey(pid, FedoraUtils.DC_STREAM));
        if (dc != null) { //cache hit
            return dc;
        } else { //cache miss
            dc = super.getDC(pid);
            xmlscache.put(cacheKey(pid, FedoraUtils.DC_STREAM), dc);
            return dc;
        }
    }

}
