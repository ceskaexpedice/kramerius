package cz.incad.kramerius.service;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * This service provides resourceBundle
 * @author pavels
 */
public interface ResourceBundleService {
	
	/**
	 * Returns ResourceBundle 
	 * @param name name of the bundle 
	 * @param locale Bundle's locale 
	 * @return
	 * @throws IOException
	 */
	public ResourceBundle getResourceBundle(String name, Locale locale) throws IOException;

	/**
	 * Returns folder for user defined resource bundles
	 * @return
	 */
	public File bundlesFolder();
}
