package cz.incad.kramerius.services.workers.updateocrsolr;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.Worker;
import cz.incad.kramerius.services.WorkerFactory;
import cz.incad.kramerius.services.WorkerFinisher;
import cz.incad.kramerius.services.iterators.IterationItem;
import cz.incad.kramerius.services.iterators.ProcessIterator;
import cz.incad.kramerius.services.workers.updateocr.UpdateOCRFinisher;
import org.w3c.dom.Element;

import java.util.List;

public class UpdateOCRFromSolrWorkerFactory extends WorkerFactory {
    @Override
    public Worker createWorker(ProcessIterator iteratorInstance, Element worker, Client client, List<IterationItem> pids) {
        return new UpdateOCRFromSolrWorker(worker, client, pids);
    }

    @Override
    public WorkerFinisher createFinisher(Element worker, Client client) {
        return new UpdateOCRFinisher(worker, client);
    }
}
