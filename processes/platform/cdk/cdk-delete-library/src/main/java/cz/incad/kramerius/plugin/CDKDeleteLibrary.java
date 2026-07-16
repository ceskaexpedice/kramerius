package cz.incad.kramerius.plugin;

import cz.incad.kramerius.utils.XMLUtils;
import cz.inovatika.kramerius.services.iterators.IterationItem;
import cz.inovatika.kramerius.services.iterators.MigrationIterator;
import cz.inovatika.kramerius.services.iterators.solr.SolrCursorIterator;
import cz.inovatika.kramerius.services.iterators.utils.HTTPSolrUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class CDKDeleteLibrary {

    private static final Logger LOGGER = Logger.getLogger(CDKDeleteLibrary.class.getName());
    private static final int UPDATE_BATCH_SIZE = 1000;
    private static final String DEFAULT_ID_FIELD = "compositeId";
    private static final String COLLECTION_FIELD = "cdk.collection";
    //private static final String LEGACY_COLLECTION_FIELD = "cdk.collection";
    private static final List<LicenseFieldPair> LICENSE_FIELDS = Arrays.asList(
            new LicenseFieldPair("licenses", "cdk.licenses"),
            new LicenseFieldPair("contains_licenses", "cdk.contains_licenses"),
            new LicenseFieldPair("licenses_of_ancestors", "cdk.licenses_of_ancestors"));

    private CDKDeleteLibrary() {
    }

    public static void deleteLibrary(String destinationUrl, String library, String filterQuery, int rows, int reportEvery, boolean onlyShowConfiguration) throws Exception {
        String libraryFilter = String.format("(%s:\"%s\")",
                COLLECTION_FIELD, escapeQueryValue(library));
                //LEGACY_COLLECTION_FIELD, escapeQueryValue(library));
        String finalFilter = StringUtils.isBlank(filterQuery) ? libraryFilter : String.format("(%s) AND (%s)", libraryFilter, filterQuery);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            long totalDocuments = countDocuments(client, destinationUrl, finalFilter);
            MigrationIterator iterator = new SolrCursorIterator(
                    destinationUrl,
                    "*:*",
                    finalFilter,
                    "select",
                    "compositeId",
                    "compositeId" + " asc",
                    rows,
                    new String[]{"pid", "compositeId", "cdk.collection", "cdk.leader", "licenses", "contains_licenses", "licenses_of_ancestors", "cdk.*"},
                    null,
                    null);

            List<String> deleteIds = new ArrayList<>();
            List<UpdateItem> updateItems = new ArrayList<>();
            ProgressTracker progressTracker = new ProgressTracker(reportEvery, library, totalDocuments);

            iterator.iterate(client, items -> {
                for (IterationItem item : items) {
                    progressTracker.incrementProcessed();
                    List<String> collections = values(item.getDoc().get(COLLECTION_FIELD));
                    if (collections.size() > 1) {
                        updateItems.add(updateItem(library, item.getId(), item.getDoc(), collections));
                    } else {
                        deleteIds.add(item.getId());
                    }

                    if (deleteIds.size() + updateItems.size() >= UPDATE_BATCH_SIZE) {
                        flush(client, destinationUrl, library, deleteIds, updateItems, onlyShowConfiguration, progressTracker);
                    }
                }
            }, () -> {
                flush(client, destinationUrl, library, deleteIds, updateItems, onlyShowConfiguration, progressTracker);
                progressTracker.maybeReport(true);
            });
        }
    }

    private static void flush(CloseableHttpClient client, String destinationUrl, String library,List<String> deleteIds, List<UpdateItem> updateItems, boolean onlyShowConfiguration, ProgressTracker progressTracker) {
        if (deleteIds.isEmpty() && updateItems.isEmpty()) {
            progressTracker.maybeReport(false);
            return;
        }

        try {
            if (!updateItems.isEmpty()) {
                Document update = updateBatch(library,  updateItems);
                sendOrLog(client, destinationUrl + "/update?commit=true", update, onlyShowConfiguration);
                progressTracker.addUpdated(updateItems.size());
                updateItems.clear();
            }

            if (!deleteIds.isEmpty()) {
                Document delete = deleteBatch(deleteIds);
                sendOrLog(client, destinationUrl + "/update?commit=true", delete, onlyShowConfiguration);
                progressTracker.addDeleted(deleteIds.size());
                deleteIds.clear();
            }
            progressTracker.maybeReport(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static Document updateBatch(String library,  List<UpdateItem> items) throws ParserConfigurationException {
        Document update = XMLUtils.crateDocument("add");
        for (UpdateItem item : items) {
            Element doc = update.createElement("doc");
            update.getDocumentElement().appendChild(doc);

            Element idField = update.createElement("field");
            idField.setAttribute("name", "compositeId");
            idField.setTextContent(item.id);
            doc.appendChild(idField);

            Element collection = update.createElement("field");
            collection.setAttribute("name", COLLECTION_FIELD);
            collection.setAttribute("update", "remove");
            collection.setTextContent(library);
            doc.appendChild(collection);

//            Element legacyCollection = update.createElement("field");
//            legacyCollection.setAttribute("name", LEGACY_COLLECTION_FIELD);
//            legacyCollection.setAttribute("update", "remove");
//            legacyCollection.setTextContent(library);
//            doc.appendChild(legacyCollection);

            for (String fieldName : item.fieldsToClear) {
                Element field = update.createElement("field");
                field.setAttribute("name", fieldName);
                field.setAttribute("update", "set");
                field.setAttribute("null", "true");
                doc.appendChild(field);
            }

            for (LicenseUpdate licenseUpdate : item.licenseUpdates) {
                appendSetFields(update, doc, licenseUpdate.cdkField, licenseUpdate.cdkValues);
                appendSetFields(update, doc, licenseUpdate.sourceField, licenseUpdate.sourceValues);
            }

            if (StringUtils.isNotBlank(item.nextLeader)) {
                Element leader = update.createElement("field");
                leader.setAttribute("name", "cdk.leader");
                leader.setAttribute("update", "set");
                leader.setTextContent(item.nextLeader);
                doc.appendChild(leader);
            }
        }
        return update;
    }

    static UpdateItem updateItem(String library, String id, Map<String, Object> doc, List<String> collections) {
        List<LicenseUpdate> licenseUpdates = new ArrayList<>();
        String prefix = library + "_";
        for (LicenseFieldPair pair : LICENSE_FIELDS) {
            List<String> remainingCdkValues = new ArrayList<>(values(doc.get(pair.cdkField)));
            boolean changed = remainingCdkValues.removeIf(value -> value.startsWith(prefix));
            if (changed) {
                licenseUpdates.add(new LicenseUpdate(pair.sourceField, pair.cdkField, remainingCdkValues, sourceLicenseValues(remainingCdkValues)));
            }
        }

        return new UpdateItem(
                id,
                nextLeader(library, collections, firstValue(doc.get("cdk.leader"))),
                fieldsToClear(library, doc),
                licenseUpdates);
    }

    static Document deleteBatch(List<String> ids) throws ParserConfigurationException {
        Document delete = XMLUtils.crateDocument("delete");
        for (String id : ids) {
            Element idElement = delete.createElement("id");
            idElement.setTextContent(id);
            delete.getDocumentElement().appendChild(idElement);
        }
        return delete;
    }

    private static void appendSetFields(Document update, Element doc, String name, List<String> values) {
        if (values.isEmpty()) {
            Element field = update.createElement("field");
            field.setAttribute("name", name);
            field.setAttribute("update", "set");
            field.setAttribute("null", "true");
            doc.appendChild(field);
        } else {
            for (String value : values) {
                Element field = update.createElement("field");
                field.setAttribute("name", name);
                field.setAttribute("update", "set");
                field.setTextContent(value);
                doc.appendChild(field);
            }
        }
    }

    private static void sendOrLog(CloseableHttpClient client, String destinationUrl, Document batch, boolean onlyShowConfiguration)
            throws TransformerException {
        if (onlyShowConfiguration) {
            StringWriter writer = new StringWriter();
            XMLUtils.print(batch, writer);
            LOGGER.fine(writer.toString());
        } else {
            String response = HTTPSolrUtils.sendToDest(destinationUrl, client, batch);
            LOGGER.fine(response);
        }
    }

    private static List<String> values(Object value) {
        Set<String> values = new LinkedHashSet<>();
        if (value instanceof Collection<?>) {
            for (Object item : (Collection<?>) value) {
                addValue(values, item);
            }
        } else {
            addValue(values, value);
        }
        return new ArrayList<>(values);
    }

    private static String firstValue(Object value) {
        List<String> values = values(value);
        return values.isEmpty() ? null : values.get(0);
    }

    private static List<String> fieldsToClear(String library, Map<String, Object> doc) {
        String suffix = "_" + library;
        List<String> fields = new ArrayList<>();
        for (String fieldName : doc.keySet()) {
            if (fieldName.startsWith("cdk.") && fieldName.endsWith(suffix)) {
                fields.add(fieldName);
            }
        }
        Collections.sort(fields);
        return fields;
    }

    private static List<String> sourceLicenseValues(List<String> cdkValues) {
        Set<String> values = new LinkedHashSet<>();
        for (String cdkValue : cdkValues) {
            int separator = cdkValue.indexOf('_');
            if (separator >= 0 && separator + 1 < cdkValue.length()) {
                values.add(cdkValue.substring(separator + 1));
            }
        }
        return new ArrayList<>(values);
    }

    private static String nextLeader(String library, List<String> collections, String currentLeader) {
        if (!library.equals(currentLeader)) {
            return null;
        }
        for (String collection : collections) {
            if (!library.equals(collection)) {
                return collection;
            }
        }
        return null;
    }

    private static String effectiveIdField(String idField) {
        if (StringUtils.isBlank(idField)) {
            return DEFAULT_ID_FIELD;
        }
        String trimmed = idField.trim();
        if ("pid".equals(trimmed) || DEFAULT_ID_FIELD.equals(trimmed)) {
            return trimmed;
        }
        throw new IllegalArgumentException("idField must be 'pid' or 'compositeId'");
    }

    private static void addValue(Set<String> values, Object value) {
        if (value != null && StringUtils.isNotBlank(value.toString())) {
            values.add(value.toString());
        }
    }

    private static String escapeQueryValue(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static long countDocuments(CloseableHttpClient client, String destinationUrl, String filterQuery) {
        String query = String.format(
                "select?q=*:*&rows=0&fq=%s&wt=xml",
                urlEncode(filterQuery));
        Element response = HTTPSolrUtils.executeQueryApache(client, null, destinationUrl, query);
        Element result = XMLUtils.findElement(response, element -> "result".equals(element.getNodeName()));
        if (result == null) {
            return -1L;
        }
        String numFound = result.getAttribute("numFound");
        if (StringUtils.isBlank(numFound)) {
            return -1L;
        }
        try {
            return Long.parseLong(numFound);
        } catch (NumberFormatException e) {
            LOGGER.warning(String.format("Unable to parse numFound='%s' for delete progress reporting", numFound));
            return -1L;
        }
    }

    private static String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8.name());
        } catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static final class ProgressTracker {
        private final int reportEvery;
        private final String library;
        private final long totalDocuments;
        private final long startedAtMillis = System.currentTimeMillis();
        private long processed;
        private long deleted;
        private long updated;
        private long lastReportedProcessed;

        private ProgressTracker(int reportEvery, String library, long totalDocuments) {
            this.reportEvery = reportEvery > 0 ? reportEvery : 5000;
            this.library = library;
            this.totalDocuments = totalDocuments;
        }

        private void incrementProcessed() {
            processed++;
        }

        private void addDeleted(int count) {
            deleted += count;
        }

        private void addUpdated(int count) {
            updated += count;
        }

        private void maybeReport(boolean force) {
            boolean shouldReport = force || processed - lastReportedProcessed >= reportEvery;
            if (!shouldReport) {
                return;
            }
            lastReportedProcessed = processed;
            long elapsedMillis = System.currentTimeMillis() - startedAtMillis;
            if (totalDocuments > 0) {
                double percent = (processed * 100.0d) / totalDocuments;
                LOGGER.info(String.format(
                        "DeleteLibrary progress for %s: %s/%s | %.1f%% | upd=%s | del=%s | %s",
                        library,
                        formatCount(processed),
                        formatCount(totalDocuments),
                        percent,
                        formatCount(updated),
                        formatCount(deleted),
                        formatElapsed(elapsedMillis)));
            } else {
                LOGGER.info(String.format(
                        "DeleteLibrary progress for %s: %s | upd=%s | del=%s | %s",
                        library,
                        formatCount(processed),
                        formatCount(updated),
                        formatCount(deleted),
                        formatElapsed(elapsedMillis)));
            }
        }

        private String formatCount(long value) {
            String raw = Long.toString(value);
            StringBuilder builder = new StringBuilder(raw.length() + raw.length() / 3);
            int firstGroupLength = raw.length() % 3;
            if (firstGroupLength == 0) {
                firstGroupLength = 3;
            }
            builder.append(raw, 0, firstGroupLength);
            for (int i = firstGroupLength; i < raw.length(); i += 3) {
                builder.append(' ');
                builder.append(raw, i, i + 3);
            }
            return builder.toString();
        }

        private String formatElapsed(long elapsedMillis) {
            long totalSeconds = elapsedMillis / 1000L;
            long hours = totalSeconds / 3600L;
            long minutes = (totalSeconds % 3600L) / 60L;
            long seconds = totalSeconds % 60L;
            return String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, seconds);
        }
    }

    static class UpdateItem {
        private final String id;
        private final String nextLeader;
        private final List<String> fieldsToClear;
        private final List<LicenseUpdate> licenseUpdates;

        private UpdateItem(String id, String nextLeader, List<String> fieldsToClear, List<LicenseUpdate> licenseUpdates) {
            this.id = id;
            this.nextLeader = nextLeader;
            this.fieldsToClear = fieldsToClear;
            this.licenseUpdates = licenseUpdates;
        }
    }

    private static class LicenseFieldPair {
        private final String sourceField;
        private final String cdkField;

        private LicenseFieldPair(String sourceField, String cdkField) {
            this.sourceField = sourceField;
            this.cdkField = cdkField;
        }
    }

    private static class LicenseUpdate {
        private final String sourceField;
        private final String cdkField;
        private final List<String> cdkValues;
        private final List<String> sourceValues;

        private LicenseUpdate(String sourceField, String cdkField, List<String> cdkValues, List<String> sourceValues) {
            this.sourceField = sourceField;
            this.cdkField = cdkField;
            this.cdkValues = cdkValues;
            this.sourceValues = sourceValues;
        }
    }
}
