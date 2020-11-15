package cz.incad.kramerius.services.workers.replicate;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.Worker;
import cz.incad.kramerius.services.WorkerFactory;
import cz.incad.kramerius.services.WorkerFinisher;
import org.w3c.dom.Element;

import java.util.List;

public class ReplicateSolrWorkerFactory extends WorkerFactory {




    @Override
    public Worker createWorker(Element base, Client client, List<String> pids) {
        return new ReplicateWorker(base, client, pids);
    }

    @Override
    public WorkerFinisher createFinisher(Element worker, Client client) {
        return null;
    }
}
