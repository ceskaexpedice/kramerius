package cz.incad.kramerius;

import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.fedora.it.ITSupport;
import cz.incad.kramerius.fedora.om.Repository;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.fedora.om.impl.AkubraDOManager;
import cz.incad.kramerius.fedora.om.impl.RepositoryInternalApiTests;
import cz.incad.kramerius.resourceindex.ProcessingIndexFeeder;
import cz.incad.kramerius.resourceindex.ResourceIndexModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.core.CoreContainer;
import org.ehcache.CacheManager;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static cz.incad.kramerius.fedora.om.impl.AkubraRepository.build;

public class ITTestsSetup {

    public static CoreContainer container;
    public static EmbeddedSolrServer solrServer;
    public static Map<String, byte[]> resources = new HashMap<>();
    public static Injector injector;

    public static void setUpBeforeClass() throws Exception {

        resources.put("page-RELS-EXT.xml", IOUtils.toByteArray(RepositoryInternalApiTests.class.getResourceAsStream("page-RELS-EXT.xml")));
        resources.put("monograph-RELS-EXT.xml",IOUtils.toByteArray(RepositoryInternalApiTests.class.getResourceAsStream("monograph-RELS-EXT.xml")));

        Arrays.asList(
            "0eaa6730-9068-11dd-97de-000d606f5dc6",
            "4308eb80-b03b-11dd-a0f6-000d606f5dc6",
            "94a68570-92d6-11dc-be5a-000d606f5dc6").stream().forEach((ident)->{

            String dcName = ident+".dc.xml";
            InputStream dcStream = RepositoryInternalApiTests.class.getResourceAsStream("drobnustky/" + dcName);
            try {
                resources.put(dcName,IOUtils.toByteArray(dcStream));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String modsName = ident+".mods.xml";
            InputStream modsStream = RepositoryInternalApiTests.class.getResourceAsStream("drobnustky/" + modsName);
            try {
                resources.put(modsName,IOUtils.toByteArray(modsStream));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String relsExt = ident+".xml";
            InputStream relsExtStream = RepositoryInternalApiTests.class.getResourceAsStream("drobnustky/" + relsExt);
            try {
                resources.put(relsExt,IOUtils.toByteArray(relsExtStream));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });


        container = new CoreContainer("src/test/resources/cz/incad/kramerius/resourceindex/IT");
        container.load();
        solrServer = new EmbeddedSolrServer( container, "processing" );
        //ITSupport.Commands.CONTROL.command();

    }

    public static void tearDownAfterClass() throws Exception {
        solrServer.close();
    }


    public static void setUp() throws Exception {
        injector = Guice.createInjector(new AbstractModule() {
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

        }, new ResourceIndexModule(), new RepoModule(), new NullStatisticsModule());
        UpdateResponse response = solrServer.deleteByQuery("*:*");
    }

    public static Repository getRepository(ProcessingIndexFeeder feeder) throws IOException, RepositoryException {
        CacheManager cacheManager = injector.getInstance(Key.get(CacheManager.class, Names.named("akubraCacheManager")));
        AkubraDOManager akubraDOManager = new AkubraDOManager(KConfiguration.getInstance(), cacheManager);
        return build(feeder, akubraDOManager);
    }
}
