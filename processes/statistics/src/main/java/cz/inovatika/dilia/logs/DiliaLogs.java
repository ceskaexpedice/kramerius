package cz.inovatika.dilia.logs;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DiliaLogs {

    public static final Logger LOGGER = Logger.getLogger(DiliaLogs.class.getName());

    private final String apiBaseUrl;
    private final Path inputDir;
    private final Path outputDir;

    public DiliaLogs(String apiBaseUrl, String inputDir, String outputDir) {
        this.apiBaseUrl = apiBaseUrl;
        this.inputDir = Paths.get(inputDir);
        this.outputDir = Paths.get(outputDir);
    }

    public void run() throws IOException {
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }


        try (Stream<Path> paths = Files.list(inputDir)) {
            paths.filter(Files::isRegularFile)
                 //.filter(path -> path.toString().endsWith(".log") || path.toString().endsWith(".json"))
                 .forEach(this::processFile);
        }
    }

    public static final int BATCH_SIZE = 30;

    private void processFile(Path filePath) {
        Path targetPath = outputDir.resolve(filePath.getFileName());
        LOGGER.info("Processing file: " + filePath.getFileName());

        try (BufferedReader reader = Files.newBufferedReader(filePath);
             BufferedWriter writer = Files.newBufferedWriter(targetPath)) {

            List<JSONObject> batch = new ArrayList<>();
            int counter = 0;

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                JSONObject logEntry = new JSONObject(line);
                batch.add(logEntry);
                counter = counter+1;
                if (counter==BATCH_SIZE) {
                    processBatch(batch).stream().map(JSONObject::toString).forEach(batchLine-> {
                        try {
                            writer.write(batchLine);
                            writer.newLine();
                        } catch (IOException ex) {
                            LOGGER.log(Level.SEVERE, null, ex);
                        }
                    });
                }
            }
            if (!batch.isEmpty()) {
                processBatch(batch).stream().map(JSONObject::toString).forEach(batchLine-> {
                    try {
                        writer.write(batchLine);
                        writer.newLine();
                    } catch (IOException ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                    }
                });

            }
            LOGGER.info("Finished processing file: " + filePath.getFileName());

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error processing file: " + filePath.getFileName(), e);
        }
    }

    private boolean needsAuthors(JSONObject logEntry) {
        return !logEntry.has("authors") || logEntry.isNull("authors");
    }

    private List<JSONObject> processBatch(List<JSONObject>  recs) {
        List<JSONObject> retVal = new ArrayList<>();
        List<String> noAuthorsPids = new ArrayList<>();
        Map<String, JSONObject> processingMap =  new HashMap<>();
        recs.forEach((item)-> {
            if (needsAuthors(item)) {
                String pid = item.getString("pid");
                noAuthorsPids.add(pid);
                processingMap.put(pid, item);
            }
        });

        if (!noAuthorsPids.isEmpty()) {
            fixAuthors(noAuthorsPids, processingMap);
        }

        recs.forEach((item)-> {
            String pid = item.getString("pid");
            if (processingMap.containsKey(pid)) {
                retVal.add(processingMap.get(pid));
            } else {
                retVal.add(item);
            }
        });
        return recs;
    }


    private void fixAuthors(List<String> pids, Map<String, JSONObject> processingMap ) {
        LOGGER.info("Fetching authors for pids: " + pids);

        String query ="pid:("+pids.stream().map(it-> '"'+it+'"').collect(Collectors.joining(" OR "))+")";
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String fullUrl = String.format("%s/api/client/v7.0/search?q=%s&fl=pid,authors.facet&rows=%d",
                    apiBaseUrl, encodedQuery, pids.size());

            HttpGet httpGet = new HttpGet(fullUrl);

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                httpClient.execute(httpGet, response -> {
                    if (response.getCode() == 200) {
                        String result = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                        JSONObject jsonResponse = new JSONObject(result);
                        JSONArray docs = jsonResponse.getJSONObject("response").getJSONArray("docs");

                        for (int i = 0; i < docs.length(); i++) {
                            JSONObject doc = docs.getJSONObject(i);
                            String pid = doc.optString("pid");
                            JSONArray authors = doc.optJSONArray("authors.facet");

                            if (pid != null && authors != null) {
                                if (processingMap.containsKey(pid)) {
                                    processingMap.get(pid).put("authors", authors);
                                }
                            }
                        }
                    } else {
                        LOGGER.log(Level.SEVERE, "Error processing request: " + fullUrl);
                    }
                    return null;
                });
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Batch processing error");
        }
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            LOGGER.info("Použití: ./statistics <api_url> <input_dir> <output_dir>");
            LOGGER.info("Příklad ./statistics https://k7.inovatika.dev/search /statistics/input /statistics/output");
            return;
        }
        try {
            DiliaLogs processor = new DiliaLogs(args[0], args[1], args[2]);
            processor.run();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }
    }
}