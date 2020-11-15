package cz.incad.kramerius.services.workers.updateocr;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.WorkerFinisher;
import cz.incad.kramerius.services.utils.SolrUtils;
import org.w3c.dom.Element;

import java.util.logging.Logger;

public class UpdateOCRFinisher extends WorkerFinisher {

    public static Logger LOGGER = Logger.getLogger(UpdateOCRFinisher.class.getName());

    private long start = 0;

    public UpdateOCRFinisher(Element workerElm, Client client) {
        super(workerElm, client);
        start = System.currentTimeMillis();
    }

    @Override
    public void finish() {
        long stop = System.currentTimeMillis();
        LOGGER.info("Finished  in "+(stop - start)+" ms");
        SolrUtils.commit(this.client, this.destinationUrl);
    }
}
