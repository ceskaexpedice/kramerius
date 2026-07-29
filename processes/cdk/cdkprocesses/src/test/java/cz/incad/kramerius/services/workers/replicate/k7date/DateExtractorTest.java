package cz.incad.kramerius.services.workers.replicate.k7date;

import org.junit.Assert;
import org.junit.Test;

public class DateExtractorTest {

    private final DateExtractor extractor = new DateExtractor();

    @Test
    public void extractsExactDay() {
        DateInfo info = extractor.extractFromString("07. 04. 1920", "uuid:exact");

        Assert.assertTrue(info.isInstant());
        Assert.assertEquals(Integer.valueOf(1920), info.instantYear);
        Assert.assertEquals(Integer.valueOf(4), info.instantMonth);
        Assert.assertEquals(Integer.valueOf(7), info.instantDay);
        Assert.assertEquals("1920-04-07T00:00:00.001Z", MyDateTimeUtils.formatForSolr(info.dateMin));
        Assert.assertEquals("1920-04-07T23:59:59.999Z", MyDateTimeUtils.formatForSolr(info.dateMax));
    }

    @Test
    public void extractsDayRangeWithSharedYear() {
        DateInfo info = extractor.extractFromString("7.4.-9.4.1920", "uuid:range");

        Assert.assertFalse(info.isInstant());
        Assert.assertEquals(Integer.valueOf(1920), info.rangeStartYear);
        Assert.assertEquals(Integer.valueOf(4), info.rangeStartMonth);
        Assert.assertEquals(Integer.valueOf(7), info.rangeStartDay);
        Assert.assertEquals(Integer.valueOf(1920), info.rangeEndYear);
        Assert.assertEquals(Integer.valueOf(4), info.rangeEndMonth);
        Assert.assertEquals(Integer.valueOf(9), info.rangeEndDay);
        Assert.assertEquals("1920-04-07T00:00:00.001Z", MyDateTimeUtils.formatForSolr(info.dateMin));
        Assert.assertEquals("1920-04-09T23:59:59.999Z", MyDateTimeUtils.formatForSolr(info.dateMax));
    }

    @Test
    public void extractsVerbalYearRange() {
        DateInfo info = extractor.extractFromString("[mezi 1695 a 1730]", "uuid:verbal");

        Assert.assertFalse(info.isInstant());
        Assert.assertEquals(Integer.valueOf(1695), info.rangeStartYear);
        Assert.assertEquals(Integer.valueOf(1730), info.rangeEndYear);
        Assert.assertEquals("1695", info.valueStart);
        Assert.assertEquals("1730", info.valueEnd);
        Assert.assertEquals("1695-01-01T00:00:00.001Z", MyDateTimeUtils.formatForSolr(info.dateMin));
        Assert.assertEquals("1730-12-31T23:59:59.999Z", MyDateTimeUtils.formatForSolr(info.dateMax));
    }

    @Test
    public void extractsPartialYearRange() {
        DateInfo info = extractor.extractFromString("192u-19uu", "uuid:partial");

        Assert.assertEquals("1920-01-01T00:00:00.001Z", MyDateTimeUtils.formatForSolr(info.dateMin));
        Assert.assertEquals("1999-12-31T23:59:59.999Z", MyDateTimeUtils.formatForSolr(info.dateMax));
    }

    @Test
    public void unsupportedValueKeepsOriginalValueAndHasNoDateBounds() {
        DateInfo info = extractor.extractFromString("not a date", "uuid:bad");

        Assert.assertEquals("not a date", info.value);
        Assert.assertNull(info.dateMin);
        Assert.assertNull(info.dateMax);
        Assert.assertNull(info.instantYear);
    }
}
