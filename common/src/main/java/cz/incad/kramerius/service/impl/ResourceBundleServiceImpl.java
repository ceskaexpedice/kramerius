package cz.incad.kramerius.service.impl;

import static cz.incad.kramerius.utils.IOUtils.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;

import com.google.inject.Inject;
import com.google.inject.internal.Nullable;
import com.google.inject.name.Named;

import sun.applet.resources.MsgAppletViewer;

import cz.incad.kramerius.Constants;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.IOUtils;

public class ResourceBundleServiceImpl implements ResourceBundleService {

	@Inject(optional=true)
	@Named("workingDir")
	private String workingDir;
	
	@Override
	public File bundlesFolder() {
		String dirName = Constants.WORKING_DIR + File.separator + "bundles";
		if (workingDir != null) dirName = workingDir + File.separator + "bundles";
		File dir = new File(dirName);
		if (!dir.exists()) { dir.mkdirs(); }
		return dir;
	}


	@Override
	public ResourceBundle getResourceBundle(String name, Locale locale) throws IOException {
		File resourcesDir = checkFiles(name);
		ResourceBundle parentBundle = ResourceBundle.getBundle(name, locale);
		FolderResourceBundle resBundle = (FolderResourceBundle) ResourceBundle.getBundle(name, locale, new ResourceClassLoader(), new ResourceBundleControl(resourcesDir));
		resBundle.setParentBundle(parentBundle);
		return resBundle;
	}


	public File checkFiles(String name) throws IOException {
		File resourcesDir = bundlesFolder();
		if ((resourcesDir.listFiles() == null) || (resourcesDir.listFiles().length == 0)) {
			copyDefault();
		} else {
			boolean copyDefault = true;
			File[] listFiles = resourcesDir.listFiles();
			for (File file : listFiles) {
				if (file.getName().equals(name+".properties")) {
					copyDefault = false;
					break;
				}
			}
			if (copyDefault) copyDefault();
		}
		return resourcesDir;
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

		public ResourceClassLoader() {
			super();
		}

		@Override
		protected URL findResource(String name) {
			return null;
		}

		@Override
		protected Enumeration<URL> findResources(String name) throws IOException {
			Enumeration<URL> elements = new Vector().elements();
			return elements; 
		}
	}

	private static class ResourceBundleControl extends ResourceBundle.Control {

		private File folder;

		public ResourceBundleControl(File folder) {
			super();
			this.folder = folder;
		}
		
		@Override
		public List<String> getFormats(String baseName) {
			return Arrays.asList("properties");
		}


		@Override
		public ResourceBundle newBundle(String baseName, Locale locale,
				String format, ClassLoader loader, boolean reload)
				throws IllegalAccessException, InstantiationException,
				IOException {
			
			  String bundleName = toBundleName(baseName, locale);
              String resourceName = toResourceName(bundleName, format);
              File inFile = new File(folder, resourceName);
              ByteArrayInputStream bos = IOUtils.bos(inFile);
              return new FolderResourceBundle(bos);
		}

		@Override
		public String toBundleName(String baseName, Locale locale) {
			return super.toBundleName(baseName, locale);
		}

		@Override
		public boolean needsReload(String baseName, Locale locale,
				String format, ClassLoader loader, ResourceBundle bundle,
				long loadTime) {
			return super.needsReload(baseName, locale, format, loader, bundle, loadTime);
		}
	}
	
	private static class FolderResourceBundle extends PropertyResourceBundle {

		public FolderResourceBundle(InputStream stream) throws IOException {
			super(stream);
		}

		public void setParentBundle(ResourceBundle parentBundle) {
			super.setParent(parentBundle);
		}
	}
}
