package cz.incad.kramerius.services.workers.copy.cdk;

import cz.inovatika.kramerius.services.config.ProcessConfig;
import cz.inovatika.kramerius.services.iterators.ApacheHTTPRequestEnricher;
import cz.inovatika.kramerius.services.workers.MigrationIndexFeeder;
import cz.inovatika.kramerius.services.workers.factories.MigrationIndexFeederFactory;
import cz.inovatika.kramerius.services.workers.MigrationIndexFeederFinisher;
import cz.inovatika.kramerius.services.iterators.IterationItem;
import cz.inovatika.kramerius.services.iterators.ProcessIterator;
import cz.inovatika.kramerius.services.workers.batch.BatchTransformation;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

import java.util.List;

public class CDKCopySolrMigrationIndexFeederFactory extends MigrationIndexFeederFactory {

    @Override
    public MigrationIndexFeeder createFeeder(ProcessConfig processConfig, ProcessIterator iteratorInstance, CloseableHttpClient client, ApacheHTTPRequestEnricher enricher, List<IterationItem> pids, MigrationIndexFeederFinisher finisher) {
        return new CDKCopyMigrationIndexFeeder(processConfig, client, enricher, pids, finisher);
    }
    @Override
    public MigrationIndexFeederFinisher createFinisher(ProcessConfig processConfig, CloseableHttpClient client) {
        return new CDKCopyFinisher(processConfig, client);
    }

    public BatchTransformation createTransform() {
        return null;
    }
}
