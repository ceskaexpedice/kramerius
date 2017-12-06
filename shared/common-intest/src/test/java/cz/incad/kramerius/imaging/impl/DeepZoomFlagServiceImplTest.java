package cz.incad.kramerius.imaging.impl;

import com.google.inject.Key;
import com.google.inject.name.Names;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.fedora.om.Repository;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.fedora.om.RepositoryObject;
import cz.incad.kramerius.resourceindex.ProcessingIndexFeeder;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.XMLUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import static cz.incad.kramerius.ITTestSupport.*;
import static cz.incad.kramerius.ITTestSupport.resources;
import static cz.incad.kramerius.utils.XMLUtils.findElement;

public class DeepZoomFlagServiceImplTest {

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
    public void deleteAndSetFlag() throws RepositoryException, IOException, SolrServerException, TransformerException, ParserConfigurationException, SAXException {
        ProcessingIndexFeeder feeder = injector.getInstance(ProcessingIndexFeeder.class);
        Repository repository = Repository.build(feeder, false);
        Arrays.asList(
                "0eaa6730-9068-11dd-97de-000d606f5dc6",
                "4308eb80-b03b-11dd-a0f6-000d606f5dc6",
                "94a68570-92d6-11dc-be5a-000d606f5dc6").stream().forEach((ident)-> {

            try {

                if (repository.objectExists("uuid:"+ident)) {
                    repository.deleteobject("uuid:"+ident);
                }

                RepositoryObject object = repository.createOrFindObject("uuid:"+ident);
                object.createStream(FedoraUtils.DC_STREAM,"text/xml", new ByteArrayInputStream(resources.get(ident+".dc.xml")));
                object.createStream(FedoraUtils.BIBLIO_MODS_STREAM,"text/xml", new ByteArrayInputStream(resources.get(ident+".mods.xml")));
                object.createStream(FedoraUtils.RELS_EXT_STREAM,"text/xml", new ByteArrayInputStream(resources.get(ident+".xml")));

            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        });

        feeder.commit();


        FedoraAccess fa = injector.getInstance(Key.get(FedoraAccess.class, Names.named("rawFedoraAccess")));
        DeepZoomFlagServiceImpl service = new DeepZoomFlagServiceImpl();
        service.fedoraAccess = fa;
        service.deleteFlagToPID("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6");

        checkNoExistence(repository.getObject("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6").getMetadata());
        checkNoExistence(XMLUtils.parseDocument(repository.getObject("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6").getStream(FedoraUtils.RELS_EXT_STREAM).getContent(), true));

        service.setFlagToPID("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6","kramerius4://deepZoomCache");


        checkExistence(repository.getObject("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6").getMetadata());
        checkExistence(XMLUtils.parseDocument(repository.getObject("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6").getStream(FedoraUtils.RELS_EXT_STREAM).getContent(), true));
    }

    private void checkExistence(Document document) throws IOException, SAXException, ParserConfigurationException {
        Element policy = findElement(document.getDocumentElement(), (elm) -> {
            if (elm.getLocalName().equals("tiles-url") && elm.getNamespaceURI().equals("http://www.nsdl.org/ontologies/relationships#")) {
                return true;
            }
            return false;
        });
        Assert.assertNotNull(policy);
    }
    private void checkNoExistence(Document document) throws IOException, SAXException, ParserConfigurationException {
        Element policy = findElement(document.getDocumentElement(), (elm) -> {
            if (elm.getLocalName().equals("tiles-url") && elm.getNamespaceURI().equals("http://www.nsdl.org/ontologies/relationships#")) {
                return true;
            }
            return false;
        });
        Assert.assertNull(policy);
    }

}
