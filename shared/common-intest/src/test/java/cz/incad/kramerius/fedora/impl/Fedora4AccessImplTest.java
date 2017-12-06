package cz.incad.kramerius.fedora.impl;
import static cz.incad.kramerius.ITTestsSetup.*;

import com.google.inject.*;
import com.google.inject.name.Names;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.fedora.om.Repository;
import cz.incad.kramerius.fedora.om.RepositoryDatastream;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.fedora.om.RepositoryObject;
import cz.incad.kramerius.resourceindex.ProcessingIndexFeeder;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static cz.incad.kramerius.fedora.om.Repository.build;

public class Fedora4AccessImplTest  {

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
    public void testGetPids() throws RepositoryException, IOException, SolrServerException, TransformerException, ParserConfigurationException, SAXException {
        byte[] bytes = resources.get("monograph-RELS-EXT.xml");
        ProcessingIndexFeeder feeder = injector.getInstance(ProcessingIndexFeeder.class);
        Repository repository = build(feeder, false);
        if (repository.objectExists("uuid:5035a48a-5e2e-486c-8127-2fa650842e46")) {
            repository.deleteobject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        }
        RepositoryObject object = repository.createOrFindObject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        RepositoryDatastream stream = object.createStream("RELS-EXT", "text/xml", new ByteArrayInputStream(bytes));

        feeder.commit();

        FedoraAccess fa = injector.getInstance(Key.get(FedoraAccess.class, Names.named("rawFedoraAccess")));
        Assert.assertNotNull(fa);

        List<String> pids = fa.getPids("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        Assert.assertTrue(pids.get(0).equals("uuid:5035a48a-5e2e-486c-8127-2fa650842e46"));
    }
}