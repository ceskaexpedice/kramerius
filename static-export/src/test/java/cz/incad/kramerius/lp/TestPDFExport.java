package cz.incad.kramerius.lp;

import java.io.File;

import cz.incad.kramerius.lp.utils.FileUtils;

import junit.framework.TestCase;

public class TestPDFExport extends TestCase {

    File tmpFolder;

    @Override
    protected void setUp() throws Exception {
	tmpFolder = FileUtils.createTMPFolder();
    }

    @Override
    protected void tearDown() throws Exception {
	FileUtils.deleteTMPFolder();
    }

    public void testExportHTML() {
	PDFExport.copyHTMLContent(tmpFolder, "titleFromTest", Medium.CD, "1");
	File file = new File(tmpFolder, "html" + File.separator + "index.html");
	assertTrue(file.exists());
	assertTrue(file.isFile());

	File img = new File(tmpFolder, "html" + File.separator + "img");
	assertTrue(img.exists());
	assertTrue(img.isDirectory());

	File css = new File(tmpFolder, "html" + File.separator + "css");
	assertTrue(css.exists());
	assertTrue(css.isDirectory());

	File js = new File(tmpFolder, "html" + File.separator + "js");
	assertTrue(js.exists());
	assertTrue(js.isDirectory());
    }
}
