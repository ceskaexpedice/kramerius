package cz.incad.kramerius.service.impl;

import static cz.incad.kramerius.utils.IOUtils.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.logging.Level;

import cz.incad.kramerius.Constants;
import cz.incad.kramerius.service.ResourceBundleService;

public class ResourceBundleServiceImpl implements ResourceBundleService {


	
	@Override
	public File bundlesFolder() {
		String dirName = Constants.WORKING_DIR + File.separator + "bundles";
		File dir = new File(dirName);
		if (!dir.exists()) { dir.mkdirs(); }
		return dir;
	}


	@Override
	public ResourceBundle getResourceBundle(String name, Locale locale) throws IOException {
		File resourcesDir = bundlesFolder();
		if ((resourcesDir.listFiles() == null) || (resourcesDir.listFiles().length == 0)) {
			copyDefault();
		} else {
			File[] listFiles = resourcesDir.listFiles();
			for (File file : listFiles) {
				if (file.getName().equals(name+".properties")) {
					break;
				}
			}
			copyDefault();
			
		}
		ResourceBundle parentBundle = ResourceBundle.getBundle(name, locale);
		FolderResourceBundle resBundle = new FolderResourceBundle(name, locale, resourcesDir);
		resBundle.setParentBundle(parentBundle);
		return resBundle;
	}


	
	
	private void copyDefault() throws IOException {
		String[] defaults = 
		{"base.properties",
		"base_en.properties",
		"base_cs.properties"};
		for (String base : defaults) {
			InputStream is = null;
			OutputStream os = null;
			try {
				is = this.getClass().getResourceAsStream("res/"+base);
				os = new FileOutputStream(new File(bundlesFolder(),base));
				copyStreams(is, os);
			} finally {
				if (os != null) is.close();
				if (is != null) is.close();
			}
		}
	}
	
	
	public static class ResourceClassLoader extends ClassLoader {

		public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
				.getLogger(ResourceBundleServiceImpl.ResourceClassLoader.class
						.getName());
		
		private File folder;

		public ResourceClassLoader(File folder) {
			super();
			this.folder = folder;
		}

		@Override
		protected URL findResource(String name) {
			try {
				File file = new File(this.folder, name);
				if (file.exists()) {
					return file.toURI().toURL();
				} else return null;
			} catch (MalformedURLException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
				return null;
			}
		}

		@Override
		protected Enumeration<URL> findResources(String name) throws IOException {
			URL url = findResource(name);
			Enumeration<URL> elements = new Vector(Arrays.asList(url)).elements();
			return elements; 
		}
	}

	private static class FolderResourceBundle extends ResourceBundle {

		private ResourceBundle childBundle;

		public FolderResourceBundle(String name, Locale locale, File resourcesDir) {
			super();
			childBundle = ResourceBundle.getBundle(name, locale, new ResourceClassLoader(resourcesDir));
		}

		@Override
		public Enumeration<String> getKeys() {
			return childBundle.getKeys();
		}

		@Override
		protected Object handleGetObject(String key) {
			return childBundle.getString(key);
		}
		
		public void setParentBundle(ResourceBundle parentBundle) {
			super.setParent(parentBundle);
		}
		
	}
}
