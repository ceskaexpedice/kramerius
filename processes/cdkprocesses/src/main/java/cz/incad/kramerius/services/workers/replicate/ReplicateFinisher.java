package cz.incad.kramerius.services.workers.replicate;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.WorkerFinisher;
import cz.incad.kramerius.services.utils.SolrUtils;
import cz.incad.kramerius.services.workers.nullworker.NullWorkerFactory;
import org.w3c.dom.Element;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class ReplicateFinisher   extends WorkerFinisher {

    public static final Logger LOGGER = Logger.getLogger(ReplicateFinisher.class.getName());

    public static AtomicInteger COUNTER = new AtomicInteger(0);
    public static AtomicInteger NEWINDEXED = new AtomicInteger(0);
    public static AtomicInteger UPDATED = new AtomicInteger(0);

    long start = System.currentTimeMillis();

    public ReplicateFinisher(Element workerElm, Client client) {
        super(workerElm, client);
    }

    @Override
    public void finish() {
        SolrUtils.commit(this.client, this.destinationUrl);
        LOGGER.info(String.format("Finishes in %d ms and number of total records %d(indexed %d, updated %d)", (System.currentTimeMillis() - this.start), COUNTER.get(), NEWINDEXED.get(), UPDATED.get()));

    }
}
