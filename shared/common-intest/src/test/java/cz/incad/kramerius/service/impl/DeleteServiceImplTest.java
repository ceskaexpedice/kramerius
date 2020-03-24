package cz.incad.kramerius.service.impl;

import static org.easymock.EasyMock.*;

import static cz.incad.kramerius.ITTestsSetup.*;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import com.google.inject.Key;
import com.google.inject.name.Names;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.fedora.om.Repository;
import cz.incad.kramerius.fedora.om.RepositoryDatastream;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.fedora.om.RepositoryObject;
import cz.incad.kramerius.fedora.utils.Fedora4Utils;
import cz.incad.kramerius.resourceindex.IResourceIndex;
import cz.incad.kramerius.resourceindex.ProcessingIndexFeeder;
import cz.incad.kramerius.resourceindex.ResourceIndexException;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.solr.client.solrj.SolrServerException;
import org.easymock.EasyMock;
import org.junit.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.IOException;


public class DeleteServiceImplTest  {

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
    public void testDeleteMonograph() throws RepositoryException, IOException, SolrServerException, TransformerException, ParserConfigurationException, SAXException, ResourceIndexException {

        byte[] bytes = resources.get("monograph-RELS-EXT.xml");
        ProcessingIndexFeeder feeder = injector.getInstance(ProcessingIndexFeeder.class);
        Repository repository = getRepository(feeder);
        if (repository.objectExists("uuid:5035a48a-5e2e-486c-8127-2fa650842e46")) {
            repository.deleteobject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        }
        RepositoryObject object = repository.createOrFindObject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        RepositoryDatastream stream = object.createStream("RELS-EXT", "text/xml", new ByteArrayInputStream(bytes));

        feeder.commit();

        DeleteServiceImpl inst = EasyMock.createMockBuilder(DeleteServiceImpl.class).withConstructor()
                .addMockedMethod("spawnIndexer")
                .addMockedMethod("spawnIndexRemover")
                .createMock();

        inst.spawnIndexer(isA(String.class),isA(String.class));
        EasyMock.expectLastCall().anyTimes();

        inst.spawnIndexRemover(isA(String.class));
        EasyMock.expectLastCall().anyTimes();

        EasyMock.replay(inst);


        FedoraAccess fa = injector.getInstance(Key.get(FedoraAccess.class, Names.named("rawFedoraAccess")));
        Assert.assertNotNull(fa);


        inst.resourceIndex = injector.getInstance(IResourceIndex.class);
        inst.fedoraAccess = fa;
        inst.predicates =  Lists.transform(KConfiguration.getInstance().getConfiguration().getList("fedora.treePredicates"), Functions.toStringFunction());

        Fedora4Utils.doWithProcessingIndexCommit(inst.fedoraAccess.getInternalAPI(), (repo)->{
            try {
                inst.deleteTree(repo,"uuid:5035a48a-5e2e-486c-8127-2fa650842e46", "uuid:5035a48a-5e2e-486c-8127-2fa650842e46", "Marked as deleted", false, false );
            } catch (IOException e) {
                throw new RepositoryException(e);
            } catch (ResourceIndexException e) {
                throw new RepositoryException(e);
            } catch (SolrServerException e) {
                throw new RepositoryException(e);
            }
        });

        fa.isObjectAvailable("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
    }

    @Test
    public void testDeleteOnePage() throws RepositoryException, IOException, SolrServerException, TransformerException, ParserConfigurationException, SAXException, ResourceIndexException {


        byte[] bytes = resources.get("monograph-RELS-EXT.xml");
        ProcessingIndexFeeder feeder = injector.getInstance(ProcessingIndexFeeder.class);
        Repository repository = getRepository(feeder);
        if (repository.objectExists("uuid:5035a48a-5e2e-486c-8127-2fa650842e46")) {
            repository.deleteobject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        }
        RepositoryObject object = repository.createOrFindObject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        RepositoryDatastream stream = object.createStream("RELS-EXT", "text/xml", new ByteArrayInputStream(bytes));

        feeder.commit();

        DeleteServiceImpl inst = EasyMock.createMockBuilder(DeleteServiceImpl.class).withConstructor()
                .addMockedMethod("spawnIndexer")
                .addMockedMethod("spawnIndexRemover")
                .createMock();

        inst.spawnIndexer(isA(String.class),isA(String.class));
        EasyMock.expectLastCall().anyTimes();

        inst.spawnIndexRemover(isA(String.class));
        EasyMock.expectLastCall().anyTimes();

        EasyMock.replay(inst);


        FedoraAccess fa = injector.getInstance(Key.get(FedoraAccess.class, Names.named("rawFedoraAccess")));
        Assert.assertNotNull(fa);


        inst.resourceIndex = injector.getInstance(IResourceIndex.class);
        inst.fedoraAccess = fa;
        inst.predicates =  Lists.transform(KConfiguration.getInstance().getConfiguration().getList("fedora.treePredicates"), Functions.toStringFunction());

        Document document = XMLUtils.parseDocument(repository.getObject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46").getStream(FedoraUtils.RELS_EXT_STREAM).getContent(), true);
        Assert.assertNotNull(XMLUtils.findElement(document.getDocumentElement(), (element) -> {
            if (element.getNamespaceURI().equals(FedoraNamespaces.KRAMERIUS_URI)) {
                return (element.getLocalName().equals("hasPage") && element.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource").equals("info:fedora/uuid:86c0425f-11fd-406b-8def-6726ad87982b"));
            }
            return false;
        }));
        Fedora4Utils.doWithProcessingIndexCommit(inst.fedoraAccess.getInternalAPI(), (repo)->{
            try {
                inst.deleteTree(repo,"uuid:86c0425f-11fd-406b-8def-6726ad87982b", "uuid:5035a48a-5e2e-486c-8127-2fa650842e46/uuid:86c0425f-11fd-406b-8def-6726ad87982b", "Marked as deleted", false, false);
            } catch (IOException e) {
                throw new RepositoryException(e);
            } catch (ResourceIndexException e) {
                throw new RepositoryException(e);
            } catch (SolrServerException e) {
                throw new RepositoryException(e);
            }
        });
        fa.isObjectAvailable("uuid:86c0425f-11fd-406b-8def-6726ad87982b");

        document = XMLUtils.parseDocument(repository.getObject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46").getStream(FedoraUtils.RELS_EXT_STREAM).getContent(), true);
        Assert.assertNull(XMLUtils.findElement(document.getDocumentElement(), (element) -> {
            if (element.getNamespaceURI().equals(FedoraNamespaces.KRAMERIUS_URI)) {
                return (element.getLocalName().equals("hasPage") && element.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource").equals("info:fedora/uuid:86c0425f-11fd-406b-8def-6726ad87982b"));
            }
            return false;
        }));
    }
}
