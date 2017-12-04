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
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

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

    private Map<String, String> internalTexts = new HashMap<String, String>();
    
	
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
        
        this.introspectClassPath();
    }

    
    public  String getText(String name, Locale locale) throws IOException {
        if (locale == null) {
        	locale = Locale.getDefault();
        }
        File textFile = textFile(name, locale);
        if ((!textFile.exists())  || (!textFile.canRead())) {
            if (this.internalTexts.containsKey(longLocalizedName(name, locale))) {
                return this.internalTexts.get(longLocalizedName(name, locale));
            } else if (this.internalTexts.containsKey(shortLocalizedName(name, locale))) {
                return this.internalTexts.get(shortLocalizedName(name, locale));
            } else {
                return this.internalTexts.get(name);
            }
        } else {
            return IOUtils.readAsString(new FileInputStream(textFile), Charset.forName("UTF-8"), true);
        }
    }



    private File textFile(String name, Locale locale) {
        File textFile = longTextFile(textsFolder(), name, locale);
    	LOGGER.fine("trying long text file  '"+textFile.getAbsolutePath()+"'");
        if (!textFile.exists()) {
        	LOGGER.fine("trying short text file  '"+textFile.getAbsolutePath()+"'");
        	textFile = shortTextFile(textsFolder(), name, locale);
        	if (!textFile.exists()) {
            	LOGGER.fine("using textFile without locale '"+textFile+"'");
            	textFile = textFileWithoutLocale(textsFolder(), name);
        	}
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
    	if (locale == null) {
    		locale = Locale.getDefault();
    	}
        File file = longTextFile(textsFolder(), name, locale);
        if (!file.exists()) {
            file.createNewFile();
        }
        IOUtils.saveToFile(text.getBytes("UTF-8"), file);
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
    
	
	// v adresari texts, prvni hodnota je jmeno textu
	public void introspectClassPath() throws IOException {
        Enumeration<URL> resources = this.getClass().getClassLoader().getResources("texts/paths");
        while(resources.hasMoreElements()) {
            URL url = resources.nextElement();
            InputStream stream = url.openStream();
            String content = IOUtils.readAsString(stream, Charset.forName("UTF-8"), true);
            StringReader sReader = new StringReader(content);
            BufferedReader bReader = new BufferedReader(sReader);
            String line = null;
            while((line=bReader.readLine())!=null) {
                StringTokenizer tokenizer = new StringTokenizer(line,",");
                if (tokenizer.hasMoreTokens()) {
                    String name = tokenizer.nextToken();
                    if (tokenizer.hasMoreTokens()) {
                        internalTexts.put(name, readInternalTexts(tokenizer.nextToken()));
                    }
                }
            }
        }
	}

	
	
	private String readInternalTexts(String path) throws IOException {
	    String str = IOUtils.readAsString(this.getClass().getClassLoader().getResourceAsStream(path), Charset.forName("UTF-8"), true);
        return str ;
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


	private File longTextFile(File textsDir, String name, Locale locale) {
		File textFile = new File(textsDir, longLocalizedName(name, locale));
		return textFile;
	}

	private File shortTextFile(File textsDir, String name, Locale locale) {
		File textFile = new File(textsDir, shortLocalizedName(name, locale));
		return textFile;
	}


    public String shortLocalizedName(String name, Locale locale) {
    	return name + "_"+locale.getLanguage();
    }
	
	
    public String longLocalizedName(String name, Locale locale) {
    	return name + "_" + locale.getCountry()+"_"+locale.getLanguage();
    }

}
