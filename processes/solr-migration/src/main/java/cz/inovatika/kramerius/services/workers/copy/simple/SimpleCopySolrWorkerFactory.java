package cz.inovatika.kramerius.services.workers.copy.simple;

import com.sun.jersey.api.client.Client;
import cz.inovatika.kramerius.services.config.ProcessConfig;
import cz.inovatika.kramerius.services.iterators.IterationItem;
import cz.inovatika.kramerius.services.iterators.ProcessIterator;
import cz.inovatika.kramerius.services.workers.Worker;
import cz.inovatika.kramerius.services.workers.WorkerFinisher;
import cz.inovatika.kramerius.services.workers.factories.WorkerFactory;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

import java.util.List;

public class SimpleCopySolrWorkerFactory extends WorkerFactory {

    @Override
    public Worker createWorker(ProcessConfig processConfig, ProcessIterator iteratorInstance, CloseableHttpClient client, List<IterationItem> pids, WorkerFinisher finisher) {
        return  new SimpleCopyWorker(processConfig, client,  pids, finisher);
    }

    @Override
    public WorkerFinisher createFinisher(ProcessConfig processConfig, CloseableHttpClient client) {
        return new SimpleCopySolrFinisher(processConfig, client);
    }
}
