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
import org.apache.commons.codec.binary.Hex;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.Hash;
import org.junit.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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
    public void testCreateObject() throws RepositoryException, IOException {
        ProcessingIndexFeeder feeder = this.injector.getInstance(ProcessingIndexFeeder.class);
        Repository repository = getRepository(feeder);
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
        Repository repository = getRepository(feeder);
        if (repository.objectExists("uuid:d1e8361c-8933-4c24-b7c9-7a1c65c83ba8")) {
            repository.deleteobject("uuid:d1e8361c-8933-4c24-b7c9-7a1c65c83ba8");
        }
        RepositoryObject object = repository.createOrFindObject("uuid:d1e8361c-8933-4c24-b7c9-7a1c65c83ba8");
        RepositoryDatastream stream = object.createStream("RELS-EXT", "text/xml", new ByteArrayInputStream(bytes));

        feeder.commit();

        QueryResponse response = solrServer.query(new SolrQuery("*:*"));
        SolrDocumentList results = response.getResults();
        Assert.assertTrue(results.getNumFound() == 1);

        //Document metadata = object.getMetadata();
        //Element pid = XMLUtils.findElement(metadata.getDocumentElement(), "PID", FedoraNamespaces.FEDORA_FOXML_URI);
        //Assert.assertEquals(pid.getTextContent(), "uuid:d1e8361c-8933-4c24-b7c9-7a1c65c83ba8");

        AkubraObject akubraObject = (AkubraObject) object;
        Assert.assertEquals("uuid:d1e8361c-8933-4c24-b7c9-7a1c65c83ba8", akubraObject.digitalObject.getPID());

        Assert.assertTrue(akubraObject.relationsExists("hasModel","info:fedora/fedora-system:def/model#"));
        //Element model = XMLUtils.findElement(metadata.getDocumentElement(), "hasModel","info:fedora/fedora-system:def/model#");
        //Assert.assertEquals("http://localhost:18080/rest/model/page",model.getAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#","resource"));
    }

    @Test
    public void testMonographRELSEXT() throws RepositoryException, IOException, SolrServerException, TransformerException, ParserConfigurationException, SAXException {
        byte[] bytes = resources.get("monograph-RELS-EXT.xml");

        ProcessingIndexFeeder feeder = this.injector.getInstance(ProcessingIndexFeeder.class);
        Repository repository = getRepository(feeder);
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


        // relsext peages
        List<String> pages = XMLUtils.getElementsRecursive(XMLUtils.parseDocument(object.getStream(FedoraUtils.RELS_EXT_STREAM).getContent(),true).getDocumentElement(), (element) -> {
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
    public void testMonographRELSEXT_RemoveRelation_RelsExt() throws RepositoryException, IOException, SolrServerException, TransformerException, ParserConfigurationException, SAXException {
        byte[] bytes = resources.get("monograph-RELS-EXT.xml");

        ProcessingIndexFeeder feeder = this.injector.getInstance(ProcessingIndexFeeder.class);
        Repository repository = getRepository(feeder);
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
        Repository repository = getRepository(feeder);
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
    public void testMonographRELSEXT_Rebuild_ProcessingIndex() throws RepositoryException, IOException, SolrServerException, TransformerException, ParserConfigurationException, SAXException, NoSuchAlgorithmException {
        byte[] bytes = resources.get("monograph-RELS-EXT.xml");

        ProcessingIndexFeeder feeder = this.injector.getInstance(ProcessingIndexFeeder.class);
        Repository repository = getRepository(feeder);
        if (repository.objectExists("uuid:5035a48a-5e2e-486c-8127-2fa650842e46")) {
            repository.deleteobject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        }
        RepositoryObject object = repository.createOrFindObject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        object.createStream("RELS-EXT", "text/xml", new ByteArrayInputStream(bytes));

        feeder.commit();

        QueryResponse response = solrServer.query(new SolrQuery("source:\"uuid:5035a48a-5e2e-486c-8127-2fa650842e46\" AND type:\"relation\"").setRows(100));
        List<Object> targets = response.getResults().stream().map((doc) -> doc.getFieldValue("targetPid")).collect(Collectors.toList());
        Assert.assertTrue(targets.contains("uuid:12993b4a-71b4-4f19-8953-0701243cc25d"));

        List<SolrDocument> beforeRebuilding = new ArrayList<>();
        feeder.iterateProcessing("*:*" , (doc)->{
            beforeRebuilding.add(doc);
        });

        System.out.println(beforeRebuilding.size());
        Assert.assertFalse(beforeRebuilding.isEmpty());

        feeder.deleteProcessingIndex();
        feeder.commit();

        List<SolrDocument> afterClearing = new ArrayList<>();
        System.out.println("Iterate over processing index - after");
        feeder.iterateProcessing("*:*" , (doc)->{
            afterClearing.add(doc);
        });
        Assert.assertTrue(afterClearing.isEmpty());

        repository.getObject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46").rebuildProcessingIndex();
        feeder.commit();


        List<SolrDocument> afterRebuilding = new ArrayList<>();
        feeder.iterateProcessing("*:*" , (doc)->{
            afterRebuilding.add(doc);
        });

        MessageDigest md5 = MessageDigest.getInstance("MD5");

        Assert.assertTrue(afterRebuilding.size() == beforeRebuilding.size());
        List<String> beforeRebuildingHashes = beforeRebuilding.stream().map((doc)->  {
            try {
                StringBuilder builder = new StringBuilder();
                builder.append(doc.getFieldValue("source"));
                builder.append(doc.getFieldValue("type"));
                String type = doc.getFieldValue("type").toString();
                if (type.equals("description")) {
                    builder.append(doc.getFieldValue("model").toString());
                    builder.append(doc.getFieldValue("ref").toString());
                } else {
                    builder.append(doc.getFieldValue("relation").toString());
                    builder.append(doc.getFieldValue("targetPid").toString());
                }

                byte[] digest = md5.digest(builder.toString().getBytes("UTF-8"));
                return new String(Hex.encodeHex(digest));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
        List<String> afterRebuildingHashes = afterRebuilding.stream().map((doc)->  {
            try {
                StringBuilder builder = new StringBuilder();
                builder.append(doc.getFieldValue("source"));
                builder.append(doc.getFieldValue("type"));
                String type = doc.getFieldValue("type").toString();
                if (type.equals("description")) {
                    builder.append(doc.getFieldValue("model").toString());
                    builder.append(doc.getFieldValue("ref").toString());
                } else {
                    builder.append(doc.getFieldValue("relation").toString());
                    builder.append(doc.getFieldValue("targetPid").toString());
                }
                byte[] digest = md5.digest(builder.toString().getBytes("UTF-8"));
                return new String(Hex.encodeHex(digest));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());


        beforeRebuildingHashes.stream().forEach(hash->{
            Assert.assertTrue(afterRebuildingHashes.contains(hash));
        });
    }


    @Test
    public void testMonographSetFlag_TilesUrl() throws RepositoryException, IOException, SolrServerException, TransformerException, ParserConfigurationException, SAXException {
        byte[] bytes = resources.get("monograph-RELS-EXT.xml");

        ProcessingIndexFeeder feeder = this.injector.getInstance(ProcessingIndexFeeder.class);
        Repository repository = getRepository(feeder);
        if (repository.objectExists("uuid:5035a48a-5e2e-486c-8127-2fa650842e46")) {
            repository.deleteobject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        }
        RepositoryObject object = repository.createOrFindObject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        object.createStream("RELS-EXT", "text/xml", new ByteArrayInputStream(bytes));

        feeder.commit();

        Assert.assertFalse(object.relationsExists("tiles-url", FedoraNamespaces.KRAMERIUS_URI));

        object.addLiteral("tiles-url", FedoraNamespaces.KRAMERIUS_URI, "http://seznam.cz");
        Assert.assertTrue(object.relationsExists("tiles-url", FedoraNamespaces.KRAMERIUS_URI));

        Element tilesUrl = XMLUtils.findElement(XMLUtils.parseDocument(object.getStream(FedoraUtils.RELS_EXT_STREAM).getContent(), true).getDocumentElement(), (elm) -> {
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
        Repository repository = getRepository(feeder);
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
    }

    @Test
    public void testMonographAddAndRemoveCollection() throws RepositoryException, IOException, SolrServerException, TransformerException, ParserConfigurationException, SAXException {
        byte[] bytes = resources.get("monograph-RELS-EXT.xml");

        ProcessingIndexFeeder feeder = this.injector.getInstance(ProcessingIndexFeeder.class);
        Repository repository = getRepository(feeder);
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
        Repository repository = getRepository(feeder);
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
    //@Ignore
    public void testMonographDeleteOnePage_ProcessIndex() throws RepositoryException, IOException, SolrServerException, TransformerException, ParserConfigurationException, SAXException {
        byte[] bytes = resources.get("monograph-RELS-EXT.xml");

        ProcessingIndexFeeder feeder = this.injector.getInstance(ProcessingIndexFeeder.class);
        Repository repository = getRepository(feeder);
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
