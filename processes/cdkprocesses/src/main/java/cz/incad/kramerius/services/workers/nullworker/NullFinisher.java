package cz.incad.kramerius.services.workers.nullworker;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.WorkerFinisher;
import org.w3c.dom.Element;

import java.util.logging.Logger;

public class NullFinisher extends WorkerFinisher {

    public static final Logger LOGGER = Logger.getLogger(NullFinisher.class.getName());

    private long start = 0;

    public NullFinisher(Element workerElm, Client client) {
        super(workerElm, client);
        start = System.currentTimeMillis();
    }

    @Override
    public void finish() {
        LOGGER.info(String.format("Finishes in %d ms and number of records %d", (System.currentTimeMillis() - this.start), NullWorkerFactory.COUNTER));
    }
}
//11201 11201