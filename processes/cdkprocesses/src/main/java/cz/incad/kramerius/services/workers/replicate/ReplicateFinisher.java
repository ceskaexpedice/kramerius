package cz.incad.kramerius.services.workers.replicate;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.services.WorkerFinisher;
import cz.incad.kramerius.services.utils.SolrUtils;
import cz.incad.kramerius.timestamps.TimestampStore;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;

import org.apache.commons.collections4.list.FixedSizeList;
import org.json.JSONObject;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

public class ReplicateFinisher   extends WorkerFinisher {

    public static final Logger LOGGER = Logger.getLogger(ReplicateFinisher.class.getName());

    // celkova prace predena workerum
    public static AtomicInteger WORKERS = new AtomicInteger(0);

    // rozdeleno do davek
    public static AtomicInteger BATCHES = new AtomicInteger(0);

    // indexovano
    public static AtomicInteger NEWINDEXED = new AtomicInteger(0);

    // updatovano
    public static AtomicInteger UPDATED = new AtomicInteger(0);
    

    // not indexed - composite id
    public static AtomicInteger NOT_INDEXED_COMPOSITEID = new AtomicInteger(0);
    public static AtomicInteger NOT_INDEXED_SKIPPED = new AtomicInteger(0);
    
    // uknown exception during crawl
    public static final int EXCEPTION_DURING_CRAWL_LIMIT = 10000;
    public static List<Exception> EXCEPTION_DURING_CRAWL = new ArrayList<>();
    
    
    long start = System.currentTimeMillis();

    protected String typeOfCrawl;

    
    public ReplicateFinisher(String timestampUrl, Element workerElm, Client client) {
        super(timestampUrl, workerElm, client);

        Element typeElm = XMLUtils.findElement((Element)workerElm.getParentNode(), "type");
        if (typeElm != null) {
            typeOfCrawl = typeElm.getTextContent();
        }

    }

	private JSONObject storeTimestamp() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("workers", WORKERS.get());
		jsonObject.put("batches", BATCHES.get());
		jsonObject.put("indexed", NEWINDEXED);
		jsonObject.put("updated", UPDATED);
		
		if (typeOfCrawl != null) {
	        jsonObject.put("type", typeOfCrawl);
		}
		
        LOGGER.info(String.format("[" + Thread.currentThread().getName() + "] url %s", timestampUrl));
    	WebResource r = client.resource(timestampUrl);
        String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).entity(jsonObject.toString()).put(String.class);
        return new JSONObject(t);
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
        if (StringUtils.isAnyString(timestampUrl) && EXCEPTION_DURING_CRAWL.isEmpty()) {
    		storeTimestamp();
    	}
    	SolrUtils.commit(this.client, this.destinationUrl);
        LOGGER.info(String.format("Finishes in %d ms ;All work for workers: %d; work in batches: %d; indexed: %d; updated %d, compositeIderror %d, skipped %d", (System.currentTimeMillis() - this.start), WORKERS.get(), BATCHES.get(), NEWINDEXED.get(), UPDATED.get(), NOT_INDEXED_COMPOSITEID.get(), NOT_INDEXED_SKIPPED.get()));
    }
}
