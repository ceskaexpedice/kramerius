package cz.incad.kramerius.services;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import cz.incad.kramerius.service.MigrateSolrIndexException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.xmlunit.builder.Input;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URISyntaxException;
import java.util.List;

import static cz.incad.kramerius.services.utils.SolrMockWebCall.*;
import static org.easymock.EasyMock.*;

import static org.xmlunit.assertj.XmlAssert.assertThat;

public class ParallelProcessImplTest_REPLICATE {

    @Test
    public void testFullProcess_EXISTS() throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException, SAXException, ParserConfigurationException, NoSuchMethodException, MigrateSolrIndexException, URISyntaxException {
        InputStream configurationStream = ParallelProcessImplTest_REPLICATE.class.getResourceAsStream("config.replicate.mlp.xml");
        String _configurationContent = IOUtils.toString(configurationStream, "UTF-8");

        InputStream logFileStream = ParallelProcessImplTest_REPLICATE.class.getResourceAsStream("logfile_log");
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

        // ---  20 updated ---
        String secondReq = "http://10.19.6.10:8983/solr/k7_5/select/?q=PID:(%22uuid%3A00000834-c06c-49ed-91e9-d54a0f8c8571%22+OR+%22uuid%3A00001fe4-1d09-42e2-845a-0201dd900e87%22+OR+%22uuid%3A0000211b-7c66-4730-b343-8ea2ef427f99%22+OR+%22uuid%3A000026fa-3695-4af4-aaaa-50557d8c2c6d%22+OR+%22uuid%3A0000329c-2940-89be-717b-82cac72948a8%22+OR+%22uuid%3A0000338c-4dd1-4d11-bda5-4c1b44d83628%22+OR+%22uuid%3A0000338c-4dd1-4d11-bda5-4c1b44d83628%2F%401%22+OR+%22uuid%3A0000338c-4dd1-4d11-bda5-4c1b44d83628%2F%402%22+OR+%22uuid%3A00003962-b232-4055-9a77-8d8db9487b90%22+OR+%22uuid%3A00003bb1-429d-11e2-849c-005056a60003%22+OR+%22uuid%3A000040ee-eda5-49d3-ae14-510a74921cda%22+OR+%22uuid%3A000047d7-3b67-4f08-a7cf-e843db917097%22+OR+%22uuid%3A000059eb-d782-4285-857a-41b370dc1afd%22+OR+%22uuid%3A00006174-e3da-488b-9a18-eef7b3d5e527%22+OR+%22uuid%3A00006bd9-100d-4c89-97e6-3529e99c45be%22+OR+%22uuid%3A00006e78-04c4-4fa0-9a9d-d7ba78f7c341%22+OR+%22uuid%3A00007ca2-054e-4fde-b73c-81f55a282ebc%22+OR+%22uuid%3A00007e8c-3f3a-4f73-ab70-0f95b1024f59%22+OR+%22uuid%3A000091fb-dc07-4594-9300-8a503dc09210%22+OR+%22uuid%3A0000a0c8-41b2-4fdd-9597-619b3a7ff965%22)&fl=PID+collection&wt=xml&rows=20";
        String secondResp  = IOUtils.toString(this.getClass().getResourceAsStream("exists/1.xml"), "UTF-8");
        List<Object> mocksFromSecondCall = webCallExpect(client, secondReq, secondResp);
        // ----------

        // ---  40 updated ---
        String thirdReq = "http://10.19.6.10:8983/solr/k7_5/select/?q=PID:(%22uuid%3A0000a140-892d-48e3-851c-c0722578db50%22+OR+%22uuid%3A0000a210-40ad-48c6-a324-b4bd5fefe0f1%22+OR+%22uuid%3A0000a64f-d935-4260-acce-88833c115963%22+OR+%22uuid%3A0000ab19-c07d-41ac-9c6c-b03240271247%22+OR+%22uuid%3A0000abeb-c9ef-4ee9-8db1-536132bfac84%22+OR+%22uuid%3A0000b3cd-478a-4756-b302-31765cab9efc%22+OR+%22uuid%3A0000b3cd-478a-4756-b302-31765cab9efc%2F%401%22+OR+%22uuid%3A0000b3cd-478a-4756-b302-31765cab9efc%2F%402%22+OR+%22uuid%3A0000b509-ed4f-447e-895f-d17d0d4ad0a3%22+OR+%22uuid%3A0000bbf9-0881-4e13-a793-457ed22bc58d%22+OR+%22uuid%3A0000c101-821b-4e21-95a3-13110119e10c%22+OR+%22uuid%3A0000c509-14e4-40b6-b555-f9ad46bce4c0%22+OR+%22uuid%3A0000c71a-aec6-4772-95f7-9412fd19f74c%22+OR+%22uuid%3A0000d5b8-44be-4473-b5f3-9e1f2437ce61%22+OR+%22uuid%3A0000dc41-0c83-4fe3-b013-081b57c6db8e%22+OR+%22uuid%3A0000dcd8-9a40-4b91-b3be-c7ffa05f6295%22+OR+%22uuid%3A0000dedc-cf17-11e1-acea-005056a60003%22+OR+%22uuid%3A0000e1cf-0621-4f8a-9bbc-cfdf4d25f096%22+OR+%22uuid%3A0000ed1b-31a8-4275-96fe-e6c80ef3e1b1%22+OR+%22uuid%3A0000ffd8-d89c-42db-a818-537144d80261%22)&fl=PID+collection&wt=xml&rows=20";
        String thirdResp  = IOUtils.toString(this.getClass().getResourceAsStream("exists/2.xml"), "UTF-8");
        List<Object> mocksFromThirdCall = webCallExpect(client, thirdReq, thirdResp);
        // ----------

        // ---  60 updated ---
        String fourthReq = "http://10.19.6.10:8983/solr/k7_5/select/?q=PID:(%22uuid%3A00010fd5-d7c8-46fa-b87a-d87ca2aa84a5%22+OR+%22uuid%3A00011d48-9699-4b21-b000-9b0c34995b63%22+OR+%22uuid%3A00012082-f119-4370-9c0c-b6055137d6d8%22+OR+%22uuid%3A00012289-fae2-44a5-ba57-db7e5ab1159b%22+OR+%22uuid%3A0001257f-b598-4a0e-9c1f-8b199c115929%22+OR+%22uuid%3A00012835-2127-42b5-b598-6ece58276164%22+OR+%22uuid%3A00012ff0-a588-470b-9428-f2e1eddc2e6a%22+OR+%22uuid%3A00012ff0-a588-470b-9428-f2e1eddc2e6a%2F%401%22+OR+%22uuid%3A00013431-3c37-40db-9b4c-e87bbdd1d8eb%22+OR+%22uuid%3A00013618-6699-4721-b8d1-61b3a96bd969%22+OR+%22uuid%3A000136ec-ae05-426b-85e1-e6b38576f761%22+OR+%22uuid%3A000136ec-ae05-426b-85e1-e6b38576f761%2F%401%22+OR+%22uuid%3A00013afa-b718-42b1-b9bf-7f633b97c747%22+OR+%22uuid%3A00013d61-2317-43d2-9a69-39686e37c527%22+OR+%22uuid%3A00015940-ee76-415d-8169-797789406cff%22+OR+%22uuid%3A00016234-018a-4cce-90a4-5eaecd78122f%22+OR+%22uuid%3A00016cdf-2591-465d-bfd0-fece6ae08bf1%22+OR+%22uuid%3A0001714e-bbb4-4d7a-856a-c79aa618bfa2%22+OR+%22uuid%3A00017177-b182-46da-b88a-1377215389ea%22+OR+%22uuid%3A0001759e-cad0-467b-b61a-cfe1381a76f6%22)&fl=PID+collection&wt=xml&rows=20";
        String fourthResp  = IOUtils.toString(this.getClass().getResourceAsStream("exists/3.xml"), "UTF-8");
        List<Object> mocksFromFourthCall = webCallExpect(client, fourthReq, fourthResp);
        // ----------

        // ---  80 updated ---
        String fifthReq = "http://10.19.6.10:8983/solr/k7_5/select/?q=PID:(%22uuid%3A000182c3-0215-4103-90ce-9f912a3caf02%22+OR+%22uuid%3A00018da8-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001900d-e592-4ad4-8f61-21289fa0108c%22+OR+%22uuid%3A000199e3-6c01-4055-b620-d809be8c34c9%22+OR+%22uuid%3A00019d0e-ebcf-4aa0-ae46-e85403f867d0%22+OR+%22uuid%3A0001a28e-d40f-4caa-b99d-56b6b272be10%22+OR+%22uuid%3A0001b5ad-a2aa-43ea-912c-f472f7d0a786%22+OR+%22uuid%3A0001c4ba-bedf-4dbd-beb4-f87baf5bec3c%22+OR+%22uuid%3A0001c89d-0b86-448c-91c1-688f6882d548%22+OR+%22uuid%3A0001caa1-2fd5-4e0d-a74a-516286ee9414%22+OR+%22uuid%3A0001cd7d-446b-4696-b10d-a7115c6e4ed7%22+OR+%22uuid%3A0001cff1-9c7c-47ac-a2f0-340514a12852%22+OR+%22uuid%3A0001d02f-af3e-4d9d-9cae-31f1373aeffb%22+OR+%22uuid%3A0001dbc9-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001dbca-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001dbcb-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001dbcc-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001dbcd-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001dbce-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001dbcf-3105-11e9-8847-005056a2b051%22)&fl=PID+collection&wt=xml&rows=20";
        String fifthResp  = IOUtils.toString(this.getClass().getResourceAsStream("exists/4.xml"), "UTF-8");
        List<Object> mocksFromFifthCall = webCallExpect(client, fifthReq, fifthResp);
        // ----------

        // ---  100 updated ---
        String sixthReq = "http://10.19.6.10:8983/solr/k7_5/select/?q=PID:(%22uuid%3A0001dbd0-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001de4b-75ee-4428-bb20-737cc2cee698%22+OR+%22uuid%3A0001e211-3e75-4c7e-871d-662c2bfc4471%22+OR+%22uuid%3A0001e71d-9c40-482c-b0b4-70fa1d62d4ed%22+OR+%22uuid%3A0001e8ae-02ab-49e7-bb84-d83291bc075e%22+OR+%22uuid%3A0001eff8-4618-417a-aede-4ab6b5274f8c%22+OR+%22uuid%3A0001f258-7472-42df-a994-c14817e8e9c5%22+OR+%22uuid%3A0001f761-5a0c-47cc-87de-db9513e4c305%22+OR+%22uuid%3A000202e1-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e2-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e3-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e4-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e5-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e6-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e7-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e8-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e9-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A00020759-510b-429d-a46c-ed0b25eb4a02%22+OR+%22uuid%3A000229fa-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000229fb-3105-11e9-8847-005056a2b051%22)&fl=PID+collection&wt=xml&rows=20";
        String sixthResp  = IOUtils.toString(this.getClass().getResourceAsStream("exists/5.xml"), "UTF-8");
        List<Object> mocksFromSixthCall = webCallExpect(client, sixthReq, sixthResp);
        // ----------


        EasyMock.expect(process.buildClient()).andReturn(client).anyTimes();

        // all updates in one check; (anyString)
        WebResource updateResource = EasyMock.createMock(WebResource.class);
        ClientResponse updateResponse = EasyMock.createMock(ClientResponse.class);
        WebResource.Builder updateResponseBuilder = EasyMock.createMock(WebResource.Builder.class);
        EasyMock.expect(client.resource("http://192.168.10.109:18984/solr-test/kramerius-cdk-test/update")).andReturn(updateResource).anyTimes();
        EasyMock.expect(updateResource.accept(MediaType.TEXT_XML)).andReturn(updateResponseBuilder).anyTimes();
        EasyMock.expect(updateResponseBuilder.type(MediaType.TEXT_XML)).andReturn(updateResponseBuilder).anyTimes();
        // entity
        //EasyMock.expect(updateResponseBuilder.entity(EasyMock.anyObject(String.class), EasyMock.eq(MediaType.TEXT_XML))).andReturn(updateResponseBuilder).anyTimes();

        Capture firstArg = newCapture();
        EasyMock.expect(updateResponseBuilder.entity(capture(firstArg), EasyMock.eq(MediaType.TEXT_XML))).andReturn(updateResponseBuilder).once();

        Capture secondArg = newCapture();
        EasyMock.expect(updateResponseBuilder.entity(capture(secondArg), EasyMock.eq(MediaType.TEXT_XML))).andReturn(updateResponseBuilder).once();

        Capture thirdArg = newCapture();
        EasyMock.expect(updateResponseBuilder.entity(capture(thirdArg), EasyMock.eq(MediaType.TEXT_XML))).andReturn(updateResponseBuilder).once();

        Capture fourthArg = newCapture();
        EasyMock.expect(updateResponseBuilder.entity(capture(fourthArg), EasyMock.eq(MediaType.TEXT_XML))).andReturn(updateResponseBuilder).once();

        Capture fifthArg = newCapture();
        EasyMock.expect(updateResponseBuilder.entity(capture(fifthArg), EasyMock.eq(MediaType.TEXT_XML))).andReturn(updateResponseBuilder).once();


        EasyMock.expect(updateResponseBuilder.post(ClientResponse.class)).andReturn(updateResponse).anyTimes();

        EasyMock.expect(updateResponse.getStatus()).andReturn(ClientResponse.Status.OK.getStatusCode()).anyTimes();
        EasyMock.expect(updateResponse.getEntityInputStream()).andDelegateTo(new MockClientResponse()).anyTimes();

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
        mocksFromSecondCall.stream().forEach(obj-> {
            EasyMock.replay(obj);
        });
        mocksFromThirdCall.stream().forEach(obj-> {
            EasyMock.replay(obj);
        });
        mocksFromFourthCall.stream().forEach(obj-> {
            EasyMock.replay(obj);
        });
        mocksFromFifthCall.stream().forEach(obj-> {
            EasyMock.replay(obj);
        });
        mocksFromSixthCall.stream().forEach(obj-> {
            EasyMock.replay(obj);
        });

        process.setClient(client);

        // start whole process
        process.migrate(configurationFile);

        assertThat(Input.fromReader(new StringReader((String) firstArg.getValue())))
                .and(Input.fromStream(this.getClass().getResourceAsStream("exists/batches/1_batch.xml")))
                .ignoreComments()
                .ignoreWhitespace()
                .areSimilar();

        assertThat(Input.fromReader(new StringReader((String) secondArg.getValue())))
                .and(Input.fromStream(this.getClass().getResourceAsStream("exists/batches/2_batch.xml")))
                .ignoreComments()
                .ignoreWhitespace()
                .areSimilar();

        assertThat(Input.fromReader(new StringReader((String) thirdArg.getValue())))
                .and(Input.fromStream(this.getClass().getResourceAsStream("exists/batches/3_batch.xml")))
                .ignoreComments()
                .ignoreWhitespace()
                .areSimilar();

        assertThat(Input.fromReader(new StringReader((String) fourthArg.getValue())))
                .and(Input.fromStream(this.getClass().getResourceAsStream("exists/batches/4_batch.xml")))
                .ignoreComments()
                .ignoreWhitespace()
                .areSimilar();

        assertThat(Input.fromReader(new StringReader((String) fifthArg.getValue())))
                .and(Input.fromStream(this.getClass().getResourceAsStream("exists/batches/5_batch.xml")))
                .ignoreComments()
                .ignoreWhitespace()
                .areSimilar();

    }

    @Test
    public void testFullProcess_NOT_EXISTS() throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException, SAXException, ParserConfigurationException, NoSuchMethodException, MigrateSolrIndexException, URISyntaxException {
        InputStream configurationStream = ParallelProcessImplTest_REPLICATE.class.getResourceAsStream("config.replicate.mlp.xml");
        String _configurationContent = IOUtils.toString(configurationStream, "UTF-8");

        InputStream logFileStream = ParallelProcessImplTest_REPLICATE.class.getResourceAsStream("logfile_log");
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

        // ---  20 indexed ---
        String secondReq = "http://10.19.6.10:8983/solr/k7_5/select/?q=PID:(%22uuid%3A00000834-c06c-49ed-91e9-d54a0f8c8571%22+OR+%22uuid%3A00001fe4-1d09-42e2-845a-0201dd900e87%22+OR+%22uuid%3A0000211b-7c66-4730-b343-8ea2ef427f99%22+OR+%22uuid%3A000026fa-3695-4af4-aaaa-50557d8c2c6d%22+OR+%22uuid%3A0000329c-2940-89be-717b-82cac72948a8%22+OR+%22uuid%3A0000338c-4dd1-4d11-bda5-4c1b44d83628%22+OR+%22uuid%3A0000338c-4dd1-4d11-bda5-4c1b44d83628%2F%401%22+OR+%22uuid%3A0000338c-4dd1-4d11-bda5-4c1b44d83628%2F%402%22+OR+%22uuid%3A00003962-b232-4055-9a77-8d8db9487b90%22+OR+%22uuid%3A00003bb1-429d-11e2-849c-005056a60003%22+OR+%22uuid%3A000040ee-eda5-49d3-ae14-510a74921cda%22+OR+%22uuid%3A000047d7-3b67-4f08-a7cf-e843db917097%22+OR+%22uuid%3A000059eb-d782-4285-857a-41b370dc1afd%22+OR+%22uuid%3A00006174-e3da-488b-9a18-eef7b3d5e527%22+OR+%22uuid%3A00006bd9-100d-4c89-97e6-3529e99c45be%22+OR+%22uuid%3A00006e78-04c4-4fa0-9a9d-d7ba78f7c341%22+OR+%22uuid%3A00007ca2-054e-4fde-b73c-81f55a282ebc%22+OR+%22uuid%3A00007e8c-3f3a-4f73-ab70-0f95b1024f59%22+OR+%22uuid%3A000091fb-dc07-4594-9300-8a503dc09210%22+OR+%22uuid%3A0000a0c8-41b2-4fdd-9597-619b3a7ff965%22)&fl=PID+collection&wt=xml&rows=20";
        String secondResp  = IOUtils.toString(this.getClass().getResourceAsStream("notexists/1.xml"), "UTF-8");
        List<Object> mocksFromSecondCall = webCallExpect(client, secondReq, secondResp);

        String secondReqFetch = "http://kramerius4.mlp.cz/search/api/v5.0/search/?q=PID:(%22uuid%3A00000834-c06c-49ed-91e9-d54a0f8c8571%22+OR+%22uuid%3A00001fe4-1d09-42e2-845a-0201dd900e87%22+OR+%22uuid%3A0000211b-7c66-4730-b343-8ea2ef427f99%22+OR+%22uuid%3A000026fa-3695-4af4-aaaa-50557d8c2c6d%22+OR+%22uuid%3A0000329c-2940-89be-717b-82cac72948a8%22+OR+%22uuid%3A0000338c-4dd1-4d11-bda5-4c1b44d83628%22+OR+%22uuid%3A0000338c-4dd1-4d11-bda5-4c1b44d83628%2F%401%22+OR+%22uuid%3A0000338c-4dd1-4d11-bda5-4c1b44d83628%2F%402%22+OR+%22uuid%3A00003962-b232-4055-9a77-8d8db9487b90%22+OR+%22uuid%3A00003bb1-429d-11e2-849c-005056a60003%22+OR+%22uuid%3A000040ee-eda5-49d3-ae14-510a74921cda%22+OR+%22uuid%3A000047d7-3b67-4f08-a7cf-e843db917097%22+OR+%22uuid%3A000059eb-d782-4285-857a-41b370dc1afd%22+OR+%22uuid%3A00006174-e3da-488b-9a18-eef7b3d5e527%22+OR+%22uuid%3A00006bd9-100d-4c89-97e6-3529e99c45be%22+OR+%22uuid%3A00006e78-04c4-4fa0-9a9d-d7ba78f7c341%22+OR+%22uuid%3A00007ca2-054e-4fde-b73c-81f55a282ebc%22+OR+%22uuid%3A00007e8c-3f3a-4f73-ab70-0f95b1024f59%22+OR+%22uuid%3A000091fb-dc07-4594-9300-8a503dc09210%22+OR+%22uuid%3A0000a0c8-41b2-4fdd-9597-619b3a7ff965%22)&fl=PID+timestamp+fedora.model+document_type+handle+status+created_date+modified_date+parent_model+parent_pid+parent_pid+parent_title+root_model+root_pid+root_title+text_ocr+pages_count+datum_str+datum+rok+datum_begin+datum_end+datum_page+issn+mdt+ddt+dostupnost+keywords+geographic_names+collection+sec+model_path+pid_path+rels_ext_index+level+dc.title+title_sort+title_sort+dc.creator+dc.identifier+language+dc.description+details+facet_title+browse_title+browse_autor+img_full_mime+viewable+virtual+location+range+mods.shelfLocator+mods.physicalLocation+text+dnnt&wt=xml&rows=20";
        String secondRespFetch  = IOUtils.toString(this.getClass().getResourceAsStream("notexists/1_fetch.xml"), "UTF-8");
        List<Object> mocksFromSecondFetchCall = webCallExpect(client, secondReqFetch, secondRespFetch);
        // ----------

        // ---  40 indexed ---
        String thirdReq = "http://10.19.6.10:8983/solr/k7_5/select/?q=PID:(%22uuid%3A0000a140-892d-48e3-851c-c0722578db50%22+OR+%22uuid%3A0000a210-40ad-48c6-a324-b4bd5fefe0f1%22+OR+%22uuid%3A0000a64f-d935-4260-acce-88833c115963%22+OR+%22uuid%3A0000ab19-c07d-41ac-9c6c-b03240271247%22+OR+%22uuid%3A0000abeb-c9ef-4ee9-8db1-536132bfac84%22+OR+%22uuid%3A0000b3cd-478a-4756-b302-31765cab9efc%22+OR+%22uuid%3A0000b3cd-478a-4756-b302-31765cab9efc%2F%401%22+OR+%22uuid%3A0000b3cd-478a-4756-b302-31765cab9efc%2F%402%22+OR+%22uuid%3A0000b509-ed4f-447e-895f-d17d0d4ad0a3%22+OR+%22uuid%3A0000bbf9-0881-4e13-a793-457ed22bc58d%22+OR+%22uuid%3A0000c101-821b-4e21-95a3-13110119e10c%22+OR+%22uuid%3A0000c509-14e4-40b6-b555-f9ad46bce4c0%22+OR+%22uuid%3A0000c71a-aec6-4772-95f7-9412fd19f74c%22+OR+%22uuid%3A0000d5b8-44be-4473-b5f3-9e1f2437ce61%22+OR+%22uuid%3A0000dc41-0c83-4fe3-b013-081b57c6db8e%22+OR+%22uuid%3A0000dcd8-9a40-4b91-b3be-c7ffa05f6295%22+OR+%22uuid%3A0000dedc-cf17-11e1-acea-005056a60003%22+OR+%22uuid%3A0000e1cf-0621-4f8a-9bbc-cfdf4d25f096%22+OR+%22uuid%3A0000ed1b-31a8-4275-96fe-e6c80ef3e1b1%22+OR+%22uuid%3A0000ffd8-d89c-42db-a818-537144d80261%22)&fl=PID+collection&wt=xml&rows=20";
        String thirdResp  = IOUtils.toString(this.getClass().getResourceAsStream("notexists/2.xml"), "UTF-8");
        List<Object> mocksFromThirdCall = webCallExpect(client, thirdReq, thirdResp);

        String thirdReqFetch = "http://kramerius4.mlp.cz/search/api/v5.0/search/?q=PID:(%22uuid%3A0000a140-892d-48e3-851c-c0722578db50%22+OR+%22uuid%3A0000a210-40ad-48c6-a324-b4bd5fefe0f1%22+OR+%22uuid%3A0000a64f-d935-4260-acce-88833c115963%22+OR+%22uuid%3A0000ab19-c07d-41ac-9c6c-b03240271247%22+OR+%22uuid%3A0000abeb-c9ef-4ee9-8db1-536132bfac84%22+OR+%22uuid%3A0000b3cd-478a-4756-b302-31765cab9efc%22+OR+%22uuid%3A0000b3cd-478a-4756-b302-31765cab9efc%2F%401%22+OR+%22uuid%3A0000b3cd-478a-4756-b302-31765cab9efc%2F%402%22+OR+%22uuid%3A0000b509-ed4f-447e-895f-d17d0d4ad0a3%22+OR+%22uuid%3A0000bbf9-0881-4e13-a793-457ed22bc58d%22+OR+%22uuid%3A0000c101-821b-4e21-95a3-13110119e10c%22+OR+%22uuid%3A0000c509-14e4-40b6-b555-f9ad46bce4c0%22+OR+%22uuid%3A0000c71a-aec6-4772-95f7-9412fd19f74c%22+OR+%22uuid%3A0000d5b8-44be-4473-b5f3-9e1f2437ce61%22+OR+%22uuid%3A0000dc41-0c83-4fe3-b013-081b57c6db8e%22+OR+%22uuid%3A0000dcd8-9a40-4b91-b3be-c7ffa05f6295%22+OR+%22uuid%3A0000dedc-cf17-11e1-acea-005056a60003%22+OR+%22uuid%3A0000e1cf-0621-4f8a-9bbc-cfdf4d25f096%22+OR+%22uuid%3A0000ed1b-31a8-4275-96fe-e6c80ef3e1b1%22+OR+%22uuid%3A0000ffd8-d89c-42db-a818-537144d80261%22)&fl=PID+timestamp+fedora.model+document_type+handle+status+created_date+modified_date+parent_model+parent_pid+parent_pid+parent_title+root_model+root_pid+root_title+text_ocr+pages_count+datum_str+datum+rok+datum_begin+datum_end+datum_page+issn+mdt+ddt+dostupnost+keywords+geographic_names+collection+sec+model_path+pid_path+rels_ext_index+level+dc.title+title_sort+title_sort+dc.creator+dc.identifier+language+dc.description+details+facet_title+browse_title+browse_autor+img_full_mime+viewable+virtual+location+range+mods.shelfLocator+mods.physicalLocation+text+dnnt&wt=xml&rows=20";
        String thirdRespFetch  = IOUtils.toString(this.getClass().getResourceAsStream("notexists/2_fetch.xml"), "UTF-8");
        List<Object> mocksFromThirdFetchCall = webCallExpect(client, thirdReqFetch, thirdRespFetch);
        // ----------

        // ---  60 indexed ---
        String fourthReq = "http://10.19.6.10:8983/solr/k7_5/select/?q=PID:(%22uuid%3A00010fd5-d7c8-46fa-b87a-d87ca2aa84a5%22+OR+%22uuid%3A00011d48-9699-4b21-b000-9b0c34995b63%22+OR+%22uuid%3A00012082-f119-4370-9c0c-b6055137d6d8%22+OR+%22uuid%3A00012289-fae2-44a5-ba57-db7e5ab1159b%22+OR+%22uuid%3A0001257f-b598-4a0e-9c1f-8b199c115929%22+OR+%22uuid%3A00012835-2127-42b5-b598-6ece58276164%22+OR+%22uuid%3A00012ff0-a588-470b-9428-f2e1eddc2e6a%22+OR+%22uuid%3A00012ff0-a588-470b-9428-f2e1eddc2e6a%2F%401%22+OR+%22uuid%3A00013431-3c37-40db-9b4c-e87bbdd1d8eb%22+OR+%22uuid%3A00013618-6699-4721-b8d1-61b3a96bd969%22+OR+%22uuid%3A000136ec-ae05-426b-85e1-e6b38576f761%22+OR+%22uuid%3A000136ec-ae05-426b-85e1-e6b38576f761%2F%401%22+OR+%22uuid%3A00013afa-b718-42b1-b9bf-7f633b97c747%22+OR+%22uuid%3A00013d61-2317-43d2-9a69-39686e37c527%22+OR+%22uuid%3A00015940-ee76-415d-8169-797789406cff%22+OR+%22uuid%3A00016234-018a-4cce-90a4-5eaecd78122f%22+OR+%22uuid%3A00016cdf-2591-465d-bfd0-fece6ae08bf1%22+OR+%22uuid%3A0001714e-bbb4-4d7a-856a-c79aa618bfa2%22+OR+%22uuid%3A00017177-b182-46da-b88a-1377215389ea%22+OR+%22uuid%3A0001759e-cad0-467b-b61a-cfe1381a76f6%22)&fl=PID+collection&wt=xml&rows=20";
        String fourthResp  = IOUtils.toString(this.getClass().getResourceAsStream("notexists/3.xml"), "UTF-8");
        List<Object> mocksFromFourthCall = webCallExpect(client, fourthReq, fourthResp);

        String fourthReqFetch = "http://kramerius4.mlp.cz/search/api/v5.0/search/?q=PID:(%22uuid%3A00010fd5-d7c8-46fa-b87a-d87ca2aa84a5%22+OR+%22uuid%3A00011d48-9699-4b21-b000-9b0c34995b63%22+OR+%22uuid%3A00012082-f119-4370-9c0c-b6055137d6d8%22+OR+%22uuid%3A00012289-fae2-44a5-ba57-db7e5ab1159b%22+OR+%22uuid%3A0001257f-b598-4a0e-9c1f-8b199c115929%22+OR+%22uuid%3A00012835-2127-42b5-b598-6ece58276164%22+OR+%22uuid%3A00012ff0-a588-470b-9428-f2e1eddc2e6a%22+OR+%22uuid%3A00012ff0-a588-470b-9428-f2e1eddc2e6a%2F%401%22+OR+%22uuid%3A00013431-3c37-40db-9b4c-e87bbdd1d8eb%22+OR+%22uuid%3A00013618-6699-4721-b8d1-61b3a96bd969%22+OR+%22uuid%3A000136ec-ae05-426b-85e1-e6b38576f761%22+OR+%22uuid%3A000136ec-ae05-426b-85e1-e6b38576f761%2F%401%22+OR+%22uuid%3A00013afa-b718-42b1-b9bf-7f633b97c747%22+OR+%22uuid%3A00013d61-2317-43d2-9a69-39686e37c527%22+OR+%22uuid%3A00015940-ee76-415d-8169-797789406cff%22+OR+%22uuid%3A00016234-018a-4cce-90a4-5eaecd78122f%22+OR+%22uuid%3A00016cdf-2591-465d-bfd0-fece6ae08bf1%22+OR+%22uuid%3A0001714e-bbb4-4d7a-856a-c79aa618bfa2%22+OR+%22uuid%3A00017177-b182-46da-b88a-1377215389ea%22+OR+%22uuid%3A0001759e-cad0-467b-b61a-cfe1381a76f6%22)&fl=PID+timestamp+fedora.model+document_type+handle+status+created_date+modified_date+parent_model+parent_pid+parent_pid+parent_title+root_model+root_pid+root_title+text_ocr+pages_count+datum_str+datum+rok+datum_begin+datum_end+datum_page+issn+mdt+ddt+dostupnost+keywords+geographic_names+collection+sec+model_path+pid_path+rels_ext_index+level+dc.title+title_sort+title_sort+dc.creator+dc.identifier+language+dc.description+details+facet_title+browse_title+browse_autor+img_full_mime+viewable+virtual+location+range+mods.shelfLocator+mods.physicalLocation+text+dnnt&wt=xml&rows=20";
        String fourthRespFetch  = IOUtils.toString(this.getClass().getResourceAsStream("notexists/3_fetch.xml"), "UTF-8");
        List<Object> mocksFromFourthFetchCall = webCallExpect(client, fourthReqFetch, fourthRespFetch);
        // ----------


        // ---  80 indexed ---
        String fifthReq = "http://10.19.6.10:8983/solr/k7_5/select/?q=PID:(%22uuid%3A000182c3-0215-4103-90ce-9f912a3caf02%22+OR+%22uuid%3A00018da8-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001900d-e592-4ad4-8f61-21289fa0108c%22+OR+%22uuid%3A000199e3-6c01-4055-b620-d809be8c34c9%22+OR+%22uuid%3A00019d0e-ebcf-4aa0-ae46-e85403f867d0%22+OR+%22uuid%3A0001a28e-d40f-4caa-b99d-56b6b272be10%22+OR+%22uuid%3A0001b5ad-a2aa-43ea-912c-f472f7d0a786%22+OR+%22uuid%3A0001c4ba-bedf-4dbd-beb4-f87baf5bec3c%22+OR+%22uuid%3A0001c89d-0b86-448c-91c1-688f6882d548%22+OR+%22uuid%3A0001caa1-2fd5-4e0d-a74a-516286ee9414%22+OR+%22uuid%3A0001cd7d-446b-4696-b10d-a7115c6e4ed7%22+OR+%22uuid%3A0001cff1-9c7c-47ac-a2f0-340514a12852%22+OR+%22uuid%3A0001d02f-af3e-4d9d-9cae-31f1373aeffb%22+OR+%22uuid%3A0001dbc9-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001dbca-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001dbcb-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001dbcc-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001dbcd-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001dbce-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001dbcf-3105-11e9-8847-005056a2b051%22)&fl=PID+collection&wt=xml&rows=20";
        String fifthResp  = IOUtils.toString(this.getClass().getResourceAsStream("notexists/4.xml"), "UTF-8");
        List<Object> mocksFromFifthCall = webCallExpect(client, fifthReq, fifthResp);

        String fifthReqFetch = "http://kramerius4.mlp.cz/search/api/v5.0/search/?q=PID:(%22uuid%3A000182c3-0215-4103-90ce-9f912a3caf02%22+OR+%22uuid%3A00018da8-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001900d-e592-4ad4-8f61-21289fa0108c%22+OR+%22uuid%3A000199e3-6c01-4055-b620-d809be8c34c9%22+OR+%22uuid%3A00019d0e-ebcf-4aa0-ae46-e85403f867d0%22+OR+%22uuid%3A0001a28e-d40f-4caa-b99d-56b6b272be10%22+OR+%22uuid%3A0001b5ad-a2aa-43ea-912c-f472f7d0a786%22+OR+%22uuid%3A0001c4ba-bedf-4dbd-beb4-f87baf5bec3c%22+OR+%22uuid%3A0001c89d-0b86-448c-91c1-688f6882d548%22+OR+%22uuid%3A0001caa1-2fd5-4e0d-a74a-516286ee9414%22+OR+%22uuid%3A0001cd7d-446b-4696-b10d-a7115c6e4ed7%22+OR+%22uuid%3A0001cff1-9c7c-47ac-a2f0-340514a12852%22+OR+%22uuid%3A0001d02f-af3e-4d9d-9cae-31f1373aeffb%22+OR+%22uuid%3A0001dbc9-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001dbca-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001dbcb-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001dbcc-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001dbcd-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001dbce-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001dbcf-3105-11e9-8847-005056a2b051%22)&fl=PID+timestamp+fedora.model+document_type+handle+status+created_date+modified_date+parent_model+parent_pid+parent_pid+parent_title+root_model+root_pid+root_title+text_ocr+pages_count+datum_str+datum+rok+datum_begin+datum_end+datum_page+issn+mdt+ddt+dostupnost+keywords+geographic_names+collection+sec+model_path+pid_path+rels_ext_index+level+dc.title+title_sort+title_sort+dc.creator+dc.identifier+language+dc.description+details+facet_title+browse_title+browse_autor+img_full_mime+viewable+virtual+location+range+mods.shelfLocator+mods.physicalLocation+text+dnnt&wt=xml&rows=20";
        String fifthRespFetch  = IOUtils.toString(this.getClass().getResourceAsStream("notexists/4_fetch.xml"), "UTF-8");
        List<Object> mocksFromFifthFetchCall = webCallExpect(client, fifthReqFetch, fifthRespFetch);
        // ----------


        // ---  100 indexed ---
        String sixthReq = "http://10.19.6.10:8983/solr/k7_5/select/?q=PID:(%22uuid%3A0001dbd0-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001de4b-75ee-4428-bb20-737cc2cee698%22+OR+%22uuid%3A0001e211-3e75-4c7e-871d-662c2bfc4471%22+OR+%22uuid%3A0001e71d-9c40-482c-b0b4-70fa1d62d4ed%22+OR+%22uuid%3A0001e8ae-02ab-49e7-bb84-d83291bc075e%22+OR+%22uuid%3A0001eff8-4618-417a-aede-4ab6b5274f8c%22+OR+%22uuid%3A0001f258-7472-42df-a994-c14817e8e9c5%22+OR+%22uuid%3A0001f761-5a0c-47cc-87de-db9513e4c305%22+OR+%22uuid%3A000202e1-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e2-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e3-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e4-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e5-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e6-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e7-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e8-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e9-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A00020759-510b-429d-a46c-ed0b25eb4a02%22+OR+%22uuid%3A000229fa-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000229fb-3105-11e9-8847-005056a2b051%22)&fl=PID+collection&wt=xml&rows=20";
        String sixthResp  = IOUtils.toString(this.getClass().getResourceAsStream("notexists/5.xml"), "UTF-8");
        List<Object> mocksFromSixthCall = webCallExpect(client, sixthReq, sixthResp);

        String sixthReqFetch = "http://kramerius4.mlp.cz/search/api/v5.0/search/?q=PID:(%22uuid%3A0001dbd0-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001de4b-75ee-4428-bb20-737cc2cee698%22+OR+%22uuid%3A0001e211-3e75-4c7e-871d-662c2bfc4471%22+OR+%22uuid%3A0001e71d-9c40-482c-b0b4-70fa1d62d4ed%22+OR+%22uuid%3A0001e8ae-02ab-49e7-bb84-d83291bc075e%22+OR+%22uuid%3A0001eff8-4618-417a-aede-4ab6b5274f8c%22+OR+%22uuid%3A0001f258-7472-42df-a994-c14817e8e9c5%22+OR+%22uuid%3A0001f761-5a0c-47cc-87de-db9513e4c305%22+OR+%22uuid%3A000202e1-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e2-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e3-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e4-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e5-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e6-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e7-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e8-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e9-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A00020759-510b-429d-a46c-ed0b25eb4a02%22+OR+%22uuid%3A000229fa-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000229fb-3105-11e9-8847-005056a2b051%22)&fl=PID+timestamp+fedora.model+document_type+handle+status+created_date+modified_date+parent_model+parent_pid+parent_pid+parent_title+root_model+root_pid+root_title+text_ocr+pages_count+datum_str+datum+rok+datum_begin+datum_end+datum_page+issn+mdt+ddt+dostupnost+keywords+geographic_names+collection+sec+model_path+pid_path+rels_ext_index+level+dc.title+title_sort+title_sort+dc.creator+dc.identifier+language+dc.description+details+facet_title+browse_title+browse_autor+img_full_mime+viewable+virtual+location+range+mods.shelfLocator+mods.physicalLocation+text+dnnt&wt=xml&rows=20";
        String sixthRespFetch  = IOUtils.toString(this.getClass().getResourceAsStream("notexists/5_fetch.xml"), "UTF-8");
        List<Object> mocksFromSixthFetchCall = webCallExpect(client, sixthReqFetch, sixthRespFetch);
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

        Capture secondArg = newCapture();
        EasyMock.expect(updateResponseBuilder.entity(capture(secondArg), EasyMock.eq(MediaType.TEXT_XML))).andReturn(updateResponseBuilder).once();

        Capture thirdArg = newCapture();
        EasyMock.expect(updateResponseBuilder.entity(capture(thirdArg), EasyMock.eq(MediaType.TEXT_XML))).andReturn(updateResponseBuilder).once();

        Capture fourthArg = newCapture();
        EasyMock.expect(updateResponseBuilder.entity(capture(fourthArg), EasyMock.eq(MediaType.TEXT_XML))).andReturn(updateResponseBuilder).once();

        Capture fifthArg = newCapture();
        EasyMock.expect(updateResponseBuilder.entity(capture(fifthArg), EasyMock.eq(MediaType.TEXT_XML))).andReturn(updateResponseBuilder).once();


        EasyMock.expect(updateResponseBuilder.post(ClientResponse.class)).andReturn(updateResponse).anyTimes();
        EasyMock.expect(updateResponse.getStatus()).andReturn(ClientResponse.Status.OK.getStatusCode()).anyTimes();
        EasyMock.expect(updateResponse.getEntityInputStream()).andDelegateTo(new MockClientResponse()).anyTimes();

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

        // check & fetch
        mocksFromThirdCall.stream().forEach(obj-> {
            EasyMock.replay(obj);
        });
        mocksFromThirdFetchCall.stream().forEach(obj-> {
            EasyMock.replay(obj);
        });

        // check & fetch
        mocksFromFourthCall.stream().forEach(obj-> {
            EasyMock.replay(obj);
        });
        mocksFromFourthFetchCall.stream().forEach(obj-> {
            EasyMock.replay(obj);
        });

        // check & fetch
        mocksFromFifthCall.stream().forEach(obj-> {
            EasyMock.replay(obj);
        });
        mocksFromFifthFetchCall.stream().forEach(obj-> {
            EasyMock.replay(obj);
        });


        // check & fetch
        mocksFromSixthCall.stream().forEach(obj-> {
            EasyMock.replay(obj);
        });
        mocksFromSixthFetchCall.stream().forEach(obj-> {
            EasyMock.replay(obj);
        });

        process.setClient(client);

        // start whole process
        process.migrate(configurationFile);



        assertThat(Input.fromReader(new StringReader((String) firstArg.getValue())))
                .and(Input.fromStream(this.getClass().getResourceAsStream("notexists/batches/1_batch.xml")))
                .ignoreComments()
                .ignoreWhitespace()
                .areSimilar();

        assertThat(Input.fromReader(new StringReader((String) secondArg.getValue())))
                .and(Input.fromStream(this.getClass().getResourceAsStream("notexists/batches/2_batch.xml")))
                .ignoreComments()
                .ignoreWhitespace()
                .areSimilar();

        assertThat(Input.fromReader(new StringReader((String) thirdArg.getValue())))
                .and(Input.fromStream(this.getClass().getResourceAsStream("notexists/batches/3_batch.xml")))
                .ignoreComments()
                .ignoreWhitespace()
                .areSimilar();

        assertThat(Input.fromReader(new StringReader((String) fourthArg.getValue())))
                .and(Input.fromStream(this.getClass().getResourceAsStream("notexists/batches/4_batch.xml")))
                .ignoreComments()
                .ignoreWhitespace()
                .areSimilar();

        assertThat(Input.fromReader(new StringReader((String) fifthArg.getValue())))
                .and(Input.fromStream(this.getClass().getResourceAsStream("notexists/batches/5_batch.xml")))
                .ignoreComments()
                .ignoreWhitespace()
                .areSimilar();


    }



    @Test
    public void testFullProcess_EXISTS_SOLR_CLOUD() throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException, SAXException, ParserConfigurationException, NoSuchMethodException, MigrateSolrIndexException, URISyntaxException {
        InputStream configurationStream = ParallelProcessImplTest_REPLICATE.class.getResourceAsStream("config.replicate.mlp_solrcloud.xml");
        String _configurationContent = IOUtils.toString(configurationStream, "UTF-8");

        InputStream logFileStream = ParallelProcessImplTest_REPLICATE.class.getResourceAsStream("logfile_log");
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

        // ---  20 updated ---
        String secondReq = "http://10.19.6.10:8983/solr/k7_5/select/?q=PID:(%22uuid%3A00000834-c06c-49ed-91e9-d54a0f8c8571%22+OR+%22uuid%3A00001fe4-1d09-42e2-845a-0201dd900e87%22+OR+%22uuid%3A0000211b-7c66-4730-b343-8ea2ef427f99%22+OR+%22uuid%3A000026fa-3695-4af4-aaaa-50557d8c2c6d%22+OR+%22uuid%3A0000329c-2940-89be-717b-82cac72948a8%22+OR+%22uuid%3A0000338c-4dd1-4d11-bda5-4c1b44d83628%22+OR+%22uuid%3A0000338c-4dd1-4d11-bda5-4c1b44d83628%2F%401%22+OR+%22uuid%3A0000338c-4dd1-4d11-bda5-4c1b44d83628%2F%402%22+OR+%22uuid%3A00003962-b232-4055-9a77-8d8db9487b90%22+OR+%22uuid%3A00003bb1-429d-11e2-849c-005056a60003%22+OR+%22uuid%3A000040ee-eda5-49d3-ae14-510a74921cda%22+OR+%22uuid%3A000047d7-3b67-4f08-a7cf-e843db917097%22+OR+%22uuid%3A000059eb-d782-4285-857a-41b370dc1afd%22+OR+%22uuid%3A00006174-e3da-488b-9a18-eef7b3d5e527%22+OR+%22uuid%3A00006bd9-100d-4c89-97e6-3529e99c45be%22+OR+%22uuid%3A00006e78-04c4-4fa0-9a9d-d7ba78f7c341%22+OR+%22uuid%3A00007ca2-054e-4fde-b73c-81f55a282ebc%22+OR+%22uuid%3A00007e8c-3f3a-4f73-ab70-0f95b1024f59%22+OR+%22uuid%3A000091fb-dc07-4594-9300-8a503dc09210%22+OR+%22uuid%3A0000a0c8-41b2-4fdd-9597-619b3a7ff965%22)&fl=PID+collection+root_pid&wt=xml&rows=20";
        String secondResp  = IOUtils.toString(this.getClass().getResourceAsStream("exists/1_solrcloud.xml"), "UTF-8");
        List<Object> mocksFromSecondCall = webCallExpect(client, secondReq, secondResp);
        // ----------

        // ---  40 updated ---
        String thirdReq = "http://10.19.6.10:8983/solr/k7_5/select/?q=PID:(%22uuid%3A0000a140-892d-48e3-851c-c0722578db50%22+OR+%22uuid%3A0000a210-40ad-48c6-a324-b4bd5fefe0f1%22+OR+%22uuid%3A0000a64f-d935-4260-acce-88833c115963%22+OR+%22uuid%3A0000ab19-c07d-41ac-9c6c-b03240271247%22+OR+%22uuid%3A0000abeb-c9ef-4ee9-8db1-536132bfac84%22+OR+%22uuid%3A0000b3cd-478a-4756-b302-31765cab9efc%22+OR+%22uuid%3A0000b3cd-478a-4756-b302-31765cab9efc%2F%401%22+OR+%22uuid%3A0000b3cd-478a-4756-b302-31765cab9efc%2F%402%22+OR+%22uuid%3A0000b509-ed4f-447e-895f-d17d0d4ad0a3%22+OR+%22uuid%3A0000bbf9-0881-4e13-a793-457ed22bc58d%22+OR+%22uuid%3A0000c101-821b-4e21-95a3-13110119e10c%22+OR+%22uuid%3A0000c509-14e4-40b6-b555-f9ad46bce4c0%22+OR+%22uuid%3A0000c71a-aec6-4772-95f7-9412fd19f74c%22+OR+%22uuid%3A0000d5b8-44be-4473-b5f3-9e1f2437ce61%22+OR+%22uuid%3A0000dc41-0c83-4fe3-b013-081b57c6db8e%22+OR+%22uuid%3A0000dcd8-9a40-4b91-b3be-c7ffa05f6295%22+OR+%22uuid%3A0000dedc-cf17-11e1-acea-005056a60003%22+OR+%22uuid%3A0000e1cf-0621-4f8a-9bbc-cfdf4d25f096%22+OR+%22uuid%3A0000ed1b-31a8-4275-96fe-e6c80ef3e1b1%22+OR+%22uuid%3A0000ffd8-d89c-42db-a818-537144d80261%22)&fl=PID+collection+root_pid&wt=xml&rows=20";
        String thirdResp  = IOUtils.toString(this.getClass().getResourceAsStream("exists/2_solrcloud.xml"), "UTF-8");
        List<Object> mocksFromThirdCall = webCallExpect(client, thirdReq, thirdResp);
        // ----------

        // ---  60 updated ---
        String fourthReq = "http://10.19.6.10:8983/solr/k7_5/select/?q=PID:(%22uuid%3A00010fd5-d7c8-46fa-b87a-d87ca2aa84a5%22+OR+%22uuid%3A00011d48-9699-4b21-b000-9b0c34995b63%22+OR+%22uuid%3A00012082-f119-4370-9c0c-b6055137d6d8%22+OR+%22uuid%3A00012289-fae2-44a5-ba57-db7e5ab1159b%22+OR+%22uuid%3A0001257f-b598-4a0e-9c1f-8b199c115929%22+OR+%22uuid%3A00012835-2127-42b5-b598-6ece58276164%22+OR+%22uuid%3A00012ff0-a588-470b-9428-f2e1eddc2e6a%22+OR+%22uuid%3A00012ff0-a588-470b-9428-f2e1eddc2e6a%2F%401%22+OR+%22uuid%3A00013431-3c37-40db-9b4c-e87bbdd1d8eb%22+OR+%22uuid%3A00013618-6699-4721-b8d1-61b3a96bd969%22+OR+%22uuid%3A000136ec-ae05-426b-85e1-e6b38576f761%22+OR+%22uuid%3A000136ec-ae05-426b-85e1-e6b38576f761%2F%401%22+OR+%22uuid%3A00013afa-b718-42b1-b9bf-7f633b97c747%22+OR+%22uuid%3A00013d61-2317-43d2-9a69-39686e37c527%22+OR+%22uuid%3A00015940-ee76-415d-8169-797789406cff%22+OR+%22uuid%3A00016234-018a-4cce-90a4-5eaecd78122f%22+OR+%22uuid%3A00016cdf-2591-465d-bfd0-fece6ae08bf1%22+OR+%22uuid%3A0001714e-bbb4-4d7a-856a-c79aa618bfa2%22+OR+%22uuid%3A00017177-b182-46da-b88a-1377215389ea%22+OR+%22uuid%3A0001759e-cad0-467b-b61a-cfe1381a76f6%22)&fl=PID+collection+root_pid&wt=xml&rows=20";
        String fourthResp  = IOUtils.toString(this.getClass().getResourceAsStream("exists/3_solrcloud.xml"), "UTF-8");
        List<Object> mocksFromFourthCall = webCallExpect(client, fourthReq, fourthResp);
        // ----------

        // ---  80 updated ---
        String fifthReq = "http://10.19.6.10:8983/solr/k7_5/select/?q=PID:(%22uuid%3A000182c3-0215-4103-90ce-9f912a3caf02%22+OR+%22uuid%3A00018da8-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001900d-e592-4ad4-8f61-21289fa0108c%22+OR+%22uuid%3A000199e3-6c01-4055-b620-d809be8c34c9%22+OR+%22uuid%3A00019d0e-ebcf-4aa0-ae46-e85403f867d0%22+OR+%22uuid%3A0001a28e-d40f-4caa-b99d-56b6b272be10%22+OR+%22uuid%3A0001b5ad-a2aa-43ea-912c-f472f7d0a786%22+OR+%22uuid%3A0001c4ba-bedf-4dbd-beb4-f87baf5bec3c%22+OR+%22uuid%3A0001c89d-0b86-448c-91c1-688f6882d548%22+OR+%22uuid%3A0001caa1-2fd5-4e0d-a74a-516286ee9414%22+OR+%22uuid%3A0001cd7d-446b-4696-b10d-a7115c6e4ed7%22+OR+%22uuid%3A0001cff1-9c7c-47ac-a2f0-340514a12852%22+OR+%22uuid%3A0001d02f-af3e-4d9d-9cae-31f1373aeffb%22+OR+%22uuid%3A0001dbc9-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001dbca-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001dbcb-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001dbcc-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001dbcd-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001dbce-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001dbcf-3105-11e9-8847-005056a2b051%22)&fl=PID+collection+root_pid&wt=xml&rows=20";
        String fifthResp  = IOUtils.toString(this.getClass().getResourceAsStream("exists/4_solrcloud.xml"), "UTF-8");
        List<Object> mocksFromFifthCall = webCallExpect(client, fifthReq, fifthResp);
        // ----------

        // ---  100 updated ---
        String sixthReq = "http://10.19.6.10:8983/solr/k7_5/select/?q=PID:(%22uuid%3A0001dbd0-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001de4b-75ee-4428-bb20-737cc2cee698%22+OR+%22uuid%3A0001e211-3e75-4c7e-871d-662c2bfc4471%22+OR+%22uuid%3A0001e71d-9c40-482c-b0b4-70fa1d62d4ed%22+OR+%22uuid%3A0001e8ae-02ab-49e7-bb84-d83291bc075e%22+OR+%22uuid%3A0001eff8-4618-417a-aede-4ab6b5274f8c%22+OR+%22uuid%3A0001f258-7472-42df-a994-c14817e8e9c5%22+OR+%22uuid%3A0001f761-5a0c-47cc-87de-db9513e4c305%22+OR+%22uuid%3A000202e1-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e2-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e3-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e4-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e5-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e6-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e7-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e8-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e9-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A00020759-510b-429d-a46c-ed0b25eb4a02%22+OR+%22uuid%3A000229fa-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000229fb-3105-11e9-8847-005056a2b051%22)&fl=PID+collection+root_pid&wt=xml&rows=20";
        String sixthResp  = IOUtils.toString(this.getClass().getResourceAsStream("exists/5_solrcloud.xml"), "UTF-8");
        List<Object> mocksFromSixthCall = webCallExpect(client, sixthReq, sixthResp);
        // ----------


        EasyMock.expect(process.buildClient()).andReturn(client).anyTimes();

        // all updates in one check; (anyString)
        WebResource updateResource = EasyMock.createMock(WebResource.class);
        ClientResponse updateResponse = EasyMock.createMock(ClientResponse.class);
        WebResource.Builder updateResponseBuilder = EasyMock.createMock(WebResource.Builder.class);
        EasyMock.expect(client.resource("http://192.168.10.109:18984/solr-test/kramerius-cdk-test/update")).andReturn(updateResource).anyTimes();
        EasyMock.expect(updateResource.accept(MediaType.TEXT_XML)).andReturn(updateResponseBuilder).anyTimes();
        EasyMock.expect(updateResponseBuilder.type(MediaType.TEXT_XML)).andReturn(updateResponseBuilder).anyTimes();


        Capture firstArg = newCapture();
        EasyMock.expect(updateResponseBuilder.entity(capture(firstArg), EasyMock.eq(MediaType.TEXT_XML))).andReturn(updateResponseBuilder).once();

        Capture secondArg = newCapture();
        EasyMock.expect(updateResponseBuilder.entity(capture(secondArg), EasyMock.eq(MediaType.TEXT_XML))).andReturn(updateResponseBuilder).once();

        Capture thirdArg = newCapture();
        EasyMock.expect(updateResponseBuilder.entity(capture(thirdArg), EasyMock.eq(MediaType.TEXT_XML))).andReturn(updateResponseBuilder).once();

        Capture fourthArg = newCapture();
        EasyMock.expect(updateResponseBuilder.entity(capture(fourthArg), EasyMock.eq(MediaType.TEXT_XML))).andReturn(updateResponseBuilder).once();

        Capture fifthArg = newCapture();
        EasyMock.expect(updateResponseBuilder.entity(capture(fifthArg), EasyMock.eq(MediaType.TEXT_XML))).andReturn(updateResponseBuilder).once();


        //EasyMock.expect(updateResponseBuilder.entity(EasyMock.anyObject(String.class), EasyMock.eq(MediaType.TEXT_XML))).andReturn(updateResponseBuilder).anyTimes();

        EasyMock.expect(updateResponseBuilder.post(ClientResponse.class)).andReturn(updateResponse).anyTimes();
        EasyMock.expect(updateResponse.getStatus()).andReturn(ClientResponse.Status.OK.getStatusCode()).anyTimes();
        EasyMock.expect(updateResponse.getEntityInputStream()).andDelegateTo(new MockClientResponse()).anyTimes();

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
        mocksFromSecondCall.stream().forEach(obj-> {
            EasyMock.replay(obj);
        });
        mocksFromThirdCall.stream().forEach(obj-> {
            EasyMock.replay(obj);
        });
        mocksFromFourthCall.stream().forEach(obj-> {
            EasyMock.replay(obj);
        });
        mocksFromFifthCall.stream().forEach(obj-> {
            EasyMock.replay(obj);
        });
        mocksFromSixthCall.stream().forEach(obj-> {
            EasyMock.replay(obj);
        });

        process.setClient(client);

        // start whole process
        process.migrate(configurationFile);

        assertThat(Input.fromReader(new StringReader((String) firstArg.getValue())))
                .and(Input.fromStream(this.getClass().getResourceAsStream("exists/batches/1_batch_solrcloud.xml")))
                .ignoreComments()
                .ignoreWhitespace()
                .areSimilar();

        assertThat(Input.fromReader(new StringReader((String) secondArg.getValue())))
                .and(Input.fromStream(this.getClass().getResourceAsStream("exists/batches/2_batch_solrcloud.xml")))
                .ignoreComments()
                .ignoreWhitespace()
                .areSimilar();

        assertThat(Input.fromReader(new StringReader((String) thirdArg.getValue())))
                .and(Input.fromStream(this.getClass().getResourceAsStream("exists/batches/3_batch_solrcloud.xml")))
                .ignoreComments()
                .ignoreWhitespace()
                .areSimilar();

        assertThat(Input.fromReader(new StringReader((String) fourthArg.getValue())))
                .and(Input.fromStream(this.getClass().getResourceAsStream("exists/batches/4_batch_solrcloud.xml")))
                .ignoreComments()
                .ignoreWhitespace()
                .areSimilar();

        assertThat(Input.fromReader(new StringReader((String) fifthArg.getValue())))
                .and(Input.fromStream(this.getClass().getResourceAsStream("exists/batches/5_batch_solrcloud.xml")))
                .ignoreComments()
                .ignoreWhitespace()
                .areSimilar();

    }

    @Test
    public void testFullProcess_NOT_EXISTS_SOLR_CLOUD() throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException, SAXException, ParserConfigurationException, NoSuchMethodException, MigrateSolrIndexException, URISyntaxException {
        InputStream configurationStream = ParallelProcessImplTest_REPLICATE.class.getResourceAsStream("config.replicate.mlp_solrcloud.xml");
        String _configurationContent = IOUtils.toString(configurationStream, "UTF-8");

        InputStream logFileStream = ParallelProcessImplTest_REPLICATE.class.getResourceAsStream("logfile_log");
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

        // ---  20 indexed ---
        String secondReq = "http://10.19.6.10:8983/solr/k7_5/select/?q=PID:(%22uuid%3A00000834-c06c-49ed-91e9-d54a0f8c8571%22+OR+%22uuid%3A00001fe4-1d09-42e2-845a-0201dd900e87%22+OR+%22uuid%3A0000211b-7c66-4730-b343-8ea2ef427f99%22+OR+%22uuid%3A000026fa-3695-4af4-aaaa-50557d8c2c6d%22+OR+%22uuid%3A0000329c-2940-89be-717b-82cac72948a8%22+OR+%22uuid%3A0000338c-4dd1-4d11-bda5-4c1b44d83628%22+OR+%22uuid%3A0000338c-4dd1-4d11-bda5-4c1b44d83628%2F%401%22+OR+%22uuid%3A0000338c-4dd1-4d11-bda5-4c1b44d83628%2F%402%22+OR+%22uuid%3A00003962-b232-4055-9a77-8d8db9487b90%22+OR+%22uuid%3A00003bb1-429d-11e2-849c-005056a60003%22+OR+%22uuid%3A000040ee-eda5-49d3-ae14-510a74921cda%22+OR+%22uuid%3A000047d7-3b67-4f08-a7cf-e843db917097%22+OR+%22uuid%3A000059eb-d782-4285-857a-41b370dc1afd%22+OR+%22uuid%3A00006174-e3da-488b-9a18-eef7b3d5e527%22+OR+%22uuid%3A00006bd9-100d-4c89-97e6-3529e99c45be%22+OR+%22uuid%3A00006e78-04c4-4fa0-9a9d-d7ba78f7c341%22+OR+%22uuid%3A00007ca2-054e-4fde-b73c-81f55a282ebc%22+OR+%22uuid%3A00007e8c-3f3a-4f73-ab70-0f95b1024f59%22+OR+%22uuid%3A000091fb-dc07-4594-9300-8a503dc09210%22+OR+%22uuid%3A0000a0c8-41b2-4fdd-9597-619b3a7ff965%22)&fl=PID+collection+root_pid&wt=xml&rows=20";
        String secondResp  = IOUtils.toString(this.getClass().getResourceAsStream("notexists/1.xml"), "UTF-8");
        List<Object> mocksFromSecondCall = webCallExpect(client, secondReq, secondResp);

        String secondReqFetch = "http://kramerius4.mlp.cz/search/api/v5.0/search/?q=PID:(%22uuid%3A00000834-c06c-49ed-91e9-d54a0f8c8571%22+OR+%22uuid%3A00001fe4-1d09-42e2-845a-0201dd900e87%22+OR+%22uuid%3A0000211b-7c66-4730-b343-8ea2ef427f99%22+OR+%22uuid%3A000026fa-3695-4af4-aaaa-50557d8c2c6d%22+OR+%22uuid%3A0000329c-2940-89be-717b-82cac72948a8%22+OR+%22uuid%3A0000338c-4dd1-4d11-bda5-4c1b44d83628%22+OR+%22uuid%3A0000338c-4dd1-4d11-bda5-4c1b44d83628%2F%401%22+OR+%22uuid%3A0000338c-4dd1-4d11-bda5-4c1b44d83628%2F%402%22+OR+%22uuid%3A00003962-b232-4055-9a77-8d8db9487b90%22+OR+%22uuid%3A00003bb1-429d-11e2-849c-005056a60003%22+OR+%22uuid%3A000040ee-eda5-49d3-ae14-510a74921cda%22+OR+%22uuid%3A000047d7-3b67-4f08-a7cf-e843db917097%22+OR+%22uuid%3A000059eb-d782-4285-857a-41b370dc1afd%22+OR+%22uuid%3A00006174-e3da-488b-9a18-eef7b3d5e527%22+OR+%22uuid%3A00006bd9-100d-4c89-97e6-3529e99c45be%22+OR+%22uuid%3A00006e78-04c4-4fa0-9a9d-d7ba78f7c341%22+OR+%22uuid%3A00007ca2-054e-4fde-b73c-81f55a282ebc%22+OR+%22uuid%3A00007e8c-3f3a-4f73-ab70-0f95b1024f59%22+OR+%22uuid%3A000091fb-dc07-4594-9300-8a503dc09210%22+OR+%22uuid%3A0000a0c8-41b2-4fdd-9597-619b3a7ff965%22)&fl=PID+timestamp+fedora.model+document_type+handle+status+created_date+modified_date+parent_model+parent_pid+parent_pid+parent_title+root_model+root_pid+root_title+text_ocr+pages_count+datum_str+datum+rok+datum_begin+datum_end+datum_page+issn+mdt+ddt+dostupnost+keywords+geographic_names+collection+sec+model_path+pid_path+rels_ext_index+level+dc.title+title_sort+title_sort+dc.creator+dc.identifier+language+dc.description+details+facet_title+browse_title+browse_autor+img_full_mime+viewable+virtual+location+range+mods.shelfLocator+mods.physicalLocation+text+dnnt&wt=xml&rows=20";
        String secondRespFetch  = IOUtils.toString(this.getClass().getResourceAsStream("notexists/1_fetch.xml"), "UTF-8");
        List<Object> mocksFromSecondFetchCall = webCallExpect(client, secondReqFetch, secondRespFetch);
        // ----------

        // ---  40 indexed ---
        String thirdReq = "http://10.19.6.10:8983/solr/k7_5/select/?q=PID:(%22uuid%3A0000a140-892d-48e3-851c-c0722578db50%22+OR+%22uuid%3A0000a210-40ad-48c6-a324-b4bd5fefe0f1%22+OR+%22uuid%3A0000a64f-d935-4260-acce-88833c115963%22+OR+%22uuid%3A0000ab19-c07d-41ac-9c6c-b03240271247%22+OR+%22uuid%3A0000abeb-c9ef-4ee9-8db1-536132bfac84%22+OR+%22uuid%3A0000b3cd-478a-4756-b302-31765cab9efc%22+OR+%22uuid%3A0000b3cd-478a-4756-b302-31765cab9efc%2F%401%22+OR+%22uuid%3A0000b3cd-478a-4756-b302-31765cab9efc%2F%402%22+OR+%22uuid%3A0000b509-ed4f-447e-895f-d17d0d4ad0a3%22+OR+%22uuid%3A0000bbf9-0881-4e13-a793-457ed22bc58d%22+OR+%22uuid%3A0000c101-821b-4e21-95a3-13110119e10c%22+OR+%22uuid%3A0000c509-14e4-40b6-b555-f9ad46bce4c0%22+OR+%22uuid%3A0000c71a-aec6-4772-95f7-9412fd19f74c%22+OR+%22uuid%3A0000d5b8-44be-4473-b5f3-9e1f2437ce61%22+OR+%22uuid%3A0000dc41-0c83-4fe3-b013-081b57c6db8e%22+OR+%22uuid%3A0000dcd8-9a40-4b91-b3be-c7ffa05f6295%22+OR+%22uuid%3A0000dedc-cf17-11e1-acea-005056a60003%22+OR+%22uuid%3A0000e1cf-0621-4f8a-9bbc-cfdf4d25f096%22+OR+%22uuid%3A0000ed1b-31a8-4275-96fe-e6c80ef3e1b1%22+OR+%22uuid%3A0000ffd8-d89c-42db-a818-537144d80261%22)&fl=PID+collection+root_pid&wt=xml&rows=20";
        String thirdResp  = IOUtils.toString(this.getClass().getResourceAsStream("notexists/2.xml"), "UTF-8");
        List<Object> mocksFromThirdCall = webCallExpect(client, thirdReq, thirdResp);

        String thirdReqFetch = "http://kramerius4.mlp.cz/search/api/v5.0/search/?q=PID:(%22uuid%3A0000a140-892d-48e3-851c-c0722578db50%22+OR+%22uuid%3A0000a210-40ad-48c6-a324-b4bd5fefe0f1%22+OR+%22uuid%3A0000a64f-d935-4260-acce-88833c115963%22+OR+%22uuid%3A0000ab19-c07d-41ac-9c6c-b03240271247%22+OR+%22uuid%3A0000abeb-c9ef-4ee9-8db1-536132bfac84%22+OR+%22uuid%3A0000b3cd-478a-4756-b302-31765cab9efc%22+OR+%22uuid%3A0000b3cd-478a-4756-b302-31765cab9efc%2F%401%22+OR+%22uuid%3A0000b3cd-478a-4756-b302-31765cab9efc%2F%402%22+OR+%22uuid%3A0000b509-ed4f-447e-895f-d17d0d4ad0a3%22+OR+%22uuid%3A0000bbf9-0881-4e13-a793-457ed22bc58d%22+OR+%22uuid%3A0000c101-821b-4e21-95a3-13110119e10c%22+OR+%22uuid%3A0000c509-14e4-40b6-b555-f9ad46bce4c0%22+OR+%22uuid%3A0000c71a-aec6-4772-95f7-9412fd19f74c%22+OR+%22uuid%3A0000d5b8-44be-4473-b5f3-9e1f2437ce61%22+OR+%22uuid%3A0000dc41-0c83-4fe3-b013-081b57c6db8e%22+OR+%22uuid%3A0000dcd8-9a40-4b91-b3be-c7ffa05f6295%22+OR+%22uuid%3A0000dedc-cf17-11e1-acea-005056a60003%22+OR+%22uuid%3A0000e1cf-0621-4f8a-9bbc-cfdf4d25f096%22+OR+%22uuid%3A0000ed1b-31a8-4275-96fe-e6c80ef3e1b1%22+OR+%22uuid%3A0000ffd8-d89c-42db-a818-537144d80261%22)&fl=PID+timestamp+fedora.model+document_type+handle+status+created_date+modified_date+parent_model+parent_pid+parent_pid+parent_title+root_model+root_pid+root_title+text_ocr+pages_count+datum_str+datum+rok+datum_begin+datum_end+datum_page+issn+mdt+ddt+dostupnost+keywords+geographic_names+collection+sec+model_path+pid_path+rels_ext_index+level+dc.title+title_sort+title_sort+dc.creator+dc.identifier+language+dc.description+details+facet_title+browse_title+browse_autor+img_full_mime+viewable+virtual+location+range+mods.shelfLocator+mods.physicalLocation+text+dnnt&wt=xml&rows=20";
        String thirdRespFetch  = IOUtils.toString(this.getClass().getResourceAsStream("notexists/2_fetch.xml"), "UTF-8");
        List<Object> mocksFromThirdFetchCall = webCallExpect(client, thirdReqFetch, thirdRespFetch);
        // ----------

        // ---  60 indexed ---
        String fourthReq = "http://10.19.6.10:8983/solr/k7_5/select/?q=PID:(%22uuid%3A00010fd5-d7c8-46fa-b87a-d87ca2aa84a5%22+OR+%22uuid%3A00011d48-9699-4b21-b000-9b0c34995b63%22+OR+%22uuid%3A00012082-f119-4370-9c0c-b6055137d6d8%22+OR+%22uuid%3A00012289-fae2-44a5-ba57-db7e5ab1159b%22+OR+%22uuid%3A0001257f-b598-4a0e-9c1f-8b199c115929%22+OR+%22uuid%3A00012835-2127-42b5-b598-6ece58276164%22+OR+%22uuid%3A00012ff0-a588-470b-9428-f2e1eddc2e6a%22+OR+%22uuid%3A00012ff0-a588-470b-9428-f2e1eddc2e6a%2F%401%22+OR+%22uuid%3A00013431-3c37-40db-9b4c-e87bbdd1d8eb%22+OR+%22uuid%3A00013618-6699-4721-b8d1-61b3a96bd969%22+OR+%22uuid%3A000136ec-ae05-426b-85e1-e6b38576f761%22+OR+%22uuid%3A000136ec-ae05-426b-85e1-e6b38576f761%2F%401%22+OR+%22uuid%3A00013afa-b718-42b1-b9bf-7f633b97c747%22+OR+%22uuid%3A00013d61-2317-43d2-9a69-39686e37c527%22+OR+%22uuid%3A00015940-ee76-415d-8169-797789406cff%22+OR+%22uuid%3A00016234-018a-4cce-90a4-5eaecd78122f%22+OR+%22uuid%3A00016cdf-2591-465d-bfd0-fece6ae08bf1%22+OR+%22uuid%3A0001714e-bbb4-4d7a-856a-c79aa618bfa2%22+OR+%22uuid%3A00017177-b182-46da-b88a-1377215389ea%22+OR+%22uuid%3A0001759e-cad0-467b-b61a-cfe1381a76f6%22)&fl=PID+collection+root_pid&wt=xml&rows=20";
        String fourthResp  = IOUtils.toString(this.getClass().getResourceAsStream("notexists/3.xml"), "UTF-8");
        List<Object> mocksFromFourthCall = webCallExpect(client, fourthReq, fourthResp);

        String fourthReqFetch = "http://kramerius4.mlp.cz/search/api/v5.0/search/?q=PID:(%22uuid%3A00010fd5-d7c8-46fa-b87a-d87ca2aa84a5%22+OR+%22uuid%3A00011d48-9699-4b21-b000-9b0c34995b63%22+OR+%22uuid%3A00012082-f119-4370-9c0c-b6055137d6d8%22+OR+%22uuid%3A00012289-fae2-44a5-ba57-db7e5ab1159b%22+OR+%22uuid%3A0001257f-b598-4a0e-9c1f-8b199c115929%22+OR+%22uuid%3A00012835-2127-42b5-b598-6ece58276164%22+OR+%22uuid%3A00012ff0-a588-470b-9428-f2e1eddc2e6a%22+OR+%22uuid%3A00012ff0-a588-470b-9428-f2e1eddc2e6a%2F%401%22+OR+%22uuid%3A00013431-3c37-40db-9b4c-e87bbdd1d8eb%22+OR+%22uuid%3A00013618-6699-4721-b8d1-61b3a96bd969%22+OR+%22uuid%3A000136ec-ae05-426b-85e1-e6b38576f761%22+OR+%22uuid%3A000136ec-ae05-426b-85e1-e6b38576f761%2F%401%22+OR+%22uuid%3A00013afa-b718-42b1-b9bf-7f633b97c747%22+OR+%22uuid%3A00013d61-2317-43d2-9a69-39686e37c527%22+OR+%22uuid%3A00015940-ee76-415d-8169-797789406cff%22+OR+%22uuid%3A00016234-018a-4cce-90a4-5eaecd78122f%22+OR+%22uuid%3A00016cdf-2591-465d-bfd0-fece6ae08bf1%22+OR+%22uuid%3A0001714e-bbb4-4d7a-856a-c79aa618bfa2%22+OR+%22uuid%3A00017177-b182-46da-b88a-1377215389ea%22+OR+%22uuid%3A0001759e-cad0-467b-b61a-cfe1381a76f6%22)&fl=PID+timestamp+fedora.model+document_type+handle+status+created_date+modified_date+parent_model+parent_pid+parent_pid+parent_title+root_model+root_pid+root_title+text_ocr+pages_count+datum_str+datum+rok+datum_begin+datum_end+datum_page+issn+mdt+ddt+dostupnost+keywords+geographic_names+collection+sec+model_path+pid_path+rels_ext_index+level+dc.title+title_sort+title_sort+dc.creator+dc.identifier+language+dc.description+details+facet_title+browse_title+browse_autor+img_full_mime+viewable+virtual+location+range+mods.shelfLocator+mods.physicalLocation+text+dnnt&wt=xml&rows=20";
        String fourthRespFetch  = IOUtils.toString(this.getClass().getResourceAsStream("notexists/3_fetch.xml"), "UTF-8");
        List<Object> mocksFromFourthFetchCall = webCallExpect(client, fourthReqFetch, fourthRespFetch);
        // ----------


        // ---  80 indexed ---
        String fifthReq = "http://10.19.6.10:8983/solr/k7_5/select/?q=PID:(%22uuid%3A000182c3-0215-4103-90ce-9f912a3caf02%22+OR+%22uuid%3A00018da8-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001900d-e592-4ad4-8f61-21289fa0108c%22+OR+%22uuid%3A000199e3-6c01-4055-b620-d809be8c34c9%22+OR+%22uuid%3A00019d0e-ebcf-4aa0-ae46-e85403f867d0%22+OR+%22uuid%3A0001a28e-d40f-4caa-b99d-56b6b272be10%22+OR+%22uuid%3A0001b5ad-a2aa-43ea-912c-f472f7d0a786%22+OR+%22uuid%3A0001c4ba-bedf-4dbd-beb4-f87baf5bec3c%22+OR+%22uuid%3A0001c89d-0b86-448c-91c1-688f6882d548%22+OR+%22uuid%3A0001caa1-2fd5-4e0d-a74a-516286ee9414%22+OR+%22uuid%3A0001cd7d-446b-4696-b10d-a7115c6e4ed7%22+OR+%22uuid%3A0001cff1-9c7c-47ac-a2f0-340514a12852%22+OR+%22uuid%3A0001d02f-af3e-4d9d-9cae-31f1373aeffb%22+OR+%22uuid%3A0001dbc9-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001dbca-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001dbcb-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001dbcc-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001dbcd-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001dbce-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001dbcf-3105-11e9-8847-005056a2b051%22)&fl=PID+collection+root_pid&wt=xml&rows=20";
        String fifthResp  = IOUtils.toString(this.getClass().getResourceAsStream("notexists/4.xml"), "UTF-8");
        List<Object> mocksFromFifthCall = webCallExpect(client, fifthReq, fifthResp);

        String fifthReqFetch = "http://kramerius4.mlp.cz/search/api/v5.0/search/?q=PID:(%22uuid%3A000182c3-0215-4103-90ce-9f912a3caf02%22+OR+%22uuid%3A00018da8-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001900d-e592-4ad4-8f61-21289fa0108c%22+OR+%22uuid%3A000199e3-6c01-4055-b620-d809be8c34c9%22+OR+%22uuid%3A00019d0e-ebcf-4aa0-ae46-e85403f867d0%22+OR+%22uuid%3A0001a28e-d40f-4caa-b99d-56b6b272be10%22+OR+%22uuid%3A0001b5ad-a2aa-43ea-912c-f472f7d0a786%22+OR+%22uuid%3A0001c4ba-bedf-4dbd-beb4-f87baf5bec3c%22+OR+%22uuid%3A0001c89d-0b86-448c-91c1-688f6882d548%22+OR+%22uuid%3A0001caa1-2fd5-4e0d-a74a-516286ee9414%22+OR+%22uuid%3A0001cd7d-446b-4696-b10d-a7115c6e4ed7%22+OR+%22uuid%3A0001cff1-9c7c-47ac-a2f0-340514a12852%22+OR+%22uuid%3A0001d02f-af3e-4d9d-9cae-31f1373aeffb%22+OR+%22uuid%3A0001dbc9-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001dbca-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001dbcb-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001dbcc-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001dbcd-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001dbce-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001dbcf-3105-11e9-8847-005056a2b051%22)&fl=PID+timestamp+fedora.model+document_type+handle+status+created_date+modified_date+parent_model+parent_pid+parent_pid+parent_title+root_model+root_pid+root_title+text_ocr+pages_count+datum_str+datum+rok+datum_begin+datum_end+datum_page+issn+mdt+ddt+dostupnost+keywords+geographic_names+collection+sec+model_path+pid_path+rels_ext_index+level+dc.title+title_sort+title_sort+dc.creator+dc.identifier+language+dc.description+details+facet_title+browse_title+browse_autor+img_full_mime+viewable+virtual+location+range+mods.shelfLocator+mods.physicalLocation+text+dnnt&wt=xml&rows=20";
        String fifthRespFetch  = IOUtils.toString(this.getClass().getResourceAsStream("notexists/4_fetch.xml"), "UTF-8");
        List<Object> mocksFromFifthFetchCall = webCallExpect(client, fifthReqFetch, fifthRespFetch);
        // ----------


        // ---  100 indexed ---
        String sixthReq = "http://10.19.6.10:8983/solr/k7_5/select/?q=PID:(%22uuid%3A0001dbd0-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001de4b-75ee-4428-bb20-737cc2cee698%22+OR+%22uuid%3A0001e211-3e75-4c7e-871d-662c2bfc4471%22+OR+%22uuid%3A0001e71d-9c40-482c-b0b4-70fa1d62d4ed%22+OR+%22uuid%3A0001e8ae-02ab-49e7-bb84-d83291bc075e%22+OR+%22uuid%3A0001eff8-4618-417a-aede-4ab6b5274f8c%22+OR+%22uuid%3A0001f258-7472-42df-a994-c14817e8e9c5%22+OR+%22uuid%3A0001f761-5a0c-47cc-87de-db9513e4c305%22+OR+%22uuid%3A000202e1-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e2-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e3-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e4-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e5-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e6-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e7-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e8-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e9-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A00020759-510b-429d-a46c-ed0b25eb4a02%22+OR+%22uuid%3A000229fa-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000229fb-3105-11e9-8847-005056a2b051%22)&fl=PID+collection+root_pid&wt=xml&rows=20";
        String sixthResp  = IOUtils.toString(this.getClass().getResourceAsStream("notexists/5.xml"), "UTF-8");
        List<Object> mocksFromSixthCall = webCallExpect(client, sixthReq, sixthResp);

        String sixthReqFetch = "http://kramerius4.mlp.cz/search/api/v5.0/search/?q=PID:(%22uuid%3A0001dbd0-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A0001de4b-75ee-4428-bb20-737cc2cee698%22+OR+%22uuid%3A0001e211-3e75-4c7e-871d-662c2bfc4471%22+OR+%22uuid%3A0001e71d-9c40-482c-b0b4-70fa1d62d4ed%22+OR+%22uuid%3A0001e8ae-02ab-49e7-bb84-d83291bc075e%22+OR+%22uuid%3A0001eff8-4618-417a-aede-4ab6b5274f8c%22+OR+%22uuid%3A0001f258-7472-42df-a994-c14817e8e9c5%22+OR+%22uuid%3A0001f761-5a0c-47cc-87de-db9513e4c305%22+OR+%22uuid%3A000202e1-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e2-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e3-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e4-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e5-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e6-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e7-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e8-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000202e9-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A00020759-510b-429d-a46c-ed0b25eb4a02%22+OR+%22uuid%3A000229fa-3105-11e9-8847-005056a2b051%22+OR+%22uuid%3A000229fb-3105-11e9-8847-005056a2b051%22)&fl=PID+timestamp+fedora.model+document_type+handle+status+created_date+modified_date+parent_model+parent_pid+parent_pid+parent_title+root_model+root_pid+root_title+text_ocr+pages_count+datum_str+datum+rok+datum_begin+datum_end+datum_page+issn+mdt+ddt+dostupnost+keywords+geographic_names+collection+sec+model_path+pid_path+rels_ext_index+level+dc.title+title_sort+title_sort+dc.creator+dc.identifier+language+dc.description+details+facet_title+browse_title+browse_autor+img_full_mime+viewable+virtual+location+range+mods.shelfLocator+mods.physicalLocation+text+dnnt&wt=xml&rows=20";
        String sixthRespFetch  = IOUtils.toString(this.getClass().getResourceAsStream("notexists/5_fetch.xml"), "UTF-8");
        List<Object> mocksFromSixthFetchCall = webCallExpect(client, sixthReqFetch, sixthRespFetch);
        // ----------

        EasyMock.expect(process.buildClient()).andReturn(client).anyTimes();

        // all updates in one check
        WebResource updateResource = EasyMock.createMock(WebResource.class);
        ClientResponse updateResponse = EasyMock.createMock(ClientResponse.class);
        WebResource.Builder updateResponseBuilder = EasyMock.createMock(WebResource.Builder.class);
        EasyMock.expect(client.resource("http://192.168.10.109:18984/solr-test/kramerius-cdk-test/update")).andReturn(updateResource).anyTimes();
        EasyMock.expect(updateResource.accept(MediaType.TEXT_XML)).andReturn(updateResponseBuilder).anyTimes();
        EasyMock.expect(updateResponseBuilder.type(MediaType.TEXT_XML)).andReturn(updateResponseBuilder).anyTimes();

        Capture firstArg = newCapture();
        EasyMock.expect(updateResponseBuilder.entity(capture(firstArg), EasyMock.eq(MediaType.TEXT_XML))).andReturn(updateResponseBuilder).once();

        Capture secondArg = newCapture();
        EasyMock.expect(updateResponseBuilder.entity(capture(secondArg), EasyMock.eq(MediaType.TEXT_XML))).andReturn(updateResponseBuilder).once();

        Capture thirdArg = newCapture();
        EasyMock.expect(updateResponseBuilder.entity(capture(thirdArg), EasyMock.eq(MediaType.TEXT_XML))).andReturn(updateResponseBuilder).once();

        Capture fourthArg = newCapture();
        EasyMock.expect(updateResponseBuilder.entity(capture(fourthArg), EasyMock.eq(MediaType.TEXT_XML))).andReturn(updateResponseBuilder).once();

        Capture fifthArg = newCapture();
        EasyMock.expect(updateResponseBuilder.entity(capture(fifthArg), EasyMock.eq(MediaType.TEXT_XML))).andReturn(updateResponseBuilder).once();

        //EasyMock.expect(updateResponseBuilder.entity(EasyMock.anyObject(String.class), EasyMock.eq(MediaType.TEXT_XML))).andReturn(updateResponseBuilder).anyTimes();

        EasyMock.expect(updateResponseBuilder.post(ClientResponse.class)).andReturn(updateResponse).anyTimes();
        EasyMock.expect(updateResponse.getStatus()).andReturn(ClientResponse.Status.OK.getStatusCode()).anyTimes();
        EasyMock.expect(updateResponse.getEntityInputStream()).andDelegateTo(new MockClientResponse()).anyTimes();

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

        // check & fetch
        mocksFromThirdCall.stream().forEach(obj-> {
            EasyMock.replay(obj);
        });
        mocksFromThirdFetchCall.stream().forEach(obj-> {
            EasyMock.replay(obj);
        });

        // check & fetch
        mocksFromFourthCall.stream().forEach(obj-> {
            EasyMock.replay(obj);
        });
        mocksFromFourthFetchCall.stream().forEach(obj-> {
            EasyMock.replay(obj);
        });

        // check & fetch
        mocksFromFifthCall.stream().forEach(obj-> {
            EasyMock.replay(obj);
        });
        mocksFromFifthFetchCall.stream().forEach(obj-> {
            EasyMock.replay(obj);
        });


        // check & fetch
        mocksFromSixthCall.stream().forEach(obj-> {
            EasyMock.replay(obj);
        });
        mocksFromSixthFetchCall.stream().forEach(obj-> {
            EasyMock.replay(obj);
        });

        process.setClient(client);

        // start whole process
        process.migrate(configurationFile);


        assertThat(Input.fromReader(new StringReader((String) firstArg.getValue())))
                .and(Input.fromStream(this.getClass().getResourceAsStream("notexists/batches/1_batch_solrcloud.xml")))
                .ignoreComments()
                .ignoreWhitespace()
                .areSimilar();

        assertThat(Input.fromReader(new StringReader((String) secondArg.getValue())))
                .and(Input.fromStream(this.getClass().getResourceAsStream("notexists/batches/2_batch_solrcloud.xml")))
                .ignoreComments()
                .ignoreWhitespace()
                .areSimilar();

        assertThat(Input.fromReader(new StringReader((String) thirdArg.getValue())))
                .and(Input.fromStream(this.getClass().getResourceAsStream("notexists/batches/3_batch_solrcloud.xml")))
                .ignoreComments()
                .ignoreWhitespace()
                .areSimilar();

        assertThat(Input.fromReader(new StringReader((String) fourthArg.getValue())))
                .and(Input.fromStream(this.getClass().getResourceAsStream("notexists/batches/4_batch_solrcloud.xml")))
                .ignoreComments()
                .ignoreWhitespace()
                .areSimilar();

        assertThat(Input.fromReader(new StringReader((String) fifthArg.getValue())))
                .and(Input.fromStream(this.getClass().getResourceAsStream("notexists/batches/5_batch_solrcloud.xml")))
                .ignoreComments()
                .ignoreWhitespace()
                .areSimilar();


    }



    @Test
    public void testFullProcess_NOT_EXISTS_K7_TRANSFORM() throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException, SAXException, ParserConfigurationException, NoSuchMethodException, MigrateSolrIndexException, URISyntaxException {

        InputStream configurationStream = ParallelProcessImplTest_REPLICATE.class.getResourceAsStream("config.replicate.mlp_k7.xml");
        String _configurationContent = IOUtils.toString(configurationStream, "UTF-8");

        InputStream logFileStream = ParallelProcessImplTest_REPLICATE.class.getResourceAsStream("k7notexists/logfile_log");
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

        // ---  20 indexed ---
        String secondReq = "http://10.19.6.10:8983/solr/k7_5/select/?q=PID:(%22uuid%3Ae15e3a10-7774-11e7-89ee-5ef3fc9ae867%22+OR+%22uuid%3A420f4830-d6b2-11e7-8558-005056825209%22+OR+%22uuid%3Ae665ffa0-d6ad-11e7-8294-005056827e51%22+OR+%22uuid%3A88459bc0-cf86-11e7-a351-005056825209%22+OR+%22uuid%3A74bddf50-cf85-11e7-b059-5ef3fc9ae867%22+OR+%22uuid%3Ab7d82771-d69f-11e7-8294-005056827e51%22+OR+%22uuid%3A9f3f7bb0-d6b2-11e7-8558-005056825209%22+OR+%22uuid%3Af7217151-cf8a-11e7-80e7-5ef3fc9bb22f%22+OR+%22uuid%3A30bcc500-cf8f-11e7-80e7-5ef3fc9bb22f%22+OR+%22uuid%3Ae32eb050-d466-11e7-a047-005056825209%22+OR+%22uuid%3Ac39f12a0-d44a-11e7-a536-5ef3fc9ae867%22+OR+%22uuid%3Ab6632460-7641-11e4-9605-005056825209%22+OR+%22uuid%3A1e062920-d6be-11e7-9268-5ef3fc9ae867%22+OR+%22uuid%3Ab6c1a8f0-a730-11e2-b6da-005056827e52%22+OR+%22uuid%3A7c5e1e70-cc80-11e3-aec3-005056827e52%22+OR+%22uuid%3A8afd1150-1b3e-11e3-b62e-005056825209%22+OR+%22uuid%3A72356250-92c3-11e9-b601-005056825209%22+OR+%22uuid%3Aca058c00-929e-11e9-bc42-5ef3fc9ae867%22+OR+%22uuid%3A69eb8720-932f-11e9-b601-005056825209%22+OR+%22uuid%3A69eb8720-932f-11e9-b601-005056825209%22)&fl=PID+collection&wt=xml&rows=20";
        String secondResp  = IOUtils.toString(this.getClass().getResourceAsStream("k7notexists/1.xml"), "UTF-8");
        List<Object> mocksFromSecondCall = webCallExpect(client, secondReq, secondResp);

        String secondReqFetch = "http://kramerius4.mlp.cz/search/api/v5.0/search/?q=PID:(%22uuid%3Ae15e3a10-7774-11e7-89ee-5ef3fc9ae867%22+OR+%22uuid%3A420f4830-d6b2-11e7-8558-005056825209%22+OR+%22uuid%3Ae665ffa0-d6ad-11e7-8294-005056827e51%22+OR+%22uuid%3A88459bc0-cf86-11e7-a351-005056825209%22+OR+%22uuid%3A74bddf50-cf85-11e7-b059-5ef3fc9ae867%22+OR+%22uuid%3Ab7d82771-d69f-11e7-8294-005056827e51%22+OR+%22uuid%3A9f3f7bb0-d6b2-11e7-8558-005056825209%22+OR+%22uuid%3Af7217151-cf8a-11e7-80e7-5ef3fc9bb22f%22+OR+%22uuid%3A30bcc500-cf8f-11e7-80e7-5ef3fc9bb22f%22+OR+%22uuid%3Ae32eb050-d466-11e7-a047-005056825209%22+OR+%22uuid%3Ac39f12a0-d44a-11e7-a536-5ef3fc9ae867%22+OR+%22uuid%3Ab6632460-7641-11e4-9605-005056825209%22+OR+%22uuid%3A1e062920-d6be-11e7-9268-5ef3fc9ae867%22+OR+%22uuid%3Ab6c1a8f0-a730-11e2-b6da-005056827e52%22+OR+%22uuid%3A7c5e1e70-cc80-11e3-aec3-005056827e52%22+OR+%22uuid%3A8afd1150-1b3e-11e3-b62e-005056825209%22+OR+%22uuid%3A72356250-92c3-11e9-b601-005056825209%22+OR+%22uuid%3Aca058c00-929e-11e9-bc42-5ef3fc9ae867%22+OR+%22uuid%3A69eb8720-932f-11e9-b601-005056825209%22+OR+%22uuid%3A69eb8720-932f-11e9-b601-005056825209%22)&fl=PID+timestamp+fedora.model+document_type+handle+status+created_date+modified_date+parent_model+parent_pid+parent_pid+parent_title+root_model+root_pid+root_title+text_ocr+pages_count+datum_str+datum+rok+datum_begin+datum_end+datum_page+issn+mdt+ddt+dostupnost+keywords+geographic_names+collection+sec+model_path+pid_path+rels_ext_index+level+dc.title+title_sort+title_sort+dc.creator+dc.identifier+language+dc.description+details+facet_title+browse_title+browse_autor+img_full_mime+viewable+virtual+location+range+mods.shelfLocator+mods.physicalLocation+text+dnnt&wt=xml&rows=20";
        String secondRespFetch  = IOUtils.toString(this.getClass().getResourceAsStream("k7notexists/1_fetch.xml"), "UTF-8");
        List<Object> mocksFromSecondFetchCall = webCallExpect(client, secondReqFetch, secondRespFetch);
        // ----------

        // ---  3 indexed ---
        String thirdReq = "http://10.19.6.10:8983/solr/k7_5/select/?q=PID:(%22uuid%3A3b624880-96b5-11e2-9a08-005056827e52%22+OR+%22uuid%3A87d2a060-a692-11e2-8b87-005056827e51%22+OR+%22uuid%3A653cfcf0-3fac-11e7-b56f-005056827e52%22)&fl=PID+collection&wt=xml&rows=3";
        String thirdResp  = IOUtils.toString(this.getClass().getResourceAsStream("k7notexists/1.xml"), "UTF-8");
        List<Object> mocksFromThirdCall = webCallExpect(client, thirdReq, thirdResp);

        String thirdReqFetch = "http://kramerius4.mlp.cz/search/api/v5.0/search/?q=PID:(%22uuid%3A3b624880-96b5-11e2-9a08-005056827e52%22+OR+%22uuid%3A87d2a060-a692-11e2-8b87-005056827e51%22+OR+%22uuid%3A653cfcf0-3fac-11e7-b56f-005056827e52%22)&fl=PID+timestamp+fedora.model+document_type+handle+status+created_date+modified_date+parent_model+parent_pid+parent_pid+parent_title+root_model+root_pid+root_title+text_ocr+pages_count+datum_str+datum+rok+datum_begin+datum_end+datum_page+issn+mdt+ddt+dostupnost+keywords+geographic_names+collection+sec+model_path+pid_path+rels_ext_index+level+dc.title+title_sort+title_sort+dc.creator+dc.identifier+language+dc.description+details+facet_title+browse_title+browse_autor+img_full_mime+viewable+virtual+location+range+mods.shelfLocator+mods.physicalLocation+text+dnnt&wt=xml&rows=3";
        String thirdRespFetch  = IOUtils.toString(this.getClass().getResourceAsStream("k7notexists/2_fetch.xml"), "UTF-8");
        List<Object> mocksFromThirdFetchCall = webCallExpect(client, thirdReqFetch, thirdRespFetch);
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

        Capture secondArg = newCapture();
        EasyMock.expect(updateResponseBuilder.entity(capture(secondArg), EasyMock.eq(MediaType.TEXT_XML))).andReturn(updateResponseBuilder).once();

        Capture thirdArg = newCapture();
        EasyMock.expect(updateResponseBuilder.entity(capture(thirdArg), EasyMock.eq(MediaType.TEXT_XML))).andReturn(updateResponseBuilder).once();

        Capture fourthArg = newCapture();
        EasyMock.expect(updateResponseBuilder.entity(capture(fourthArg), EasyMock.eq(MediaType.TEXT_XML))).andReturn(updateResponseBuilder).once();

        Capture fifthArg = newCapture();
        EasyMock.expect(updateResponseBuilder.entity(capture(fifthArg), EasyMock.eq(MediaType.TEXT_XML))).andReturn(updateResponseBuilder).once();


        EasyMock.expect(updateResponseBuilder.post(ClientResponse.class)).andReturn(updateResponse).anyTimes();
        EasyMock.expect(updateResponse.getStatus()).andReturn(ClientResponse.Status.OK.getStatusCode()).anyTimes();
        EasyMock.expect(updateResponse.getEntityInputStream()).andDelegateTo(new MockClientResponse()).anyTimes();

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

        mocksFromThirdCall.stream().forEach(obj-> {
            EasyMock.replay(obj);
        });

        mocksFromThirdFetchCall.stream().forEach(obj-> {
            EasyMock.replay(obj);
        });

        process.setClient(client);

        // start whole process
        process.migrate(configurationFile);



        assertThat(Input.fromReader(new StringReader((String) firstArg.getValue())))
                .and(Input.fromStream(this.getClass().getResourceAsStream("k7notexists/k7batches/1_batch.xml")))
                .ignoreComments()
                .ignoreWhitespace()
                .areSimilar();

        assertThat(Input.fromReader(new StringReader((String) secondArg.getValue())))
                .and(Input.fromStream(this.getClass().getResourceAsStream("k7notexists/k7batches/2_batch.xml")))
                .ignoreComments()
                .ignoreWhitespace()
                .areSimilar();

    }

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


