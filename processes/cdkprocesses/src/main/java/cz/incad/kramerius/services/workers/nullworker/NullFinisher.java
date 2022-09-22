package cz.incad.kramerius.services.workers.nullworker;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.WorkerFinisher;
import cz.incad.kramerius.services.iterators.timestamps.TimestampStore;

import org.w3c.dom.Element;

import java.util.logging.Logger;

public class NullFinisher extends WorkerFinisher {

    public static final Logger LOGGER = Logger.getLogger(NullFinisher.class.getName());

    private long start = 0;

    public NullFinisher(TimestampStore store,  Element workerElm, Client client) {
        super(store, workerElm, client);
        start = System.currentTimeMillis();
    }

    @Override
    public void finish() {
        LOGGER.info(String.format("Finishes in %d ms and number of records %d", (System.currentTimeMillis() - this.start), NullWorkerFactory.COUNTER));
    }
}
//11201 11201