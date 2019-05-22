package cz.incad.kramerius.virtualcollections.impl.fedora;

import com.google.inject.Key;
import com.google.inject.name.Names;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.fedora.om.Repository;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.resourceindex.IResourceIndex;
import cz.incad.kramerius.resourceindex.ProcessingIndexFeeder;
import cz.incad.kramerius.virtualcollections.Collection;
import cz.incad.kramerius.virtualcollections.CollectionException;
import cz.incad.kramerius.virtualcollections.CollectionUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.easymock.EasyMock;
import org.junit.*;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cz.incad.kramerius.ITTestsSetup.*;
import static org.easymock.EasyMock.isA;

public class FedoraCollectionsManagerImplTest {

    @BeforeClass
    public static void beforeClass() throws Exception {
        setUpBeforeClass();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        tearDownAfterClass();
    }

    @Before
    public void setUpTest() throws Exception {
        setUp();
    }

    @Test
    public void testCreateAndDeleteCollection() throws IOException, XPathExpressionException, CollectionException, RepositoryException, InterruptedException, SolrServerException {
        IResourceIndex ri = injector.getInstance(IResourceIndex.class);
        ProcessingIndexFeeder feeder = injector.getInstance(ProcessingIndexFeeder.class);
        Repository repository = getRepository(feeder);

        Assert.assertNotNull(ri);
        FedoraAccess fa = injector.getInstance(Key.get(FedoraAccess.class, Names.named("rawFedoraAccess")));
        SolrAccess sa = null;


        FedoraCollectionsManagerImpl inst = EasyMock.createMockBuilder(FedoraCollectionsManagerImpl.class)
                .withConstructor(IResourceIndex.class, SolrAccess.class, FedoraAccess.class)
                .withArgs(ri, null, fa)
                .addMockedMethod("enhanceNumberOfDocs")
                .createMock();

        inst.enhanceNumberOfDocs(isA(Collection.class));
        EasyMock.expectLastCall().anyTimes();

        EasyMock.replay(inst);

        //clean up
        List<Collection> collections = inst.getCollections();
        collections.stream().forEach((col)->{
            try {
                repository.deleteobject(col.getPid());
            } catch (RepositoryException e) {
                throw new  RuntimeException(e);
            }
        });
        fa.getInternalAPI().getProcessingIndexFeeder().commit();

        Map<String, String> plainTexts = new HashMap<>();
        plainTexts.put("cs","Prvni ceska veta");
        plainTexts.put("en","The first czech sentence");

        String pid = CollectionUtils.create(fa, "Title", true, plainTexts, null);

        collections = inst.getCollections();
        Assert.assertTrue(collections.size() == 1);
        Assert.assertTrue(collections.get(0).getPid().equals(pid));
        Assert.assertTrue(collections.get(0).isCanLeaveFlag());
        Assert.assertTrue(collections.get(0).getLabel().equals("Title"));

        CollectionUtils.modify(pid,"The new title",false, fa);

        collections = inst.getCollections();
        System.out.println(collections);
        Assert.assertTrue(collections.size() == 1);
        Assert.assertTrue(collections.get(0).getPid().equals(pid));
        Assert.assertFalse(collections.get(0).isCanLeaveFlag());
        Assert.assertTrue(collections.get(0).getLabel().equals("The new title"));

        repository.deleteobject(pid);

    }

}
