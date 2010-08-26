package cz.incad.kramerius.service.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Locale;

import cz.incad.kramerius.Constants;
import cz.incad.kramerius.service.TextsService;
import cz.incad.kramerius.utils.IOUtils;

/**
 *
 * @author Administrator
 */
public class TextsServiceImpl implements TextsService {
	
	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(TextsServiceImpl.class.getName());
	
	private static HashMap<String, Locale> LOCALES = new HashMap<String, Locale>() {{
    	put("en",Locale.ENGLISH);
    	put("cs",new Locale("cs", "cz"));
    }};

    
	
    public TextsServiceImpl() {
        super();
        try {
            this.init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void init() throws IOException {
        String[] texts = 
        {"default_intro",
        "default_intro_EN_en"};
        IOUtils.copyBundledResources(this.getClass(), texts,"res/", this.textsFolder());
    }

    
    public  String getText(String name, Locale locale) throws IOException {
        if (locale == null) {
        	locale = Locale.getDefault();
        }
        File textFile = textFile(name, locale);
        if ((!textFile.exists())  || (!textFile.canRead())) throw new IOException("cannot read from file '"+name+"'");
        String retVal = IOUtils.readAsString(new FileInputStream(textFile), Charset.forName("UTF-8"), true);
        return retVal;
    }



    private File textFile(String name, Locale locale) {
        File textFile = textFile(textsFolder(), name, locale);
        if (!textFile.exists()) {
        	textFile = textFileWithoutLocale(textsFolder(), name);
        	LOGGER.info("using textFile without locale '"+textFile+"'");
        }
        return textFile;
    }
    
    

    private File textFileWithoutLocale(File textsDir, String name) {
		File textFile = new File(textsDir, name );
		return textFile;
	}

    

	@Override
    public boolean isAvailable(String name, Locale locale) {
        File textFile = textFile(name, locale);
	    return ((textFile.exists())  && (textFile.canRead()));
    }



    public  void writeText(String name, Locale locale, String text) throws  IOException {
        if (text == null) {
            System.out.println("invalid text");
        } else {
        	if (locale == null) {
        		locale = Locale.getDefault();
        	}
            File file = textFile(textsFolder(), name, locale);
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
            out.write(text);
            out.close();
        }
    }

    
	@Override
	public Locale findLocale(String languageAcronym) {
		Locale locale = LOCALES.get(languageAcronym);
		if (locale == null) {
			Locale[] aLocales = Locale.getAvailableLocales();
			for (Locale iterate : aLocales) {
				if (iterate.getLanguage().equals(languageAcronym)) {
					locale = iterate;
					break;
				}
			}
		}
		return locale;
	}
    


	
	
	@Override
	public File textsFolder() {
		String dirName = Constants.WORKING_DIR + File.separator + "texts";
		File dir = new File(dirName);
		if (!dir.exists()) { 
			boolean mkdirs = dir.mkdirs();
			if (!mkdirs)  throw new RuntimeException("cannot create dir '"+dir.getAbsolutePath()+"'");
		}
		return dir;
	}


	private File textFile(File textsDir, String name, Locale locale) {
		File textFile = new File(textsDir, name + "_" + locale.getCountry()+"_"+locale.getLanguage());
		return textFile;
	}

	public static void main(String[] args) {
		Locale[] locales = Locale.getAvailableLocales();
		for (Locale locale : locales) {
			System.out.println(locale);
		}
		System.out.println(new TextsServiceImpl().findLocale("cs").getDisplayCountry());
	}
}
