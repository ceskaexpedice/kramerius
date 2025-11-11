package cz.inovatika.kramerius.services.workers.copy;

import com.sun.jersey.api.client.Client;
import cz.inovatika.kramerius.services.config.ProcessConfig;
import cz.inovatika.kramerius.services.iterators.utils.KubernetesSolrUtils;
import cz.inovatika.kramerius.services.utils.SolrUtils;
import cz.inovatika.kramerius.services.workers.WorkerFinisher;

public class SimpleCopySolrFinisher extends WorkerFinisher {

    public SimpleCopySolrFinisher(ProcessConfig config, Client client) {
        super(config, client);
    }

    @Override
    public void finish() {
        SolrUtils.commitJersey(this.client, this.processConfig.getWorkerConfig().getDestinationConfig().getDestinationUrl());
    }
}
