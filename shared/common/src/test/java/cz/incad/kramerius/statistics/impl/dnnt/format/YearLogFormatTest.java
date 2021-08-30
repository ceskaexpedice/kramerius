package cz.incad.kramerius.statistics.impl.dnnt.format;

import cz.incad.kramerius.statistics.accesslogs.dnnt.date.YearLogFormat;
import junit.framework.Assert;
import org.junit.Test;

import java.util.Calendar;

public class YearLogFormatTest {

    @Test
    public void testDefaultFormat() {
        Assert.assertEquals(new YearLogFormat().format(" c2001"),"2001");
        Assert.assertEquals(new YearLogFormat().format(" (1989-2014)"),"2014");
        Assert.assertEquals(new YearLogFormat().format(" [2010?]"),"2010");
        Assert.assertEquals(new YearLogFormat().format(" 2000, c1994"),"2000");
        Assert.assertEquals(new YearLogFormat().format(" 2006 [i.e. 2007]"),"2007");
        Assert.assertEquals(new YearLogFormat().format("20.11. 2000 - 1.12. 2001"),"2001");
        Assert.assertEquals(new YearLogFormat().format("1900-1902, 1900 - 1903"),"1903");
        Assert.assertEquals(new YearLogFormat().format("19uu"),"1999");
        Assert.assertEquals(new YearLogFormat().format("191-"),"1919");
        Assert.assertEquals(new YearLogFormat().format("19uu, 191-, 193-"),"1999");
        Assert.assertEquals(new YearLogFormat().format("20uu"),""+Calendar.getInstance().get(Calendar.YEAR));
    }
}
