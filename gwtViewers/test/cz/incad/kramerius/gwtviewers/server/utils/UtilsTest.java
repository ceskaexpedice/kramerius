package cz.incad.kramerius.gwtviewers.server.utils;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import cz.incad.kramerius.gwtviewers.client.SimpleImageTO;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import junit.framework.TestCase;

public class UtilsTest extends TestCase {

	public void testPages() throws IOException, ParserConfigurationException, SAXException, LexerException {
		KConfiguration kConfiguration = KConfiguration.getKConfiguration();
		ArrayList<SimpleImageTO> pages = Utils.getPages(kConfiguration, null, "966dfeb3-e640-11de-a504-001143e3f55c");
		System.out.println(pages.size());
	}
}
