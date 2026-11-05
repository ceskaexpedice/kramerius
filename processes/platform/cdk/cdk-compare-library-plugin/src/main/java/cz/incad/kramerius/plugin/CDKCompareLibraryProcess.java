package cz.incad.kramerius.plugin;

import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.kramerius.services.iterators.IterationItem;
import cz.inovatika.kramerius.services.iterators.ApacheHTTPRequestEnricher;
import cz.inovatika.kramerius.services.iterators.solr.SolrCursorIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;
import org.ceskaexpedice.processplatform.api.context.PluginContext;
import org.ceskaexpedice.processplatform.api.context.PluginContextHolder;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.logging.Logger;

public class CDKCompareLibraryProcess {
    private static final Logger LOGGER = Logger.getLogger(CDKCompareLibraryProcess.class.getName());

    @ProcessMethod
    public static void compareLibraryMain(
            @ParameterName("sourceUrl") @IsRequired String sourceUrl,
            @ParameterName("sourceApiKey") String sourceApiKey,
            @ParameterName("destinationUrl") @IsRequired String destinationUrl,
            @ParameterName("library") @IsRequired String library,
            @ParameterName("iterationId") String iterationId,
            @ParameterName("models") String sModels,
            @ParameterName("license") String license,
            @ParameterName("onlyModelsWithMoreInCdk") Boolean onlyModelsWithMoreInCdk,
            @ParameterName("deleteMissing") Boolean deleteMissing,
            @ParameterName("rows") String sRows,
            @ParameterName("reportEvery") String sReportEvery,
            @ParameterName("onlyShowConfiguration") Boolean onlyShowConfiguration
    ) throws Exception {
        int rows = StringUtils.isNumeric(sRows) ? Integer.parseInt(sRows) : 300;
        String idField = StringUtils.defaultIfBlank(iterationId, "pid");
        List<String> rootModels = StringUtils.isBlank(sModels)
                ? Arrays.asList(KConfiguration.getInstance().getPropertyList("fedora.topLevelModels"))
                : Arrays.stream(sModels.split(","))
                        .map(String::trim)
                        .filter(StringUtils::isNotBlank)
                        .distinct()
                        .collect(Collectors.toList());
        String processName = "CDK porovnání – " + library;
        if (StringUtils.isNotBlank(sModels)) {
            processName += " | modely: " + String.join(", ", rootModels);
        }
        if (StringUtils.isNotBlank(license)) {
            processName += " | licence: " + license.trim();
        }
        PluginContext pluginContext = PluginContextHolder.getContext();
        if (pluginContext != null) {
            pluginContext.updateProcessName(processName);
        }
        ApacheHTTPRequestEnricher sourceEnricher = StringUtils.isBlank(sourceApiKey)
                ? ApacheHTTPRequestEnricher.NO_OP
                : request -> request.setHeader("X-API-KEY", sourceApiKey);
        if (StringUtils.isBlank(license) && Boolean.TRUE.equals(onlyModelsWithMoreInCdk)) {
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                Map<String, Long> sourceCounts = loadModelFacets(client, sourceUrl, null, sourceEnricher);
                Map<String, Long> cdkCounts = loadModelFacets(client, destinationUrl,
                        "cdk.collection:\"" + escape(library) + "\"", ApacheHTTPRequestEnricher.NO_OP);
                rootModels = rootModels.stream()
                        .filter(model -> !cdkCounts.getOrDefault(model, 0L)
                                .equals(sourceCounts.getOrDefault(model, 0L)))
                        .collect(Collectors.toList());
                LOGGER.info("Models with different CDK/source facet counts: " + rootModels);
            }
        }
        Map<String, Set<String>> cdkOnlyByModel = new TreeMap<>();
        Map<String, Set<String>> sourceOnlyByModel = new TreeMap<>();
        Map<String, Integer> sourcePidCountsByModel = new TreeMap<>();
        Map<String, Integer> cdkPidCountsByModel = new TreeMap<>();
        boolean compareLicense = StringUtils.isNotBlank(license);
        String selectedLicense = compareLicense ? license.trim() : null;
        for (String model : rootModels) {
            LOGGER.info("Comparing for " + (compareLicense ? "license " + selectedLicense + " and model: " : "model: ") + model);
            Set<String> sourcePids = new LinkedHashSet<>();
            Set<String> cdkPids = new LinkedHashSet<>();
            String fq = "model:" + escape(model);
            if (compareLicense) {
                fq += " AND licenses:" + quoteQueryValue(selectedLicense);
            }

            try (CloseableHttpClient client = HttpClients.createDefault()) {
                createIterator(sourceUrl, fq, rows, idField, sourceEnricher)
                        .iterate(client, items -> addPids(sourcePids, items), () -> { });
                String cdkFq = "cdk.collection:" + quoteQueryValue(library) + " AND model:" + escape(model);
                if (compareLicense) {
                    cdkFq += " AND cdk.licenses:" + quoteQueryValue(library + "_" + selectedLicense);
                }
                createIterator(destinationUrl, cdkFq, rows,
                        "compositeId", ApacheHTTPRequestEnricher.NO_OP)
                        .iterate(client, items -> addPids(cdkPids, items), () -> { });
            }

            sourcePidCountsByModel.put(model, sourcePids.size());
            cdkPidCountsByModel.put(model, cdkPids.size());

            Set<String> cdkOnlyPids = new LinkedHashSet<>(cdkPids);
            cdkOnlyPids.removeAll(sourcePids);
            Set<String> sourceOnlyPids = new LinkedHashSet<>(sourcePids);
            sourceOnlyPids.removeAll(cdkPids);
            cdkOnlyByModel.put(model, cdkOnlyPids);
            sourceOnlyByModel.put(model, sourceOnlyPids);

            if (Boolean.TRUE.equals(deleteMissing) && !cdkOnlyPids.isEmpty()) {
            }

        }
        logComparison(library, cdkOnlyByModel, sourceOnlyByModel,
                sourcePidCountsByModel, cdkPidCountsByModel, selectedLicense);
    }

    private static void logComparison(String library,
                                      Map<String, Set<String>> cdkOnlyByModel,
                                      Map<String, Set<String>> sourceOnlyByModel,
                                      Map<String, Integer> sourcePidCountsByModel,
                                      Map<String, Integer> cdkPidCountsByModel,
                                      String license) {
        StringBuilder report = new StringBuilder()
                .append("\n========== CDK LIBRARY COMPARISON: ").append(library);
        if (license != null) report.append(" | license: ").append(license);
        report.append(" ==========\n");
        int cdkOnlyTotal = cdkOnlyByModel.values().stream().mapToInt(Set::size).sum();
        int sourceOnlyTotal = sourceOnlyByModel.values().stream().mapToInt(Set::size).sum();
        report.append("Models compared: ").append(cdkOnlyByModel.size()).append('\n')
                .append("Only in CDK (missing from source): ").append(cdkOnlyTotal).append(" PID(s)\n")
                .append("Only in source (new in source, not in CDK): ").append(sourceOnlyTotal)
                .append(" PID(s)\n");

        for (String model : cdkOnlyByModel.keySet()) {
            Set<String> cdkOnly = cdkOnlyByModel.get(model);
            Set<String> sourceOnly = sourceOnlyByModel.getOrDefault(model, Set.of());
            report.append("\n[model: ").append(model).append("]\n")
                    .append("  Matched PID counts: source=")
                    .append(sourcePidCountsByModel.getOrDefault(model, 0))
                    .append(", CDK=")
                    .append(cdkPidCountsByModel.getOrDefault(model, 0)).append('\n');
            appendPidSection(report, "ONLY IN CDK - missing from source", cdkOnly);
            appendPidSection(report, "ONLY IN SOURCE - new in source, not in CDK", sourceOnly);
        }
        report.append("===========================================================");
        LOGGER.info(report.toString());
    }

    private static void appendPidSection(StringBuilder report, String label, Set<String> pids) {
        report.append("  ").append(label).append(" (").append(pids.size()).append(" PID(s))\n");
        if (pids.isEmpty()) {
            report.append("    (none)\n");
            return;
        }
        pids.stream().sorted().forEach(pid -> report.append("    ").append(pid)
                .append(" <a href=\"/cdk/object-reharvest?dialog=introspection&amp;pid=")
                .append(pid).append("\" target=\"_blank\" rel=\"noopener noreferrer\">introspection</a>")
                .append(" | <a href=\"/cdk/object-reharvest?dialog=reharvest&amp;pid=")
                .append(pid).append("\" target=\"_blank\" rel=\"noopener noreferrer\">reharvest</a>\n"));
    }

    private static Map<String, Long> loadModelFacets(CloseableHttpClient client, String baseUrl, String filter,
                                                      ApacheHTTPRequestEnricher enricher) throws Exception {
        URIBuilder uriBuilder = new URIBuilder(baseUrl.replaceAll("/+$", "") + "/select")
                .addParameter("q", "*:*" )
                .addParameter("rows", "0")
                .addParameter("wt", "json")
                .addParameter("facet", "true")
                .addParameter("facet.field", "model")
                .addParameter("facet.limit", "-1")
                .addParameter("facet.mincount", "1");
        if (StringUtils.isNotBlank(filter)) uriBuilder.addParameter("fq", filter);
        HttpGet request = new HttpGet(uriBuilder.build());
        enricher.enrich(request);
        return client.execute(request, response -> {
            String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            if (response.getCode() < 200 || response.getCode() >= 300) {
                throw new IOException("Model facet request failed with HTTP " + response.getCode() + ": " + body);
            }
            JSONArray values = new JSONObject(body).getJSONObject("facet_counts")
                    .getJSONObject("facet_fields").getJSONArray("model");
            Map<String, Long> counts = new LinkedHashMap<>();
            for (int i = 0; i + 1 < values.length(); i += 2) {
                counts.put(values.getString(i), values.getLong(i + 1));
            }
            return counts;
        });
    }

    private static SolrCursorIterator createIterator(String url, String filter, int rows, String idField,
                                                     ApacheHTTPRequestEnricher enricher) {
        return new SolrCursorIterator(url, "*:*", filter, "select", idField, idField + " asc", rows,
                new String[]{"pid"}, null, enricher);
    }

    private static void addPids(Set<String> pids, List<IterationItem> items) {
        for (IterationItem item : items) {
            Object pid = item.getDoc().get("pid");
            if (pid != null && StringUtils.isNotBlank(pid.toString())) pids.add(pid.toString());
        }
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String quoteQueryValue(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
