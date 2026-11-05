package cz.incad.kramerius.cache;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

public class CacheStatisticsTest {

    @Test
    public void snapshotReportsHitRatioAndExpiredLookups() {
        String cacheName = "test-" + UUID.randomUUID();
        CacheStatistics.hit(cacheName);
        CacheStatistics.miss(cacheName);
        CacheStatistics.expired(cacheName);
        CacheStatistics.miss(cacheName);

        JSONObject stats = CacheStatistics.snapshot().getJSONObject("caches").getJSONObject(cacheName);
        Assert.assertEquals(1L, stats.getLong("hits"));
        Assert.assertEquals(2L, stats.getLong("misses"));
        Assert.assertEquals(1L, stats.getLong("expired"));
        Assert.assertEquals(3L, stats.getLong("lookups"));
        Assert.assertEquals(1.0 / 3.0, stats.getDouble("hitRatio"), 0.000001);
    }
}
