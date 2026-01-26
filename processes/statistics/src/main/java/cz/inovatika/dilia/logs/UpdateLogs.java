package cz.inovatika.dilia.logs;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class UpdateLogs {

    // === CONFIGURATION ===

    private static final String LOGS_HOST   = "http://localhost:8983/solr/logs/";
    private static final String SEARCH_HOST = "http://localhost:8983/solr/search/";

    private static final int PAGE_SIZE = 100;
    private static final int UPDATE_BATCH_SIZE = 100;

    // === ENTRY POINT ===

    public static void main(String[] args) {
        System.out.println("Starting EnrichLogsAuthorsJob...");
        UpdateLogs updateLogs = new UpdateLogs();
        updateLogs.run();
    }

    public void run() {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            int start = 0;
            boolean done = false;
            List<UpdateEntry> updateBuffer = new ArrayList<>();
            while (!done) {
                String query = "q=*:*"
                        + "&start=" + start
                        + "&rows=" + PAGE_SIZE
                        + "&fl=id,pid,authors";
                try (InputStream is = requestSelectReturningStream(client, LOGS_HOST, query)) {
                    String jsonText = IOUtils.toString(is, StandardCharsets.UTF_8);
                    JSONObject root = new JSONObject(jsonText);

                    JSONObject response = root.getJSONObject("response");
                    int numFound = response.getInt("numFound");
                    JSONArray docs = response.getJSONArray("docs");

                    System.out.println("Processing logs docs " + start + " .. " + (start + docs.length()) + " / " + numFound);

                    for (int i = 0; i < docs.length(); i++) {
                        JSONObject doc = docs.getJSONObject(i);

                        // Skip if authors already present
                        if (doc.has("authors")) {
                            continue;
                        }

                        if (!doc.has("id") || !doc.has("pid")) {
                            continue;
                        }

                        String id = doc.getString("id");
                        String pid = doc.getString("pid");

                        Object authorsFromSearch = findAuthorsInSearch(client, pid);

                        if (authorsFromSearch != null) {
                            updateBuffer.add(new UpdateEntry(id, authorsFromSearch));

                            if (updateBuffer.size() >= UPDATE_BATCH_SIZE) {
                                flushUpdates(client, updateBuffer);
                                updateBuffer.clear();
                            }
                        }
                    }

                    start += PAGE_SIZE;
                    if (start >= numFound) {
                        done = true;
                    }
                }
            }

            // Flush remaining updates
            if (!updateBuffer.isEmpty()) {
                flushUpdates(client, updateBuffer);
            }

            System.out.println("EnrichLogsAuthorsJob finished successfully.");

        } catch (Exception e) {
            System.err.println("Job failed with exception:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    // === SOLR SELECT HELPERS ===

    private static InputStream requestSelectReturningStream(CloseableHttpClient client,
                                                            String solrHost,
                                                            String query) throws Exception {

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

    // === QUERY SEARCH INDEX BY PID ===

    private static Object findAuthorsInSearch(CloseableHttpClient client, String pid) throws Exception {

        String query = "q=pid:\"" + urlEscape(pid) + "\""
                + "&rows=1"
                + "&fl=authors";

        try (InputStream is = requestSelectReturningStream(client, SEARCH_HOST, query)) {

            String jsonText = IOUtils.toString(is, StandardCharsets.UTF_8);
            JSONObject root = new JSONObject(jsonText);

            JSONArray docs = root.getJSONObject("response").getJSONArray("docs");

            if (docs.length() == 0) {
                return null;
            }

            JSONObject first = docs.getJSONObject(0);

            if (!first.has("authors")) {
                return null;
            }

            // This can be String or JSONArray → return as-is
            return first.get("authors");
        }
    }

    // === SEND ATOMIC UPDATES TO LOGS CORE ===

    private static void flushUpdates(CloseableHttpClient client, List<UpdateEntry> updates) throws Exception {

        JSONArray updateArray = new JSONArray();

        for (UpdateEntry e : updates) {
            JSONObject updateDoc = new JSONObject();
            updateDoc.put("id", e.id);

            JSONObject setObj = new JSONObject();
            setObj.put("set", e.authorsValue);

            updateDoc.put("authors", setObj);

            updateArray.put(updateDoc);
        }

        String updateUrl = LOGS_HOST.endsWith("/")
                ? LOGS_HOST + "update?commit=true"
                : LOGS_HOST + "/update?commit=true";

        HttpPost post = new HttpPost(updateUrl);
        post.setEntity(new StringEntity(updateArray.toString(), ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse resp = client.execute(post)) {
            if (resp.getCode() != 200) {
                String err = IOUtils.toString(resp.getEntity().getContent(), StandardCharsets.UTF_8);
                throw new RuntimeException("Solr UPDATE error " + resp.getCode() + ": " + err);
            }
        }

        System.out.println("Committed " + updates.size() + " updates.");
    }

    // === SMALL HELPERS ===

    private static String urlEscape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // Simple holder for pending updates
    private static class UpdateEntry {
        final String id;
        final Object authorsValue; // String or JSONArray

        UpdateEntry(String id, Object authorsValue) {
            this.id = id;
            this.authorsValue = authorsValue;
        }
    }
}
