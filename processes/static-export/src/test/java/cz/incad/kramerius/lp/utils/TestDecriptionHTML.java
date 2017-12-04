package cz.incad.kramerius.lp.utils;

import java.io.IOException;

import cz.incad.kramerius.lp.Medium;
import junit.framework.TestCase;

public class TestDecriptionHTML extends TestCase {

	public void testDescription() throws IOException {
		String html = DecriptionHTML.descriptionHTML("dctitle", Medium.CD, new String[] {"att1.pdf","att2.pdf"},"1");
		assertNotNull(html);
	}
}
