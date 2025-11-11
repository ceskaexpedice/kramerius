package cz.inovatika.kramerius.services.workers.copy;

import com.sun.jersey.api.client.Client;
import cz.inovatika.kramerius.services.config.ProcessConfig;
import cz.inovatika.kramerius.services.iterators.IterationItem;
import cz.inovatika.kramerius.services.iterators.ProcessIterator;
import cz.inovatika.kramerius.services.transform.CopyTransformation;
import cz.inovatika.kramerius.services.transform.SourceToDestTransform;
import cz.inovatika.kramerius.services.workers.Worker;
import cz.inovatika.kramerius.services.workers.WorkerFinisher;
import cz.inovatika.kramerius.services.workers.factories.WorkerFactory;

import java.util.List;

public class SimpleCopySolrWorkerFactory extends WorkerFactory {

    @Override
    public Worker createWorker(ProcessConfig processConfig, ProcessIterator iteratorInstance, Client client, List<IterationItem> pids, WorkerFinisher finisher) {
        //public SimpleCopyWorker(ProcessConfig processConfig, Client client, List<IterationItem> items, WorkerFinisher finisher) {
        return  new SimpleCopyWorker(processConfig, client,  pids, finisher);
    }

    @Override
    public WorkerFinisher createFinisher(ProcessConfig processConfig, Client client) {
        return new SimpleCopySolrFinisher(processConfig, client);
    }


    public SourceToDestTransform createTransform() {
        return new CopyTransformation();
    }
}
