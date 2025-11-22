package cz.inovatika.kramerius.services.workers.copy.simple;

import com.sun.jersey.api.client.Client;
import cz.inovatika.kramerius.services.config.ProcessConfig;
import cz.inovatika.kramerius.services.utils.SolrUtils;
import cz.inovatika.kramerius.services.workers.WorkerFinisher;

import java.util.logging.Logger;

public class SimpleCopySolrFinisher extends WorkerFinisher {

    public static final Logger LOGGER = Logger.getLogger(SimpleCopySolrFinisher.class.getName());

    public SimpleCopySolrFinisher(ProcessConfig config, Client client) {
        super(config, client);
    }

    @Override
    public void finish() {
        LOGGER.info("Copy finished");
        SolrUtils.commitJersey(this.client, this.processConfig.getWorkerConfig().getDestinationConfig().getDestinationUrl());
    }
}
