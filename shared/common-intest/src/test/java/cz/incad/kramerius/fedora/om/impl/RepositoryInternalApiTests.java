package cz.incad.kramerius.fedora.om.impl;

import cz.incad.kramerius.ITTestsSetup;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.fedora.om.Repository;
import cz.incad.kramerius.fedora.om.RepositoryDatastream;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.fedora.om.RepositoryObject;
import cz.incad.kramerius.resourceindex.ProcessingIndexFeeder;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import junit.framework.Assert;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.junit.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static cz.incad.kramerius.fedora.om.impl.Fedora4Repository.*;
import static cz.incad.kramerius.fedora.utils.Fedora4Utils.*;

public class RepositoryInternalApiTests extends ITTestsSetup {

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
    public void testCheckRunning() throws IOException {
        URL url = new URL("http://localhost:18080/rest");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setConnectTimeout(10000);
        int responseCode = urlConnection.getResponseCode();
        Assert.assertTrue(responseCode==200);
    }

    @Test
    public void testCreateObject() throws RepositoryException {
        ProcessingIndexFeeder feeder = this.injector.getInstance(ProcessingIndexFeeder.class);
        Repository repository = build(feeder, false);

        RepositoryObject object = repository.createOrFindObject("uuid:d1e8361c-8933-4c24-b7c9-7a1c65c83ba8");
        boolean trueFlag = repository.objectExists("uuid:d1e8361c-8933-4c24-b7c9-7a1c65c83ba8");
        Assert.assertTrue(trueFlag);

        repository.deleteobject("uuid:d1e8361c-8933-4c24-b7c9-7a1c65c83ba8");
        boolean falseFlag = repository.objectExists("uuid:d1e8361c-8933-4c24-b7c9-7a1c65c83ba8");
        Assert.assertFalse(falseFlag);

    }

    @Test
    public void testPageRELSEXT() throws RepositoryException, IOException, SolrServerException, TransformerException {
        byte[] bytes = resources.get("page-RELS-EXT.xml");

        ProcessingIndexFeeder feeder = this.injector.getInstance(ProcessingIndexFeeder.class);
        Repository repository = build(feeder, false);
        if (repository.objectExists("uuid:d1e8361c-8933-4c24-b7c9-7a1c65c83ba8")) {
            repository.deleteobject("uuid:d1e8361c-8933-4c24-b7c9-7a1c65c83ba8");
        }
        RepositoryObject object = repository.createOrFindObject("uuid:d1e8361c-8933-4c24-b7c9-7a1c65c83ba8");
        RepositoryDatastream stream = object.createStream("RELS-EXT", "text/xml", new ByteArrayInputStream(bytes));

        feeder.commit();

        QueryResponse response = solrServer.query(new SolrQuery("*:*"));
        SolrDocumentList results = response.getResults();
        Assert.assertTrue(results.getNumFound() == 1);

        Document metadata = object.getMetadata();
        Element pid = XMLUtils.findElement(metadata.getDocumentElement(), "PID", FedoraNamespaces.FEDORA_FOXML_URI);
        Assert.assertEquals(pid.getTextContent(), "uuid:d1e8361c-8933-4c24-b7c9-7a1c65c83ba8");

        Element model = XMLUtils.findElement(metadata.getDocumentElement(), "hasModel","info:fedora/fedora-system:def/model#");
        Assert.assertEquals("http://localhost:18080/rest/model/page",model.getAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#","resource"));
    }

    @Test
    public void testMonographRELSEXT() throws RepositoryException, IOException, SolrServerException, TransformerException, ParserConfigurationException, SAXException {
        byte[] bytes = resources.get("monograph-RELS-EXT.xml");

        ProcessingIndexFeeder feeder = this.injector.getInstance(ProcessingIndexFeeder.class);
        Repository repository = build(feeder, false);
        if (repository.objectExists("uuid:5035a48a-5e2e-486c-8127-2fa650842e46")) {
            repository.deleteobject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        }
        RepositoryObject object = repository.createOrFindObject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        RepositoryDatastream stream = object.createStream("RELS-EXT", "text/xml", new ByteArrayInputStream(bytes));

        feeder.commit();

        QueryResponse response = solrServer.query(new SolrQuery("*:*"));
        SolrDocumentList results = response.getResults();
        Assert.assertTrue(results.getNumFound() == 37);

        response = solrServer.query(new SolrQuery("type:\"description\""));
        results = response.getResults();
        Assert.assertTrue(results.getNumFound() == 1);

        response = solrServer.query(new SolrQuery("type:\"relation\""));
        results = response.getResults();
        Assert.assertTrue(results.getNumFound() == 36);

        // metadata
        Document metadata = object.getMetadata();
        List<String> pages = XMLUtils.getElementsRecursive(metadata.getDocumentElement(), (element) -> {
            boolean namespaceEq = element.getNamespaceURI().equals("http://www.nsdl.org/ontologies/relationships#");
            boolean nameEq = element.getLocalName().equals("hasPage");
            return namespaceEq && nameEq;
        }).stream().map((elm)->{
            String reference =elm.getAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#","resource");
            if (reference.startsWith("info:fedora/")) {
                return StringUtils.minus(reference, "info:fedora/");
            } else {
                return reference;
            }
        }).collect(Collectors.toList());
        Assert.assertTrue(pages.size() == 36);

        // relsext peages
        pages = XMLUtils.getElementsRecursive(XMLUtils.parseDocument(object.getStream(FedoraUtils.RELS_EXT_STREAM).getContent(),true).getDocumentElement(), (element) -> {
            boolean namespaceEq = element.getNamespaceURI().equals("http://www.nsdl.org/ontologies/relationships#");
            boolean nameEq = element.getLocalName().equals("hasPage");
            return namespaceEq && nameEq;
        }).stream().map((elm)->{
            String reference =elm.getAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#","resource");
            if (reference.startsWith("info:fedora/")) {
                return StringUtils.minus(reference, "info:fedora/");
            } else {
                return reference;
            }
        }).collect(Collectors.toList());
        Assert.assertTrue(pages.size() == 36);

    }

    @Test
    public void testMonographRELSEXT_RemoveRelation_Metadata() throws RepositoryException, IOException, SolrServerException, TransformerException, ParserConfigurationException, SAXException {
        byte[] bytes = resources.get("monograph-RELS-EXT.xml");

        ProcessingIndexFeeder feeder = this.injector.getInstance(ProcessingIndexFeeder.class);
        Repository repository = build(feeder, false);
        if (repository.objectExists("uuid:5035a48a-5e2e-486c-8127-2fa650842e46")) {
            repository.deleteobject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        }
        RepositoryObject object = repository.createOrFindObject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        object.createStream("RELS-EXT", "text/xml", new ByteArrayInputStream(bytes));

        feeder.commit();

        // metadata check
        Document metadata = object.getMetadata();
        Element oneElement = XMLUtils.findElement(metadata.getDocumentElement(), (element) -> {
            boolean namespaceEq = element.getNamespaceURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#");
            boolean nameEq = element.getLocalName().equals("Description");
            if (namespaceEq && nameEq) {
                return (element.getAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "about").endsWith("data/503/5a4/8a5/e2e/486/c81/272/fa6/508/42e/46#page1"));
            } else return false;
        });

        Assert.assertNotNull(oneElement);
        String ref = XMLUtils.findElement(oneElement, (elm) -> {
            return (elm.getNamespaceURI().equals("http://www.w3.org/2002/07/owl#") && elm.getLocalName().equals("sameAs"));
        }).getAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "resource");

        Assert.assertTrue(ref.endsWith("129/93b/4a7/1b4/4f1/989/530/701/243/cc2/5d"));


        // remove first page
        object.removeRelation("hasPage", FedoraNamespaces.KRAMERIUS_URI,"uuid:12993b4a-71b4-4f19-8953-0701243cc25d");


        // metadata check
        metadata = object.getMetadata();
        oneElement = XMLUtils.findElement(metadata.getDocumentElement(), (element) -> {
            boolean namespaceEq = element.getNamespaceURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#");
            boolean nameEq = element.getLocalName().equals("Description");
            if (namespaceEq && nameEq) {
                return (element.getAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "about").endsWith("data/503/5a4/8a5/e2e/486/c81/272/fa6/508/42e/46#page1"));
            } else return false;
        });
        Assert.assertNotNull(oneElement);
        ref = XMLUtils.findElement(oneElement, (elm) -> {
            return (elm.getNamespaceURI().equals("http://www.w3.org/2002/07/owl#") && elm.getLocalName().equals("sameAs"));
        }).getAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "resource");

        Assert.assertFalse(ref.endsWith("129/93b/4a7/1b4/4f1/989/530/701/243/cc2/5d"));
        // second page should be the first now
        Assert.assertTrue(ref.endsWith(path(normalizePath("uuid:f80d5a2f-c546-4d21-ae64-9845afb324a6"))));
    }

    @Test
    public void testMonographRELSEXT_RemoveRelation_RelsExt() throws RepositoryException, IOException, SolrServerException, TransformerException, ParserConfigurationException, SAXException {
        byte[] bytes = resources.get("monograph-RELS-EXT.xml");

        ProcessingIndexFeeder feeder = this.injector.getInstance(ProcessingIndexFeeder.class);
        Repository repository = build(feeder, false);
        if (repository.objectExists("uuid:5035a48a-5e2e-486c-8127-2fa650842e46")) {
            repository.deleteobject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        }
        RepositoryObject object = repository.createOrFindObject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        object.createStream("RELS-EXT", "text/xml", new ByteArrayInputStream(bytes));

        feeder.commit();

        // rels-ext check
        RepositoryDatastream relsExtStream = object.getStream("RELS-EXT");
        Document relsExt = XMLUtils.parseDocument(relsExtStream.getContent(), true);
        List<String> pages = XMLUtils.getElementsRecursive(relsExt.getDocumentElement(), (element) -> {
            boolean namespaceEq = element.getNamespaceURI().equals("http://www.nsdl.org/ontologies/relationships#");
            boolean nameEq = element.getLocalName().equals("hasPage");
            return namespaceEq && nameEq;
        }).stream().map((elm)->{
            String reference =elm.getAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#","resource");
            if (reference.startsWith("info:fedora/")) {
                return StringUtils.minus(reference, "info:fedora/");
            } else {
                return reference;
            }
        }).collect(Collectors.toList());
        Collections.reverse(pages);
        Assert.assertTrue(pages.get(0).equals("uuid:12993b4a-71b4-4f19-8953-0701243cc25d"));

        // remove first page
        object.removeRelation("hasPage", FedoraNamespaces.KRAMERIUS_URI,"uuid:12993b4a-71b4-4f19-8953-0701243cc25d");

        // relsext stream again
        relsExtStream = object.getStream("RELS-EXT");
        relsExt = XMLUtils.parseDocument(relsExtStream.getContent(), true);

        pages = XMLUtils.getElementsRecursive(relsExt.getDocumentElement(), (element) -> {
            boolean namespaceEq = element.getNamespaceURI().equals("http://www.nsdl.org/ontologies/relationships#");
            boolean nameEq = element.getLocalName().equals("hasPage");
            return namespaceEq && nameEq;
        }).stream().map((elm)->{
            String reference =elm.getAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#","resource");
            if (reference.startsWith("info:fedora/")) {
                return StringUtils.minus(reference, "info:fedora/");
            } else {
                return reference;
            }
        }).collect(Collectors.toList());
        Collections.reverse(pages);
        Assert.assertFalse(pages.get(0).equals("uuid:12993b4a-71b4-4f19-8953-0701243cc25d"));
        Assert.assertFalse(pages.contains("uuid:12993b4a-71b4-4f19-8953-0701243cc25d"));
    }

    @Test
    public void testMonographRELSEXT_RemoveRelation_ProcessingIndex() throws RepositoryException, IOException, SolrServerException, TransformerException, ParserConfigurationException, SAXException {
        byte[] bytes = resources.get("monograph-RELS-EXT.xml");

        ProcessingIndexFeeder feeder = this.injector.getInstance(ProcessingIndexFeeder.class);
        Repository repository = build(feeder, false);
        if (repository.objectExists("uuid:5035a48a-5e2e-486c-8127-2fa650842e46")) {
            repository.deleteobject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        }
        RepositoryObject object = repository.createOrFindObject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        object.createStream("RELS-EXT", "text/xml", new ByteArrayInputStream(bytes));

        feeder.commit();

        QueryResponse response = solrServer.query(new SolrQuery("source:\"uuid:5035a48a-5e2e-486c-8127-2fa650842e46\" AND type:\"relation\"").setRows(100));
        List<Object> targets = response.getResults().stream().map((doc) -> doc.getFieldValue("targetPid")).collect(Collectors.toList());
        Assert.assertTrue(targets.contains("uuid:12993b4a-71b4-4f19-8953-0701243cc25d"));

        // remove first page
        object.removeRelation("hasPage", FedoraNamespaces.KRAMERIUS_URI,"uuid:12993b4a-71b4-4f19-8953-0701243cc25d");
        feeder.commit();

        QueryResponse responseAfter = solrServer.query(new SolrQuery("source:\"uuid:5035a48a-5e2e-486c-8127-2fa650842e46\" AND type:\"relation\"").setRows(100));
        List<Object> targetsAfter = responseAfter.getResults().stream().map((doc) -> doc.getFieldValue("targetPid")).collect(Collectors.toList());
        Assert.assertFalse(targetsAfter.contains("uuid:12993b4a-71b4-4f19-8953-0701243cc25d"));
   }



    @Test
    public void testMonographSetFlag_TilesUrl() throws RepositoryException, IOException, SolrServerException, TransformerException, ParserConfigurationException, SAXException {
        byte[] bytes = resources.get("monograph-RELS-EXT.xml");

        ProcessingIndexFeeder feeder = this.injector.getInstance(ProcessingIndexFeeder.class);
        Repository repository = build(feeder, false);
        if (repository.objectExists("uuid:5035a48a-5e2e-486c-8127-2fa650842e46")) {
            repository.deleteobject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        }
        RepositoryObject object = repository.createOrFindObject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        object.createStream("RELS-EXT", "text/xml", new ByteArrayInputStream(bytes));

        feeder.commit();

        Assert.assertFalse(object.relationsExists("tiles-url", FedoraNamespaces.KRAMERIUS_URI));

        object.addLiteral("tiles-url", FedoraNamespaces.KRAMERIUS_URI, "http://seznam.cz");
        Assert.assertTrue(object.relationsExists("tiles-url", FedoraNamespaces.KRAMERIUS_URI));

        Element tilesUrl = XMLUtils.findElement(object.getMetadata().getDocumentElement(), (elm) -> {
            String namespace = elm.getNamespaceURI();
            String localName = elm.getLocalName();
            return namespace.equals("http://www.nsdl.org/ontologies/relationships#") && (localName.equals("tiles-url"));
        });
        Assert.assertEquals(tilesUrl.getTextContent(), "http://seznam.cz");

        tilesUrl = XMLUtils.findElement(XMLUtils.parseDocument(object.getStream(FedoraUtils.RELS_EXT_STREAM).getContent(), true).getDocumentElement(), (elm) -> {
            String namespace = elm.getNamespaceURI();
            String localName = elm.getLocalName();
            return namespace.equals("http://www.nsdl.org/ontologies/relationships#") && (localName.equals("tiles-url"));
        });
        Assert.assertEquals(tilesUrl.getTextContent(), "http://seznam.cz");

        object.removeRelationsByNameAndNamespace("tiles-url", FedoraNamespaces.KRAMERIUS_URI);
        Assert.assertFalse(object.relationsExists("tiles-url", FedoraNamespaces.KRAMERIUS_URI));
   }

    @Test
    public void testMonographRemoveAllRelationsByNamespace() throws RepositoryException, IOException, SolrServerException, TransformerException, ParserConfigurationException, SAXException {
        byte[] bytes = resources.get("monograph-RELS-EXT.xml");

        ProcessingIndexFeeder feeder = this.injector.getInstance(ProcessingIndexFeeder.class);
        Repository repository = build(feeder, false);
        if (repository.objectExists("uuid:5035a48a-5e2e-486c-8127-2fa650842e46")) {
            repository.deleteobject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        }
        RepositoryObject object = repository.createOrFindObject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        object.createStream("RELS-EXT", "text/xml", new ByteArrayInputStream(bytes));

        feeder.commit();

        object.removeRelationsByNamespace(FedoraNamespaces.KRAMERIUS_URI);

        RepositoryDatastream relsExtStream = object.getStream("RELS-EXT");
        Document relsExt = XMLUtils.parseDocument(relsExtStream.getContent(), true);
        List<String> pages = XMLUtils.getElementsRecursive(relsExt.getDocumentElement(), (element) -> {
            boolean namespaceEq = element.getNamespaceURI().equals("http://www.nsdl.org/ontologies/relationships#");
            boolean nameEq = element.getLocalName().equals("hasPage");
            return namespaceEq && nameEq;
        }).stream().map((elm)->{
            String reference =elm.getAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#","resource");
            if (reference.startsWith("info:fedora/")) {
                return StringUtils.minus(reference, "info:fedora/");
            } else {
                return reference;
            }
        }).collect(Collectors.toList());
        Assert.assertTrue(pages.isEmpty());

        pages = XMLUtils.getElementsRecursive(object.getMetadata().getDocumentElement(), (element) -> {
            boolean namespaceEq = element.getNamespaceURI().equals("http://www.nsdl.org/ontologies/relationships#");
            boolean nameEq = element.getLocalName().equals("hasPage");
            return namespaceEq && nameEq;
        }).stream().map((elm)->{
            String reference =elm.getAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#","resource");
            if (reference.startsWith("info:fedora/")) {
                return StringUtils.minus(reference, "info:fedora/");
            } else {
                return reference;
            }
        }).collect(Collectors.toList());
        Assert.assertTrue(pages.isEmpty());
    }

    @Test
    public void testMonographAddAndRemoveCollection() throws RepositoryException, IOException, SolrServerException, TransformerException, ParserConfigurationException, SAXException {
        byte[] bytes = resources.get("monograph-RELS-EXT.xml");

        ProcessingIndexFeeder feeder = this.injector.getInstance(ProcessingIndexFeeder.class);
        Repository repository = build(feeder, false);
        if (repository.objectExists("uuid:5035a48a-5e2e-486c-8127-2fa650842e46")) {
            repository.deleteobject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        }
        RepositoryObject object = repository.createOrFindObject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        object.createStream("RELS-EXT", "text/xml", new ByteArrayInputStream(bytes));

        feeder.commit();

        object.addRelation("rdf:isMemberOfCollection", FedoraNamespaces.RDF_NAMESPACE_URI,  "vc:5035a48a-5e2e-486c-9129-9fa650842e99");

        feeder.commit();

        QueryResponse response = solrServer.query(new SolrQuery("source:\"uuid:5035a48a-5e2e-486c-8127-2fa650842e46\" AND type:\"relation\" AND relation:\"isMemberOfCollection\"").setRows(100));
        Assert.assertTrue(response.getResults().size() == 1);
        Assert.assertTrue(object.relationExists("isMemberOfCollection",FedoraNamespaces.RDF_NAMESPACE_URI,  "vc:5035a48a-5e2e-486c-9129-9fa650842e99"));
   }

    @Test
    public void testMonographDelete_ProcessIndex() throws RepositoryException, IOException, SolrServerException, TransformerException, ParserConfigurationException, SAXException {
        byte[] bytes = resources.get("monograph-RELS-EXT.xml");

        ProcessingIndexFeeder feeder = this.injector.getInstance(ProcessingIndexFeeder.class);
        Repository repository = build(feeder, false);
        if (repository.objectExists("uuid:5035a48a-5e2e-486c-8127-2fa650842e46")) {
            repository.deleteobject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        }
        RepositoryObject object = repository.createOrFindObject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        object.createStream("RELS-EXT", "text/xml", new ByteArrayInputStream(bytes));

        feeder.commit();

        QueryResponse response = solrServer.query(new SolrQuery("source:\"uuid:5035a48a-5e2e-486c-8127-2fa650842e46\"").setRows(100));
        Assert.assertTrue(response.getResults().getNumFound() == 37);
        repository.deleteobject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");

        feeder.commit();

        response = solrServer.query(new SolrQuery("source:\"uuid:5035a48a-5e2e-486c-8127-2fa650842e46\"").setRows(100));
        Assert.assertTrue(response.getResults().getNumFound() == 0);
    }

    @Test
    @Ignore
    public void testMonographDeleteOnePage_ProcessIndex() throws RepositoryException, IOException, SolrServerException, TransformerException, ParserConfigurationException, SAXException {
        byte[] bytes = resources.get("monograph-RELS-EXT.xml");

        ProcessingIndexFeeder feeder = this.injector.getInstance(ProcessingIndexFeeder.class);
        Repository repository = build(feeder, false);
        if (repository.objectExists("uuid:5035a48a-5e2e-486c-8127-2fa650842e46")) {
            repository.deleteobject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        }
        RepositoryObject object = repository.createOrFindObject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        object.createStream("RELS-EXT", "text/xml", new ByteArrayInputStream(bytes));

        feeder.commit();


        try {
            repository.deleteobject("uuid:86c0425f-11fd-406b-8def-6726ad87982b");
            Assert.fail();
        } catch (RepositoryException e) {
            // ok
            System.out.println(e.getMessage());
        }
    }
}
