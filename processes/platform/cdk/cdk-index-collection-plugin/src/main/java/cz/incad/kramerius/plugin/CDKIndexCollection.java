package cz.incad.kramerius.plugin;

import cz.incad.kramerius.cdkcollection.CDKCollectionSynchronizer;
import cz.incad.kramerius.cdkcollection.ProcessingIndexService;
import cz.incad.kramerius.cdkcollection.SearchIndexService;
import cz.incad.kramerius.cdkcollection.SolrSearchService;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;

import java.util.logging.Logger;

/**
 * CDK Index Collection starter
 */
public class CDKIndexCollection {
    private static final Logger LOGGER = Logger.getLogger(CDKIndexCollection.class.getName());

    @ProcessMethod
    public static void indexCollectionMain() {
        SearchIndexService searchIndexService = new SolrSearchService();
        ProcessingIndexService processingIndexService = null; // TODO
        new CDKCollectionSynchronizer(searchIndexService, processingIndexService);
    }

}
