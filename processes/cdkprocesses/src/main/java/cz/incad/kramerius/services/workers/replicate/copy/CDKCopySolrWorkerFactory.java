package cz.incad.kramerius.services.workers.replicate.copy;

import com.sun.jersey.api.client.Client;
import cz.inovatika.kramerius.services.config.ProcessConfig;
import cz.inovatika.kramerius.services.workers.Worker;
import cz.inovatika.kramerius.services.workers.factories.WorkerFactory;
import cz.inovatika.kramerius.services.workers.WorkerFinisher;
import cz.inovatika.kramerius.services.iterators.IterationItem;
import cz.inovatika.kramerius.services.iterators.ProcessIterator;
import cz.inovatika.kramerius.services.workers.batch.impl.CopyTransformation;
import cz.inovatika.kramerius.services.workers.batch.BatchTransformation;

import java.util.List;

public class CDKCopySolrWorkerFactory extends WorkerFactory {

//    @Override
//    public Worker createWorker(String sourceName, String reharvestUrl, ProcessIterator iteratorInstance, Element base, Client client, List<IterationItem> items, WorkerFinisher finisher) {
//        return new CDKCopyWorker(sourceName, reharvestUrl, base, client, items, finisher);
//    }
//
//    @Override
//    public WorkerFinisher createFinisher(String timestampUrl, Element worker, Client client) {
//        return new ReplicateFinisher( timestampUrl, worker, client);
//    }


    @Override
    public Worker createWorker(ProcessConfig processConfig, ProcessIterator iteratorInstance, Client client, List<IterationItem> pids, WorkerFinisher finisher) {
        return null;
    }

    @Override
    public WorkerFinisher createFinisher(ProcessConfig processConfig, Client client) {
        return null;
    }

    public BatchTransformation createTransform() {
        return new CopyTransformation();
    }
}
