package cz.incad.kramerius.security.impl.http;

import com.google.inject.Inject;
import com.google.inject.Provider;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.cache.CacheStatistics;
import cz.incad.kramerius.security.*;
import cz.incad.kramerius.security.licenses.lock.ExclusiveLockMaps;
import cz.incad.kramerius.utils.IPAddressUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.expiry.Expirations;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static cz.incad.kramerius.security.impl.criteria.utils.CriteriaLicenseUtils.*;


/**
 * RightsResolverFromRequestCached
 *
 * @author Martin Rumanek
 */
public class RightsResolverFromRequestCached extends RightsResolverFromRequest {
    static class CacheKey implements Serializable {
        private final String PID;
        private final String path;
        private final String userIdentity;
        private final String ip;

        public CacheKey(String PID, ObjectPidsPath path, User user, String ip) {
            this.PID = PID;
            this.path = path == null ? null : path.toString();
            this.userIdentity = userCacheIdentity(user);
            this.ip = ip;
        }

        static String userCacheIdentity(User user) {
            if (user == null || UserManager.NOT_LOGGED_USER.equals(user.getLoginname())) {
                return "role:" + DefaultRoles.COMMON_USERS.getName();
            }
            return "uid:" + user.getLoginname();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CacheKey cacheKey = (CacheKey) o;

            if (PID != null ? !PID.equals(cacheKey.PID) : cacheKey.PID != null) return false;
            if (path != null ? !path.equals(cacheKey.path) : cacheKey.path != null) return false;
            if (userIdentity != null ? !userIdentity.equals(cacheKey.userIdentity) : cacheKey.userIdentity != null) return false;
            return ip != null ? ip.equals(cacheKey.ip) : cacheKey.ip == null;
        }

        @Override
        public int hashCode() {
            int result = PID != null ? PID.hashCode() : 0;
            result = 31 * result + (path != null ? path.hashCode() : 0);
            result = 31 * result + (userIdentity != null ? userIdentity.hashCode() : 0);
            result = 31 * result + (ip != null ? ip.hashCode() : 0);
            return result;
        }
    }

    private static Cache<CacheKey, RightsReturnObject> cache;

    private Provider<HttpServletRequest> provider;

    private static final String CACHE_ALIAS = "ActionAllowedCache";

    public static JSONObject inspectCache(String pid, String user, String ip, String path, int limit) {
        JSONObject result = new JSONObject();
        result.put("cache", "rights");
        result.put("scope", "process-local");
        result.put("limit", limit);

        JSONArray entries = new JSONArray();
        int matched = 0;
        Cache<CacheKey, RightsReturnObject> currentCache = cache;
        if (currentCache != null) {
            for (Cache.Entry<CacheKey, RightsReturnObject> entry : currentCache) {
                CacheKey key = entry.getKey();
                if (!matches(pid, key.PID) || !matches(user, key.userIdentity)
                        || !matches(ip, key.ip) || !matches(path, key.path)) {
                    continue;
                }
                matched++;
                if (entries.length() >= limit) {
                    continue;
                }
                JSONObject keyJson = new JSONObject();
                keyJson.put("pid", jsonValue(key.PID));
                keyJson.put("path", jsonValue(key.path));
                keyJson.put("user", jsonValue(key.userIdentity));
                keyJson.put("ip", jsonValue(key.ip));
                keyJson.put("allowed", entry.getValue().flag());
                keyJson.put("state", entry.getValue().getState().name());
                entries.put(keyJson);
            }
        }
        result.put("matched", matched);
        result.put("truncated", matched > limit);
        result.put("entries", entries);
        return result;
    }

    /** Removes matching entries from this process's rights cache. Empty filters clear the cache. */
    public static int clearCache(String pid, String user, String ip, String path) {
        Cache<CacheKey, RightsReturnObject> currentCache = cache;
        if (currentCache == null) {
            return 0;
        }

        List<CacheKey> keysToRemove = new ArrayList<>();
        for (Cache.Entry<CacheKey, RightsReturnObject> entry : currentCache) {
            CacheKey key = entry.getKey();
            if (matches(pid, key.PID) && matches(user, key.userIdentity)
                    && matches(ip, key.ip) && matches(path, key.path)) {
                keysToRemove.add(key);
            }
        }
        keysToRemove.forEach(currentCache::remove);
        return keysToRemove.size();
    }

    private static boolean matches(String filter, String value) {
        return filter == null || filter.trim().isEmpty() || Objects.equals(filter, value);
    }

    private static Object jsonValue(String value) {
        return value == null ? JSONObject.NULL : value;
    }

    @Inject
    public RightsResolverFromRequestCached(Logger logger, Provider<HttpServletRequest> provider,
                                           RightsManager rightsManager, RightCriteriumContextFactory contextFactory,
                                           Provider<User> currentUserProvider, CacheManager cacheManager, ExclusiveLockMaps maps) {
        super(logger, provider, rightsManager, contextFactory, currentUserProvider, maps);

        this.provider = provider;

        cache = cacheManager.getCache(CACHE_ALIAS, CacheKey.class, RightsReturnObject.class);
        if (cache == null) {
            cache = cacheManager.createCache(CACHE_ALIAS,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(CacheKey.class, RightsReturnObject.class,
                            ResourcePoolsBuilder.heap(1000).offheap(32, MemoryUnit.MB))
                            .withExpiry(Expirations.timeToLiveExpiration(
                                    org.ehcache.expiry.Duration.of(  KConfiguration.getInstance().getCacheTimeToLiveExpiration(), TimeUnit.SECONDS))).build());
        }
    }


    public RightsReturnObject isAllowedInternalForFedoraDocuments(String actionName, String pid, String stream, ObjectPidsPath path, User user) throws RightCriteriumException {
        if (SecuredActions.A_READ.getFormalName().equals(actionName)) {
        //if ("read".equals(actionName)) {
            String ip = IPAddressUtils.getRemoteAddress(this.provider.get());
            CacheKey key = new CacheKey(pid, path, user, ip);
            RightsReturnObject allowed = cache.get(key);

            if (allowed != null) { //cache hit
                CacheStatistics.hit(CacheStatistics.RIGHTS);

                // TODO: this is hack, replace it in future
                currentThreadReturnObject.set(allowed);

                return allowed;
            } else { //cache miss
                CacheStatistics.miss(CacheStatistics.RIGHTS);
                allowed = super.isAllowedInternalForFedoraDocuments(actionName, pid, stream, path, user);
                cache.put(key, allowed);

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
