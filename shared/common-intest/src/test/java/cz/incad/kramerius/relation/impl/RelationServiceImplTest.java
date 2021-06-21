package cz.incad.kramerius.relation.impl;

import com.google.inject.Key;
import com.google.inject.name.Names;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.fedora.om.Repository;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.fedora.om.RepositoryObject;
import cz.incad.kramerius.relation.RelationModel;
import cz.incad.kramerius.relation.RelationUtils;
import cz.incad.kramerius.resourceindex.ProcessingIndexFeeder;
import cz.incad.kramerius.service.impl.PolicyServiceImpl;
import cz.incad.kramerius.utils.FedoraUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.*;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import static cz.incad.kramerius.ITTestsSetup.*;
import static cz.incad.kramerius.ITTestsSetup.resources;

public class RelationServiceImplTest {
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
    public void testRelationService() throws RepositoryException, IOException, SolrServerException {
        ProcessingIndexFeeder feeder = injector.getInstance(ProcessingIndexFeeder.class);
        Repository repository = getRepository(feeder);
        Arrays.asList(
                "0eaa6730-9068-11dd-97de-000d606f5dc6",
                "4308eb80-b03b-11dd-a0f6-000d606f5dc6",
                "94a68570-92d6-11dc-be5a-000d606f5dc6").stream().forEach((ident) -> {

            try {

                if (repository.objectExists("uuid:" + ident)) {
                    repository.deleteObject("uuid:" + ident);
                }

                RepositoryObject object = repository.createOrFindObject("uuid:" + ident);
                object.createStream(FedoraUtils.DC_STREAM, "text/xml", new ByteArrayInputStream(resources.get(ident + ".dc.xml")));
                object.createStream(FedoraUtils.BIBLIO_MODS_STREAM, "text/xml", new ByteArrayInputStream(resources.get(ident + ".mods.xml")));
                object.createStream(FedoraUtils.RELS_EXT_STREAM, "text/xml", new ByteArrayInputStream(resources.get(ident + ".xml")));

            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        });
        PolicyServiceImpl inst = new PolicyServiceImpl();
        feeder.commit();

        FedoraAccess fedoraAccess = injector.getInstance(Key.get(FedoraAccess.class, Names.named("rawFedoraAccess")));
        String pid = "uuid:0eaa6730-9068-11dd-97de-000d606f5dc6";
        try {
            Document relsExt = RelationUtils.getRelsExt(pid, fedoraAccess);
            RelationModel editorModel = RelationServiceImpl.Loader.load(pid, relsExt);
            Assert.assertNotNull(editorModel);
            Assert.assertTrue(editorModel.getRelationKinds().size() == 2);
        } catch (Exception ex) {
            throw new IOException("Cannot load relations: " + pid, ex);
        }
    }



}
