package indexer;

import cz.kramerius.searchIndex.indexer.conversions.extraction.MyDateTimeUtils;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class MyDateTimeUtilsTest {

    private Date toStartOfYear(int year) {
        return MyDateTimeUtils.toYearStart(year);
    }

    private Date toEndOfYear(int year) {
        return MyDateTimeUtils.toYearEnd(year);
    }

    private String toStartString(int year) {
        return year + "-01-01T00:00:00.001Z";
    }

    private String toEndString(int year) {
        return year + "-12-31T23:59:59.999Z";
    }

    private String format(Date date) {
        return MyDateTimeUtils.formatForSolr(date);
    }

    @Test
    public void start1313() {
        int year = 1313;
        Date date = toStartOfYear(year);
        assertEquals(toStartString(year), format(date));
    }

    @Test
    public void start1871() {
        int year = 1871;
        Date date = toStartOfYear(year);
        assertEquals(toStartString(year), format(date));
    }

    @Test
    public void start1970() {
        int year = 1970;
        Date date = toStartOfYear(year);
        assertEquals(toStartString(year), format(date));
    }

    @Test
    public void start1971() {
        int year = 1971;
        Date date = toStartOfYear(year);
        assertEquals(toStartString(year), format(date));
    }

    @Test
    public void start2003() {
        int year = 2003;
        Date date = toStartOfYear(year);
        assertEquals(toStartString(year), format(date));
    }


    @Test
    public void end1313() {
        int year = 1313;
        Date date = toEndOfYear(year);
        assertEquals(toEndString(year), format(date));
    }

    @Test
    public void end1314() {
        int year = 1314;
        Date date = toEndOfYear(year);
        assertEquals(toEndString(year), format(date));
    }


    @Test
    public void end1871() {
        int year = 1871;
        Date date = toEndOfYear(year);
        assertEquals(toEndString(year), format(date));
    }

    @Test
    public void end1970() {
        int year = 1970;
        Date date = toEndOfYear(year);
        assertEquals(toEndString(year), format(date));
    }

    @Test
    public void end1971() {
        int year = 1971;
        Date date = toEndOfYear(year);
        assertEquals(toEndString(year), format(date));
    }

    @Test
    public void end2003() {
        int year = 2003;
        Date date = toEndOfYear(year);
        assertEquals(toEndString(year), format(date));
    }

    @Test
    public void start180u() {
        Date date = MyDateTimeUtils.toYearStartFromPartialYear("180u");
        assertEquals(toStartString(1800), format(date));
    }

    @Test
    public void start180_() {
        Date date = MyDateTimeUtils.toYearStartFromPartialYear("180-");
        assertEquals(toStartString(1800), format(date));
    }

    @Test
    public void start18uu() {
        Date date = MyDateTimeUtils.toYearStartFromPartialYear("18uu");
        assertEquals(toStartString(1800), format(date));
    }

    @Test
    public void start18__() {
        Date date = MyDateTimeUtils.toYearStartFromPartialYear("18--");
        assertEquals(toStartString(1800), format(date));
    }

    @Test
    public void start1uuu() {
        Date date = MyDateTimeUtils.toYearStartFromPartialYear("1uuu");
        assertEquals(toStartString(1000), format(date));
    }

    @Test
    public void start1___() {
        Date date = MyDateTimeUtils.toYearStartFromPartialYear("1---");
        assertEquals(toStartString(1000), format(date));
    }

    @Test
    public void start18u5() {
        Date date = MyDateTimeUtils.toYearStartFromPartialYear("18u5");
        assertEquals(toStartString(1800), format(date));
    }

    @Test
    public void start18_5() {
        Date date = MyDateTimeUtils.toYearStartFromPartialYear("18-5");
        assertEquals(toStartString(1800), format(date));
    }

    @Test
    public void start1uu5() {
        Date date = MyDateTimeUtils.toYearStartFromPartialYear("1uu5");
        assertEquals(toStartString(1000), format(date));
    }

    @Test
    public void start1__5() {
        Date date = MyDateTimeUtils.toYearStartFromPartialYear("1--5");
        assertEquals(toStartString(1000), format(date));
    }


    @Test
    public void end180u() {
        Date date = MyDateTimeUtils.toYearEndFromPartialYear("180u");
        assertEquals(toEndString(1809), format(date));
    }

    @Test
    public void end180_() {
        Date date = MyDateTimeUtils.toYearEndFromPartialYear("180-");
        assertEquals(toEndString(1809), format(date));
    }

    @Test
    public void end18uu() {
        Date date = MyDateTimeUtils.toYearEndFromPartialYear("18uu");
        assertEquals(toEndString(1899), format(date));
    }

    @Test
    public void end18__() {
        Date date = MyDateTimeUtils.toYearEndFromPartialYear("18--");
        assertEquals(toEndString(1899), format(date));
    }

    @Test
    public void end1uuu() {
        Date date = MyDateTimeUtils.toYearEndFromPartialYear("1uuu");
        assertEquals(toEndString(1999), format(date));
    }

    @Test
    public void end1___() {
        Date date = MyDateTimeUtils.toYearEndFromPartialYear("1---");
        assertEquals(toEndString(1999), format(date));
    }

    @Test
    public void end18u5() {
        Date date = MyDateTimeUtils.toYearEndFromPartialYear("18u5");
        assertEquals(toEndString(1899), format(date));
    }

    @Test
    public void end18_5() {
        Date date = MyDateTimeUtils.toYearEndFromPartialYear("18-5");
        assertEquals(toEndString(1899), format(date));
    }

    @Test
    public void end1uu5() {
        Date date = MyDateTimeUtils.toYearEndFromPartialYear("1uu5");
        assertEquals(toEndString(1999), format(date));
    }

    @Test
    public void end1__5() {
        Date date = MyDateTimeUtils.toYearEndFromPartialYear("1--5");
        assertEquals(toEndString(1999), format(date));
    }

    @Test
    public void start01_2013() {
        Date date = MyDateTimeUtils.toMonthStart(1, 2013);
        assertEquals("2013-01-01T00:00:00.001Z", format(date));
    }

    @Test
    public void start02_2013() {
        Date date = MyDateTimeUtils.toMonthStart(2, 2013);
        assertEquals("2013-02-01T00:00:00.001Z", format(date));
    }

    @Test
    public void start02_leap() {
        Date date = MyDateTimeUtils.toMonthStart(2, 2016);
        assertEquals("2016-02-01T00:00:00.001Z", format(date));
    }

    @Test
    public void start06_2013() {
        Date date = MyDateTimeUtils.toMonthStart(6, 2013);
        assertEquals("2013-06-01T00:00:00.001Z", format(date));
    }

    @Test
    public void end01_2013() {
        Date date = MyDateTimeUtils.toMonthEnd(1, 2013);
        assertEquals("2013-01-31T23:59:59.999Z", format(date));
    }

    @Test
    public void end02_2013() {
        Date date = MyDateTimeUtils.toMonthEnd(2, 2013);
        assertEquals("2013-02-28T23:59:59.999Z", format(date));
    }

    @Test
    public void end02_leap() {
        Date date = MyDateTimeUtils.toMonthEnd(2, 2016);
        assertEquals("2016-02-29T23:59:59.999Z", format(date));
    }

    @Test
    public void end06_2013() {
        Date date = MyDateTimeUtils.toMonthEnd(6, 2013);
        assertEquals("2013-06-30T23:59:59.999Z", format(date));
    }

}
