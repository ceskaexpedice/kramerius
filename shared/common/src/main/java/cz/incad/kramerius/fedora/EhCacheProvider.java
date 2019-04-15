package cz.incad.kramerius.fedora;

import com.google.inject.Provider;

import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;

/**
 * CacheProvider
 *
 * @author Martin Rumanek
 */
public class EhCacheProvider implements Provider<CacheManager> {

    @Override
    public CacheManager get() {
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
               // .withSerializer(Document.class, DocumentSerializer.class).build();
        cacheManager.init();
        return cacheManager;
    }
}
