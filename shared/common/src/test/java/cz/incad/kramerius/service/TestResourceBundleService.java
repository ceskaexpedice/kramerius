package cz.incad.kramerius.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.ResourceBundle;

import com.google.inject.Guice;
import com.google.inject.Injector;

import cz.incad.kramerius.service.impl.ResourceBundleServiceImpl;
import cz.incad.kramerius.utils.IOUtils;

import junit.framework.TestCase;

public class TestResourceBundleService {

//extends TestCase {
//
//	
//	@Override
//	protected void tearDown() throws Exception {
//		String bundleDir = bundleDir();
//		File[] listFiles = new File(bundleDir).listFiles();
//		if (listFiles != null) {
//			for (File file : listFiles) {
//				file.delete();
//			}
//		}
//		new File(bundleDir).delete();
//	}
//
//	
//	public void disabled_testTwoResources() throws FileNotFoundException, IOException {
//		Injector injector = Guice.createInjector(new ResourceBundleServiceModule());
//		ResourceBundleService bundleService = injector.getInstance(ResourceBundleService.class);
//		((ResourceBundleServiceImpl) bundleService).checkFiles("base");
//		String bundleDir = bundleDir();
//		String[] files = {"base.properties"};
//		for (String nm : files) {
//			modifyFile(bundleDir, nm);
//		}
//		ResourceBundle resourceBundle = bundleService.getResourceBundle("base", Locale.getDefault());
//		String string = resourceBundle.getString("added_from_test");
//		assertNotNull(string);
//	}
//
//	private String bundleDir() {
//		String bundleDir = System.getProperty("user.dir")+File.separator+"bundles";
//		return bundleDir;
//	}
//
//	private void modifyFile(String bundleDir, String fname) throws IOException,
//			FileNotFoundException {
//		File baseProperties = new File(bundleDir,fname);
//		String string = IOUtils.readAsString(new FileInputStream(baseProperties), Charset.defaultCharset(), true);
//		string += "\nadded_from_test=Value A";
//		IOUtils.saveToFile(string, baseProperties);
//	}
	
	
}
