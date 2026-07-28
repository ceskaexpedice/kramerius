package cz.incad.kramerius.plugin;

import cz.incad.kramerius.KubernetesReharvestProcess;
import cz.incad.kramerius.cdk.ChannelUtils;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestItem;
import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.utils.ReharvestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CDKReharvest {

    private static final Logger LOGGER = Logger.getLogger(CDKReharvest.class.getName());

    @ProcessMethod
    public static void reharvestMain(
            @ParameterName("destinationUrl") @IsRequired String destinationUrl,
            @ParameterName("proxyApiUrl") @IsRequired String proxyApiUrl,
            @ParameterName("pid") @IsRequired String pid,
            @ParameterName("ownPidPath") @IsRequired String ownPidPath,
            @ParameterName("iterationUrl") String iterationUrl,
            @ParameterName("iterationType") String iterationType,
            @ParameterName("iterationRows") String iterationRows,
            @ParameterName("iterationBatch") String iterationBatch,
            @ParameterName("rootPid") String rootPid,
            @ParameterName("type") String type,
            @ParameterName("libraries") String libraries,
            @ParameterName("onlyShowConfiguration") Boolean onlyShowConfiguration,
            @ParameterName("maxItemsToDelete") Integer maxItemsToDelete
    ) throws MigrateSolrIndexException, IOException, ParserConfigurationException,
            ClassNotFoundException, IllegalAccessException, InstantiationException,
            SAXException, NoSuchMethodException {

        LOGGER.info("--- Starting method: reharvestMain ---");
        LOGGER.info(String.format("destinationUrl=%s", destinationUrl));
        LOGGER.info(String.format("proxyApiUrl=%s", proxyApiUrl));
        LOGGER.info(String.format("pid=%s", pid));
        LOGGER.info(String.format("ownPidPath=%s", ownPidPath));
        LOGGER.info(String.format("iterationUrl=%s", iterationUrl));
        LOGGER.info(String.format("type=%s", type));
        LOGGER.info(String.format("libraries=%s", libraries));

        ReharvestItem item = new ReharvestItem(UUID.randomUUID().toString(), "CDK platform reharvest", "running", pid, ownPidPath);
        item.setTypeOfReharvest(parseType(type, ReharvestItem.TypeOfReharvset.children));
        item.setRootPid(StringUtils.defaultIfBlank(rootPid, rootFromPath(ownPidPath)));
        item.setLibraries(parseLibraries(libraries));

        Map<String, String> destination = new HashMap<>();
        destination.put("url", destinationUrl);

        Map<String, String> iteration = new HashMap<>();
        iteration.put("url", StringUtils.defaultIfBlank(iterationUrl, destinationUrl));
        iteration.put("type", StringUtils.defaultIfBlank(iterationType, "CURSOR"));
        iteration.put("rows", StringUtils.defaultIfBlank(iterationRows, ReharvestUtils.ITERATION_ROWS_STRING_VALUE));
        iteration.put("batch", StringUtils.defaultIfBlank(iterationBatch, "45"));

        int maxDelete = maxItemsToDelete != null ? maxItemsToDelete : KubernetesReharvestProcess.DEFAULT_MAX_ITEMS_TO_DELETE;
        boolean showOnly = Boolean.TRUE.equals(onlyShowConfiguration);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            Map<String, JSONObject> configurations = KubernetesReharvestProcess.libraryConfigurations(client, proxyApiUrl, item);
            if (configurations.isEmpty()) {
                throw new IllegalStateException("No enabled library configuration found for reharvest");
            }
            ChannelUtils.checkSolrChannelEndpoints(client, configurations);

            List<Pair<String, String>> pids = ReharvestUtils.findPidByType(iteration, client, item, maxDelete);
            String deleteResult = ReharvestUtils.deleteAllGivenPids(client, destination, pids, showOnly);
            LOGGER.info(String.format("Deleted pids results %s", deleteResult));

            ReharvestUtils.reharvestPIDFromGivenCollections(
                    pid, configurations, Boolean.toString(showOnly), destination, iteration, item);
        }
    }

    private static ReharvestItem.TypeOfReharvset parseType(String type, ReharvestItem.TypeOfReharvset defaultType) {
        return StringUtils.isBlank(type) ? defaultType : ReharvestItem.TypeOfReharvset.valueOf(type);
    }

    private static List<String> parseLibraries(String libraries) {
        if (StringUtils.isBlank(libraries)) {
            return List.of();
        }
        return Arrays.stream(libraries.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
    }

    private static String rootFromPath(String ownPidPath) {
        if (StringUtils.isBlank(ownPidPath)) {
            return null;
        }
        int slash = ownPidPath.indexOf('/');
        return slash >= 0 ? ownPidPath.substring(0, slash) : ownPidPath;
    }

    public static void main(String[] args) throws MigrateSolrIndexException, IOException, ParserConfigurationException,
            ClassNotFoundException, IllegalAccessException, InstantiationException,
            SAXException, NoSuchMethodException {
        runReharvestOnlyPidTest();
        //runReharvestChildrenTest();
        //runReharvestDeletePidTest();
    }

    private static void runReharvestOnlyPidTest() throws MigrateSolrIndexException, IOException, ParserConfigurationException,
            ClassNotFoundException, IllegalAccessException, InstantiationException,
            SAXException, NoSuchMethodException {
        String destinationUrl = "http://localhost:8983/solr/search_cdk_v1";
        String proxyApiUrl = "http://localhost:8080/search/api/admin/v7.0/connected";
        String rootPid = "uuid:c5e400b0-b1ab-11eb-a22e-5ef3fc9bb22f";
        String pid = "uuid:d4d42790-84ea-11ec-b436-5ef3fc9bb22f";
        String ownPidPath = rootPid + "/" + pid;
        String iterationUrl = destinationUrl;
        String iterationType = "CURSOR";
        String iterationRows = "4000";
        String iterationBatch = "45";
        String type = ReharvestItem.TypeOfReharvset.only_pid.name();
        String libraries = "cbvk";
        Boolean onlyShowConfiguration = false;
        Integer maxItemsToDelete = 300000;

        CDKReharvest.reharvestMain(
                destinationUrl,
                proxyApiUrl,
                pid,
                ownPidPath,
                iterationUrl,
                iterationType,
                iterationRows,
                iterationBatch,
                rootPid,
                type,
                libraries,
                onlyShowConfiguration,
                maxItemsToDelete);
    }

    private static void runReharvestChildrenTest() throws MigrateSolrIndexException, IOException, ParserConfigurationException,
            ClassNotFoundException, IllegalAccessException, InstantiationException,
            SAXException, NoSuchMethodException {
        String destinationUrl = "http://localhost:8983/solr/search_cdk_v1";
        String proxyApiUrl = "http://localhost:8080/search/api/admin/v7.0/connected";
        String rootPid = "uuid:c5e400b0-b1ab-11eb-a22e-5ef3fc9bb22f";
        String pid = "uuid:d4d42790-84ea-11ec-b436-5ef3fc9bb22f";
        String ownPidPath = rootPid + "/" + pid;
        String iterationUrl = destinationUrl;
        String iterationType = "CURSOR";
        String iterationRows = "4000";
        String iterationBatch = "45";
        String type = ReharvestItem.TypeOfReharvset.children.name();
        String libraries = "cbvk";
        Boolean onlyShowConfiguration = false;
        Integer maxItemsToDelete = 300000;

        CDKReharvest.reharvestMain(
                destinationUrl,
                proxyApiUrl,
                pid,
                ownPidPath,
                iterationUrl,
                iterationType,
                iterationRows,
                iterationBatch,
                rootPid,
                type,
                libraries,
                onlyShowConfiguration,
                maxItemsToDelete);
    }

    private static void runReharvestDeletePidTest() throws MigrateSolrIndexException, IOException, ParserConfigurationException,
            ClassNotFoundException, IllegalAccessException, InstantiationException,
            SAXException, NoSuchMethodException {
        String destinationUrl = "http://localhost:8983/solr/search_cdk_v1";
        String proxyApiUrl = "http://localhost:8080/search/api/admin/v7.0/connected";
        String rootPid = "uuid:c5e400b0-b1ab-11eb-a22e-5ef3fc9bb22f";
        String pid = "uuid:d4d42790-84ea-11ec-b436-5ef3fc9bb22f";
        String ownPidPath = rootPid + "/" + pid;
        String iterationUrl = destinationUrl;
        String iterationType = "CURSOR";
        String iterationRows = "4000";
        String iterationBatch = "45";
        String type = ReharvestItem.TypeOfReharvset.delete_pid.name();
        String libraries = "cbvk";
        Boolean onlyShowConfiguration = false;
        Integer maxItemsToDelete = 300000;

        CDKReharvest.reharvestMain(
                destinationUrl,
                proxyApiUrl,
                pid,
                ownPidPath,
                iterationUrl,
                iterationType,
                iterationRows,
                iterationBatch,
                rootPid,
                type,
                libraries,
                onlyShowConfiguration,
                maxItemsToDelete);
    }
}
