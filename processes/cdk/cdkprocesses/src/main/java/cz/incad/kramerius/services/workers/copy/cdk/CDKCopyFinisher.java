package cz.incad.kramerius.services.workers.copy.cdk;


import cz.inovatika.kramerius.services.config.ProcessConfig;
import cz.inovatika.kramerius.services.workers.WorkerFinisher;
import cz.inovatika.kramerius.services.iterators.utils.HTTPSolrUtils;
import cz.incad.kramerius.utils.StringUtils;

import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

public class CDKCopyFinisher extends WorkerFinisher {

    public static final Logger LOGGER = Logger.getLogger(CDKCopyFinisher.class.getName());

    public static AtomicInteger WORKERS = new AtomicInteger(0);

    public static AtomicInteger BATCHES = new AtomicInteger(0);

    public static AtomicInteger NEWINDEXED = new AtomicInteger(0);

    public static AtomicInteger UPDATED = new AtomicInteger(0);

    // not indexed - composite id
    public static AtomicInteger NOT_INDEXED_COMPOSITEID = new AtomicInteger(0);
    public static AtomicInteger NOT_INDEXED_SKIPPED = new AtomicInteger(0);
    
    // uknown exception during crawl
    public static final int EXCEPTION_DURING_CRAWL_LIMIT = 10000;
    public static List<Exception> EXCEPTION_DURING_CRAWL = new ArrayList<>();
    
    
    long start = System.currentTimeMillis();


    public CDKCopyFinisher(ProcessConfig config, CloseableHttpClient client) {
        super(config, client);
    }

    private JSONObject storeTimestamp() {
        String hostname = System.getenv("HOSTNAME");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("workers", WORKERS.get());
        jsonObject.put("batches", BATCHES.get());
        jsonObject.put("indexed", NEWINDEXED);
        jsonObject.put("updated", UPDATED);
        jsonObject.put("hostname", hostname);

        String typeOfCrawl = this.processConfig.getType();
        if (typeOfCrawl != null) {
            jsonObject.put("type", typeOfCrawl);
        }

        String url = this.processConfig.getTimestampUrl();
        HttpPut putRequest = new HttpPut(url);

        putRequest.setHeader("Accept", "application/json");

        StringEntity entity = new StringEntity(
                jsonObject.toString(),
                ContentType.APPLICATION_JSON
        );
        putRequest.setEntity(entity);
        try {
            return this.client.execute(putRequest, response -> {
                int status = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                if (status == 200 || status == 201) {
                    return new JSONObject(responseBody);
                } else {
                    throw new RuntimeException(String.format(
                            "Failed to store timestamp. HTTP status %d, Response: %s",
                            status, responseBody));
                }
            });
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Network error while storing timestamp to " + url, e);
            throw new RuntimeException("Connection failed during storeTimestamp", e);
        }
    }


	
    @Override
    public void exceptionDuringCrawl(Exception ex) {
        if (EXCEPTION_DURING_CRAWL.size() < EXCEPTION_DURING_CRAWL_LIMIT) {
            EXCEPTION_DURING_CRAWL.add(ex);
            LOGGER.info("Exception during crawl :"+EXCEPTION_DURING_CRAWL);
        }
    }


    @Override
    public void finish() {
        if (StringUtils.isAnyString(this.processConfig.getTimestampUrl()) && EXCEPTION_DURING_CRAWL.isEmpty()) {
    		storeTimestamp();
    	}
    	HTTPSolrUtils.commitApache(this.client, this.processConfig.getWorkerConfig().getDestinationConfig().getDestinationUrl());
        LOGGER.info(String.format("Finishes in %d ms ;All work for workers: %d; work in batches: %d; indexed: %d; updated %d, compositeIderror %d, skipped %d", (System.currentTimeMillis() - this.start), WORKERS.get(), BATCHES.get(), NEWINDEXED.get(), UPDATED.get(), NOT_INDEXED_COMPOSITEID.get(), NOT_INDEXED_SKIPPED.get()));

        if (!EXCEPTION_DURING_CRAWL.isEmpty()) {
            LOGGER.log(Level.SEVERE, "Exception during crawl :");
            EXCEPTION_DURING_CRAWL.forEach(ex -> {
                StringWriter stringWriter = new StringWriter();
                PrintWriter pw = new PrintWriter(stringWriter);
                ex.printStackTrace(pw);
            });
        }
    }
}
