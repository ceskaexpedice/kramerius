package cz.incad.kramerius.services.workers.updateocr;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.Worker;
import cz.incad.kramerius.services.WorkerFinisher;
import cz.incad.kramerius.services.WorkerFactory;
import org.w3c.dom.Element;

import java.util.List;

public class UpdateOCRWorkerFactory extends WorkerFactory {


    @Override
    public WorkerFinisher createFinisher(Element worker, Client client) {
        return new UpdateOCRFinisher(worker, client);
    }

    @Override
    public Worker createWorker(Element worker, Client client, List<String> pids) {
        return new UpdateOCRWorker(worker, client, pids);
    }
}
