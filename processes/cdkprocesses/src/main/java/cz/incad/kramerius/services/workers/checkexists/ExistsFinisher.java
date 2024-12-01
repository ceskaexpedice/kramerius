package cz.incad.kramerius.services.workers.checkexists;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.WorkerFinisher;
import cz.incad.kramerius.timestamps.TimestampStore;

import org.w3c.dom.Element;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class ExistsFinisher extends WorkerFinisher {

    public static final Logger LOGGER = Logger.getLogger(ExistsFinisher.class.getName());

    public static AtomicInteger LOGGED = new AtomicInteger( 0);

    private long timestamp = System.currentTimeMillis();

    public ExistsFinisher(String timestampUrl, Element workerElm, Client client) {
        super( timestampUrl, workerElm, client);
    }

    @Override
    public void finish() {
        LOGGER.info(String.format("I processed %d records in %d", LOGGED.get(), (System.currentTimeMillis() - timestamp)));
    }
}
