package cz.incad.kramerius.cache;

import org.json.JSONObject;

import java.time.Instant;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/** Process-local counters for cache lookups. */
public final class CacheStatistics {

    public static final String RIGHTS = "rights";
    public static final String CDK_ITEMS = "cdkItems";
    public static final String CDK_USERS = "cdkUsers";

    private static final Instant STARTED_AT = Instant.now();
    private static final ConcurrentHashMap<String, Counters> COUNTERS = new ConcurrentHashMap<>();

    private CacheStatistics() {
    }

    public static void hit(String cacheName) {
        counters(cacheName).hits.increment();
    }

    public static void miss(String cacheName) {
        counters(cacheName).misses.increment();
    }

    public static void expired(String cacheName) {
        counters(cacheName).expired.increment();
    }

    public static JSONObject snapshot() {
        JSONObject result = new JSONObject();
        result.put("scope", "process-local");
        result.put("since", STARTED_AT.toString());
        result.put("instance", instanceName());

        JSONObject caches = new JSONObject();
        COUNTERS.forEach((name, counters) -> {
            long hits = counters.hits.sum();
            long misses = counters.misses.sum();
            long lookups = hits + misses;
            JSONObject values = new JSONObject();
            values.put("hits", hits);
            values.put("misses", misses);
            values.put("expired", counters.expired.sum());
            values.put("lookups", lookups);
            values.put("hitRatio", lookups == 0 ? JSONObject.NULL : (double) hits / lookups);
            caches.put(name, values);
        });
        result.put("caches", caches);
        return result;
    }

    private static Counters counters(String cacheName) {
        return COUNTERS.computeIfAbsent(cacheName, ignored -> new Counters());
    }

    private static String instanceName() {
        String host = System.getenv("HOSTNAME");
        if (host == null || host.trim().isEmpty()) {
            host = System.getenv("COMPUTERNAME");
        }
        if (host == null || host.trim().isEmpty()) {
            try {
                host = InetAddress.getLocalHost().getHostName();
            } catch (Exception ignored) {
                host = "unknown";
            }
        }
        return host;
    }

    private static final class Counters {
        private final LongAdder hits = new LongAdder();
        private final LongAdder misses = new LongAdder();
        private final LongAdder expired = new LongAdder();
    }
}
