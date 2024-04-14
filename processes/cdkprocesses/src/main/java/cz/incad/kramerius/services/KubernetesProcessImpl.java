package cz.incad.kramerius.services;

import cz.incad.kramerius.service.MigrateSolrIndexException;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;


import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Helper class for kubernetes jobs
 */
public class KubernetesProcessImpl {

    public static final String TMSP_URL ="TIMESTAMP_URL";
    public static final String ITERATION_URL = "ITERATION_URL";
    public static final String CHECK_URL = "CHECK_URL";
    public static final String DESTINATION_URL = "DESTINATION_URL";
    public static final String CONFIG_SOURCE = "CONFIG_SOURCE";
    public static final String ONLY_SHOW_CONFIGURATION = "ONLY_SHOW_CONFIGURATION";

    public static final String ITERATION_PREFIX = "ITERATION_";
    public static final String CHECK_PREFIX = "CHECK_";
    public static final String DEST_PREFIX = "DESTINATION_";

    public static final String TMSP_PREFIX = "TIMESTAMP_";


    public static final Logger LOGGER = Logger.getLogger(KubernetesProcessImpl.class.getName());

    public static void main(String[] args) throws IOException, MigrateSolrIndexException, IllegalAccessException, InstantiationException, SAXException, ParserConfigurationException, NoSuchMethodException, ClassNotFoundException {

        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Prague"));
        
        Map<String, String> env = System.getenv();
        if (env.containsKey(CONFIG_SOURCE) || args.length > 0) {
            String configSource = env.containsKey(CONFIG_SOURCE) ?  env.get(CONFIG_SOURCE) : args[0];
            InputStream stream = KubernetesProcessImpl.class.getResourceAsStream(configSource);
            if (configSource.trim().startsWith("file:///")) {
                URL fileUrl = new URL(configSource);
                stream = fileUrl.openStream();
            } 
            if (stream != null) {
                
                Map<String, String> iteration = new HashMap<>();
                prefixVariables(env, iteration, ITERATION_PREFIX);
                if (env.containsKey(ITERATION_URL)) {
                    iteration.put("url", env.get(ITERATION_URL));
                }

                Map<String, String> check = new HashMap<>();
                prefixVariables(env, check, CHECK_PREFIX);
                if (env.containsKey(CHECK_URL)) {
                    check.put("url", env.get(CHECK_URL));
                }

                Map<String, String> destination = new HashMap<>();
                prefixVariables(env, destination, DEST_PREFIX);
                if (env.containsKey(DESTINATION_URL)) {
                    destination.put("url", env.get(DESTINATION_URL));
                }

                Map<String, String> timestamps = new HashMap<>();
                prefixVariables(env, timestamps, TMSP_PREFIX);
                if (env.containsKey(TMSP_PREFIX)) {
                    destination.put("url", env.get(DESTINATION_URL));
                }

                StringTemplate template = new StringTemplate(
                        IOUtils.toString(stream, "UTF-8"), DefaultTemplateLexer.class);

                template.setAttribute("iteration", iteration);
                template.setAttribute("check", check);
                template.setAttribute("destination", destination);
                template.setAttribute("timestamp", timestamps);

                String configuration = template.toString();
                LOGGER.info("Loading configuration "+configuration);

                File tmpFile  = File.createTempFile("temp", "file");
                FileUtils.write(tmpFile, configuration, "UTF-8");


                if (!env.containsKey(ONLY_SHOW_CONFIGURATION)) {
                    ParallelProcessImpl migr = new ParallelProcessImpl();
                    migr.migrate(tmpFile);
                }

            } else {
                LOGGER.severe(String.format("Cannot find resource %s", configSource));
            }
        } else {
            LOGGER.severe("No configuration");
        }
    }

    private static void prefixVariables(Map<String, String> env, Map<String, String> templateMap, String prefix) {
        env.keySet().forEach(key-> {
            if (key.startsWith(prefix)) {
                String name = key.substring(prefix.length()).toLowerCase();
                templateMap.put(name, env.get(key));
            }
        });
    }
}
