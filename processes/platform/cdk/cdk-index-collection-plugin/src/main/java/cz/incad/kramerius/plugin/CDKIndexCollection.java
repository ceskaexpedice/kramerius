package cz.incad.kramerius.plugin;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import cz.incad.kramerius.cdkcollection.*;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;

import java.util.logging.Logger;

/**
 * CDK Index Collection starter
 */
public class CDKIndexCollection {
    private static final Logger LOGGER = Logger.getLogger(CDKIndexCollection.class.getName());

    @ProcessMethod
    public static void indexCollectionMain() {
        SearchIndexService searchIndexService = new SolrSearchIndexService(createClient());
        Injector injector = Guice.createInjector(new RepoModule());
        AkubraRepository akubraRepository = injector.getInstance(Key.get(AkubraRepository.class));
        ProcessingIndexService processingIndexService = new SolrProcessingIndexService(akubraRepository);
        CDKCollectionSynchronizer cdkCollectionSynchronizer = new CDKCollectionSynchronizer(searchIndexService, processingIndexService);
        cdkCollectionSynchronizer.synchronize();
    }

    private static HttpSolrClient createClient() {
        KConfiguration config = KConfiguration.getInstance();
        String solrSearchHost = config.getSolrSearchHost();
        return new HttpSolrClient.Builder(solrSearchHost).build();
    }
}
