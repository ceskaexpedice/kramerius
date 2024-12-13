package cz.incad.kramerius.services.iterators.logfile;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.iterators.IterationItem;
import cz.incad.kramerius.services.iterators.ProcessIterator;
import cz.incad.kramerius.services.iterators.ProcessIteratorFactory;
import cz.incad.kramerius.services.iterators.solr.SolrCursorIterator;
import cz.incad.kramerius.utils.XMLUtils;
import org.fcrepo.utilities.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.easymock.EasyMock.createMock;

public class LogfileIteratorTests {

    @Test
    public void testLogIterators() throws IOException, ParserConfigurationException, SAXException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        String _xmlConfig = "<cdkprocess>\n" +
                "    <!-- Iteration part -->\n" +
                "    <iteratorFactory class=\"cz.incad.kramerius.services.iterators.logfile.LogFileIteratorFactory\"></iteratorFactory>\n" +
                "    <iteration>\n" +
                "        <url>%s</url>\n" +
                "        <endpoint>select</endpoint>\n" +
                "    </iteration></cdkprocess>";


        Client client = createMock(Client.class);

        File tmpLogFile = File.createTempFile("junit-log", "file");
        InputStream logStream = this.getClass().getResourceAsStream("file_log");
        FileUtils.copy(logStream, new FileOutputStream(tmpLogFile));
        String xmlConfig = String.format(_xmlConfig, tmpLogFile.toURI().toURL().toString());

        Document document = XMLUtils.parseDocument(new StringReader(xmlConfig));

        Element iteratorFactory = XMLUtils.findElement(document.getDocumentElement(),"iteratorFactory");
        String iteratorClass = iteratorFactory.getAttribute("class");
        ProcessIteratorFactory processIteratorFactory = ProcessIteratorFactory.create(iteratorClass);

        // Iterator instance
        Element iterationElm = XMLUtils.findElement(document.getDocumentElement(), "iteration");
        ProcessIterator processIterator = processIteratorFactory.createProcessIterator(null,iterationElm, client);

        Assert.assertTrue(processIterator != null);
        Assert.assertTrue(processIterator instanceof LogFileIterator);

        List<String> iteratedPids = new ArrayList<>();
        processIterator.iterate(client, (List<IterationItem> idents)->{
            iteratedPids.addAll(idents.stream().map(IterationItem::getPid).collect(Collectors.toList()));
        }, ()-> { });

        Assert.assertTrue(iteratedPids.size() == 2000);
    }
}
