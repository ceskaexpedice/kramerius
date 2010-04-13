/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.incad.kramerius.utils.conf;

import static cz.incad.kramerius.Constants.*;
	
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;


public class KConfiguration {
	
	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(KConfiguration.class.getName());
	public static final String CONFIGURATION = WORKING_DIR+File.separator+"configuration.properties";
	private static KConfiguration _sharedInstance = null;

	public Properties properties = new Properties();
    
	KConfiguration() {
	    try {
	        LOGGER.info(" Loading configuration from file '"+CONFIGURATION+"'");
	    	this.properties.load(new FileInputStream(CONFIGURATION));
	    } catch (Exception ex) {
	    	LOGGER.severe("Can't load configuration");
	        throw new RuntimeException(ex.toString());
	    }
	}
	
	KConfiguration(Properties props) {
	    try {
	        LOGGER.info(" Loading configuration from properties ");
	    	this.properties.putAll(props);
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

    public String getDJVUServletUrl() {
    	return getProperty("djvuUrl");
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
    	return this.properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
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
}

