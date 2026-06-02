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
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class CDKDeleteLibrary {

    private static final Logger LOGGER = Logger.getLogger(CDKDeleteLibrary.class.getName());
    private static final int UPDATE_BATCH_SIZE = 1000;
    private static final String DEFAULT_ID_FIELD = "compositeId";

    private CDKDeleteLibrary() {
    }

    public static void deleteLibrary(String destinationUrl, String library, String filterQuery, int rows, String idField, boolean onlyShowConfiguration) throws Exception {
        String effectiveIdField = effectiveIdField(idField);
        String libraryFilter = String.format("cdk.collection:\"%s\"", escapeQueryValue(library));
        String finalFilter = StringUtils.isBlank(filterQuery) ? libraryFilter : String.format("(%s) AND (%s)", libraryFilter, filterQuery);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            MigrationIterator iterator = new SolrCursorIterator(
                    destinationUrl,
                    "*:*",
                    finalFilter,
                    "select",
                    effectiveIdField,
                    effectiveIdField + " asc",
                    rows,
                    new String[]{"pid", "compositeId", "cdk.collection", "cdk.leader"},
                    null,
                    null);

            List<String> deleteIds = new ArrayList<>();
            List<UpdateItem> updateItems = new ArrayList<>();

            iterator.iterate(client, items -> {
                for (IterationItem item : items) {
                    List<String> collections = values(item.getDoc().get("cdk.collection"));
                    if (collections.size() > 1) {
                        updateItems.add(new UpdateItem(item.getId(), nextLeader(library, collections, firstValue(item.getDoc().get("cdk.leader")))));
                    } else {
                        deleteIds.add(item.getId());
                    }

                    if (deleteIds.size() + updateItems.size() >= UPDATE_BATCH_SIZE) {
                        flush(client, destinationUrl, library, effectiveIdField, deleteIds, updateItems, onlyShowConfiguration);
                    }
                }
            }, () -> flush(client, destinationUrl, library, effectiveIdField, deleteIds, updateItems, onlyShowConfiguration));
        }
    }

    private static void flush(CloseableHttpClient client, String destinationUrl, String library, String idField, List<String> deleteIds, List<UpdateItem> updateItems, boolean onlyShowConfiguration) {
        if (deleteIds.isEmpty() && updateItems.isEmpty()) {
            return;
        }

        try {
            if (!updateItems.isEmpty()) {
                Document update = updateBatch(library, idField, updateItems);
                sendOrLog(client, destinationUrl + "/update?commit=true", update, onlyShowConfiguration);
                LOGGER.info(String.format("Updated %d documents; removed cdk.collection=%s", updateItems.size(), library));
                updateItems.clear();
            }

            if (!deleteIds.isEmpty()) {
                Document delete = deleteBatch(deleteIds);
                sendOrLog(client, destinationUrl + "/update?commit=true", delete, onlyShowConfiguration);
                LOGGER.info(String.format("Deleted %d documents for cdk.collection=%s", deleteIds.size(), library));
                deleteIds.clear();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Document updateBatch(String library, String idFieldName, List<UpdateItem> items) throws ParserConfigurationException {
        Document update = XMLUtils.crateDocument("add");
        for (UpdateItem item : items) {
            Element doc = update.createElement("doc");
            update.getDocumentElement().appendChild(doc);

            Element idField = update.createElement("field");
            idField.setAttribute("name", idFieldName);
            idField.setTextContent(item.id);
            doc.appendChild(idField);

            Element collection = update.createElement("field");
            collection.setAttribute("name", "cdk.collection");
            collection.setAttribute("update", "remove");
            collection.setTextContent(library);
            doc.appendChild(collection);

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

    private static Document deleteBatch(List<String> ids) throws ParserConfigurationException {
        Document delete = XMLUtils.crateDocument("delete");
        for (String id : ids) {
            Element idElement = delete.createElement("id");
            idElement.setTextContent(id);
            delete.getDocumentElement().appendChild(idElement);
        }
        return delete;
    }

    private static void sendOrLog(CloseableHttpClient client, String destinationUrl, Document batch, boolean onlyShowConfiguration)
            throws TransformerException {
        if (onlyShowConfiguration) {
            StringWriter writer = new StringWriter();
            XMLUtils.print(batch, writer);
            LOGGER.info(writer.toString());
        } else {
            String response = HTTPSolrUtils.sendToDest(destinationUrl, client, batch);
            LOGGER.info(response);
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

    private static class UpdateItem {
        private final String id;
        private final String nextLeader;

        private UpdateItem(String id, String nextLeader) {
            this.id = id;
            this.nextLeader = nextLeader;
        }
    }
}
