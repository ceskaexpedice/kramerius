package cz.incad.kramerius.services.workers.updateocrcdk_dep;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.Worker;
import cz.incad.kramerius.services.WorkerFinisher;
import cz.incad.kramerius.services.WorkerFactory;
import cz.incad.kramerius.services.iterators.IterationItem;
import cz.incad.kramerius.services.iterators.ProcessIterator;
import cz.incad.kramerius.services.workers.updateocr_dep.UpdateOCRFinisher;
import cz.incad.kramerius.timestamps.TimestampStore;

import org.w3c.dom.Element;

import java.util.List;

public class UpdateOCRFromCDKEndpointWorkerFactory extends WorkerFactory {


    @Override
    public WorkerFinisher createFinisher(String timestampUrl, Element worker, Client client) {
        return new UpdateOCRFinisher(timestampUrl, worker, client);
    }

    @Override
    public Worker createWorker(ProcessIterator iteratorInstance, Element worker, Client client, List<IterationItem> items) {
        return new UpdateOCRFromCDKEndpointWorker(worker, client, items);
    }
}
