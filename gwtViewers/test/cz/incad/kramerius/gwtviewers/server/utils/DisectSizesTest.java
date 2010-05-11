package cz.incad.kramerius.gwtviewers.server.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import cz.incad.kramerius.gwtviewers.client.SimpleImageTO;
import cz.incad.kramerius.gwtviewers.server.utils.ThumbnailServerUtils;
import cz.incad.kramerius.gwtviewers.server.utils.Utils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import junit.framework.TestCase;

public class DisectSizesTest extends TestCase {
	
	private static int ITERATIONS = 10;
	//String currentProcessinguuid = "775211f0-9280-11de-9660-000d606f5dc6";
	private static String UUID = "8f526130-8b0d-11de-8994-000d606f5dc6";
	//private static String UUID = "775211f0-9280-11de-9660-000d606f5dc6";

	public void testDisectOld() throws IOException, ParserConfigurationException, SAXException, LexerException {
		KConfiguration configuration = KConfiguration.getKConfiguration();
		int sum = 0;
		for (int i = 0; i < ITERATIONS; i++) {
			long start = System.currentTimeMillis();
			//String currentProcessinguuid = "775211f0-9280-11de-9660-000d606f5dc6";
			ArrayList<SimpleImageTO> pages = Utils.getPages(configuration, null, UUID);
			ThumbnailServerUtils.disectSizesOldStyle( UUID, pages);
			long stop = System.currentTimeMillis();
			//System.out.println((stop-start)+"ms");
			sum += (stop - start);
		}
		System.out.println("average old style:"+(sum / ITERATIONS));
	}
	
	public void testDisect() throws FileNotFoundException, IOException, ParserConfigurationException, SAXException, LexerException {
		KConfiguration configuration = KConfiguration.getKConfiguration();
		int sum = 0;
		for (int i = 0; i < ITERATIONS; i++) {
			long start = System.currentTimeMillis();
			ArrayList<SimpleImageTO> pages = Utils.getPages(configuration, null, UUID);
			ThumbnailServerUtils.disectSizes( UUID, pages);
			long stop = System.currentTimeMillis();
			long trvalo = stop - start;
			sum += trvalo;
		}
		System.out.println("average:"+(sum / ITERATIONS));
	}
	
}
