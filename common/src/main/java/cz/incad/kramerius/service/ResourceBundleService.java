package cz.incad.kramerius.service;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public interface ResourceBundleService {
	
	public ResourceBundle getResourceBundle(String name, Locale locale) throws IOException;

	public File bundlesFolder();
}
