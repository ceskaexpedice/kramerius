package cz.incad.kramerius.views;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import cz.incad.Kramerius.views.ProcessViewObject;
import cz.incad.kramerius.FedoraAccess;

public class ProcessViewObjectTest {

	public static final String BUNDLES="administrator.processes.duration.days=days \n" +
			"administrator.processes.duration.hours=hours\n" +
			"administrator.processes.duration.minutes=minutes\n " +
			"administrator.processes.duration.seconds=seconds\n" +
			"administrator.processes.duration.miliseconds=miliseconds";

	
	@Test
	public void testDateCalculations() throws ParseException, IOException {
		PropertyResourceBundle propRes = new PropertyResourceBundle(new StringReader(BUNDLES));
		
    	String start = "2013/03/04 - 08:13:57";
		String stop = "2013/03/04 - 10:08:20";
        StringBuilder calculated = ProcessViewObject.formatDuration(ProcessViewObject.FORMAT.parse(start).getTime(), ProcessViewObject.FORMAT.parse(stop).getTime(), propRes);
        Assert.assertTrue(calculated.toString().trim().equals("1 hours 54 minutes 23 seconds"));

    	start = "2013/03/04 - 08:13:57";
		stop = "2013/03/04 - 08:13:58";
        calculated = ProcessViewObject.formatDuration(ProcessViewObject.FORMAT.parse(start).getTime(), ProcessViewObject.FORMAT.parse(stop).getTime(), propRes);
        Assert.assertTrue(calculated.toString().trim().equals("1 seconds"));

    	start = "2013/03/04 - 08:13:57";
		stop = "2013/03/04 - 08:14:58";
        calculated = ProcessViewObject.formatDuration(ProcessViewObject.FORMAT.parse(start).getTime(), ProcessViewObject.FORMAT.parse(stop).getTime(), propRes);
        Assert.assertTrue(calculated.toString().trim().equals("1 minutes 1 seconds"));

    	start = "2013/03/04 - 08:13:57";
		stop = "2013/03/04 - 08:14:56";
        calculated = ProcessViewObject.formatDuration(ProcessViewObject.FORMAT.parse(start).getTime(), ProcessViewObject.FORMAT.parse(stop).getTime(), propRes);
        Assert.assertTrue(calculated.toString().trim().equals("59 seconds"));

    	start = "2013/03/04 - 08:13:57";
		stop = "2013/03/04 - 09:14:56";
        calculated = ProcessViewObject.formatDuration(ProcessViewObject.FORMAT.parse(start).getTime(), ProcessViewObject.FORMAT.parse(stop).getTime(), propRes);
        Assert.assertTrue(calculated.toString().trim().equals("1 hours 59 seconds"));

    	start = "2013/03/04 - 08:13:57";
		stop = "2013/03/04 - 09:16:22";
        calculated = ProcessViewObject.formatDuration(ProcessViewObject.FORMAT.parse(start).getTime(), ProcessViewObject.FORMAT.parse(stop).getTime(), propRes);
        Assert.assertTrue(calculated.toString().trim().equals("1 hours 2 minutes 25 seconds"));

    	start = "2013/03/04 - 08:13:57";
		stop = "2013/12/04 - 10:33:22";
        calculated = ProcessViewObject.formatDuration(ProcessViewObject.FORMAT.parse(start).getTime(), ProcessViewObject.FORMAT.parse(stop).getTime(), propRes);
        Assert.assertTrue(calculated.toString().trim().equals("9 days  2 hours 19 minutes 25 seconds"));
	}
	
}
