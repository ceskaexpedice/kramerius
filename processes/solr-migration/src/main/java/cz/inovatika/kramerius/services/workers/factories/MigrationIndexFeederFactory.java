package cz.inovatika.kramerius.services.workers.factories;

import cz.inovatika.kramerius.services.config.MigrationConfig;
import cz.inovatika.kramerius.services.iterators.ApacheHTTPRequestEnricher;
import cz.inovatika.kramerius.services.workers.MigrationIndexFeeder;
import cz.inovatika.kramerius.services.workers.MigrationIndexFeederFinisher;
import cz.inovatika.kramerius.services.iterators.IterationItem;
import cz.inovatika.kramerius.services.iterators.MigrationIterator;

import cz.inovatika.kramerius.services.workers.config.FeederConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;


import java.util.List;
import java.util.logging.Logger;

public abstract class MigrationIndexFeederFactory {

    public static final Logger LOGGER = Logger.getLogger(MigrationIndexFeederFactory.class.getName());

    public abstract MigrationIndexFeeder createFeeder(MigrationConfig migrationConfig, MigrationIterator iteratorInstance, CloseableHttpClient client, ApacheHTTPRequestEnricher enricher, List<IterationItem> pids, MigrationIndexFeederFinisher finisher);

    public abstract MigrationIndexFeederFinisher createFinisher(MigrationConfig migrationConfig, CloseableHttpClient client);

    public static MigrationIndexFeederFactory create(String instanceName) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        LOGGER.info(String.format("Creating factory %s", instanceName));
        Class<MigrationIndexFeederFactory> aClass = (Class<MigrationIndexFeederFactory>) Class.forName(instanceName);
        return aClass.newInstance();
    }

    public static MigrationIndexFeederFactory create(FeederConfig config) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        return create(config.getFactoryClz());
    }
}
