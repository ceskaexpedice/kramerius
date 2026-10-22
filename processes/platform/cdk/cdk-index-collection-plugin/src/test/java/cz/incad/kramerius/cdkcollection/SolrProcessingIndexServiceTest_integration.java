
package cz.incad.kramerius.cdkcollection;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

@Ignore
public class SolrProcessingIndexServiceTest_integration {
    private AkubraRepository akubraRepository;

    @Before
    public void setUp() {
        Injector injector = Guice.createInjector(new SolrModule(), new RepoModule(), new NullStatisticsModule());
        akubraRepository = injector.getInstance(Key.get(AkubraRepository.class));
    }

    @Test
    public void testGetCollections() {
        SolrProcessingIndexService solrProcessingIndexService = new SolrProcessingIndexService(akubraRepository);
        List<String> collections = solrProcessingIndexService.getCollections();
        System.out.println(collections);
    }

    @Test
    public void testGetReferences() {
        SolrProcessingIndexService solrProcessingIndexService = new SolrProcessingIndexService(akubraRepository);
        List<String> references = solrProcessingIndexService.getReferences("uuid:3b358ab4-33a6-4234-bf12-4161df3e0147");
        System.out.println(references);
    }

    @Test
    public void testGetParent() {
        SolrProcessingIndexService solrProcessingIndexService = new SolrProcessingIndexService(akubraRepository);
        List<String> parentCollections = solrProcessingIndexService.getParentCollections("uuid:3b358ab4-33a6-4234-bf12-4161df3e0147");
        System.out.println(parentCollections);
    }

    @Test
    public void testIsCollection() {
        SolrProcessingIndexService solrProcessingIndexService = new SolrProcessingIndexService(akubraRepository);
        boolean isCollection = solrProcessingIndexService.isCollection("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        System.out.println(isCollection);
    }

}
