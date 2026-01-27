package cz.inovatika.dilia.logs;

import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

/**
 * UpdateLogs
 * @author ppodsednik
 */
public class UpdateLogs {
    public static final Logger LOGGER = Logger.getLogger(UpdateLogs.class.getName());

    private static final String LOGS_HOST_DEFAULT = "http://localhost:8983/solr/logs/";
    private static final String SEARCH_HOST_DEFAULT = "http://localhost:8983/solr/search/";

    private static final int PAGE_SIZE = 100;
    private static final int UPDATE_BATCH_SIZE = 100;

    private String logsEndpoint;
    private String searchEndpoint;

    public UpdateLogs(){
        this.logsEndpoint = logsEndpoint();
        this.searchEndpoint = searchEndpoint();
    }

    private void run() {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String cursorMark = "*";
            List<UpdateEntry> updateBuffer = new ArrayList<>();
            int totalDocsProcessed = 0;
            int totalDocsUpdated = 0;
            int numFound = -1; // total docs missing authors or langs

            while (true) {
                // --- 1. Fetch logs page where authors or langs missing ---
                String rawQuery = "-authors:[* TO *] OR -langs:[* TO *]";
                String encodedQuery = URLEncoder.encode(rawQuery, StandardCharsets.UTF_8);
                String sortValue = "date desc,id asc";
                String encodedSort = URLEncoder.encode(sortValue, StandardCharsets.UTF_8);
                String encodedCursorMark = URLEncoder.encode(cursorMark, StandardCharsets.UTF_8);
                String query = "q=" + encodedQuery
                        + "&rows=" + PAGE_SIZE
                        + "&fl=id,pid,authors,langs"
                        + "&sort=" + encodedSort
                        + "&cursorMark=" + encodedCursorMark
                        + "&trackTotalHits=true"
                        + "&wt=json";
                JSONArray docs;
                String nextCursorMark;
                try (InputStream is = requestSelectReturningStream(client, LOGS_HOST_DEFAULT, query)) {
                    String jsonText = IOUtils.toString(is, StandardCharsets.UTF_8);
                    JSONObject root = new JSONObject(jsonText);
                    JSONObject response = root.getJSONObject("response");
                    if (numFound < 0) { // first page
                        numFound = response.getInt("numFound");
                        LOGGER.info("Total logs docs to process (missing authors or langs): " + numFound);
                    }
                    docs = response.getJSONArray("docs");
                    nextCursorMark = root.getString("nextCursorMark");
                }

                if (docs.length() == 0 || cursorMark.equals(nextCursorMark)) {
                    break;
                }

                totalDocsProcessed += docs.length();
                double percentComplete = (numFound > 0) ? (totalDocsProcessed / (double) numFound) * 100 : -1;
                LOGGER.info(String.format("Processing page: %d logs docs, total processed: %d, %.2f%% complete%n",
                        docs.length(), totalDocsProcessed, percentComplete));

                // --- 2️. Collect missing pids ---
                Set<String> pids = new HashSet<>();
                for (int i = 0; i < docs.length(); i++) {
                    JSONObject doc = docs.getJSONObject(i);
                    if (!doc.has("id") || !doc.has("pid")){
                        continue;
                    }
                    boolean needsAuthors = !doc.has("authors");
                    boolean needsLangs   = !doc.has("langs");
                    if (needsAuthors || needsLangs) {
                        pids.add(doc.getString("pid"));
                    }
                }

                // --- 3️. Batch fetch from search index ---
                Map<String, SearchFields> searchMap = fetchSearchFields(client, pids);

                // --- 4️. Enrich logs documents ---
                int pageUpdates = 0;
                for (int i = 0; i < docs.length(); i++) {
                    JSONObject doc = docs.getJSONObject(i);
                    String pid = doc.getString("pid");
                    String id = doc.getString("id");

                    boolean needsAuthors = !doc.has("authors");
                    boolean needsLangs   = !doc.has("langs");
                    if (!needsAuthors && !needsLangs){
                        continue;
                    }

                    SearchFields fields = searchMap.get(pid);
                    if (fields == null) continue;

                    JSONObject updateFields = new JSONObject();
                    if (needsAuthors && fields.authors != null){
                        updateFields.put("authors", fields.authors);
                    }
                    if (needsLangs   && fields.langs   != null){
                        updateFields.put("langs", fields.langs);
                    }

                    if (updateFields.length() > 0) {
                        updateBuffer.add(new UpdateEntry(id, updateFields));
                        pageUpdates++;
                        totalDocsUpdated++;
                    }

                    if (updateBuffer.size() >= UPDATE_BATCH_SIZE) {
                        flushUpdates(client, updateBuffer);
                        LOGGER.info(String.format("Flushed batch of %d updates, total updated: %d%n", updateBuffer.size(), totalDocsUpdated));
                        updateBuffer.clear();
                    }
                }

                LOGGER.info(String.format("Page processed. Docs enriched in this page: %d%n", pageUpdates));

                cursorMark = nextCursorMark;
            }

            // --- Flush remaining updates ---
            if (!updateBuffer.isEmpty()) {
                flushUpdates(client, updateBuffer);
                LOGGER.info(String.format("Flushed final batch of %d updates, total updated: %d%n", updateBuffer.size(), totalDocsUpdated));
            }

            LOGGER.info("UpdateLogs finished successfully.");
            LOGGER.info("Total logs docs processed: " + totalDocsProcessed);
            LOGGER.info("Total logs docs updated: " + totalDocsUpdated);
        } catch (Exception e) {
            LOGGER.severe("UpdateLogs failed with exception:");
            throw new RuntimeException(e);
        }
    }

    private static InputStream requestSelectReturningStream(CloseableHttpClient client, String solrHost, String query) throws IOException {
        String host = solrHost.endsWith("/") ? solrHost : solrHost + "/";
        String url = host + "select?" + query + "&wt=json";
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = client.execute(httpGet);
        if (response.getCode() == 200) {
            // Caller must close this stream
            return response.getEntity().getContent();
        } else {
            String err = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            throw new RuntimeException("Solr SELECT error " + response.getCode() + ": " + err);
        }
    }

    private Map<String, SearchFields> fetchSearchFields(CloseableHttpClient client, Set<String> pids) throws IOException {
        if (pids.isEmpty()){
            return Collections.emptyMap();
        }

        // Build OR query for all pids
        StringBuilder pidQuery = new StringBuilder();
        pidQuery.append("pid:(");
        boolean first = true;
        for (String pid : pids) {
            if (!first){
                pidQuery.append(" OR ");
            }
            pidQuery.append("\"").append(pid).append("\""); // quotes for safe matching
            first = false;
        }
        pidQuery.append(")");

        // Encode query safely
        String encodedQ = URLEncoder.encode(pidQuery.toString(), StandardCharsets.UTF_8);

        // Full query for Solr
        String query = "q=" + encodedQ
                + "&rows=" + pids.size()
                + "&fl=pid,authors,langs"
                + "&wt=json";

        // Execute query
        JSONArray searchDocs;
        try (InputStream is = requestSelectReturningStream(client, searchEndpoint, query)) {
            String jsonText = IOUtils.toString(is, StandardCharsets.UTF_8);
            JSONObject root = new JSONObject(jsonText);
            searchDocs = root.getJSONObject("response").getJSONArray("docs");
        }

        // Map pid -> authors/langs
        Map<String, SearchFields> map = new HashMap<>();
        for (int i = 0; i < searchDocs.length(); i++) {
            JSONObject sdoc = searchDocs.getJSONObject(i);
            String pid = sdoc.getString("pid");
            Object authors = sdoc.has("authors") ? sdoc.get("authors") : null;
            Object langs   = sdoc.has("langs")   ? sdoc.get("langs")   : null;
            map.put(pid, new SearchFields(authors, langs));
        }

        return map;
    }

    private void flushUpdates(CloseableHttpClient client, List<UpdateEntry> updates) throws IOException {
        JSONArray updateArray = new JSONArray();
        for (UpdateEntry e : updates) {
            JSONObject updateDoc = new JSONObject();
            updateDoc.put("id", e.id);

            java.util.Iterator<String> keys = e.fieldsToSet.keys();
            while (keys.hasNext()) {
                String fieldName = keys.next();
                Object value = e.fieldsToSet.get(fieldName);
                JSONObject setObj = new JSONObject();
                setObj.put("set", value);
                updateDoc.put(fieldName, setObj);
            }
            updateArray.put(updateDoc);
        }
        String updateUrl = logsEndpoint + "update?commit=true";
        HttpPost post = new HttpPost(updateUrl);
        post.setEntity(new StringEntity(updateArray.toString(), ContentType.APPLICATION_JSON));
        try (CloseableHttpResponse resp = client.execute(post)) {
            if (resp.getCode() != 200) {
                String err = IOUtils.toString(resp.getEntity().getContent(), StandardCharsets.UTF_8);
                throw new RuntimeException("Solr UPDATE error " + resp.getCode() + ": " + err);
            }
        }
        LOGGER.info("Committed " + updates.size() + " updates.");
    }

    private String searchEndpoint() {
        String searchPoint = KConfiguration.getInstance().getSolrSearchHost();
        if(searchPoint == null || searchPoint.isEmpty()) {
            searchPoint = SEARCH_HOST_DEFAULT;
        }
        String selectEndpoint = searchPoint + (searchPoint.endsWith("/") ? "" : "/" ) +"";
        return selectEndpoint;
    }

    private String logsEndpoint() {
        String loggerPoint = KConfiguration.getInstance().getProperty("k7.log.solr.point", LOGS_HOST_DEFAULT);
        String selectEndpoint = loggerPoint + (loggerPoint.endsWith("/") ? "" : "/" ) +"";
        return selectEndpoint;
    }

    private static class UpdateEntry {
        final String id;
        final JSONObject fieldsToSet; // contains only missing fields

        UpdateEntry(String id, JSONObject fieldsToSet) {
            this.id = id;
            this.fieldsToSet = fieldsToSet;
        }
    }

    private static class SearchFields {
        final Object authors; // String or JSONArray
        final Object langs;   // String or JSONArray

        SearchFields(Object authors, Object langs) {
            this.authors = authors;
            this.langs = langs;
        }
    }

    public static void main(String[] args) {
        LOGGER.info("Starting UpdateLogs...");
        UpdateLogs updateLogs = new UpdateLogs();
        updateLogs.run();
    }

}
