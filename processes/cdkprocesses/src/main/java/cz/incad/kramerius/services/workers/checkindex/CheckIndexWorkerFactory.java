package cz.incad.kramerius.services.workers.checkindex;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.Worker;
import cz.incad.kramerius.services.WorkerFinisher;
import cz.incad.kramerius.services.WorkerFactory;
import cz.incad.kramerius.services.iterators.IterationItem;
import org.w3c.dom.Element;

import java.util.List;

public class CheckIndexWorkerFactory extends WorkerFactory {

    public CheckIndexWorkerFactory() {  }


    @Override
    public WorkerFinisher createFinisher(Element worker, Client client) {
        return null;
    }

    @Override
    public Worker createWorker(Element base, Client client, List<IterationItem> items) {
        return new CheckIndexWorker(base, client, items);
    }
}
