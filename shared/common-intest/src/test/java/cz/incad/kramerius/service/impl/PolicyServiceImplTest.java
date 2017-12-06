package cz.incad.kramerius.service.impl;

import static cz.incad.kramerius.ITTestsSetup.*;
import static cz.incad.kramerius.fedora.om.Repository.build;
import static cz.incad.kramerius.utils.XMLUtils.*;

import com.google.inject.Key;
import com.google.inject.name.Names;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.fedora.om.Repository;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.fedora.om.RepositoryObject;
import cz.incad.kramerius.resourceindex.ProcessingIndexFeeder;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
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

public class PolicyServiceImplTest {

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
    public void testPolicy() throws IOException, SolrServerException, RepositoryException, ParserConfigurationException, SAXException, TransformerException {
        ProcessingIndexFeeder feeder = injector.getInstance(ProcessingIndexFeeder.class);
        Repository repository = build(feeder, false);
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
        PolicyServiceImpl inst = new PolicyServiceImpl();
        feeder.commit();

        inst.fedoraAccess = injector.getInstance(Key.get(FedoraAccess.class, Names.named("rawFedoraAccess")));

        inst.configuration = KConfiguration.getInstance();
        inst.setPolicy("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6", "private");
        feeder.commit();

        Arrays.asList(
                "0eaa6730-9068-11dd-97de-000d606f5dc6",
                "4308eb80-b03b-11dd-a0f6-000d606f5dc6",
                "94a68570-92d6-11dc-be5a-000d606f5dc6").stream().forEach((pid)->{

            try {
                Document relsExt = XMLUtils.parseDocument(repository.getObject(pid).getStream(FedoraUtils.RELS_EXT_STREAM).getContent(), true);
                checkRELS_EXT(relsExt,"policy:private");

                Document dc = XMLUtils.parseDocument(repository.getObject(pid).getStream(FedoraUtils.DC_STREAM).getContent(), true);
                checkDC(dc,"policy:private");

                Document metadata = repository.getObject(pid).getMetadata();
                checkRELS_EXT(metadata,"policy:private");

            } catch (ParserConfigurationException e) {
                throw new RuntimeException(e);
            } catch (SAXException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        });

        inst.setPolicy("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6", "public");
        feeder.commit();

        Arrays.asList(
                "0eaa6730-9068-11dd-97de-000d606f5dc6",
                "4308eb80-b03b-11dd-a0f6-000d606f5dc6",
                "94a68570-92d6-11dc-be5a-000d606f5dc6").stream().forEach((pid)->{

            try {
                Document relsExt = XMLUtils.parseDocument(repository.getObject(pid).getStream(FedoraUtils.RELS_EXT_STREAM).getContent(), true);
                checkRELS_EXT(relsExt,"policy:public");

                Document dc = XMLUtils.parseDocument(repository.getObject(pid).getStream(FedoraUtils.DC_STREAM).getContent(), true);
                checkDC(dc,"policy:public");

                Document metadata = repository.getObject(pid).getMetadata();
                checkRELS_EXT(metadata,"policy:public");

            } catch (ParserConfigurationException e) {
                throw new RuntimeException(e);
            } catch (SAXException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        });

    }

    private void checkDC(Document document, String expectingValue) throws IOException, SAXException, ParserConfigurationException {
        Element policy = findElement(document.getDocumentElement(), (elm) -> {
            if (elm.getLocalName().equals("rights") && elm.getNamespaceURI().equals("http://purl.org/dc/elements/1.1/")) {
                return true;
            }
            return false;
        });
        Assert.assertNotNull(policy);
        Assert.assertEquals(expectingValue, policy.getTextContent());
    }

    private void checkRELS_EXT(Document document, String expectingValue) throws IOException, SAXException, ParserConfigurationException {
        Element policy = findElement(document.getDocumentElement(), (elm) -> {
            if (elm.getLocalName().equals("policy") && elm.getNamespaceURI().equals("http://www.nsdl.org/ontologies/relationships#")) {
                return true;
            }
            return false;
        });
        Assert.assertNotNull(policy);
        Assert.assertEquals(expectingValue, policy.getTextContent());
    }
}
