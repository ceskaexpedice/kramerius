package cz.incad.kramerius.plugin;

import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.services.utils.kubernetes.KubernetesEnvSupport;
import cz.inovatika.kramerius.services.Migration;
import cz.inovatika.kramerius.services.config.EffectiveMigrationConfigRenderer;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

/**
 * CDK update migration starter.
 */
public class CDKUpdateMigration {
    private static final Logger LOGGER = Logger.getLogger(CDKUpdateMigration.class.getName());
    private static final String DEFAULT_UPDATE_CONFIG_SOURCE =
            "/cz/incad/kramerius/services/workers/replicate/configurations/default_k7_v2_update.xml";

    // Factory methods that we can override/mock if needed
    static Migration createMigration() throws MigrateSolrIndexException {
        return new Migration();
    }

    static File createTempFile() throws IOException {
        return File.createTempFile("temp", "file");
    }

    @ProcessMethod
    public static void updateMain(
            @ParameterName("destinationUrl") @IsRequired String destinationUrl,
            @ParameterName("iterationDl") @IsRequired String iterationDl,
            @ParameterName("iterationId") @IsRequired String iterationId,
            @ParameterName("iterationUrl") @IsRequired String iterationUrl,
            @ParameterName("iterationFQuery") String iterationFQuery,
            @ParameterName("iterationApiKey") String iterationApiKey,
            @ParameterName("iterationWorkingtime") String iterationWorkingtime,
            @ParameterName("timestampUrl") @IsRequired String timestampUrl,
            @ParameterName("comparingIdentifier") String comparingIdentifier,
            @ParameterName("feederBatchSize") String feederBatchSize,
            @ParameterName("showConfigurationOnly") @IsRequired Boolean showConfigurationOnly,
            @ParameterName("showEffectiveConfigurationOnly") Boolean showEffectiveConfigurationOnly
    ) throws MigrateSolrIndexException, IOException, ParserConfigurationException,
            ClassNotFoundException, IllegalAccessException, InstantiationException,
            SAXException, NoSuchMethodException, TransformerException {

        long startedAtMillis = System.currentTimeMillis();
        LOGGER.info(String.format(
                "CDK update migration start: dst=%s | dl=%s | id=%s | url=%s | fq=%s | work=%s | ts=%s | cmp=%s | batch=%s | cfgOnly=%s | effOnly=%s",
                destinationUrl,
                iterationDl,
                iterationId,
                iterationUrl,
                emptyToDash(iterationFQuery),
                emptyToDash(iterationWorkingtime),
                emptyToDash(timestampUrl),
                emptyToDash(comparingIdentifier),
                emptyToDash(feederBatchSize),
                showConfigurationOnly,
                showEffectiveConfigurationOnly));

        Map<String, String> env = createEnvMapFromPars(destinationUrl,
                iterationDl,
                iterationId,
                iterationUrl,
                iterationFQuery,
                iterationWorkingtime,
                iterationApiKey,
                timestampUrl,
                comparingIdentifier,
                feederBatchSize);

        try (InputStream stream = CDKUpdateMigration.class.getResourceAsStream(DEFAULT_UPDATE_CONFIG_SOURCE)) {
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
                if (Boolean.TRUE.equals(showEffectiveConfigurationOnly)) {
                    try (CloseableHttpClient client = HttpClients.createDefault()) {
                        configuration = EffectiveMigrationConfigRenderer.render(configuration, client);
                    }
                }
                LOGGER.fine("Loading configuration " + configuration);

                File tmpFile = createTempFile();
                FileUtils.write(tmpFile, configuration, "UTF-8");

                if (!showConfigurationOnly && !Boolean.TRUE.equals(showEffectiveConfigurationOnly)) {
                    Migration migr = createMigration();
                    migr.migrate(tmpFile);
                }
                LOGGER.info(String.format("CDK update migration finished in %s", formatElapsed(System.currentTimeMillis() - startedAtMillis)));
            } else {
                LOGGER.severe(String.format("Cannot find resource %s", DEFAULT_UPDATE_CONFIG_SOURCE));
            }
        }
    }

    private static Map<String, String> createEnvMapFromPars(
            String destinationUrl,
            String iterationDl,
            String iterationId,
            String iterationUrl,
            String iterationFQuery,
            String iterationWorkingtime,
            String iterationApiKey,
            String timestampUrl,
            String comparingIdentifier,
            String feederBatchSize
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
        if (StringUtils.isNotEmpty(timestampUrl)) {
            envMap.put("TIMESTAMP_URL", timestampUrl);
        }
        if (StringUtils.isNotEmpty(comparingIdentifier)) {
            envMap.put("COMPARING_IDENTIFIER", comparingIdentifier);
        }
        if (StringUtils.isNotEmpty(feederBatchSize)) {
            envMap.put("FEEDER_BATCH_SIZE", feederBatchSize);
        }
        return envMap;
    }

    private static String emptyToDash(String value) {
        return StringUtils.isBlank(value) ? "-" : value;
    }

    private static String formatElapsed(long elapsedMillis) {
        long totalSeconds = elapsedMillis / 1000L;
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;
        return String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static void main(String[] args) throws MigrateSolrIndexException, IOException, ParserConfigurationException,
            ClassNotFoundException, IllegalAccessException, InstantiationException,
            SAXException, NoSuchMethodException, TransformerException {
        //runUpdateMigrationTestTul();
        runUpdateMigrationTestKKV();
    }

    private static void runUpdateMigrationTestTul() throws MigrateSolrIndexException, IOException, ParserConfigurationException,
            ClassNotFoundException, IllegalAccessException, InstantiationException,
            SAXException, NoSuchMethodException, TransformerException {
        String destinationUrl = "http://localhost:8983/solr/search_cdk_v1";
        String iterationDl = "tul";
        String iterationUrl = "https://kramerius.tul.cz/search/api/cdk/v7.0/forward/sync/solr";
        String iterationId = "pid";
        String iterationApiKey = System.getenv("API_KEY");
        String iterationWorkingtime = "";
        String timestampUrl = "http://localhost:8080/search/api/admin/v7.0/connected";
        String comparingIdentifier = "pid";
        //String iterationFQuery = "pid:\"uuid:31121f1c-649f-4deb-8e9e-501ed5780520\"";
        String iterationFQuery = "";
        String showConfigurationOnly = "false";
        String showEffectiveConfigurationOnly = "false";

        CDKUpdateMigration.updateMain(
                destinationUrl,
                iterationDl,
                iterationId,
                iterationUrl,
                iterationFQuery,
                iterationApiKey,
                iterationWorkingtime,
                timestampUrl,
                comparingIdentifier,
                null,
                Boolean.valueOf(showConfigurationOnly),
                Boolean.valueOf(showEffectiveConfigurationOnly));
    }

    private static void runUpdateMigrationTestKKV() throws MigrateSolrIndexException, IOException, ParserConfigurationException,
            ClassNotFoundException, IllegalAccessException, InstantiationException,
            SAXException, NoSuchMethodException, TransformerException {
        String destinationUrl = "http://localhost:8983/solr/search_cdk_v1";
        String iterationDl = "kkkv";
        String iterationUrl = "https://kramerius.knihovnakv.cz/search/api/cdk/v7.0/forward/sync/solr";
        String iterationId = "pid";
        String iterationApiKey = System.getenv("API_KEY");
        String iterationWorkingtime = "";
        String timestampUrl = "http://localhost:8080/search/api/admin/v7.0/connected";
        String comparingIdentifier = "pid";
        //String iterationFQuery = "pid:\"uuid:31121f1c-649f-4deb-8e9e-501ed5780520\"";
        String iterationFQuery = "";
        String showConfigurationOnly = "false";
        String showEffectiveConfigurationOnly = "false";

        CDKUpdateMigration.updateMain(
                destinationUrl,
                iterationDl,
                iterationId,
                iterationUrl,
                iterationFQuery,
                iterationApiKey,
                iterationWorkingtime,
                timestampUrl,
                comparingIdentifier,
                "10",
                Boolean.valueOf(showConfigurationOnly),
                Boolean.valueOf(showEffectiveConfigurationOnly));
    }
}
