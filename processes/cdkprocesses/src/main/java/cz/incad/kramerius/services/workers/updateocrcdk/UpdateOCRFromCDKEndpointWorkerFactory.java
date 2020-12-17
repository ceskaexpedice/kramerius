package cz.incad.kramerius.services.workers.updateocrcdk;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.Worker;
import cz.incad.kramerius.services.WorkerFinisher;
import cz.incad.kramerius.services.WorkerFactory;
import cz.incad.kramerius.services.iterators.IterationItem;
import cz.incad.kramerius.services.workers.updateocr.UpdateOCRFinisher;
import cz.incad.kramerius.services.workers.updateocr.UpdateOCRWorker;
import org.w3c.dom.Element;

import java.util.List;

public class UpdateOCRFromCDKEndpointWorkerFactory extends WorkerFactory {


    @Override
    public WorkerFinisher createFinisher(Element worker, Client client) {
        return new UpdateOCRFinisher(worker, client);
    }

    @Override
    public Worker createWorker(Element worker, Client client, List<IterationItem> items) {
        return new UpdateOCRFromCDKEndpointWorker(worker, client, items);
    }
}
