package cz.incad.kramerius.resourceindex.IT;

import com.google.inject.*;
import com.google.inject.name.Named;
import com.hp.hpl.jena.sparql.pfunction.library.container;
import cz.incad.kramerius.resourceindex.IResourceIndex;
import cz.incad.kramerius.resourceindex.ProcessingIndexFeeder;
import cz.incad.kramerius.resourceindex.ResourceIndexException;
import cz.incad.kramerius.resourceindex.ResourceIndexModule;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.utils.conf.KConfiguration;
import junit.framework.TestCase;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.core.CoreContainer;
import org.junit.*;

import java.io.IOException;
import java.util.List;

import static com.hp.hpl.jena.enhanced.BuiltinPersonalities.model;

/**
 * Created by pstastny on 10/20/2017.
 */

public class ProcessingFeederTest {

    private static CoreContainer container;
    private static EmbeddedSolrServer solrServer;

    private Injector injector;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        container = new CoreContainer("src/test/resources/cz/incad/kramerius/resourceindex/IT");
        container.load();
        solrServer = new EmbeddedSolrServer( container, "processing" );
    }



    @Before
    public void setUp() throws IOException, SolrServerException {
        this.injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {}

            @Provides
            @Named("processingQuery")
            @Singleton
            public SolrClient processingQueryClient() {
                return solrServer;
            }

            @Provides
            @Named("processingUpdate")
            @Singleton
            public SolrClient processingUpdateClient() {
                return solrServer;
            }

        }, new ResourceIndexModule());
        UpdateResponse response = solrServer.deleteByQuery("*:*");
    }


    @Test
    public void testFeed() throws IOException, SolrServerException, ResourceIndexException {
        ProcessingIndexFeeder feeder = this.injector.getInstance(ProcessingIndexFeeder.class);
        feeder.feedDescriptionDocument("uuid:abc-monograph","monograph","Title","http://localhost:18080/data/ad/bc/cd");
        feeder.feedDescriptionDocument("uuid:abc-page","page","Title","http://localhost:18080/data/bd/bc/cd");
        feeder.feedDescriptionDocument("uuid:def-page","page","Title","http://localhost:18080/data/ed/bc/cd");


        QueryResponse query = solrServer.query(new SolrQuery("source:\"uuid:abc-monograph\""));
        SolrDocumentList results = query.getResults();

        IResourceIndex instance = this.injector.getInstance(IResourceIndex.class);
        List<String> monograph = instance.getObjectsByModel("monograph", 10, 0, null, null);
        Assert.assertTrue(monograph.size() == 1);
        Assert.assertTrue(monograph.get(0).equals("uuid:abc-monograph"));

        List<String> pages = instance.getObjectsByModel("page", 10, 0, null, null);
        Assert.assertTrue(pages.size() == 2);
        Assert.assertTrue(pages.get(0).equals("uuid:abc-page"));
        Assert.assertTrue(pages.get(1).equals("uuid:def-page"));
    }

    @AfterClass
    public static void tearDownAfterClass() {
        container.shutdown();
    }
}
