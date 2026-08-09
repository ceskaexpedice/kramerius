package cz.inovatika.sync;

import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class SyncUtils {

    private static final Logger LOGGER = Logger.getLogger(SyncUtils.class.getName());

    private SyncUtils() {
    }

    public static SolrDocumentList getById(HttpSolrClient client, List<String> ids, String collection, int batchSize)
            throws SolrServerException, IOException {
        SolrDocumentList list = new SolrDocumentList();
        int getBatch = Math.max(1, batchSize);
        int numberOfBatch = ids.size() / getBatch;
        numberOfBatch = numberOfBatch + (ids.size() % getBatch == 0 ? 0 : 1);
        for (int i = 0; i < numberOfBatch; i++) {
            int from = i * getBatch;
            int to = Math.min((i + 1) * getBatch, ids.size());
            list.addAll(client.getById(collection, ids.subList(from, to)));
        }
        return list;
    }

    public static List<String> distinctModifierValues(Collection<Object> fieldValues) {
        List<Object> data = new ArrayList<>();
        if (fieldValues != null) {
            fieldValues.forEach(obj -> {
                Map<String, Object> map = (Map<String, Object>) obj;
                Iterator<Entry<String, Object>> iterator = map.entrySet().iterator();
                if (iterator.hasNext()) {
                    Object value = iterator.next().getValue();
                    if (value instanceof Collection) {
                        data.addAll((Collection) value);
                    } else {
                        data.add(value);
                    }
                }
            });
        }
        return data.stream().map(Object::toString).collect(Collectors.toList());
    }

    public static List<String> distinctValues(Collection<Object> fieldValues) {
        List<Object> data = new ArrayList<>();
        if (fieldValues != null) {
            data.addAll(fieldValues);
        }
        return data.stream()
                .map(Object::toString)
                .distinct()
                .collect(Collectors.toList());
    }

    public static void atomicOneValSet(SolrInputDocument document, Object value, String fieldName) {
        Object fieldValue = document.getFieldValue(fieldName);
        if (fieldValue == null) {
            Map<String, Object> modifier = new HashMap<>(1);
            modifier.put("set", value);
            document.addField(fieldName, modifier);
        }
    }

    public static void atomicSet(SolrInputDocument document, Object value, String fieldName) {
        if (!addToExistingModifier(document, value, fieldName)) {
            Map<String, Object> modifier = new HashMap<>(1);
            modifier.put("set", value);
            document.addField(fieldName, modifier);
        }
    }

    public static void atomicAddDistinct(SolrInputDocument document, Object value, String fieldName) {
        if (!addToExistingModifier(document, value, fieldName)) {
            Map<String, Object> modifier = new HashMap<>(1);
            modifier.put("add-distinct", value);
            document.addField(fieldName, modifier);
        }
    }

    private static boolean addToExistingModifier(SolrInputDocument document, Object value, String fieldName) {
        Object fieldValue = document.getFieldValue(fieldName);
        if (fieldValue != null) {
            String key = null;
            List<Object> values = new ArrayList<>();
            Map<String, Object> map = (Map<String, Object>) fieldValue;
            Iterator<Entry<String, Object>> iterator = map.entrySet().iterator();
            if (iterator.hasNext()) {
                Entry<String, Object> entry = iterator.next();
                key = entry.getKey();
                Object currentValue = entry.getValue();
                if (currentValue instanceof Collection) {
                    values.addAll((Collection) currentValue);
                    values.add(value);
                } else {
                    values.add(currentValue);
                    values.add(value);
                }
            }

            if (key != null) {
                map.put(key, values);
            }
            return true;
        }
        return false;
    }

    public static File throttle(CloseableHttpClient httpClient, String url) throws IOException, InterruptedException {
        Set<Integer> throttleStatuses = throttleStatuses();
        LOGGER.fine("Throttle statuses: " + throttleStatuses);
        int maxRepetition = 5;

        for (int i = 0; i < maxRepetition; i++) {
            LOGGER.fine("Throttle iteration " + (i + 1) + " of " + maxRepetition + ", url = " + url);
            HttpGet request = new HttpGet(url);
            request.setHeader("Accept", "application/json");

            File resultFile = httpClient.execute(request, response -> {
                int status = response.getCode();
                if (status == 200) {
                    File tmpFile = File.createTempFile("sync", "resp");
                    tmpFile.deleteOnExit();
                    try (FileOutputStream fos = new FileOutputStream(tmpFile)) {
                        IOUtils.copy(response.getEntity().getContent(), fos);
                    }
                    return tmpFile;
                } else if (throttleStatuses.contains(status)) {
                    LOGGER.info("Throttle status (" + status + "); retrying");
                    return null;
                } else {
                    throw new IOException("Unexpected response status: " + status);
                }
            });

            if (resultFile != null) {
                return resultFile;
            }

            int sleep = KConfiguration.getInstance().getConfiguration().getInt("sdnnt.throttle.wait", 30_000);
            LOGGER.info("Server returned throttled status; waiting for " + (sleep / 1000 / 60) + " min");
            Thread.sleep(sleep);
        }
        throw new IllegalStateException("Maximum number of waiting exceeded");
    }

    private static Set<Integer> throttleStatuses() {
        List<Object> configured = KConfiguration.getInstance().getConfiguration()
                .getList("sdnnt.throttle.statuses", Arrays.asList("409"));
        LOGGER.fine("Throttle statuses configured: " + configured);
        return configured.stream()
                .map(Object::toString)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toSet());
    }
}
