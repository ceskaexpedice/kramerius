package cz.incad.kramerius.services;

import org.junit.Assert;
import org.junit.Test;

public class CDKMIgrationTest {

    @Test
    public void testParsOnly() throws Exception{
        final String CONFIG_SOURCE = "/cz/incad/kramerius/services/workers/copy/K7.xml";
        final String DESTINATION_URL = "http://solr-proxy.cdk-val-kramerius.svc.cluster.local:8983/solr/search_v2";
        final String ITERATION_DL = "knav";
        final String ITERATION_ID = "compositeId";
        final String ITERATION_URL = "http://knav-tunnel.cdk-proxy.svc.cluster.local/search/api/cdk/v7.0/forward/sync/solr";
        final boolean ONLY_SHOW_CONFIGURATION = true;
        CDKMigration.migrateMain(CONFIG_SOURCE, DESTINATION_URL, ITERATION_DL, ITERATION_ID, ITERATION_URL, ONLY_SHOW_CONFIGURATION);
    }

}
