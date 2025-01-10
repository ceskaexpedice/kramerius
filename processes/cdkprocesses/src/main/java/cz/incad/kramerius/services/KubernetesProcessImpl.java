package cz.incad.kramerius.services;

import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.services.utils.kubernetes.KubernetesEnvSupport;

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
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Helper class for kubernetes jobs
 */
public class KubernetesProcessImpl {

    
    public static final String ONLY_SHOW_CONFIGURATION = "ONLY_SHOW_CONFIGURATION";

    public static final Logger LOGGER = Logger.getLogger(KubernetesProcessImpl.class.getName());

    public static void main(String[] args) throws IOException, MigrateSolrIndexException, IllegalAccessException, InstantiationException, SAXException, ParserConfigurationException, NoSuchMethodException, ClassNotFoundException {

        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Prague"));
        
        Map<String, String> env = System.getenv();
        if (env.containsKey(KubernetesEnvSupport.CONFIG_SOURCE) || args.length > 0) {
            String configSource = env.containsKey(KubernetesEnvSupport.CONFIG_SOURCE) ?  env.get(KubernetesEnvSupport.CONFIG_SOURCE) : args[0];
            InputStream stream = KubernetesProcessImpl.class.getResourceAsStream(configSource);
            if (configSource.trim().startsWith("file:///")) {
                URL fileUrl = new URL(configSource);
                stream = fileUrl.openStream();
            } 
            if (stream != null) {
                
                Map<String, String> iteration = KubernetesEnvSupport.iterationMap(env);
                Map<String, String> check = KubernetesEnvSupport.checkMap(env);
                Map<String, String> destination = KubernetesEnvSupport.destinationMap(env);
                Map<String, String> timestamps = KubernetesEnvSupport.timestampMap(env, destination);

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
}
