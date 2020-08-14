package cz.incad.kramerius.security.impl.http;

import com.google.inject.Inject;
import com.google.inject.Provider;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.security.*;
import cz.incad.kramerius.utils.IPAddressUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.expiry.Expirations;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static cz.incad.kramerius.security.impl.criteria.utils.CriteriaDNNTUtils.*;


/**
 * RightsResolverFromRequestCached
 *
 * @author Martin Rumanek
 */
public class RightsResolverFromRequestCached extends RightsResolverFromRequest {
    static class CacheKey implements Serializable {
        private final String PID;
        private final User user;
        private final String ip;

        public CacheKey(String PID, User user, String ip) {
            this.PID = PID;
            this.user = user;
            this.ip = ip;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CacheKey cacheKey = (CacheKey) o;

            if (PID != null ? !PID.equals(cacheKey.PID) : cacheKey.PID != null) return false;
            if (user != null ? !user.equals(cacheKey.user) : cacheKey.user != null) return false;
            return ip != null ? ip.equals(cacheKey.ip) : cacheKey.ip == null;
        }

        @Override
        public int hashCode() {
            int result = PID != null ? PID.hashCode() : 0;
            result = 31 * result + (user != null ? user.hashCode() : 0);
            result = 31 * result + (ip != null ? ip.hashCode() : 0);
            return result;
        }
    }

    private static Cache<CacheKey, RightsReturnObject> cache;

    private Provider<HttpServletRequest> provider;

    private static final String CACHE_ALIAS = "ActionAllowedCache";

    @Inject
    public RightsResolverFromRequestCached(Logger logger, Provider<HttpServletRequest> provider,
                                           RightsManager rightsManager, RightCriteriumContextFactory contextFactory,
                                           Provider<User> currentUserProvider, CacheManager cacheManager,
                                           KConfiguration configuration) {
        super(logger, provider, rightsManager, contextFactory, currentUserProvider);

        this.provider = provider;

        cache = cacheManager.getCache(CACHE_ALIAS, CacheKey.class, RightsReturnObject.class);
        if (cache == null) {
            cache = cacheManager.createCache(CACHE_ALIAS,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(CacheKey.class, RightsReturnObject.class,
                            ResourcePoolsBuilder.heap(1000).offheap(32, MemoryUnit.MB))
                            .withExpiry(Expirations.timeToLiveExpiration(
                                    org.ehcache.expiry.Duration.of(configuration.getCacheTimeToLiveExpiration(), TimeUnit.SECONDS))).build());
        }
    }


    public RightsReturnObject isAllowedInternalForFedoraDocuments(String actionName, String pid, String stream, ObjectPidsPath path, User user) throws RightCriteriumException {
        if ("read".equals(actionName)) {
            String ip = IPAddressUtils.getRemoteAddress(this.provider.get(), KConfiguration.getInstance().getConfiguration());
            RightsReturnObject allowed = cache.get(new CacheKey(pid, user, ip));

            if (allowed != null) { //cache hit

                // TODO: this is hack, replace it in future
                currentThreadReturnObject.set(allowed);

                return allowed;
            } else { //cache miss
                allowed = super.isAllowedInternalForFedoraDocuments(actionName, pid, stream, path, user);
                cache.put(new CacheKey(pid, user, ip), allowed);

                // TODO: this is hack, replace it in future
                currentThreadReturnObject.set(allowed);

                return allowed;
            }
        } else {
            RightsReturnObject allowed = super.isAllowedInternalForFedoraDocuments(actionName, pid, stream, path, user);

            // TODO: this is hack, replace it in future
            currentThreadReturnObject.set(allowed);

            return allowed;
        }

    }


}
