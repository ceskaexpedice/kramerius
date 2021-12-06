package cz.incad.kramerius.services.workers.replicate;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.WorkerFinisher;
import cz.incad.kramerius.services.utils.SolrUtils;
import org.w3c.dom.Element;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

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

    long start = System.currentTimeMillis();

    public ReplicateFinisher(Element workerElm, Client client) {
        super(workerElm, client);
    }

    @Override
    public void finish() {
        SolrUtils.commit(this.client, this.destinationUrl);
        LOGGER.info(String.format("Finishes in %d ms ;All work for workers: %d; work in batches: %d; indexed: %d; updated %d", (System.currentTimeMillis() - this.start), WORKERS.get(), BATCHES.get(), NEWINDEXED.get(), UPDATED.get()));

    }
}
