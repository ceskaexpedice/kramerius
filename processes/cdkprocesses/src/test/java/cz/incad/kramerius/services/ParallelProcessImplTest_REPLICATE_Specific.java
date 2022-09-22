package cz.incad.kramerius.services;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.utils.XMLUtils;
import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xmlunit.builder.Input;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.List;

import static cz.incad.kramerius.services.utils.SolrMockWebCall.webCallExpect;
import static org.easymock.EasyMock.*;
import static org.easymock.EasyMock.anyObject;
import static org.xmlunit.assertj.XmlAssert.assertThat;

public class ParallelProcessImplTest_REPLICATE_Specific {

    /** K7 transform - KNAV error - duplicate values rels-exts.sort */
    @Test
    public void testSpecificCase_1() throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException, SAXException, ParserConfigurationException, NoSuchMethodException, MigrateSolrIndexException, URISyntaxException {

        InputStream configurationStream = ParallelProcessImplTest_REPLICATE.class.getResourceAsStream("config.replicate.mlp_k7_solrcloud.xml");
        String _configurationContent = IOUtils.toString(configurationStream, "UTF-8");

        InputStream logFileStream = ParallelProcessImplTest_REPLICATE.class.getResourceAsStream("k7notexists_specific/_1/logfile_log");
        String logFileContent = IOUtils.toString(logFileStream, "UTF-8");
        File logFile = File.createTempFile("junit","logfile");
        FileUtils.write(logFile, logFileContent, "UTF-8");

        String configurationContent = String.format(_configurationContent, logFile.toURI().toURL().toString());
        File configurationFile = File.createTempFile("junit","conf");
        FileUtils.write(configurationFile, configurationContent, "UTF-8");


        Client client = EasyMock.createMock(Client.class);
        ParallelProcessImpl process = createMockBuilder(ParallelProcessImpl.class)
                .addMockedMethod("buildClient")
                .createMock();

        // ---  1 doc indexed ---
        String secondReq = "http://10.19.6.10:8983/solr/k7_5/select/?q=pid:(%22uuid%3A63bd79e0-d62a-40f1-a374-57e2d78827c9%22)&fl=pid+collection+root.pid&wt=xml&rows=1";
        String secondResp  = IOUtils.toString(this.getClass().getResourceAsStream("k7notexists_specific/_1/1.xml"), "UTF-8");
        List<Object> mocksFromSecondCall = webCallExpect(client, secondReq, secondResp);

        String secondReqFetch = "http://kramerius4.mlp.cz/search/api/v5.0/search/?q=PID:(%22uuid%3A63bd79e0-d62a-40f1-a374-57e2d78827c9%22)&fl=PID+timestamp+fedora.model+document_type+handle+status+created_date+modified_date+parent_model+parent_pid+parent_pid+parent_title+root_model+root_pid+root_title+text_ocr+pages_count+datum_str+datum+rok+datum_begin+datum_end+datum_page+issn+mdt+ddt+dostupnost+keywords+geographic_names+collection+sec+model_path+pid_path+rels_ext_index+level+dc.title+title_sort+title_sort+dc.creator+dc.identifier+language+dc.description+details+facet_title+browse_title+browse_autor+img_full_mime+viewable+virtual+location+range+mods.shelfLocator+mods.physicalLocation+text+dnnt+dnnt-labels&wt=xml&rows=1";
        String secondRespFetch  = IOUtils.toString(this.getClass().getResourceAsStream("k7notexists_specific/_1/1_fetch.xml"), "UTF-8");
        List<Object> mocksFromSecondFetchCall = webCallExpect(client, secondReqFetch, secondRespFetch);
        // ----------

        EasyMock.expect(process.buildClient()).andReturn(client).anyTimes();

        // all updates in one check
        WebResource updateResource = EasyMock.createMock(WebResource.class);
        ClientResponse updateResponse = EasyMock.createMock(ClientResponse.class);
        WebResource.Builder updateResponseBuilder = EasyMock.createMock(WebResource.Builder.class);
        EasyMock.expect(client.resource("http://192.168.10.109:18984/solr-test/kramerius-cdk-test/update")).andReturn(updateResource).anyTimes();
        EasyMock.expect(updateResource.accept(MediaType.TEXT_XML)).andReturn(updateResponseBuilder).anyTimes();
        EasyMock.expect(updateResponseBuilder.type(MediaType.TEXT_XML)).andReturn(updateResponseBuilder).anyTimes();

        //EasyMock.expect(updateResponseBuilder.entity(EasyMock.anyObject(String.class), EasyMock.eq(MediaType.TEXT_XML))).andReturn(updateResponseBuilder).anyTimes();
        //Capture captureSingleArgument = EasyMock.newCa
        Capture firstArg = newCapture();
        EasyMock.expect(updateResponseBuilder.entity(capture(firstArg), EasyMock.eq(MediaType.TEXT_XML))).andReturn(updateResponseBuilder).once();

        EasyMock.expect(updateResponseBuilder.post(ClientResponse.class)).andReturn(updateResponse).anyTimes();
        EasyMock.expect(updateResponse.getStatus()).andReturn(ClientResponse.Status.OK.getStatusCode()).anyTimes();
        EasyMock.expect(updateResponse.getEntityInputStream()).andDelegateTo(new ParallelProcessImplTest_REPLICATE.MockClientResponse()).anyTimes();

        // final commit
        WebResource commitResource = EasyMock.createMock(WebResource.class);
        WebResource.Builder commitResourceBuilder = EasyMock.createMock(WebResource.Builder.class);

        EasyMock.expect(client.resource("http://192.168.10.109:18984/solr-test/kramerius-cdk-test/update?commit=true")).andReturn(commitResource).anyTimes();
        EasyMock.expect(commitResource.accept(MediaType.TEXT_XML)).andReturn(commitResourceBuilder).anyTimes();
        EasyMock.expect(commitResourceBuilder.type(MediaType.TEXT_XML)).andReturn(commitResourceBuilder).anyTimes();
        EasyMock.expect(commitResourceBuilder.entity(anyObject(String.class), EasyMock.eq(MediaType.TEXT_XML))).andReturn(commitResourceBuilder).anyTimes();
        EasyMock.expect(commitResourceBuilder.post(String.class)).andReturn("<commited/>").anyTimes();

        EasyMock.replay(client, process, updateResource, updateResponse, updateResponseBuilder,
                commitResource,
                commitResourceBuilder);

        // check & fetch
        mocksFromSecondCall.stream().forEach(obj-> {
            EasyMock.replay(obj);
        });
        mocksFromSecondFetchCall.stream().forEach(obj-> {
            EasyMock.replay(obj);
        });

        process.setClient(client);

        // start whole process
        process.migrate(configurationFile);

        //System.out.println(firstArg.getValue());

        Document document = XMLUtils.parseDocument(new StringReader(firstArg.getValue().toString()));
        List<Element> docs = XMLUtils.getElements(document.getDocumentElement(), new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                return element.getNodeName().equals("doc");
            }
        });

        docs.stream().forEach(doc-> {
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
}
