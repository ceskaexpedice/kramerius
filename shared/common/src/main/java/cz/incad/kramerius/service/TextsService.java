package cz.incad.kramerius.service;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

/**
 * This service provides localized texts.  
 * It is similar like resource bundle but intended to longer texts.
 * @author pavels
 */
public interface TextsService {
	
    /**
     * Returns true if text with given name and locale is defined
     * @param name Text name
     * @param locale Locale
     * @return found text
     */
    public boolean isAvailable(String name, Locale locale);
    
	/**
	 * Return localized texts
	 * @param name Name of text
	 * @param locale Locale
	 * @return
	 * @throws IOException
	 */
	public String getText(String name, Locale locale) throws IOException;

	/**
	 * Save new text
	 * @param name name of text
	 * @param locale locale
	 * @param text new text
	 * @throws IOException
	 */
	public void writeText(String name, Locale locale, String text) throws IOException;

	/**
	 * Folder for user defined texts
	 * @return
	 */
	public File textsFolder();
	
	
	public Locale findLocale(String languageAcronym);
}
