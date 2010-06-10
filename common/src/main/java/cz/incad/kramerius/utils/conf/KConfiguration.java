/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.incad.kramerius.utils.conf;

import static cz.incad.kramerius.Constants.*;
	
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import cz.incad.kramerius.utils.IOUtils;


public class KConfiguration {
	
	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(KConfiguration.class.getName());
	public static final String CONFIGURATION = WORKING_DIR+File.separator+"configuration.properties";
	private static KConfiguration _sharedInstance = null;

	private Properties propertiesLoadedFromFile = new Properties();
	private Properties bundledProperties = new Properties();
    
	
	KConfiguration() {
	    try {
	        LOGGER.info(" Loading configuration from jar '"+CONFIGURATION+"'");
	        bundledProperties.load(this.getClass().getResourceAsStream("res/configuration.properties"));
	        File confFile = new File(CONFIGURATION);
	    	if (!confFile.exists()) {
	    		if (confFile.createNewFile()) {
			        LOGGER.info(" Using bundled configuration ");
	    			propertiesLoadedFromFile.putAll(bundledProperties);
	    			FileOutputStream fos = new FileOutputStream(confFile);
	    			propertiesLoadedFromFile.store(fos, "Kramerius 4 configuration file");
	    		} else throw new RuntimeException("cannot create conf file");
	    	} else {
		        LOGGER.info(" Loading configuration from file '"+CONFIGURATION+"'");
		        this.propertiesLoadedFromFile.load(new FileInputStream(CONFIGURATION));
	    	}
	    } catch (Exception ex) {
	    	ex.printStackTrace();
	    	LOGGER.severe("Can't load configuration");
	        throw new RuntimeException(ex.toString());
	    }
	}
	
	KConfiguration(Properties props) {
	    try {
	        LOGGER.info(" Loading configuration from properties ");
	    	this.propertiesLoadedFromFile.putAll(props);
	    } catch (Exception ex) {
	    	LOGGER.severe("Can't load configuration");
	        throw new RuntimeException(ex.toString());
	    }
	}
    
    public String getFedoraHost(){
        return getProperty("fedoraHost");
    }
    public String getSolrHost(){
        return getProperty("solrHost");
    }
    public String getIndexerHost(){
        return getProperty("indexerHost");
    }
    public String getFedoraUser(){
        return getProperty("fedoraUser");
    }
    public String getFedoraPass(){
        return getProperty("fedoraPass");
    }
    
    public String getThumbServletUrl() {
    	return getProperty("thumbUrl");
    }


    public String getScaledHeight() {
    	return getProperty("scaledHeight");
    }
    
    
    public String getJdbcUrl() {
    	return getProperty("jdbcUrl");
    }
    
    
    public String getJdbcUserName() {
    	return getProperty("jdbcUserName");
    }
    
    public String getJdbcUserPass() {
    	return getProperty("jdbcUserPass");
    }
    
    public String getProperty(String key) {
    	if (!this.propertiesLoadedFromFile.contains(key)) {
        	return this.bundledProperties.getProperty(key);
    	} else {
        	return this.propertiesLoadedFromFile.getProperty(key);
    	}
    }

    public String getProperty(String key, String defaultValue) {
        return propertiesLoadedFromFile.getProperty(key, defaultValue);
    }
    
    public synchronized static KConfiguration getKConfiguration() {
    	if (_sharedInstance == null) {
    		_sharedInstance = new KConfiguration();
    	}
    	return _sharedInstance;
    }

    
    public synchronized static KConfiguration getKConfiguration(Properties properties) {
    	if (_sharedInstance == null) {
    		_sharedInstance = new KConfiguration(properties);
    	}
    	return _sharedInstance;
    }
    
	public String getLongRunningProcessDefiniton() {
    	return getProperty("longRunningProcessDefinition");
	}

	public String getLRServletURL() {
    	return getProperty("lrControlUrl");
	}

	public String getApplicationURL() {
    	String applicationUrl = getProperty("applicationUrl");
    	if (applicationUrl != null) {
        	applicationUrl = applicationUrl + (applicationUrl.endsWith("/") ? "":"/");
    	}
    	return applicationUrl;
	}

	public List<String> getPatterns() {
		List<String> retval = new ArrayList<String>();
		String property = getProperty("accessPatterns");
		if (property !=null ) {
			StringTokenizer tokenizer = new StringTokenizer(property, "||");
			while(tokenizer.hasMoreTokens()) {
				retval.add(tokenizer.nextToken());
			}
		}
		return retval;
	}
}

