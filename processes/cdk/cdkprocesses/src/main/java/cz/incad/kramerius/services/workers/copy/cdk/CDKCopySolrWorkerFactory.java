package cz.incad.kramerius.services.workers.copy.cdk;

import com.sun.jersey.api.client.Client;
import cz.inovatika.kramerius.services.config.ProcessConfig;
import cz.inovatika.kramerius.services.iterators.ApacheHTTPRequestEnricher;
import cz.inovatika.kramerius.services.workers.Worker;
import cz.inovatika.kramerius.services.workers.factories.WorkerFactory;
import cz.inovatika.kramerius.services.workers.WorkerFinisher;
import cz.inovatika.kramerius.services.iterators.IterationItem;
import cz.inovatika.kramerius.services.iterators.ProcessIterator;
import cz.inovatika.kramerius.services.workers.batch.BatchTransformation;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

import java.util.List;

public class CDKCopySolrWorkerFactory extends WorkerFactory {

    @Override
    public Worker createWorker(ProcessConfig processConfig, ProcessIterator iteratorInstance, CloseableHttpClient client, ApacheHTTPRequestEnricher enricher, List<IterationItem> pids, WorkerFinisher finisher) {
        return new CDKCopyWorker(processConfig, client, enricher, pids, finisher);
    }
    @Override
    public WorkerFinisher createFinisher(ProcessConfig processConfig, CloseableHttpClient client) {
        return new CDKCopyFinisher(processConfig, client);
    }

    public BatchTransformation createTransform() {
        return null;
    }
}
