package cz.incad.kramerius.services;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.utils.XMLUtils;
import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cz.incad.kramerius.services.utils.SolrMockWebCall.webCallExpectJSON;
import static cz.incad.kramerius.services.utils.SolrMockWebCall.webCallExpectXML;
import static org.easymock.EasyMock.*;
import static org.easymock.EasyMock.anyObject;

public class ParallelProcessImplTest_REPLICATE {

    /**
     * Tests the basic creation of a record in the index (copy operation).
     */
    @Test
    public void testCreate_1()
            throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException, SAXException,
            ParserConfigurationException, NoSuchMethodException, MigrateSolrIndexException, URISyntaxException, TransformerException {
        InputStream configurationStream = ParallelProcessImplTest_REPLICATE.class
                .getResourceAsStream("k7/1c/knav.xml");
        String _configurationContent = IOUtils.toString(configurationStream, "UTF-8");
        System.out.println(_configurationContent);

        InputStream logFileStream = ParallelProcessImplTest_REPLICATE.class
                .getResourceAsStream("k7/1c/log");
        String logFileContent = IOUtils.toString(logFileStream, "UTF-8");
        File logFile = File.createTempFile("junit", "logfile");
        FileUtils.write(logFile, logFileContent, "UTF-8");

        String configurationContent = String.format(_configurationContent, logFile.toURI().toURL().toString());
        File configurationFile = File.createTempFile("junit", "conf");
        FileUtils.write(configurationFile, configurationContent, "UTF-8");

        Client client = EasyMock.createMock(Client.class);
        ParallelProcessImpl process = createMockBuilder(ParallelProcessImpl.class).addMockedMethod("buildClient")
                .createMock();

        // --- 1 doc indexed ---
        // uuid:2abf3eaa-f327-44fa-8cb8-873ce3ecafa7
        String firstReq = "http://192.168.10.109:18984/solr-test/kramerius-cdk-test/update/select/?q=pid:(%22uuid%3A2abf3eaa-f327-44fa-8cb8-873ce3ecafa7%22)&fl=pid+cdk.collection+cdk.leader+cdk.collection+cdk.licenses+cdk.licenses_of_ancestors+cdk.contains_licenses++root.pid+compositeId&wt=xml&rows=1";
        String firstResp = IOUtils.toString(this.getClass().getResourceAsStream("k7/1c/cdk_2abf3eaa-f327-44fa-8cb8-873ce3ecafa7.xml"),
                "UTF-8");
        List<Object> mocksFromSecondCall = webCallExpectXML(client, firstReq, firstResp);


        String firstReqFetch = "http://kramerius.lib.cas.cz/search/api/client/v7.0/select/?q=pid:(%22uuid%3A2abf3eaa-f327-44fa-8cb8-873ce3ecafa7%22)&fl=indexer_version+pid+root.pid+model+created+modified+indexed+keywords.*+geographic_*+genres.*+publishers.*+publication_places.*+authors+authors.*+titles.*+title.*+root.*+own_parent.*+own_pid_path+own_model_path+rels_ext_index.sort+foster_parents.pids+in_collections+in_collections.direct+level+pid_paths+date.*+date_range_*++date.str+part.*+issue.*++page.*+id_*+count_*+coords.*+languages.*+physical_locations.*+shelf_locators+accessibility+has_tiles+ds.*+collection.*+mdt+ddt+donator+text_ocr+licenses+contains_licenses+licenses_of_ancestors&wt=xml&rows=1";
        String firstReqResp = IOUtils
                .toString(this.getClass().getResourceAsStream("k7/1c/knav_2abf3eaa-f327-44fa-8cb8-873ce3ecafa7.xml"), "UTF-8");
        List<Object> mocksFromSecondFetchCall = webCallExpectXML(client, firstReqFetch, firstReqResp);

        // ----------
        EasyMock.expect(process.buildClient()).andReturn(client).anyTimes();

        // all updates in one check
        WebResource updateResource = EasyMock.createMock(WebResource.class);
        ClientResponse updateResponse = EasyMock.createMock(ClientResponse.class);
        WebResource.Builder updateResponseBuilder = EasyMock.createMock(WebResource.Builder.class);
        EasyMock.expect(client.resource("http://192.168.10.109:18984/solr-test/kramerius-cdk-test/update"))
                .andReturn(updateResource).anyTimes();
        EasyMock.expect(updateResource.accept(MediaType.TEXT_XML)).andReturn(updateResponseBuilder).anyTimes();
        EasyMock.expect(updateResponseBuilder.type(MediaType.TEXT_XML)).andReturn(updateResponseBuilder).anyTimes();

        Capture firstArg = newCapture();
        EasyMock.expect(updateResponseBuilder.entity(capture(firstArg), EasyMock.eq(MediaType.TEXT_XML)))
                .andReturn(updateResponseBuilder).once();

        EasyMock.expect(updateResponseBuilder.post(ClientResponse.class)).andReturn(updateResponse).anyTimes();
        EasyMock.expect(updateResponse.getStatus()).andReturn(ClientResponse.Status.OK.getStatusCode()).anyTimes();
        EasyMock.expect(updateResponse.getEntityInputStream())
                .andDelegateTo(new ParallelProcessImplTest_REPLICATE.MockClientResponse()).anyTimes();

        // final commit
        WebResource commitResource = EasyMock.createMock(WebResource.class);
        WebResource.Builder commitResourceBuilder = EasyMock.createMock(WebResource.Builder.class);

        EasyMock.expect(client.resource("http://192.168.10.109:18984/solr-test/kramerius-cdk-test/update?commit=true"))
                .andReturn(commitResource).anyTimes();
        EasyMock.expect(commitResource.accept(MediaType.TEXT_XML)).andReturn(commitResourceBuilder).anyTimes();
        EasyMock.expect(commitResourceBuilder.type(MediaType.TEXT_XML)).andReturn(commitResourceBuilder).anyTimes();
        EasyMock.expect(commitResourceBuilder.entity(anyObject(String.class), EasyMock.eq(MediaType.TEXT_XML)))
                .andReturn(commitResourceBuilder).anyTimes();
        EasyMock.expect(commitResourceBuilder.post(String.class)).andReturn("<commited/>").anyTimes();

        EasyMock.replay(client, process, updateResource, updateResponse, updateResponseBuilder, commitResource,
                commitResourceBuilder);

        // check & fetch
        mocksFromSecondCall.stream().forEach(obj -> {
            EasyMock.replay(obj);
        });
        mocksFromSecondFetchCall.stream().forEach(obj -> {
            EasyMock.replay(obj);
        });

        process.setClient(client);

        // start whole process
        process.migrate(configurationFile);

        Document document = XMLUtils.parseDocument(new StringReader(firstArg.getValue().toString()));
        XMLUtils.print(document.getDocumentElement(), System.out);

        List<Element> docs = XMLUtils.getElements(document.getDocumentElement(), new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                return element.getNodeName().equals("doc");
            }
        });
        Assert.assertTrue(docs.size() == 1);
        List<Element> fields = XMLUtils.getElementsRecursive(docs.get(0), (elm) -> {
            return elm.getNodeName().equals("field");
        });

        // no update attribute --
        fields.stream().forEach(elm->{
            Assert.assertFalse(elm.hasAttribute("update"));
        });


        docs.stream().forEach(doc -> {
            List<Element> elements = XMLUtils.getElements(doc, new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    if (element.getNodeName().equals("field")) {
                        return element.getAttribute("name").equals("rels_ext_index.sort");
                    }
                    return false;
                }
            });
            if (!elements.isEmpty()) {
                Assert.assertTrue(elements.size() == 1);
            }
        });
    }


    /**
     * Tests the update of an existing record in the index.
     *
     * This test verifies that the replication service correctly updates an already
     * indexed document by modifying specific fields. The updated fields include:
     * - pid
     * - root.pid
     * - licenses
     * - contains_licenses
     * - licenses_of_ancestors
     * - titles.*
     * - collection.*
     * - in_collections
     * - in_collections.*
     * - title.*
     * - titles.*
     * - text_ocr
     *
     */
    @Test
    public void testUpdate_1()
            throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException, SAXException,
            ParserConfigurationException, NoSuchMethodException, MigrateSolrIndexException, URISyntaxException, TransformerException {
        InputStream configurationStream = ParallelProcessImplTest_REPLICATE.class
                .getResourceAsStream("k7/1u/knav.xml");
        String _configurationContent = IOUtils.toString(configurationStream, "UTF-8");
        System.out.println(_configurationContent);

        InputStream logFileStream = ParallelProcessImplTest_REPLICATE.class
                .getResourceAsStream("k7/1u/log");
        String logFileContent = IOUtils.toString(logFileStream, "UTF-8");
        File logFile = File.createTempFile("junit", "logfile");
        FileUtils.write(logFile, logFileContent, "UTF-8");

        String configurationContent = String.format(_configurationContent, logFile.toURI().toURL().toString());
        File configurationFile = File.createTempFile("junit", "conf");
        FileUtils.write(configurationFile, configurationContent, "UTF-8");

        Client client = EasyMock.createMock(Client.class);
        ParallelProcessImpl process = createMockBuilder(ParallelProcessImpl.class).addMockedMethod("buildClient")
                .createMock();

        // --- 1 doc indexed ---
        // uuid:2abf3eaa-f327-44fa-8cb8-873ce3ecafa7

        String firstReq = "http://192.168.10.109:18984/solr-test/kramerius-cdk-test/update/select/?q=pid:(%22uuid%3A2abf3eaa-f327-44fa-8cb8-873ce3ecafa7%22)&fl=pid+cdk.collection+cdk.leader+cdk.collection+cdk.licenses+cdk.licenses_of_ancestors+cdk.contains_licenses++root.pid+compositeId&wt=xml&rows=1";
        String firstResp = IOUtils.toString(this.getClass().getResourceAsStream("k7/1u/cdk_2abf3eaa-f327-44fa-8cb8-873ce3ecafa7.xml"),
                "UTF-8");
        List<Object> mocksFromSecondCall = webCallExpectXML(client, firstReq, firstResp);


        String firstReqFetch = "http://kramerius.lib.cas.cz/search/api/client/v7.0/select/?q=pid:(%22uuid%3A2abf3eaa-f327-44fa-8cb8-873ce3ecafa7%22)&fl=pid+root.pid+licenses+contains_licenses+licenses_of_ancestors+titles.*+collection.*+in_collections+in_collections.*+title.*+titles.*+text_ocr&wt=xml&rows=1";
        String firstReqResp = IOUtils
                .toString(this.getClass().getResourceAsStream("k7/1u/knav_2abf3eaa-f327-44fa-8cb8-873ce3ecafa7.xml"), "UTF-8");
        List<Object> mocksFromSecondFetchCall = webCallExpectXML(client, firstReqFetch, firstReqResp);

        // ----------
        EasyMock.expect(process.buildClient()).andReturn(client).anyTimes();

        // all updates in one check
        WebResource updateResource = EasyMock.createMock(WebResource.class);
        ClientResponse updateResponse = EasyMock.createMock(ClientResponse.class);
        WebResource.Builder updateResponseBuilder = EasyMock.createMock(WebResource.Builder.class);
        EasyMock.expect(client.resource("http://192.168.10.109:18984/solr-test/kramerius-cdk-test/update"))
                .andReturn(updateResource).anyTimes();
        EasyMock.expect(updateResource.accept(MediaType.TEXT_XML)).andReturn(updateResponseBuilder).anyTimes();
        EasyMock.expect(updateResponseBuilder.type(MediaType.TEXT_XML)).andReturn(updateResponseBuilder).anyTimes();

        Capture firstArg = newCapture();
        EasyMock.expect(updateResponseBuilder.entity(capture(firstArg), EasyMock.eq(MediaType.TEXT_XML)))
                .andReturn(updateResponseBuilder).once();

        EasyMock.expect(updateResponseBuilder.post(ClientResponse.class)).andReturn(updateResponse).anyTimes();
        EasyMock.expect(updateResponse.getStatus()).andReturn(ClientResponse.Status.OK.getStatusCode()).anyTimes();
        EasyMock.expect(updateResponse.getEntityInputStream())
                .andDelegateTo(new ParallelProcessImplTest_REPLICATE.MockClientResponse()).anyTimes();

        // final commit
        WebResource commitResource = EasyMock.createMock(WebResource.class);
        WebResource.Builder commitResourceBuilder = EasyMock.createMock(WebResource.Builder.class);

        EasyMock.expect(client.resource("http://192.168.10.109:18984/solr-test/kramerius-cdk-test/update?commit=true"))
                .andReturn(commitResource).anyTimes();
        EasyMock.expect(commitResource.accept(MediaType.TEXT_XML)).andReturn(commitResourceBuilder).anyTimes();
        EasyMock.expect(commitResourceBuilder.type(MediaType.TEXT_XML)).andReturn(commitResourceBuilder).anyTimes();
        EasyMock.expect(commitResourceBuilder.entity(anyObject(String.class), EasyMock.eq(MediaType.TEXT_XML)))
                .andReturn(commitResourceBuilder).anyTimes();
        EasyMock.expect(commitResourceBuilder.post(String.class)).andReturn("<commited/>").anyTimes();

        EasyMock.replay(client, process, updateResource, updateResponse, updateResponseBuilder, commitResource,
                commitResourceBuilder);

        // check & fetch
        mocksFromSecondCall.stream().forEach(obj -> {
            EasyMock.replay(obj);
        });
        mocksFromSecondFetchCall.stream().forEach(obj -> {
            EasyMock.replay(obj);
        });

        process.setClient(client);

        // start whole process
        process.migrate(configurationFile);

        Document document = XMLUtils.parseDocument(new StringReader(firstArg.getValue().toString()));
        List<Element> docs = XMLUtils.getElements(document.getDocumentElement(), new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                return element.getNodeName().equals("doc");
            }
        });

        Assert.assertTrue(docs.size() == 1);
        List<Element> fields = XMLUtils.getElementsRecursive(docs.get(0), (elm) -> {
            return elm.getNodeName().equals("field");
        });

        // no update attribute --
        fields.stream().forEach(elm->{
            boolean attribute = elm.hasAttribute("update");
            if (!attribute) {
                Assert.assertTrue(elm.getAttribute("name").equals("compositeId"));
            }
        });
    }

    /** changed license */
    @Test
    public void testUpdate_2()
            throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException, SAXException,
            ParserConfigurationException, NoSuchMethodException, MigrateSolrIndexException, URISyntaxException, TransformerException {
        InputStream configurationStream = ParallelProcessImplTest_REPLICATE.class
                .getResourceAsStream("k7/2u/knav.xml");
        String _configurationContent = IOUtils.toString(configurationStream, "UTF-8");
        System.out.println(_configurationContent);

        InputStream logFileStream = ParallelProcessImplTest_REPLICATE.class
                .getResourceAsStream("k7/2u/log");
        String logFileContent = IOUtils.toString(logFileStream, "UTF-8");
        File logFile = File.createTempFile("junit", "logfile");
        FileUtils.write(logFile, logFileContent, "UTF-8");

        String configurationContent = String.format(_configurationContent, logFile.toURI().toURL().toString());
        File configurationFile = File.createTempFile("junit", "conf");
        FileUtils.write(configurationFile, configurationContent, "UTF-8");

        Client client = EasyMock.createMock(Client.class);
        ParallelProcessImpl process = createMockBuilder(ParallelProcessImpl.class).addMockedMethod("buildClient")
                .createMock();

        // --- 1 doc indexed ---
        // uuid:2abf3eaa-f327-44fa-8cb8-873ce3ecafa7
        String firstReq = "http://192.168.10.109:18984/solr-test/kramerius-cdk-test/update/select/?q=pid:(%22uuid%3A2abf3eaa-f327-44fa-8cb8-873ce3ecafa7%22)&fl=pid+cdk.collection+cdk.leader+cdk.collection+cdk.licenses+cdk.licenses_of_ancestors+cdk.contains_licenses++root.pid+compositeId&wt=xml&rows=1";
        String firstResp = IOUtils.toString(this.getClass().getResourceAsStream("k7/2u/cdk_2abf3eaa-f327-44fa-8cb8-873ce3ecafa7.xml"),
                "UTF-8");
        List<Object> mocksFromSecondCall = webCallExpectXML(client, firstReq, firstResp);


        String firstReqFetch = "http://kramerius.lib.cas.cz/search/api/client/v7.0/select/?q=pid:(%22uuid%3A2abf3eaa-f327-44fa-8cb8-873ce3ecafa7%22)&fl=pid+root.pid+licenses+contains_licenses+licenses_of_ancestors+titles.*+collection.*+in_collections+in_collections.*+title.*+titles.*+text_ocr&wt=xml&rows=1";
        String firstReqResp = IOUtils
                .toString(this.getClass().getResourceAsStream("k7/2u/knav_2abf3eaa-f327-44fa-8cb8-873ce3ecafa7.xml"), "UTF-8");
        List<Object> mocksFromSecondFetchCall = webCallExpectXML(client, firstReqFetch, firstReqResp);

        // ----------
        EasyMock.expect(process.buildClient()).andReturn(client).anyTimes();

        // all updates in one check
        WebResource updateResource = EasyMock.createMock(WebResource.class);
        ClientResponse updateResponse = EasyMock.createMock(ClientResponse.class);
        WebResource.Builder updateResponseBuilder = EasyMock.createMock(WebResource.Builder.class);
        EasyMock.expect(client.resource("http://192.168.10.109:18984/solr-test/kramerius-cdk-test/update"))
                .andReturn(updateResource).anyTimes();
        EasyMock.expect(updateResource.accept(MediaType.TEXT_XML)).andReturn(updateResponseBuilder).anyTimes();
        EasyMock.expect(updateResponseBuilder.type(MediaType.TEXT_XML)).andReturn(updateResponseBuilder).anyTimes();

        Capture firstArg = newCapture();
        EasyMock.expect(updateResponseBuilder.entity(capture(firstArg), EasyMock.eq(MediaType.TEXT_XML)))
                .andReturn(updateResponseBuilder).once();

        EasyMock.expect(updateResponseBuilder.post(ClientResponse.class)).andReturn(updateResponse).anyTimes();
        EasyMock.expect(updateResponse.getStatus()).andReturn(ClientResponse.Status.OK.getStatusCode()).anyTimes();
        EasyMock.expect(updateResponse.getEntityInputStream())
                .andDelegateTo(new ParallelProcessImplTest_REPLICATE.MockClientResponse()).anyTimes();

        // final commit
        WebResource commitResource = EasyMock.createMock(WebResource.class);
        WebResource.Builder commitResourceBuilder = EasyMock.createMock(WebResource.Builder.class);

        EasyMock.expect(client.resource("http://192.168.10.109:18984/solr-test/kramerius-cdk-test/update?commit=true"))
                .andReturn(commitResource).anyTimes();
        EasyMock.expect(commitResource.accept(MediaType.TEXT_XML)).andReturn(commitResourceBuilder).anyTimes();
        EasyMock.expect(commitResourceBuilder.type(MediaType.TEXT_XML)).andReturn(commitResourceBuilder).anyTimes();
        EasyMock.expect(commitResourceBuilder.entity(anyObject(String.class), EasyMock.eq(MediaType.TEXT_XML)))
                .andReturn(commitResourceBuilder).anyTimes();
        EasyMock.expect(commitResourceBuilder.post(String.class)).andReturn("<commited/>").anyTimes();

        EasyMock.replay(client, process, updateResource, updateResponse, updateResponseBuilder, commitResource,
                commitResourceBuilder);

        // check & fetch
        mocksFromSecondCall.stream().forEach(obj -> {
            EasyMock.replay(obj);
        });
        mocksFromSecondFetchCall.stream().forEach(obj -> {
            EasyMock.replay(obj);
        });

        process.setClient(client);

        // start whole process
        process.migrate(configurationFile);

        Document document = XMLUtils.parseDocument(new StringReader(firstArg.getValue().toString()));

        List<Element> docs = XMLUtils.getElements(document.getDocumentElement(), new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                return element.getNodeName().equals("doc");
            }
        });

        Assert.assertTrue(docs.size() == 1);
        Map<String, List<Pair<String, String>>> fields = updateFields(XMLUtils.getElementsRecursive(docs.get(0), (elm) -> {
            return elm.getNodeName().equals("field");
        }));

        Assert.assertTrue(fields.get("licenses_of_ancestors").size() == 2);
        Assert.assertTrue(fields.get("licenses_of_ancestors").stream().map(Pair::getKey).collect(Collectors.toList()).contains("public"));
        Assert.assertTrue(fields.get("licenses_of_ancestors").stream().map(Pair::getKey).collect(Collectors.toList()).contains("onsite"));

        Assert.assertTrue(fields.get("cdk.licenses_of_ancestors").stream().map(Pair::getKey).collect(Collectors.toList()).contains("knav_public"));
        Assert.assertTrue(fields.get("cdk.licenses_of_ancestors").stream().map(Pair::getKey).collect(Collectors.toList()).contains("mzk_onsite"));
    }



    /** changed license */
    @Test
    public void testExistingConflictUpdate_1()
            throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException, SAXException,
            ParserConfigurationException, NoSuchMethodException, MigrateSolrIndexException, URISyntaxException, TransformerException {
        InputStream configurationStream = ParallelProcessImplTest_REPLICATE.class
                .getResourceAsStream("k7/1cf/knav.xml");
        String _configurationContent = IOUtils.toString(configurationStream, "UTF-8");
        System.out.println(_configurationContent);

        InputStream logFileStream = ParallelProcessImplTest_REPLICATE.class
                .getResourceAsStream("k7/1cf/log");
        String logFileContent = IOUtils.toString(logFileStream, "UTF-8");
        File logFile = File.createTempFile("junit", "logfile");
        FileUtils.write(logFile, logFileContent, "UTF-8");

        String configurationContent = String.format(_configurationContent, "http://api.ceskadigitalniknihovna.cz/search/api/admin/v7.0/reharvest", logFile.toURI().toURL().toString());
        File configurationFile = File.createTempFile("junit", "conf");
        FileUtils.write(configurationFile, configurationContent, "UTF-8");

        Client client = EasyMock.createMock(Client.class);
        ParallelProcessImpl process = createMockBuilder(ParallelProcessImpl.class).addMockedMethod("buildClient")
                .createMock();

        // --- 1 doc indexed ---
        // uuid:2abf3eaa-f327-44fa-8cb8-873ce3ecafa7
        String firstReq = "http://192.168.10.109:18984/solr-test/kramerius-cdk-test/update/select/?q=pid:(%22uuid%3A2abf3eaa-f327-44fa-8cb8-873ce3ecafa7%22)&fl=pid+cdk.collection+cdk.leader+cdk.collection+cdk.licenses+cdk.licenses_of_ancestors+cdk.contains_licenses++root.pid+compositeId&wt=xml&rows=1";
        String firstResp = IOUtils.toString(this.getClass().getResourceAsStream("k7/1cf/cdk_2abf3eaa-f327-44fa-8cb8-873ce3ecafa7.xml"),
                "UTF-8");
        List<Object> mocksFromSecondCall = webCallExpectXML(client, firstReq, firstResp);


        String firstReqFetch = "http://kramerius.lib.cas.cz/search/api/client/v7.0/select/?q=pid:(%22uuid%3A2abf3eaa-f327-44fa-8cb8-873ce3ecafa7%22)&fl=pid+root.pid+licenses+contains_licenses+licenses_of_ancestors+titles.*+collection.*+in_collections+in_collections.*+title.*+titles.*+text_ocr&wt=xml&rows=1";
        String firstReqResp = IOUtils
                .toString(this.getClass().getResourceAsStream("k7/1cf/knav_2abf3eaa-f327-44fa-8cb8-873ce3ecafa7.xml"), "UTF-8");
        List<Object> mocksFromSecondFetchCall = webCallExpectXML(client, firstReqFetch, firstReqResp);


        String conflictReqFetch = "http://api.ceskadigitalniknihovna.cz/search/api/admin/v7.0/reharvest/resolveconflicts/uuid:8df204f2-731c-4e3e-9789-747718c598e3,uuid:7df204f2-731c-4e3e-9789-747718c598e3";
        String conflictResp = "{}";
        List<Object> mocksFromConflictFetchCall = webCallExpectJSON(client, conflictReqFetch, conflictResp);


        // ----------
        EasyMock.expect(process.buildClient()).andReturn(client).anyTimes();


        // final commit
        WebResource commitResource = EasyMock.createMock(WebResource.class);
        WebResource.Builder commitResourceBuilder = EasyMock.createMock(WebResource.Builder.class);

        EasyMock.expect(client.resource("http://192.168.10.109:18984/solr-test/kramerius-cdk-test/update?commit=true"))
                .andReturn(commitResource).anyTimes();
        EasyMock.expect(commitResource.accept(MediaType.TEXT_XML)).andReturn(commitResourceBuilder).anyTimes();
        EasyMock.expect(commitResourceBuilder.type(MediaType.TEXT_XML)).andReturn(commitResourceBuilder).anyTimes();
        EasyMock.expect(commitResourceBuilder.entity(anyObject(String.class), EasyMock.eq(MediaType.TEXT_XML)))
                .andReturn(commitResourceBuilder).anyTimes();
        EasyMock.expect(commitResourceBuilder.post(String.class)).andReturn("<commited/>").anyTimes();

        EasyMock.replay(client, process, /* updateResource, updateResponse, updateResponseBuilder,*/ commitResource,commitResourceBuilder);

        // check & fetch
        mocksFromSecondCall.stream().forEach(obj -> {
            EasyMock.replay(obj);
        });
        mocksFromSecondFetchCall.stream().forEach(obj -> {
            EasyMock.replay(obj);
        });

        mocksFromConflictFetchCall.stream().forEach(obj-> {
            EasyMock.replay(obj);
        });

        process.setClient(client);

        // start whole process
        process.migrate(configurationFile);

        // no update; only commit
    }


    private static Map<String, List<Pair<String,String>>> updateFields(List<Element> efields) {
        Map<String, List<Pair<String,String>>> fieldVals = new HashMap<>();
        efields.stream().forEach(elm-> {
            String name = elm.getAttribute("name");
            String update = elm.getAttribute("update");
            String val = elm.getTextContent();
            if (!fieldVals.containsKey(name)) {
                fieldVals.put(name, new ArrayList<>());
            }
            fieldVals.get(name).add(Pair.of(val, update != null ? update : ""));
        });
        return fieldVals;
    }


    /** Mock response */
    static class MockClientResponse extends ClientResponse {

        public MockClientResponse() {
            super(Status.OK.getStatusCode(), null, null, null);
        }

        @Override
        public InputStream getEntityInputStream() {
            try {
                return new ByteArrayInputStream("<xml></xml>".getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
