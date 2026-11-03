package org.kramerius.plugin;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import cz.incad.kramerius.cdkcollections.*;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.kramerius.searchIndex.NewIndexerProcessIndexObject;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;

import java.util.logging.Logger;

public class CDKIndexStarter {

    public static Logger LOGGER = Logger.getLogger(CDKIndexStarter.class.getName());

    @ProcessMethod
    public static void indexerMain(
            @ParameterName("type") @IsRequired String type,
            @ParameterName("pid")  String pidsP,
            @ParameterName("pidlist")  String pidsList,
            @ParameterName("pidlist_file")  String pidListFile,
            @ParameterName("ignoreInconsistentObjects") Boolean ignoreInconsistentObjects,
            @ParameterName("title") String title,
            @ParameterName("forceSync") Boolean forceSync
    ) {

        LOGGER.info("--- Starting method: indexerMain ---");
        LOGGER.info("Parameter 'type': " + type);
        LOGGER.info("Parameter 'pid': " + pidsP);
        // Non-required parameters check for null
        LOGGER.info("Parameter 'ignoreInconsistentObjects': " + (ignoreInconsistentObjects == null ? "N/A" : ignoreInconsistentObjects));
        LOGGER.info("Parameter 'title': " + (title == null ? "N/A" : title));
        LOGGER.info("-----------------------------------");

        NewIndexerProcessIndexObject.indexerMain(type.toUpperCase(), pidsP, ignoreInconsistentObjects, title);

        if (forceSync != null && forceSync) {
            LOGGER.info("--- Starting method: indexCollectionMain ---");
            indexCollectionMain();
        }

    }

    private static void indexCollectionMain() {
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
