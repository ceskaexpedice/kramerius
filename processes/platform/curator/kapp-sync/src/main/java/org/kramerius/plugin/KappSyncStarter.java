package org.kramerius.plugin;

import cz.inovatika.kapp.KAPPFetch;
import org.apache.solr.client.solrj.SolrServerException;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;

import java.io.IOException;
import java.util.logging.Logger;

public class KappSyncStarter {

    public static final Logger LOGGER = Logger.getLogger(KappSyncStarter.class.getName());

    @ProcessMethod
    public static void kappSyncMain() throws SolrServerException, IOException, InterruptedException {
        KAPPFetch.kappFetchMain();
    }
}
