package cz.inovatika.kramerius.services.workers.copy.simple;

import cz.inovatika.kramerius.services.config.MigrationConfig;
import cz.inovatika.kramerius.services.iterators.ApacheHTTPRequestEnricher;
import cz.inovatika.kramerius.services.iterators.IterationItem;
import cz.inovatika.kramerius.services.iterators.MigrationIterator;
import cz.inovatika.kramerius.services.workers.MigrationIndexFeeder;
import cz.inovatika.kramerius.services.workers.MigrationIndexFeederFinisher;
import cz.inovatika.kramerius.services.workers.factories.MigrationIndexFeederFactory;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

import java.util.List;

public class SimpleCopySolrMigrationIndexFeederFactory extends MigrationIndexFeederFactory {

    @Override
    public MigrationIndexFeeder createFeeder(MigrationConfig migrationConfig, MigrationIterator iteratorInstance, CloseableHttpClient client, ApacheHTTPRequestEnricher enricher, List<IterationItem> pids, MigrationIndexFeederFinisher finisher) {
        return  new SimpleCopyMigrationIndexFeeder(migrationConfig, client, enricher,  pids, finisher);
    }

    @Override
    public MigrationIndexFeederFinisher createFinisher(MigrationConfig migrationConfig, CloseableHttpClient client) {
        return new SimpleCopySolrFinisher(migrationConfig, client);
    }
}
