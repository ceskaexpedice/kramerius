package cz.incad.kramerius.resourceindex.IT;

import com.google.inject.*;
import com.google.inject.name.Named;
import cz.incad.kramerius.resourceindex.*;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.utils.conf.KConfiguration;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
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

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Created by pstastny on 10/20/2017.
 */

public class ProcessingFeederTest {

    public static final Logger LOGGER = Logger.getLogger(ProcessingFeederTest.class.getName());

    private static CoreContainer container;
    private static EmbeddedSolrServer solrServer;

    private Injector injector;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        container = new CoreContainer("src/test/resources/cz/incad/kramerius/resourceindex/IT");
        container.load();
        solrServer = new EmbeddedSolrServer( container, "processing" );
    }

    @AfterClass
    public static void afterClass() throws IOException {
        solrServer.close();
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
    public void testGetObjectsByModel() throws IOException, SolrServerException, ResourceIndexException {
        ProcessingIndexFeeder feeder = this.injector.getInstance(ProcessingIndexFeeder.class);
        feeder.feedDescriptionDocument("uuid:abc-monograph","monograph","Title", "http://localhost:18080/rest", new Date());
        feeder.feedDescriptionDocument("uuid:abc-page","page","Title","http://localhost:18080/rest", new Date());
        feeder.feedDescriptionDocument("uuid:def-page","page","Title","http://localhost:18080/rest", new Date());
        // need to be commited
        feeder.commit();


        IResourceIndex instance = this.injector.getInstance(IResourceIndex.class);
        List<String> monograph = instance.getObjectsByModel("monograph", 10, 0, null, null);
        System.out.println(monograph);
        Assert.assertTrue(monograph.size() == 1);
        Assert.assertTrue(monograph.get(0).equals("uuid:abc-monograph"));

        List<String> pages = instance.getObjectsByModel("page", 10, 0, null, null);
        Assert.assertTrue(pages.size() == 2);
        Assert.assertTrue(pages.get(0).equals("uuid:abc-page"));
        Assert.assertTrue(pages.get(1).equals("uuid:def-page"));
    }


    @Test
    public void testParentPids() throws IOException, SolrServerException, ResourceIndexException {
        ProcessingIndexFeeder feeder = this.injector.getInstance(ProcessingIndexFeeder.class);
        feeder.feedDescriptionDocument("uuid:abc-periodical","periodical","Title", "http://localhost:18080/rest", new Date());
        feeder.feedDescriptionDocument("uuid:abc-periodicalvolume","periodicalvolume","Title", "http://localhost:18080/rest", new Date());
        feeder.feedDescriptionDocument("uuid:abc-periodicalissue","periodicalissue","Title", "http://localhost:18080/rest", new Date());
        feeder.feedDescriptionDocument("uuid:abc-page","page","Title","http://localhost:18080/rest", new Date());
        feeder.feedDescriptionDocument("uuid:def-page","page","Title","http://localhost:18080/rest", new Date());


        feeder.feedRelationDocument("uuid:abc-periodical", "hasVolume","uuid:abc-periodicalvolume");
        feeder.feedRelationDocument("uuid:abc-periodicalvolume", "hasIssue","uuid:abc-periodicalissue");
        feeder.feedRelationDocument("uuid:abc-periodicalissue", "hasPage","uuid:abc-page");
        feeder.feedRelationDocument("uuid:abc-periodicalissue", "hasPage","uuid:def-page");

        feeder.commit();


        IResourceIndex instance = this.injector.getInstance(IResourceIndex.class);
        List<String> parentPids1 = instance.getParentsPids("uuid:abc-page");
        Assert.assertTrue(parentPids1.size() == 1);
        Assert.assertTrue(parentPids1.get(0).equals("uuid:abc-periodicalissue"));

        List<String> parentPids2 = instance.getParentsPids("uuid:def-page");
        Assert.assertTrue(parentPids2.size() == 1);
        Assert.assertTrue(parentPids2.get(0).equals("uuid:abc-periodicalissue"));

    }


    @Test
    public void testFindByTargetPid() throws IOException, SolrServerException, ResourceIndexException {
        ProcessingIndexFeeder feeder = this.injector.getInstance(ProcessingIndexFeeder.class);
        feeder.feedDescriptionDocument("uuid:abc-periodical","periodical","Title", "http://localhost:18080/rest", new Date());
        feeder.feedDescriptionDocument("uuid:abc-periodicalvolume","periodicalvolume","Title", "http://localhost:18080/rest", new Date());
        feeder.feedDescriptionDocument("uuid:abc-periodicalissue","periodicalissue","Title", "http://localhost:18080/rest", new Date());
        feeder.feedDescriptionDocument("uuid:abc-page","page","Title","http://localhost:18080/rest", new Date());
        feeder.feedDescriptionDocument("uuid:def-page","page","Title","http://localhost:18080/rest", new Date());


        feeder.feedRelationDocument("uuid:abc-periodical", "hasVolume","uuid:abc-periodicalvolume");
        feeder.feedRelationDocument("uuid:abc-periodicalvolume", "hasIssue","uuid:abc-periodicalissue");
        feeder.feedRelationDocument("uuid:abc-periodicalissue", "hasPage","uuid:abc-page");
        feeder.feedRelationDocument("uuid:abc-periodicalissue", "hasPage","uuid:def-page");

        feeder.commit();

        List<Pair<String, String>> byTargetPid = feeder.findByTargetPid("uuid:abc-page");
        Assert.assertTrue(byTargetPid.size() == 1);
        Assert.assertTrue(byTargetPid.get(0).getLeft().equals("uuid:abc-periodicalissue"));
        Assert.assertTrue(byTargetPid.get(0).getRight().equals("hasPage"));

    }

    @AfterClass
    public static void tearDownAfterClass() {
        container.shutdown();

        try {
            File dataF = new File("src/test/resources/cz/incad/kramerius/resourceindex/IT/processing/data");
            FileUtils.deleteDirectory(dataF);
            dataF.delete();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }

    }
}
