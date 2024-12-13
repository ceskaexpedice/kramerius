package cz.incad.kramerius.services.workers.updateocrsolr_dep;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.Worker;
import cz.incad.kramerius.services.WorkerFactory;
import cz.incad.kramerius.services.WorkerFinisher;
import cz.incad.kramerius.services.iterators.IterationItem;
import cz.incad.kramerius.services.iterators.ProcessIterator;
import cz.incad.kramerius.services.workers.updateocr_dep.UpdateOCRFinisher;
import cz.incad.kramerius.timestamps.TimestampStore;

import org.w3c.dom.Element;

import java.util.List;

public class UpdateOCRFromSolrWorkerFactory extends WorkerFactory {
    @Override
    public Worker createWorker(String sourceName, ProcessIterator iteratorInstance, Element worker, Client client, List<IterationItem> pids, WorkerFinisher finisher) {
        return new UpdateOCRFromSolrWorker(sourceName, worker, client, pids);
    }

    @Override
    public WorkerFinisher createFinisher(String timestampUrl , Element worker, Client client) {
        return new UpdateOCRFinisher( timestampUrl, worker, client);
    }
}
