package cz.incad.kramerius.service;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public interface TextsService {

	public String getText(String name, Locale locale) throws IOException;

	public void writeText(String name, Locale locale, String text) throws IOException;

	public File textsFolder();
	
	public Locale findLocale(String languageAcronym);
}
