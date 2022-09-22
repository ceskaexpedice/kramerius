package cz.incad.kramerius.services.workers.updateocrcdk;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.Worker;
import cz.incad.kramerius.services.WorkerFinisher;
import cz.incad.kramerius.services.WorkerFactory;
import cz.incad.kramerius.services.iterators.IterationItem;
import cz.incad.kramerius.services.iterators.ProcessIterator;
import cz.incad.kramerius.services.iterators.timestamps.TimestampStore;
import cz.incad.kramerius.services.workers.updateocr.UpdateOCRFinisher;
import org.w3c.dom.Element;

import java.util.List;

public class UpdateOCRFromCDKEndpointWorkerFactory extends WorkerFactory {


    @Override
    public WorkerFinisher createFinisher(TimestampStore store, Element worker, Client client) {
        return new UpdateOCRFinisher(store, worker, client);
    }

    @Override
    public Worker createWorker(ProcessIterator iteratorInstance, Element worker, Client client, List<IterationItem> items) {
        return new UpdateOCRFromCDKEndpointWorker(worker, client, items);
    }
}
