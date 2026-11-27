package cz.inovatika.kramerius.services;

import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.kramerius.services.iterators.utils.HTTPSolrUtils;
import cz.inovatika.kramerius.services.utils.ResultsUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.w3c.dom.Element;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/** Shared implementation used by the Solr index copy process plugins. */
public final class SolrIndexCopyService {
    private static final Logger LOGGER = Logger.getLogger(SolrIndexCopyService.class.getName());
    private static final int DEFAULT_BATCH_SIZE = 100;
    static final List<String> SEARCH_REMOVE_FIELDS = List.of("_version_", "id_all", "coords.point", "authors.search.exact", "titles.search.exact",
            "licenses.facet", "text_ocr.exact", "physical_locations.search", "own_pid_path.children",
            "own_pid_path.parents", "own_model_path.children", "own_model_path.parents",
            "pid_paths.children", "pid_paths.parents");
    static final List<String> LOGS_REMOVE_FIELDS = List.of("_version_", "all_identifiers", "licenses");
    static final List<String> PROCESSING_REMOVE_FIELDS = List.of("_version_", "dc.title_edge", "dc.title_czech", "title", "title_edge", "source_edge", "ref_path");

    private SolrIndexCopyService() { }

    public static void copyIndex(String sourceUrl, String destinationUrl, String identifier,
                                 String fieldList, String filterQuery, Integer batchSize,
                                 Boolean verifyAfterCopy) throws IOException {
        runCopyIndex(sourceUrl, destinationUrl, identifier, fieldList, filterQuery, List.of(), batchSize, verifyAfterCopy);
    }

    public static void copyIndex(String sourceUrl, String destinationUrl, String identifier,
                                 String fieldList, String filterQuery, Integer batchSize) throws IOException {
        copyIndex(sourceUrl, destinationUrl, identifier, fieldList, filterQuery, batchSize, null);
    }

    public static void copyIndex(String sourceUrl, String destinationUrl, String identifier,
                                 String fieldList, String filterQuery) throws IOException {
        copyIndex(sourceUrl, destinationUrl, identifier, fieldList, filterQuery, null);
    }

    /** Copies from solrSearchHost to the destination URL supplied in the process payload. */
    public static void copySearchIndex(String destinationUrl, Integer batchSize, Boolean verifyAfterCopy) throws IOException {
        KConfiguration configuration = KConfiguration.getInstance();
        boolean useCompositeId = configuration.getConfiguration().getBoolean("solrSearch.useCompositeId", false);
        copyConfiguredIndex("search", configuration.getSolrSearchHost(), destinationUrl,
                searchIdentifier(useCompositeId), SEARCH_REMOVE_FIELDS, batchSize, verifyAfterCopy);
    }

    /** Copies from k7.log.solr.point to the destination URL supplied in the process payload. */
    public static void copyLogsIndex(String destinationUrl, Integer batchSize, Boolean verifyAfterCopy) throws IOException {
        KConfiguration configuration = KConfiguration.getInstance();
        String sourceUrl = configuration.getProperty("k7.log.solr.point", "http://localhost:8983/solr/logs");
        copyConfiguredIndex("logs", sourceUrl, destinationUrl, "id",
                LOGS_REMOVE_FIELDS, batchSize, verifyAfterCopy);
    }

    /** Copies from solrProcessingHost to the destination URL supplied in the process payload. */
    public static void copyProcessingIndex(String destinationUrl, Integer batchSize, Boolean verifyAfterCopy) throws IOException {
        KConfiguration configuration = KConfiguration.getInstance();
        copyConfiguredIndex("processing", configuration.getSolrProcessingHost(), destinationUrl, "pid",
                PROCESSING_REMOVE_FIELDS, batchSize, verifyAfterCopy);
    }

    static String searchIdentifier(boolean useCompositeId) {
        return useCompositeId ? "compositeId" : "pid";
    }

    private static void copyConfiguredIndex(String index, String sourceUrl, String destinationUrl, String identifier,
                                            List<String> removeFields, Integer batchSize,
                                            Boolean verifyAfterCopy) throws IOException {
        if (sourceUrl == null || sourceUrl.isBlank()) {
            throw new IllegalStateException("The configured source URL is missing for the " + index + " index copy");
        }
        if (destinationUrl == null || destinationUrl.isBlank()) {
            throw new IllegalArgumentException("A destinationUrl process payload value is required for the " + index + " index copy");
        }
        runCopyIndex(sourceUrl.trim(), destinationUrl.trim(), identifier, "*", "", removeFields, batchSize, verifyAfterCopy);
    }

    private static void runCopyIndex(String sourceUrl, String destinationUrl, String identifier,
                                     String fieldList, String filterQuery, List<String> removeFields,
                                     Integer requestedBatchSize, Boolean verifyAfterCopy) throws IOException {
        String idField = defaultValue(identifier, "compositeId");
        String fields = defaultValue(fieldList, "*");
        String query = filterQuery == null ? "" : filterQuery.trim();
        String normalizedDestinationUrl = trimTrailingSlashes(destinationUrl.trim());
        int batchSize = effectiveBatchSize(requestedBatchSize);
        String configuration = buildMigrationConfiguration(sourceUrl, normalizedDestinationUrl, idField, fields, query, removeFields, batchSize);

        Path config = Files.createTempFile("solr-index-copy-", ".xml");
        try {
            Files.writeString(config, configuration, StandardCharsets.UTF_8);
            Migration.startMigration(config.toAbsolutePath().toString());
        } finally {
            Files.deleteIfExists(config);
        }

        if (verifyAfterCopy == null || verifyAfterCopy) {
            verifyCopiedDocuments(sourceUrl, normalizedDestinationUrl, idField, query, batchSize);
        } else {
            LOGGER.info("Solr index copy verification skipped by process payload.");
        }
    }

    static String buildMigrationConfiguration(String sourceUrl, String destinationUrl, String idField,
                                              String fields, String query, List<String> removeFields) {
        return buildMigrationConfiguration(sourceUrl, destinationUrl, idField, fields, query, removeFields, DEFAULT_BATCH_SIZE);
    }

    static String buildMigrationConfiguration(String sourceUrl, String destinationUrl, String idField,
                                              String fields, String query, List<String> removeFields, int batchSize) {
        return """
                <migration>
                    <source-name>solr-index-copy</source-name>
                    <name>solr-index-copy</name>
                    <type>full</type>
                    <iteration>
                        <iteratorFactory class="cz.inovatika.kramerius.services.iterators.factories.SolrIteratorFactory"></iteratorFactory>
                        <url>%s</url>
                        <endpoint>select</endpoint>
                        <id>%s</id>
                        <fieldlist>%s</fieldlist>
                        <rows>4000</rows>
                        <type>CURSOR</type>
                        <fquery>%s</fquery>
                    </iteration>
                    <threads>1</threads>
                    <feeder>
                        <feederFactory class="cz.inovatika.kramerius.services.workers.copy.simple.SimpleCopySolrMigrationIndexFeederFactory"></feederFactory>
                        <request>
                            <url>%s</url>
                            <endpoint>select</endpoint>
                            <batchsize>%d</batchsize>
                            <id>%s</id>
                            <fieldlist>%s</fieldlist>
                            <checkUrl>%s</checkUrl>
                            <checkEndpoint>select</checkEndpoint>
                        </request>
                        <destination><url>%s/update</url>
                            <onindex><remove.dest.field>%s</remove.dest.field></onindex>
                        </destination>
                    </feeder>
                </migration>
                """.formatted(xml(sourceUrl), xml(idField), xml(fields), xml(query),
                xml(sourceUrl), batchSize, xml(idField), xml(fields), xml(destinationUrl), xml(destinationUrl), removeFieldXml(removeFields));
    }

    private static int effectiveBatchSize(Integer requestedBatchSize) {
        int batchSize = requestedBatchSize == null ? DEFAULT_BATCH_SIZE : requestedBatchSize;
        if (batchSize < 1) {
            throw new IllegalArgumentException("batchSize must be a positive integer");
        }
        return batchSize;
    }

    private static String trimTrailingSlashes(String url) {
        int end = url.length();
        while (end > 0 && url.charAt(end - 1) == '/') {
            end--;
        }
        return url.substring(0, end);
    }

    private static String removeFieldXml(List<String> removeFields) {
        return String.join("", removeFields.stream().map(name -> "<field name=\"" + xml(name) + "\"/>").toList());
    }

    private static void verifyCopiedDocuments(String sourceUrl, String destinationUrl, String idField,
                                              String filterQuery, int batchSize) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            int start = 0;
            int verified = 0;
            while (true) {
                String sourceQuery = "?q=*:*&" + (filterQuery.isBlank() ? "" : "fq=" + encode(filterQuery) + "&")
                        + "fl=" + encode(idField) + "&sort=" + encode(idField + " asc")
                        + "&rows=" + batchSize + "&start=" + start + "&wt=xml";
                List<String> sourceIds = readIds(client, sourceUrl, sourceQuery, idField);
                if (sourceIds.isEmpty()) break;

                String destinationQuery = "?q=" + encode(idField + ":(" + sourceIds.stream()
                        .map(SolrIndexCopyService::quoteSolrValue)
                        .reduce((left, right) -> left + " OR " + right).orElse("") + ")")
                        + "&fl=" + encode(idField) + "&rows=" + sourceIds.size() + "&wt=xml";
                Set<String> destinationIds = new HashSet<>(readIds(client, destinationUrl, destinationQuery, idField));
                List<String> missing = sourceIds.stream().filter(id -> !destinationIds.contains(id)).toList();
                if (!missing.isEmpty()) {
                    throw new IllegalStateException("Solr index copy verification failed: " + missing.size()
                            + " of " + sourceIds.size() + " documents are missing from destination. First missing ID: " + missing.get(0));
                }
                verified += sourceIds.size();
                start += sourceIds.size();
                if (sourceIds.size() < batchSize) break;
            }
            LOGGER.info("Solr index copy verification passed: " + verified + " source documents found in destination.");
        } catch (Exception e) {
            throw new RuntimeException("Solr index copy verification failed", e);
        }
    }

    private static List<String> readIds(CloseableHttpClient client, String solrUrl, String query, String idField) {
        String endpoint = solrUrl.endsWith("/") ? solrUrl + "select" : solrUrl + "/select";
        Element response = HTTPSolrUtils.executeQueryApache(client, null, endpoint, query);
        Element result = XMLUtils.findElement(response, "result");
        List<String> ids = new ArrayList<>();
        for (Element doc : XMLUtils.getElements(result)) {
            Object id = ResultsUtils.doc(doc).get(idField);
            if (id != null) ids.add(id.toString());
        }
        return ids;
    }

    private static String quoteSolrValue(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String defaultValue(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private static String xml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
