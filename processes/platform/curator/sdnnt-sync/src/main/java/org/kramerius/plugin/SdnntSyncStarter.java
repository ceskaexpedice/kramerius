package org.kramerius.plugin;

import cz.inovatika.sdnnt.SDNNTFetch;
import org.apache.solr.client.solrj.SolrServerException;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;

import java.io.IOException;
import java.util.logging.Logger;

public class SdnntSyncStarter {

    public static final Logger LOGGER = Logger.getLogger(SdnntSyncStarter.class.getName());

    @ProcessMethod
    public static void backupMain() throws SolrServerException, IOException, InterruptedException {
        SDNNTFetch.sdnntFetchMain();
    }
}
