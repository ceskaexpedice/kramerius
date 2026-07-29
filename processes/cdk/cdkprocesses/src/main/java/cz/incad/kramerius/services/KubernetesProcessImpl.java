package cz.incad.kramerius.services;

import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.services.utils.kubernetes.KubernetesEnvSupport;

import cz.inovatika.kramerius.services.Migration;
import cz.inovatika.kramerius.services.config.EffectiveMigrationConfigRenderer;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.xml.sax.SAXException;


import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Helper class for kubernetes jobs
 */
public class KubernetesProcessImpl {

    
    public static final String ONLY_SHOW_CONFIGURATION = "ONLY_SHOW_CONFIGURATION";
    public static final String ONLY_SHOW_EFFECTIVE_CONFIGURATION = "ONLY_SHOW_EFFECTIVE_CONFIGURATION";

    public static final Logger LOGGER = Logger.getLogger(KubernetesProcessImpl.class.getName());

    public static void main(String[] args) throws IOException, MigrateSolrIndexException, IllegalAccessException, InstantiationException, SAXException, ParserConfigurationException, NoSuchMethodException, ClassNotFoundException, TransformerException {
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
                Map<String, String> timestamps = KubernetesEnvSupport.timestampMap(env, iteration);
                Map<String, String> comparing = KubernetesEnvSupport.comparingMap(env);
                Map<String, String> feeder = KubernetesEnvSupport.feederMap(env);
                Map<String, String> proxy = KubernetesEnvSupport.proxyMap(env);
                Map<String, String> reharvest = KubernetesEnvSupport.reharvestMap(env);

                StringTemplate template = new StringTemplate(
                        IOUtils.toString(stream, "UTF-8"), DefaultTemplateLexer.class);

                template.setAttribute("iteration", iteration);
                template.setAttribute("check", check);
                template.setAttribute("destination", destination);
                template.setAttribute("timestamp", timestamps);
                template.setAttribute("comparing", comparing);
                template.setAttribute("feeder", feeder);
                template.setAttribute("proxy", proxy);
                template.setAttribute("reharvest", reharvest);

                String configuration = template.toString();
                if (env.containsKey(ONLY_SHOW_EFFECTIVE_CONFIGURATION)) {
                    try (CloseableHttpClient client = HttpClients.createDefault()) {
                        configuration = EffectiveMigrationConfigRenderer.render(configuration, client);
                    }
                }
                LOGGER.info("Loading configuration "+configuration);

                File tmpFile  = File.createTempFile("temp", "file");
                FileUtils.write(tmpFile, configuration, "UTF-8");


                if (!env.containsKey(ONLY_SHOW_CONFIGURATION) && !env.containsKey(ONLY_SHOW_EFFECTIVE_CONFIGURATION)) {
                    Migration migr = new Migration();
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
