package cz.incad.kramerius.services.workers.replicate.copy;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.Worker;
import cz.incad.kramerius.services.WorkerFactory;
import cz.incad.kramerius.services.WorkerFinisher;
import cz.incad.kramerius.services.iterators.IterationItem;
import cz.incad.kramerius.services.iterators.ProcessIterator;
import cz.incad.kramerius.services.transform.BasicSourceToDestTransform;
import cz.incad.kramerius.services.transform.SourceToDestTransform;
import cz.incad.kramerius.services.workers.replicate.ReplicateFinisher;
import cz.incad.kramerius.timestamps.TimestampStore;

import org.w3c.dom.Element;

import java.util.List;

public class CopyReplicateSolrWorkerFactory extends WorkerFactory {


    @Override
    public Worker createWorker(String sourceName, ProcessIterator iteratorInstance, Element base, Client client, List<IterationItem> items) {
        return new CopyReplicateWorker(sourceName, base, client, items);
    }

    @Override
    public WorkerFinisher createFinisher(String timestampUrl, Element worker, Client client) {
        return new ReplicateFinisher( timestampUrl, worker, client);
    }

    public SourceToDestTransform createTransform() {
        return new BasicSourceToDestTransform();
    }
}
