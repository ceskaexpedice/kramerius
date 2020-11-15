package cz.incad.kramerius.services.workers.update;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.Worker;
import cz.incad.kramerius.services.WorkerFinisher;
import cz.incad.kramerius.services.WorkerFactory;
import org.w3c.dom.Element;

import java.util.List;

public class UpdateWorkerFactory extends WorkerFactory {

    @Override
    public WorkerFinisher createFinisher(Element worker, Client client) {
        return new UpdateWorkerFinisher(worker, client);
    }

    @Override
    public Worker createWorker(Element worker, Client client, List<String> pids) {
        return new UpdateWorker(worker, client, pids);
    }
}
