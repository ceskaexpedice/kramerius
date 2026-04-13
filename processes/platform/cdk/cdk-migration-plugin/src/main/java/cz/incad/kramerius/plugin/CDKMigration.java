package cz.incad.kramerius.plugin;

import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.services.utils.kubernetes.KubernetesEnvSupport;
import cz.inovatika.kramerius.services.Migration;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
            @ParameterName("iterationFQuery") String iterationFQuery,
            @ParameterName("iterationApiKey") String iterationApiKey,
            @ParameterName("iterationWorkingtime") String iterationWorkingtime,

            @ParameterName("showConfigurationOnly") @IsRequired Boolean showConfigurationOnly
    ) throws MigrateSolrIndexException, IOException, ParserConfigurationException,
            ClassNotFoundException, IllegalAccessException, InstantiationException,
            SAXException, NoSuchMethodException {

        LOGGER.info("migrateMain called with parameters:");
        LOGGER.info(String.format("configSource=%s", configSource));
        LOGGER.info(String.format("destinationUrl=%s", destinationUrl));
        LOGGER.info(String.format("iterationDl=%s", iterationDl));
        LOGGER.info(String.format("iterationId=%s", iterationId));
        LOGGER.info(String.format("iterationUrl=%s", iterationUrl));
        LOGGER.info(String.format("iterationWorkingtime=%s", iterationWorkingtime));
        LOGGER.info(String.format("iterationFQuery=%s", iterationFQuery));
        LOGGER.info(String.format("showConfigurationOnly=%s", showConfigurationOnly));

        /*
            String destinationUrl,
            String iterationDl,
            String iterationId,
            String iterationUrl,
            String iterationFQuery,
            String iterationWorkingtime,
            String  iterationApiKey

         */
        Map<String, String> env = createEnvMapFromPars(destinationUrl,
                iterationDl,
                iterationId,
                iterationUrl,
                iterationFQuery,
                iterationWorkingtime, iterationApiKey);
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

    private static Map<String, String> createEnvMapFromPars(
            String destinationUrl,
            String iterationDl,
            String iterationId,
            String iterationUrl,
            String iterationFQuery,
            String iterationWorkingtime,
            String  iterationApiKey
            ) {
        Map<String, String> envMap = new HashMap<>();
        envMap.put("DESTINATION_URL", destinationUrl);
        envMap.put("ITERATION_DL", iterationDl);
        envMap.put("ITERATION_ID", iterationId);
        envMap.put("ITERATION_URL", iterationUrl);
        if (StringUtils.isNotEmpty(iterationFQuery)) {
            envMap.put("ITERATION_FQUERY", iterationFQuery);
        }
        if (StringUtils.isNotEmpty(iterationWorkingtime)) {
            envMap.put("ITERATION_WORKINGTIME", iterationWorkingtime);
        }
        if (StringUtils.isNotEmpty(iterationApiKey)) {
            envMap.put("ITERATION_APIKEY", iterationApiKey);
        }
        // ITERATION_FQUERY
        // ITERATION_WORKINGTIME=16:00-06:00
        // TODO add all other supported pars
        return envMap;
    }

    public static void main(String[] args) throws MigrateSolrIndexException, IOException, ParserConfigurationException, ClassNotFoundException, IllegalAccessException, InstantiationException, SAXException, NoSuchMethodException {

        String configSource = "/cz/incad/kramerius/services/workers/replicate/configurations/default_k7_v2.xml";

        String destinationUrl = "http://localhost:8983/solr/search_v5";
        String iterationDl = "inovatika";
        String iterationUrl = "https://k7.inovatika.dev/search/api/cdk/v7.0/forward/sync/solr";
        String iterationId = "compositeId";
        String iterationApiKey = "ZByhEIPjoBkMYVym415Zh9rWpdhsBNgzDoem-_QSkK8";
        String iterationWorkingtime = "";
        //String iterationFQuery = "indexed:[2006 TO 2007]";
        String iterationFQuery = "";
        String showConfigurationOnly = "false";

        /*
            @ParameterName("configSource") @IsRequired String configSource,
            @ParameterName("destinationUrl") @IsRequired String destinationUrl,
            @ParameterName("iterationDl") @IsRequired String iterationDl,
            @ParameterName("iterationId") @IsRequired String iterationId,
            @ParameterName("iterationUrl") @IsRequired String iterationUrl,
            @ParameterName("iterationFQuery") String iterationFQuery,
            @ParameterName("iterationApiKey") String iterationApiKey,
            @ParameterName("iterationWorkingtime") String iterationWorkingtime,

            @ParameterName("showConfigurationOnly") @IsRequired Boolean showConfigurationOnly

         */

        CDKMigration.migrateMain(
                configSource,
                destinationUrl,
                iterationDl,
                iterationId,
                iterationUrl,
                iterationFQuery,
                iterationApiKey,
                iterationWorkingtime,
                Boolean.valueOf(showConfigurationOnly));
    }
}
