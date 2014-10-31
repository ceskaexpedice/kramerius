package cz.incad.kramerius.client.tools;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class K5Configuration {

    public static final Logger LOGGER = Logger.getLogger(K5Configuration.class.getName());
    public static final String K5_CONF_KEY="k5client.properties";
    
    private static K5Configuration _INSTANCE = null;

    private Configuration configuration;
    
    private K5Configuration() throws ConfigurationException {
        this.configuration = initialization();
    }
    
    public static String getExtensionsHome() {
        String path = System.getProperty("user.home") + File.separator + ".kramerius4" + 
                File.separator + "k5client" + File.separator + 
                File.separator + "exts" + File.separator;
        return path;
    }

    private static Configuration initialization() throws ConfigurationException {
        CompositeConfiguration compConf = new CompositeConfiguration();
        String fileName = System.getProperty("user.home")+File.separator+".kramerius4"+File.separator+K5_CONF_KEY;
        File propsFile = new File(fileName);
        if (propsFile.exists() && propsFile.canRead()) {
            compConf.addConfiguration(new PropertiesConfiguration(fileName));
        }
        compConf.addConfiguration(new PropertiesConfiguration(K5Configuration.class.getResource(K5_CONF_KEY)));
        return compConf;
    }

    public synchronized static K5Configuration getK5ConfigurationInstance() throws ConfigurationException {
        if (_INSTANCE == null) {
            _INSTANCE = new K5Configuration();
        }
        return _INSTANCE;
    }
    
    
    public Configuration getConfigurationObject() {
        return configuration;
    }
}
