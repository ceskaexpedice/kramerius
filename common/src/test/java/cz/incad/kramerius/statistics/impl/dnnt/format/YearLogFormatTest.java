package cz.incad.kramerius.statistics.impl.dnnt.format;

import junit.framework.Assert;
import org.junit.Test;

public class YearLogFormatTest {

    @Test
    public void testDefaultFormat() {
        Assert.assertEquals(new YearLogFormat().format(" c2001"),"2001");
        Assert.assertEquals(new YearLogFormat().format(" (1989-2014)"),"2014");
        Assert.assertEquals(new YearLogFormat().format(" [2010?]"),"2010");
        Assert.assertEquals(new YearLogFormat().format(" 2000, c1994"),"2000");
        Assert.assertEquals(new YearLogFormat().format(" 2006 [i.e. 2007]"),"2007");


    }
}
