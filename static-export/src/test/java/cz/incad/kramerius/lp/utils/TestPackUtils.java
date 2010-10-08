package cz.incad.kramerius.lp.utils;

import java.io.File;
import java.io.IOException;



import junit.framework.TestCase;

public class TestPackUtils extends TestCase {

	
	@Override
	protected void tearDown() throws Exception {
		FileUtils.deleteTMPFolder();
	}

	@Override
	protected void setUp() throws Exception {
		FileUtils.deleteTMPFolder();
	}



	public void testUnpack() throws IOException {
		File ffolder = FileUtils.createTMPFolder();
		PackUtils.unpack(ffolder);
		boolean containsDir = false;
		File[] listFiles = ffolder.	listFiles();
		assertNotNull(containsDir);
		for (File folder : listFiles) {
			assertNotNull(folder);
			assertTrue(folder.exists());
			assertTrue(folder.isDirectory());
		}
	}
}
