package cz.incad.kramerius.services;

import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.services.utils.kubernetes.KubernetesEnvSupport;
import cz.inovatika.kramerius.services.Migration;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * CDK Migration starter
 */
public class CDKMigration {
    private static final Logger LOGGER = Logger.getLogger(CDKMigration.class.getName());

    // Factory methods that we can override/mock if needed
    static Migration createMigration() throws MigrateSolrIndexException {
        return new Migration();
    }

    static File createTempFile() throws IOException {
        return File.createTempFile("temp", "file");
    }

    @ProcessMethod
    public static void migrateMain(
            @ParameterName("configSource") @IsRequired String configSource,
            @ParameterName("destinationUrl") @IsRequired String destinationUrl,
            @ParameterName("iterationDl") @IsRequired String iterationDl,
            @ParameterName("iterationId") @IsRequired String iterationId,
            @ParameterName("iterationUrl") @IsRequired String iterationUrl,
            @ParameterName("showConfigurationOnly") @IsRequired Boolean showConfigurationOnly
    ) throws MigrateSolrIndexException, IOException, ParserConfigurationException,
            ClassNotFoundException, IllegalAccessException, InstantiationException,
            SAXException, NoSuchMethodException {

        Map<String, String> env = createEnvMapFromPars(destinationUrl, iterationDl, iterationId, iterationUrl);
        InputStream stream = CDKMigration.class.getResourceAsStream(configSource);
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
            LOGGER.info("Loading configuration " + configuration);

            File tmpFile = createTempFile();
            FileUtils.write(tmpFile, configuration, "UTF-8");

            if (!showConfigurationOnly) {
                Migration migr = createMigration();
                migr.migrate(tmpFile);
            }

        } else {
            LOGGER.severe(String.format("Cannot find resource %s", configSource));
        }
    }

    private static Map<String, String> createEnvMapFromPars(String destinationUrl, String iterationDl, String iterationId, String iterationUrl) {
        Map<String, String> envMap = new HashMap<>();
        envMap.put("DESTINATION_URL", destinationUrl);
        envMap.put("ITERATION_DL", iterationDl);
        envMap.put("ITERATION_ID", iterationId);
        envMap.put("ITERATION_URL", iterationUrl);
        // TODO add all other supported pars
        return envMap;
    }

}
