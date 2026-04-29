package cz.inovatika.kramerius.services.workers.copy.simple;

import cz.inovatika.kramerius.services.config.MigrationConfig;
import cz.inovatika.kramerius.services.utils.SolrUtils;
import cz.inovatika.kramerius.services.workers.MigrationIndexFeederFinisher;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

import java.util.logging.Logger;

public class SimpleCopySolrFinisher extends MigrationIndexFeederFinisher {

    public static final Logger LOGGER = Logger.getLogger(SimpleCopySolrFinisher.class.getName());

    public SimpleCopySolrFinisher(MigrationConfig config, CloseableHttpClient client) {
        super(config, client);
    }

    @Override
    public void finish() {
        LOGGER.info("Copy finished");
        SolrUtils.commitApache(this.client, this.migrationConfig.getFeederConfig().getDestinationConfig().getDestinationUrl());
    }
}
