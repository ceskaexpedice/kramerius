package cz.incad.kramerius.services.iterators.solr;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.iterators.IterationItem;
import cz.incad.kramerius.services.iterators.ProcessIterator;
import cz.incad.kramerius.services.iterators.ProcessIteratorFactory;
import cz.incad.kramerius.utils.XMLUtils;
import org.apache.commons.io.IOUtils;
import org.easymock.EasyMock;
import org.json.JSONObject;
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
import java.util.stream.Collectors;

import static org.easymock.EasyMock.*;
import static cz.incad.kramerius.services.utils.SolrMockWebCall.*;

public class SolrICursorteratorTests {

    @Test
    public void testCursorIteration() throws IOException, SAXException, ParserConfigurationException, IllegalAccessException, InstantiationException, ClassNotFoundException, URISyntaxException {
        String xmlConfig = "<cdkprocess>\n" +
                "    <!-- Iteration part -->\n" +
                "    <iteratorFactory class=\"cz.incad.kramerius.services.iterators.solr.SolrIteratorFactory\"></iteratorFactory>\n" +
                "    <iteration>\n" +
                "        <url>http://solr_test:8983/test</url>\n" +
                "        <endpoint>select</endpoint>\n" +
                "\n" +
                "        <id>PID</id>\n" +
                "        <rows>1000</rows>\n" +
                "        <type>CURSOR</type>\n" +
                "    </iteration></cdkprocess>";

        Document document = XMLUtils.parseDocument(new StringReader(xmlConfig));
        Client client = createMock(Client.class);

        String firstReq = "http://solr_test:8983/test/select?q=*:*&rows=1000&cursorMark=*&sort=PID+ASC&fl=PID&wt=xml";
        String firstResp  = IOUtils.toString(this.getClass().getResourceAsStream("cursor_first_page.xml"), "UTF-8");
        List<Object> mockFromFirstCall = webCallExpect(client, firstReq, firstResp);

        String secondReq = "http://solr_test:8983/test/select?q=*:*&rows=1000&cursorMark=AoE/CnV1aWQ6MDAwNmU0Y2MtMzEwNS0xMWU5LTg4NDctMDA1MDU2YTJiMDUx&sort=PID+ASC&fl=PID&wt=xml";
        String secondResp  = IOUtils.toString(this.getClass().getResourceAsStream("cursor_second_page.xml"), "UTF-8");
        List<Object> mockFromSecondCall = webCallExpect(client, secondReq, secondResp);

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
        Assert.assertTrue(processIterator instanceof  SolrCursorIterator);

        List<String> iteratedPids = new ArrayList<>();
        processIterator.iterate(client, (List<IterationItem> idents)->{
            iteratedPids.addAll(idents.stream().map(IterationItem::getPid).collect(Collectors.toList()));
        }, ()-> {
        });

        Assert.assertTrue(iteratedPids.size() == 2000);
    }

}
