package cz.inovatika.licenses;

import cz.incad.kramerius.security.licenses.impl.embedded.cz.CzechEmbeddedLicenses;
import cz.incad.kramerius.processes.utils.ProcessUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.kramerius.services.iterators.MigrationIterator;
import cz.inovatika.kramerius.services.iterators.MigrationIteratorFactory;
import cz.inovatika.kramerius.services.iterators.config.SolrIteratorConfig;
import cz.inovatika.kramerius.services.iterators.factories.SolrIteratorFactory;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.ceskaexpedice.processplatform.api.context.PluginContext;
import org.ceskaexpedice.processplatform.api.context.PluginContextHolder;
import org.ceskaexpedice.processplatform.common.model.ScheduleSubProcess;
import org.json.JSONException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Transforming accesibility flag to license
 * <ul>
 *  <li> policy:private -&gt; onsite </li>
 *  <li> policy:public -&gt; public </li>
 * </ul>
 *
 * @author happy
 */
public class FlagToLicenseProcess {

    public static final Logger LOGGER = Logger.getLogger(FlagToLicenseProcess.class.getName());

    // Default configuration prefix key
    public static final String PROCESSES_CONF_KEY = "processess.flag_to_license.";


    public static final int DEFAULT_BATCH_SIZE = KConfiguration.getInstance().getConfiguration().getInt("flagToLicense", 1000);
    public static final int DEFAULT_PID_QUERY_BATCH_SIZE = KConfiguration.getInstance().getConfiguration()
            .getInt(PROCESSES_CONF_KEY + "pidQueryBatchSize", 100);

    public static final List<String> DEFAULT_MODELS = Arrays.asList(
            "monograph",
            "monographunit",
            "periodicalvolume",
            "manuscript",
            "soundrecording",
            "convolute",
            "map",
            "sheetmusic",
            "graphic",
            "archive",
            "convolute");

    public enum Scope {
        OBJECT, TREE
    }


    /**
     * Iterates over search index and add public or onsite licenses according accessibility flag
     *
     * @throws BrokenBarrierException
     * @throws InterruptedException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static void main() throws Exception {
        process(null);
    }

    public static void mainForPids(String target) throws Exception {
        List<String> pids = ProcessUtils.extractPids(target).stream()
                .map(String::trim)
                .filter(pid -> !pid.isEmpty())
                .distinct()
                .collect(Collectors.toList());
        process(pids);
    }

    private static void process(List<String> filteredPids) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            List<String> models = KConfiguration.getInstance().getConfiguration().getList(PROCESSES_CONF_KEY + "models", DEFAULT_MODELS).stream().map(Objects::toString).collect(Collectors.toList());

            // don't use periodical
            models.remove("periodical");

            String query = String.format("model:(%s) AND accessibility:*", models.stream().collect(Collectors.joining(" OR ")));
            //String encodedQ = URLEncoder.encode(query, "UTF-8");

            //Client client = Client.create();


            List<String> publicPids = new ArrayList<>();
            List<String> privatePids = new ArrayList<>();

                boolean compositeId = KConfiguration.getInstance().getConfiguration().getBoolean("solrSearch.useCompositeId", false);
            LOGGER.info("Solr cloud:"+compositeId);
            for (String filterQuery : buildFilterQueries(query, filteredPids)) {
                SolrIteratorConfig config =
                        new SolrIteratorConfig.Builder(KConfiguration.getInstance().getSolrSearchHost(), "pid")
                                .fieldList("pid,accessibility,licenses")
                                .sort(compositeId ? "compositeId asc" :   "pid asc")
                                .endpoint("select")
                                .filterQuery(filterQuery)
                                .factoryClz(SolrIteratorFactory.class.getName())
                                .build();
                MigrationIteratorFactory iteratorFactory = MigrationIteratorFactory.create(config);
                MigrationIterator migrationIterator = iteratorFactory.createMigrationIterator(config, httpClient);
                migrationIterator.iterate(httpClient, (itdocs) -> {
                    itdocs.forEach(doc -> {
                        String pid = doc.getPid();

                        String license = targetLicenseFor(doc.getDoc().get("accessibility"), doc.getDoc().get("licenses"));
                        if (CzechEmbeddedLicenses.PUBLIC_LICENSE.getName().equals(license)) {
                            publicPids.add(pid);
                        } else if (CzechEmbeddedLicenses.ONSITE_LICENSE.getName().equals(license)) {
                            privatePids.add(pid);
                        }
                    });
                }, () -> {

                });
            }

            LOGGER.info(String.format("To public license: %d", publicPids.size()));
            LOGGER.info(String.format("To onsite license: %d", privatePids.size()));

            // public batches
            scheduleSetLicenses(publicPids, CzechEmbeddedLicenses.PUBLIC_LICENSE.getName());

            // private batches
            scheduleSetLicenses(privatePids, CzechEmbeddedLicenses.ONSITE_LICENSE.getName());

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }

    }

    static List<String> buildFilterQueries(String baseQuery, List<String> filteredPids) {
        return buildFilterQueries(baseQuery, filteredPids, DEFAULT_PID_QUERY_BATCH_SIZE);
    }

    static List<String> buildFilterQueries(String baseQuery, List<String> filteredPids, int batchSize) {
        if (filteredPids == null) {
            return Collections.singletonList(baseQuery);
        }

        int safeBatchSize = Math.max(1, batchSize);
        List<String> queries = new ArrayList<>();
        for (int i = 0; i < filteredPids.size(); i += safeBatchSize) {
            int end = Math.min(i + safeBatchSize, filteredPids.size());
            String pidQuery = filteredPids.subList(i, end).stream()
                    .map(FlagToLicenseProcess::pidExactQuery)
                    .collect(Collectors.joining(" OR "));
            queries.add(String.format("%s AND (%s)", baseQuery, pidQuery));
        }
        return queries;
    }

    static String pidExactQuery(String pid) {
        return "pid:\"" + escapeSolr(pid) + "\"";
    }

    private static String escapeSolr(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    static String targetLicenseFor(Object accessibility, Object licenses) {
        if (accessibility == null || hasTargetLicense(licenses)) {
            return null;
        }
        if ("public".equals(accessibility.toString())) {
            return CzechEmbeddedLicenses.PUBLIC_LICENSE.getName();
        }
        if ("private".equals(accessibility.toString())) {
            return CzechEmbeddedLicenses.ONSITE_LICENSE.getName();
        }
        return null;
    }

    static boolean hasTargetLicense(Object licenses) {
        if (licenses == null) {
            return false;
        }
        if (licenses instanceof Collection) {
            Collection<?> values = (Collection<?>) licenses;
            return values.contains(CzechEmbeddedLicenses.PUBLIC_LICENSE.getName())
                    || values.contains(CzechEmbeddedLicenses.ONSITE_LICENSE.getName());
        }
        String license = licenses.toString();
        return license.equals(CzechEmbeddedLicenses.PUBLIC_LICENSE.getName())
                || license.equals(CzechEmbeddedLicenses.ONSITE_LICENSE.getName());
    }

    private static void scheduleSetLicenses(List<String> pids, String lic) throws JSONException {
        int iterations = pids.size() / DEFAULT_BATCH_SIZE + (pids.size() % DEFAULT_BATCH_SIZE == 0 ? 0 : 1);

        for (int i = 0; i < iterations; i++) {
            int start = i * DEFAULT_BATCH_SIZE;
            int end = Math.min((i + 1) * DEFAULT_BATCH_SIZE, pids.size());

            List<String> batch = pids.subList(start, end);
            String pidlistArgument = "pidlist:" + String.join(";", batch);

            Map<String, String> payload = new HashMap<>();
            payload.put("license", lic);
            payload.put("pid", pidlistArgument);

            ScheduleSubProcess subProcess = new ScheduleSubProcess("add_license", payload);

            PluginContext pluginContext = PluginContextHolder.getContext();
            pluginContext.scheduleSubProcess(subProcess);
        }
    }

    public static void main(String[] args) throws Exception {
        main();
    }

}
