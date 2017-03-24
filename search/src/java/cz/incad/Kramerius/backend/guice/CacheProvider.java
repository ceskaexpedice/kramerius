package cz.incad.Kramerius.backend.guice;

import com.google.inject.Provider;
import cz.incad.Kramerius.utils.DocumentSerializer;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.w3c.dom.Document;

/**
 * CacheProvider
 *
 * @author Martin Rumanek
 */
public class CacheProvider implements Provider<CacheManager> {

    @Override
    public CacheManager get() {
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .withSerializer(Document.class, DocumentSerializer.class).build();
        cacheManager.init();
        return cacheManager;
    }
}
