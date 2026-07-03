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
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
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
            @ParameterName("timestampUrl") String timestampUrl,
            @ParameterName("comparingIdentifier") String comparingIdentifier,
            @ParameterName("feederBatchSize") String feederBatchSize,

            @ParameterName("showConfigurationOnly") @IsRequired Boolean showConfigurationOnly,
            @ParameterName("showEffectiveConfigurationOnly") Boolean showEffectiveConfigurationOnly
    ) throws MigrateSolrIndexException, IOException, ParserConfigurationException,
            ClassNotFoundException, IllegalAccessException, InstantiationException,
            SAXException, NoSuchMethodException, TransformerException {

        long startedAtMillis = System.currentTimeMillis();
        LOGGER.info(String.format(
                "CDK migration start: src=%s | dst=%s | dl=%s | id=%s | url=%s | fq=%s | work=%s | ts=%s | cmp=%s | batch=%s | cfgOnly=%s | effOnly=%s",
                configSource,
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
        InputStream stream = CDKMigration.class.getResourceAsStream(configSource);
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
            LOGGER.info(String.format("CDK migration finished in %s", formatElapsed(System.currentTimeMillis() - startedAtMillis)));
       } else {
            LOGGER.severe(String.format("Cannot find resource %s", configSource));
       }
    }

    public static void migrateMain(
            String configSource,
            String destinationUrl,
            String iterationDl,
            String iterationId,
            String iterationUrl,
            String iterationFQuery,
            String iterationApiKey,
            String iterationWorkingtime,
            Boolean showConfigurationOnly
    ) throws MigrateSolrIndexException, IOException, ParserConfigurationException,
            ClassNotFoundException, IllegalAccessException, InstantiationException,
            SAXException, NoSuchMethodException, TransformerException {
        migrateMain(
                configSource,
                destinationUrl,
                iterationDl,
                iterationId,
                iterationUrl,
                iterationFQuery,
                iterationApiKey,
                iterationWorkingtime,
                null,
                null,
                null,
                showConfigurationOnly,
                false);
    }

    public static void migrateMain(
            String configSource,
            String destinationUrl,
            String iterationDl,
            String iterationId,
            String iterationUrl,
            String iterationFQuery,
            String iterationApiKey,
            String iterationWorkingtime,
            String timestampUrl,
            String comparingIdentifier,
            Boolean showConfigurationOnly
    ) throws MigrateSolrIndexException, IOException, ParserConfigurationException,
            ClassNotFoundException, IllegalAccessException, InstantiationException,
            SAXException, NoSuchMethodException, TransformerException {
        migrateMain(
                configSource,
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
                showConfigurationOnly,
                false);
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

}
