package cz.incad.kramerius.services.workers.nullworker;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.Worker;
import cz.incad.kramerius.services.WorkerFinisher;
import cz.incad.kramerius.services.WorkerFactory;
import org.w3c.dom.Element;

import java.util.List;

public class NullWorkerFactory extends WorkerFactory {

    public static int COUNTER = 0;

    @Override
    public Worker createWorker(Element worker, Client client, List<String> pids) {
        return new NullWorker(worker, client, pids);
    }

    @Override
    public WorkerFinisher createFinisher(Element worker, Client client) {
        return new NullFinisher(worker, client);
    }
}
