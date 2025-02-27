package cz.incad.kramerius.utils.conf;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import org.apache.commons.configuration.*;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;

import static cz.incad.kramerius.Constants.WORKING_DIR;

public class KConfiguration {

    public static final String DEFAULT_CONF_LOCATION = "res/configuration.properties";

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(KConfiguration.class.getName());
    public static final String CONFIGURATION = WORKING_DIR + File.separator + "configuration.properties";

    private static String workingDir  = null;
    private static KConfiguration _sharedInstance = null;


    public synchronized static void setWorkingDir(String newWorkingDir){
        if (workingDir != null){
            throw new IllegalStateException("Working dir can be set only once");
        }
        workingDir = newWorkingDir;
    }

    public static KConfiguration getInstance() {
        if (_sharedInstance == null || workingDir == null) {
            setUpInstance();
        }
        return _sharedInstance;
    }

    public static synchronized void setUpInstance() {
        if (_sharedInstance == null) {
            if (workingDir == null) {
                workingDir = WORKING_DIR;
            }
            _sharedInstance = new KConfiguration(workingDir);
        }
    }

    private Configuration allConfigurations;
    private String configDir = null;

    protected KConfiguration(String configDir) {
        this.configDir = configDir;
        try {
            allConfigurations = findAllConfigurations();
        } catch (Exception ex) {
            ex.printStackTrace();
            LOGGER.severe("Can't load configuration");
            throw new RuntimeException(ex.toString());
        }
    }

    private Configuration findAllConfigurations() throws IOException {
        CompositeConfiguration allConfiguration = new CompositeConfiguration();

        try {
            Enumeration<URL> resources = this.getClass().getClassLoader().getResources("res/configuration.properties");
            while (resources.hasMoreElements()) {
                URL nextElement = resources.nextElement();
                PropertiesConfiguration bundled = new PropertiesConfiguration(nextElement);
                String name = disectName(nextElement.getFile());
                if ((name != null) || (bundled.containsKey("_ext_configuration_file_name"))) {
                    String moduleName = name;
                    if (bundled.containsKey("_ext_configuration_file_name")) {
                        LOGGER.info("Replacing configuration file name from '" + name + "' to '" + bundled.getString("_ext_configuration_file_name") + "'");
                        name = bundled.getString("_ext_configuration_file_name");
                    }
                    String path = configDir + File.separator + (name.toLowerCase().endsWith("properties") ? name : name + ".properties");
                    File confFile = new File(path);
                    if (!confFile.exists()) {
                        boolean createdFile = confFile.createNewFile();
                        if (!createdFile)
                            throw new RuntimeException("cannot crete conf file '" + confFile.getAbsolutePath() + "'");
                        FileOutputStream confFos = new FileOutputStream(confFile);
                        try {
                            new Properties().store(confFos, "configuration file for module '" + moduleName + "'");
                        } finally {
                            confFos.close();
                        }
                    }
                    // _ext_configuration_file_name
                    CompositeConfiguration constconf = new CompositeConfiguration();
                    
                    PropertiesConfiguration file = new PropertiesConfiguration();
                    file.setEncoding("UTF-8");
                    file.setFile(confFile);
                    file.load();
                    
                    file.setReloadingStrategy(new FileChangedReloadingStrategy());
                    constconf.addConfiguration(file);
                    constconf.addConfiguration(bundled);
                    allConfiguration.addConfiguration(constconf);
                } else {
                    LOGGER.severe("ommiting '" + nextElement.getFile() + "'");
                }
            }

            /** ENV configuration is not used
            EnvironmentConfiguration environmentConfiguration = new EnvironmentConfiguration();
            for (Iterator it = environmentConfiguration.getKeys(); it.hasNext(); ) {
                String key = (String) it.next();
                String value = environmentConfiguration.getString(key);
                key = key.replaceAll("_", ".");
                key = key.replaceAll("\\.\\.", "__");
                allConfiguration.setProperty(key, value);
            }*/

            return allConfiguration;
        } catch (ConfigurationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return null;
        }

    }

    public String disectName(String path) {
        if (path.contains("!")) {
            String subString = path.substring(0, path.indexOf('!'));
            String[] pathElms = subString.split("/");
            if (pathElms.length > 0) {
                String fileName = pathElms[pathElms.length - 1];
                if (fileName.toLowerCase().endsWith(".jar")) {
                    return fileName.substring(0, fileName.length() - ".jar".length());
                } else {
                    return fileName;
                }
            } else
                throw new IllegalArgumentException("cannot disect name from '" + path + "'");
        } else {
            // ??
            return null;
        }
    }

    public String getFedoraHost() {
        return getProperty("fedoraHost");
    }

    public List<String> getSolrCachedURLS() {
        return Lists.transform(getConfiguration().getList("solr.cache.urls"), Functions.toStringFunction());
    }

    //TODO: remove usage and manually check incompatibilities
    @Deprecated
    public String getSolrHost() {
        return getProperty("solrHost");
    }

    public String getSolrProcessingHost() {
        return getProperty("solrProcessingHost");
    }

    public String getSolrUpdatesHost() {
        return getProperty("solrUpdatesHost");
    }

    public String getSolrReharvestHost() {
        return getProperty("solrReharvestHost");
    }

    public String getSolrSearchHost() {
        return getProperty("solrSearchHost");
    }

    public String getSolrSearchLogin() {
        return getProperty("solrSearchLogin");
    }

    public String getSolrSearchPassword() {
        return getProperty("solrSearchPassword");
    }

    public String getIndexerHost() {
        return getProperty("indexerHost");
    }

    public String getFedoraUser() {
        return getProperty("fedoraUser");
    }

    public String getFedoraPass() {
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

    public int getUnmarshallerPoolSize() {
        return getConfiguration().getInt("unmarshallerPoolSize", 16);
    }

    public String getProperty(String key) {
        return allConfigurations.getString(key);
    }

    public String[] getPropertyList(String key) {
        return allConfigurations.getStringArray(key);
    }


    public String getProperty(String key, String defaultValue) {
        return allConfigurations.getString(key, defaultValue);
    }

    public Configuration getConfiguration() {
        return this.allConfigurations;
    }



    public String getLongRunningProcessDefiniton() {
        return getProperty("longRunningProcessDefinition");
    }

    public String getLRServletURL() {
        return getProperty("lrControlUrl");
    }

    public String[] getSecuredAditionalStreams() {
        return getPropertyList("securedstreams");
    }

    public String getApplicationURL() {
        String applicationUrl = getProperty("applicationUrl");
        return normalizeURL(applicationUrl);
    }

    public String getUsersEditorURL() {
        String url = getProperty("usersEditorUrl", "/rightseditor");
        return url;
    }

    public String getEditorURL() {
        String url = getProperty("editorUrl");
        return normalizeURL(url);
    }

    public List<String> getPatterns() {
        List<String> retval = new ArrayList<String>();
        String property = getProperty("accessPatterns");
        if (property != null) {
            StringTokenizer tokenizer = new StringTokenizer(property, "||");
            while (tokenizer.hasMoreTokens()) {
                retval.add(tokenizer.nextToken());
            }
        }
        return retval;
    }

    public List<String> getLRControllingAddresses() {
        List<String> retval = new ArrayList<String>();
        String property = getProperty("controllingPatterns", "localhost||127.*");
        if (property != null) {
            StringTokenizer tokenizer = new StringTokenizer(property, "||");
            while (tokenizer.hasMoreTokens()) {
                retval.add(tokenizer.nextToken());
            }
        }
        return retval;

    }

    public int getDeepZoomTileSize() {
        return getConfiguration().getInt("deepZoom.tileSize", 256);
    }

    public String getDeepZoomCacheDir() {
        return getConfiguration().getString("deepZoom.cachedir", "${sys:user.home}/.kramerius4/deepZoom");
    }

    public String getRightsCriteriumScriptsDir() {
        return getConfiguration().getString("rights.criterium.scripts", "${sys:user.home}/.kramerius4/rights/criteriums");
    }

    public float getDeepZoomJPEGQuality() {
        return getConfiguration().getFloat("deepZoom.jpegQuality", 0.9f);
    }

    public boolean isDeepZoomEnabled() {
        return getConfiguration().getBoolean("deepZoom.deepZoomEnabled", false);
    }


    public boolean isDeepZoomForPathEnabled(String[] path) {
        Configuration configuration = getConfiguration();
        for (int i = path.length - 1; i >= 0; i--) {
            boolean enabled = configuration.getBoolean("deepZoom." + path[i] + ".deepZoomEnabled", false);
            if (enabled) return true;
        }
        return false;
    }

    public String[] getAPISolrFilter() {
        String[] resArray = getConfiguration().getStringArray("api.solr.filtered");
        return resArray;
    }

    public String[] getAPIPIDReplace() {
        String[] resArray = getConfiguration().getStringArray("api.solr.pidreplace");
        return resArray;
    }


    public String getShibAssocRules() {
        return getConfiguration().getString("security.shib.rules", "${sys:user.home}/.kramerius4/shibrules.txt");
    }

    public String getShibLogout() {
        return getConfiguration().getString("security.shib.logout");
    }

    public String getUrlOfIIPServer() {
        return getConfiguration().getString("UrlOfIIPserver", "");
    }

    public String getFedoraDataFolderInIIPServer() {
        return getConfiguration().getString("fedoraDataFolderOnIIPServer", "");
    }

    public String getDataFolderOnIIPServer() {
        return getConfiguration().getString("dataFolderOnIIPServer", "");
    }


    public String getWebPropertyId() {
        return getConfiguration().getString("googleanalytics.webpropertyid");
    }

    public int getCacheTimeToLiveExpiration() {
        return getConfiguration().getInt("cache.timeToLiveExpiration", 60);
    }


    private static String normalizeURL(String url) {
        if (url != null) {
            url = url.endsWith("/") ? url : url + '/';
        }
        return url;
    }

    /**
     * Find configuration file specified by given configurstion property
     * File can have absolute path or relative to Kramerius configuration home folder (.kramerius4)
     *
     * @param fileProperty
     * @return
     */
    public File findConfigFile(String fileProperty) {
        String fileName = getConfiguration().getString(fileProperty);
        if (fileName == null || "".equals(fileName)) {
            return null;
        }
        File retval = new File(fileName);
        if (!retval.exists()) {
            retval = new File(configDir + File.separator + fileName);
            if (!retval.exists()) {
                LOGGER.warning("Could not find configuration file: " + fileName);
                return null;
            }
        }
        return retval;
    }


}
