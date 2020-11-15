package cz.incad.kramerius.services.workers.checkexists;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.WorkerFinisher;
import org.w3c.dom.Element;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class ExistsFinisher extends WorkerFinisher {

    public static final Logger LOGGER = Logger.getLogger(ExistsFinisher.class.getName());

    public static AtomicInteger COUNTER = new AtomicInteger( 0);
    public static AtomicInteger BATCH_COUNTER = new AtomicInteger(0);

    private long timestamp = System.currentTimeMillis();

    public ExistsFinisher(Element workerElm, Client client) {
        super(workerElm, client);
    }

    @Override
    public void finish() {
        LOGGER.info(String.format("I processed %d, (%d) records in %d", COUNTER.get(), BATCH_COUNTER.get(), (System.currentTimeMillis() - timestamp)));
    }
}
