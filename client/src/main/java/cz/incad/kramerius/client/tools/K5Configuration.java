package cz.incad.kramerius.client.tools;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import cz.incad.kramerius.utils.conf.KConfiguration;

class K5Configuration {

    public static final Logger LOGGER = Logger.getLogger(K5Configuration.class.getName());
    public static final String K5_CONF_KEY="k5client.properties";
    
    private static K5Configuration _INSTANCE = null;

    private K5Configuration() throws ConfigurationException {
    }
    
    public static String getExtensionsHome() {
        String path = System.getProperty("user.home") + File.separator + ".kramerius4" + 
                File.separator + "k5client" + File.separator + 
                File.separator + "exts" + File.separator;
        return path;
    }

    public synchronized static K5Configuration getK5ConfigurationInstance() throws ConfigurationException {
        if (_INSTANCE == null) {
            _INSTANCE = new K5Configuration();
        }
        return _INSTANCE;
    }
    
}
