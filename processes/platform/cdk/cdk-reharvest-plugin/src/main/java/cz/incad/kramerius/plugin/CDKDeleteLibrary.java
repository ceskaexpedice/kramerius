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

    private CDKDeleteLibrary() {
    }

    public static void deleteLibrary(String destinationUrl, String library, String filterQuery, int rows, boolean onlyShowConfiguration) throws Exception {
        String libraryFilter = String.format("cdk.collection:\"%s\"", escapeQueryValue(library));
        String finalFilter = StringUtils.isBlank(filterQuery) ? libraryFilter : String.format("(%s) AND (%s)", libraryFilter, filterQuery);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            MigrationIterator iterator = new SolrCursorIterator(
                    destinationUrl,
                    "*:*",
                    finalFilter,
                    "select",
                    "compositeId",
                    "compositeId asc",
                    rows,
                    new String[]{"pid", "cdk.collection"},
                    null,
                    null);

            List<String> deleteIds = new ArrayList<>();
            List<String> updateIds = new ArrayList<>();

            iterator.iterate(client, items -> {
                for (IterationItem item : items) {
                    List<String> collections = values(item.getDoc().get("cdk.collection"));
                    if (collections.size() > 1) {
                        updateIds.add(item.getId());
                    } else {
                        deleteIds.add(item.getId());
                    }

                    if (deleteIds.size() + updateIds.size() >= UPDATE_BATCH_SIZE) {
                        flush(client, destinationUrl, library, deleteIds, updateIds, onlyShowConfiguration);
                    }
                }
            }, () -> flush(client, destinationUrl, library, deleteIds, updateIds, onlyShowConfiguration));
        }
    }

    private static void flush(CloseableHttpClient client, String destinationUrl, String library, List<String> deleteIds, List<String> updateIds, boolean onlyShowConfiguration) {
        if (deleteIds.isEmpty() && updateIds.isEmpty()) {
            return;
        }

        try {
            if (!updateIds.isEmpty()) {
                Document update = updateBatch(library, updateIds);
                sendOrLog(client, destinationUrl + "/update?commit=true", update, onlyShowConfiguration);
                LOGGER.info(String.format("Updated %d documents; removed cdk.collection=%s", updateIds.size(), library));
                updateIds.clear();
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

    private static Document updateBatch(String library, List<String> ids) throws ParserConfigurationException {
        Document update = XMLUtils.crateDocument("add");
        for (String id : ids) {
            Element doc = update.createElement("doc");
            update.getDocumentElement().appendChild(doc);

            Element idField = update.createElement("field");
            idField.setAttribute("name", "compositeId");
            idField.setTextContent(id);
            doc.appendChild(idField);

            Element collection = update.createElement("field");
            collection.setAttribute("name", "cdk.collection");
            collection.setAttribute("update", "remove");
            collection.setTextContent(library);
            doc.appendChild(collection);
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

    private static void addValue(Set<String> values, Object value) {
        if (value != null && StringUtils.isNotBlank(value.toString())) {
            values.add(value.toString());
        }
    }

    private static String escapeQueryValue(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
