package cz.incad.kramerius.services.workers.replicate;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.Worker;
import cz.incad.kramerius.services.WorkerFactory;
import cz.incad.kramerius.services.WorkerFinisher;
import cz.incad.kramerius.services.iterators.IterationItem;
import cz.incad.kramerius.services.iterators.ProcessIterator;
import org.w3c.dom.Element;

import java.util.List;

public class ReplicateSolrWorkerFactory extends WorkerFactory {


    @Override
    public Worker createWorker(ProcessIterator iteratorInstance, Element base, Client client, List<IterationItem> items) {
        return new ReplicateWorker(base, client, items);
    }

    @Override
    public WorkerFinisher createFinisher(Element worker, Client client) {
        return new ReplicateFinisher(worker, client);
    }
}
