package cz.incad.kramerius.statistics.accesslogs.solr;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class IntentHeaderTest {

    @Test
    public void normalizesTrimsLowercasesAndDeduplicates() {
        Assert.assertEquals(Arrays.asList("view", "fulltext-search"),
                SolrStatisticsAccessLogImpl.parseIntents(" View, fulltext-search ,view"));
    }

    @Test
    public void dropsInvalidValuesAndLimitsCount() {
        Assert.assertEquals(Arrays.asList("view", "fulltext-search", "x", "y", "z"),
                SolrStatisticsAccessLogImpl.parseIntents("View, fulltext-search ,BAD VALUE,view,x,y,z,w"));
        Assert.assertEquals(Collections.emptyList(), SolrStatisticsAccessLogImpl.parseIntents("-leading,,  ,ÄÖ,a".replace("a", "")));
        Assert.assertEquals(Collections.singletonList("a"), SolrStatisticsAccessLogImpl.parseIntents("-leading,,  ,ÄÖ,a"));
        String tooLong = "a123456789012345678901234567890123";
        Assert.assertEquals(Collections.emptyList(), SolrStatisticsAccessLogImpl.parseIntents(tooLong));
    }

    @Test
    public void nullAndEmptyGiveEmptyList() {
        Assert.assertEquals(Collections.emptyList(), SolrStatisticsAccessLogImpl.parseIntents(null));
        Assert.assertEquals(Collections.emptyList(), SolrStatisticsAccessLogImpl.parseIntents(""));
    }
}
