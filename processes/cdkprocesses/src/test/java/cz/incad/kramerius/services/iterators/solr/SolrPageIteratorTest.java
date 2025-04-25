package cz.incad.kramerius.services.iterators.solr;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.iterators.IterationItem;
import cz.incad.kramerius.services.iterators.ProcessIterator;
import cz.incad.kramerius.services.iterators.ProcessIteratorFactory;
import cz.incad.kramerius.utils.XMLUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.*;
import static cz.incad.kramerius.services.utils.SolrMockWebCall.*;


public class SolrPageIteratorTest  {

    @Test
    public void testPageIterator() throws IOException, SAXException, ParserConfigurationException, URISyntaxException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        String xmlConfig = "<cdkprocess>\n" +
                "    <!-- Iteration part -->\n" +
                "    <iteratorFactory class=\"cz.incad.kramerius.services.iterators.solr.SolrIteratorFactory\"></iteratorFactory>\n" +
                "    <iteration>\n" +
                "        <url>http://solr_test:8983/test</url>\n" +
                "        <endpoint>select</endpoint>\n" +
                "\n" +
                "        <id>PID</id>\n" +
                "        <rows>1000</rows>\n" +
                "        <type>PAGINATION</type>\n" +
                "    </iteration></cdkprocess>";

        Document document = XMLUtils.parseDocument(new StringReader(xmlConfig));
        Client client = createMock(Client.class);

        String firstReq = "http://solr_test:8983/test/select?q=*:*&start=0&rows=1000&fl=PID&wt=xml&sort=PID+ASC";
        String firstResp  = IOUtils.toString(this.getClass().getResourceAsStream("pagination_first_page.xml"), "UTF-8");
        List<Object> mockFromFirstCall = webCallExpectXML(client, firstReq, firstResp);

        String secondReq = "http://solr_test:8983/test/select?q=*:*&start=1000&rows=1000&fl=PID&wt=xml&sort=PID+ASC";
        String secondResp  = IOUtils.toString(this.getClass().getResourceAsStream("pagination_second_page.xml"), "UTF-8");
        List<Object> mockFromSecondCall = webCallExpectXML(client, secondReq, secondResp);

        String thirdReq = "http://solr_test:8983/test/select?q=*:*&start=2000&rows=1000&fl=PID&wt=xml&sort=PID+ASC";
        String thirdResp  = IOUtils.toString(this.getClass().getResourceAsStream("pagination_third_page.xml"), "UTF-8");
        List<Object> mockFromThirdCall = webCallExpectXML(client, thirdReq, thirdResp);

        replay(client);
        mockFromFirstCall.stream().forEach(m-> replay(m));
        mockFromSecondCall.stream().forEach(m-> replay(m));
        mockFromThirdCall.stream().forEach(m-> replay(m));

        Element iteratorFactory = XMLUtils.findElement(document.getDocumentElement(),"iteratorFactory");
        String iteratorClass = iteratorFactory.getAttribute("class");
        ProcessIteratorFactory processIteratorFactory = ProcessIteratorFactory.create(iteratorClass);

        // Iterator instance
        Element iterationElm = XMLUtils.findElement(document.getDocumentElement(), "iteration");
        ProcessIterator processIterator = processIteratorFactory.createProcessIterator(null,iterationElm, client);

        Assert.assertTrue(processIterator != null);
        Assert.assertTrue(processIterator instanceof  SolrPageIterator);

        List<IterationItem> iteratedPids = new ArrayList<>();
        processIterator.iterate(client, (List<IterationItem> idents)->{
            iteratedPids.addAll(idents);
        }, ()-> {
        });

        Assert.assertTrue(iteratedPids.size() == 2000);
    }

    @Test
    public void testPageWithFilterIterator() throws IOException, SAXException, ParserConfigurationException, URISyntaxException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        String xmlConfig = "<cdkprocess>\n" +
                "    <!-- Iteration part -->\n" +
                "    <iteratorFactory class=\"cz.incad.kramerius.services.iterators.solr.SolrIteratorFactory\"></iteratorFactory>\n" +
                "    <iteration>\n" +
                "        <url>http://solr_test:8983/test</url>\n" +
                "   <fquery>modified_date:[2016-05-03T00:16:19.440Z TO 2017-05-03T04:16:19.440Z]</fquery>\n"+
                "        <endpoint>select</endpoint>\n" +
                "\n" +
                "        <id>PID</id>\n" +
                "        <rows>1000</rows>\n" +
                "        <type>PAGINATION</type>\n" +
                "    </iteration></cdkprocess>";

        Document document = XMLUtils.parseDocument(new StringReader(xmlConfig));
        Client client = createMock(Client.class);

                         //http://solr_test:8983/test/select?q=*:*&start=0&rows=1000&fq=modified_date%3A%5B2016-05-03T00%3A16%3A19.440Z+TO+2017-05-03T04%3A16%3A19.440Z%5D&fl=PID&wt=xml&sort=PID+ASC
        String firstReq = "http://solr_test:8983/test/select?q=*:*&start=0&rows=1000&fq=modified_date%3A%5B2016-05-03T00%3A16%3A19.440Z+TO+2017-05-03T04%3A16%3A19.440Z%5D&fl=PID&wt=xml&sort=PID+ASC";
        String firstResp  = IOUtils.toString(this.getClass().getResourceAsStream("pagination_first_page.xml"), "UTF-8");
        List<Object> mockFromFirstCall = webCallExpectXML(client, firstReq, firstResp);

        String secondReq = "http://solr_test:8983/test/select?q=*:*&start=1000&rows=1000&fq=modified_date%3A%5B2016-05-03T00%3A16%3A19.440Z+TO+2017-05-03T04%3A16%3A19.440Z%5D&fl=PID&wt=xml&sort=PID+ASC";
        String secondResp  = IOUtils.toString(this.getClass().getResourceAsStream("pagination_second_page.xml"), "UTF-8");
        List<Object> mockFromSecondCall = webCallExpectXML(client, secondReq, secondResp);

        String thirdReq = "http://solr_test:8983/test/select?q=*:*&start=2000&rows=1000&fq=modified_date%3A%5B2016-05-03T00%3A16%3A19.440Z+TO+2017-05-03T04%3A16%3A19.440Z%5D&fl=PID&wt=xml&sort=PID+ASC";
        String thirdResp  = IOUtils.toString(this.getClass().getResourceAsStream("pagination_third_page.xml"), "UTF-8");
        List<Object> mockFromThirdCall = webCallExpectXML(client, thirdReq, thirdResp);

        replay(client);
        mockFromFirstCall.stream().forEach(m-> replay(m));
        mockFromSecondCall.stream().forEach(m-> replay(m));
        mockFromThirdCall.stream().forEach(m-> replay(m));

        Element iteratorFactory = XMLUtils.findElement(document.getDocumentElement(),"iteratorFactory");
        String iteratorClass = iteratorFactory.getAttribute("class");
        ProcessIteratorFactory processIteratorFactory = ProcessIteratorFactory.create(iteratorClass);

        // Iterator instance
        Element iterationElm = XMLUtils.findElement(document.getDocumentElement(), "iteration");
        ProcessIterator processIterator = processIteratorFactory.createProcessIterator(null,iterationElm, client);

        Assert.assertTrue(processIterator != null);
        Assert.assertTrue(processIterator instanceof  SolrPageIterator);

        List<IterationItem> iteratedPids = new ArrayList<>();
        processIterator.iterate(client, (List<IterationItem> idents)->{
            iteratedPids.addAll(idents);
        }, ()-> {
        });

        Assert.assertTrue(iteratedPids.size() == 2000);
    }


    @Test
    public void testPageIteratorCompositeId() throws IOException, SAXException, ParserConfigurationException, URISyntaxException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        String xmlConfig = "<cdkprocess>\n" +
                "    <!-- Iteration part -->\n" +
                "    <iteratorFactory class=\"cz.incad.kramerius.services.iterators.solr.SolrIteratorFactory\"></iteratorFactory>\n" +
                "    <iteration>\n" +
                "        <url>http://solr_test:8983/test</url>\n" +
                "        <endpoint>select</endpoint>\n" +
                "\n" +
                "        <id>compositeId</id>\n" +
                "        <rows>1000</rows>\n" +
                "        <type>PAGINATION</type>\n" +
                "    </iteration></cdkprocess>";

        Document document = XMLUtils.parseDocument(new StringReader(xmlConfig));
        Client client = createMock(Client.class);

        String firstReq = "http://solr_test:8983/test/select?q=*:*&start=0&rows=1000&fl=compositeId&wt=xml&sort=compositeId+ASC";
        String firstResp  = IOUtils.toString(this.getClass().getResourceAsStream("pagination_compositeid_first_page.xml"), "UTF-8");
        List<Object> mockFromFirstCall = webCallExpectXML(client, firstReq, firstResp);

        String secondReq = "http://solr_test:8983/test/select?q=*:*&start=1000&rows=1000&fl=compositeId&wt=xml&sort=compositeId+ASC";
        String secondResp  = IOUtils.toString(this.getClass().getResourceAsStream("pagination_third_page.xml"), "UTF-8");
        List<Object> mockFromSecondCall = webCallExpectXML(client, secondReq, secondResp);


        replay(client);
        mockFromFirstCall.stream().forEach(m-> replay(m));
        mockFromSecondCall.stream().forEach(m-> replay(m));

        Element iteratorFactory = XMLUtils.findElement(document.getDocumentElement(),"iteratorFactory");
        String iteratorClass = iteratorFactory.getAttribute("class");
        ProcessIteratorFactory processIteratorFactory = ProcessIteratorFactory.create(iteratorClass);

        // Iterator instance
        Element iterationElm = XMLUtils.findElement(document.getDocumentElement(), "iteration");
        ProcessIterator processIterator = processIteratorFactory.createProcessIterator(null,iterationElm, client);

        Assert.assertTrue(processIterator != null);
        Assert.assertTrue(processIterator instanceof  SolrPageIterator);

        List<IterationItem> iterateItems = new ArrayList<>();
        processIterator.iterate(client, (List<IterationItem> idents)->{
            iterateItems.addAll(idents);
        }, ()-> {
        });

        Assert.assertTrue(iterateItems.size() == 20);
        iterateItems.stream().forEach(i-> {

            Assert.assertTrue(i.compositeIdUsed());
            Assert.assertTrue(i.getId().contains("!"));
            Assert.assertFalse(i.getPid().contains("!"));
        });
    }

    @Test
    public void testPageIteratorCompositeIdWithFl() throws IOException, SAXException, ParserConfigurationException, URISyntaxException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        String xmlConfig = "<cdkprocess>\n" +
                "    <!-- Iteration part -->\n" +
                "    <iteratorFactory class=\"cz.incad.kramerius.services.iterators.solr.SolrIteratorFactory\"></iteratorFactory>\n" +
                "    <iteration>\n" +
                "        <url>http://solr_test:8983/test</url>\n" +
                "        <endpoint>select</endpoint>\n" +
                "\n" +
                "        <id>compositeId</id>\n" +
                "        <rows>1000</rows>\n" +
                "        <type>PAGINATION</type>\n" +
                "        <fieldlist>model</fieldlist>\n"+
                "    </iteration></cdkprocess>";

        Document document = XMLUtils.parseDocument(new StringReader(xmlConfig));
        Client client = createMock(Client.class);

        String firstReq = "http://solr_test:8983/test/select?q=*:*&start=0&rows=1000&fl=compositeId,model&wt=xml&sort=compositeId+ASC";
        String firstResp  = IOUtils.toString(this.getClass().getResourceAsStream("pagination_compositeid_model_first_page.xml"), "UTF-8");
        List<Object> mockFromFirstCall = webCallExpectXML(client, firstReq, firstResp);

        String secondReq = "http://solr_test:8983/test/select?q=*:*&start=1000&rows=1000&fl=compositeId,model&wt=xml&sort=compositeId+ASC";
        String secondResp  = IOUtils.toString(this.getClass().getResourceAsStream("pagination_third_page.xml"), "UTF-8");
        List<Object> mockFromSecondCall = webCallExpectXML(client, secondReq, secondResp);


        replay(client);
        mockFromFirstCall.stream().forEach(m-> replay(m));
        mockFromSecondCall.stream().forEach(m-> replay(m));

        Element iteratorFactory = XMLUtils.findElement(document.getDocumentElement(),"iteratorFactory");
        String iteratorClass = iteratorFactory.getAttribute("class");
        ProcessIteratorFactory processIteratorFactory = ProcessIteratorFactory.create(iteratorClass);

        // Iterator instance
        Element iterationElm = XMLUtils.findElement(document.getDocumentElement(), "iteration");
        ProcessIterator processIterator = processIteratorFactory.createProcessIterator(null,iterationElm, client);

        Assert.assertTrue(processIterator != null);
        Assert.assertTrue(processIterator instanceof  SolrPageIterator);

        List<IterationItem> iterateItems = new ArrayList<>();
        processIterator.iterate(client, (List<IterationItem> idents)->{
            iterateItems.addAll(idents);
        }, ()-> {
        });

        Assert.assertTrue(iterateItems.size() == 20);
        iterateItems.stream().forEach(i-> {

            Assert.assertTrue(i.compositeIdUsed());
            Assert.assertTrue(i.getId().contains("!"));
            Assert.assertFalse(i.getPid().contains("!"));
            Assert.assertNotNull(i.getDoc());
        });
    }

}
