package cz.inovatika.kramerius.services.iterators.config;

import cz.incad.kramerius.utils.XMLUtils;
import cz.inovatika.kramerius.services.iterators.ProcessIterator;
import cz.inovatika.kramerius.services.iterators.ProcessIteratorFactory;
import cz.inovatika.kramerius.services.iterators.solr.AbstractSolrIterator;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

public class ProcessIteratorFactoryTest {

    @Test
    public void testFactory() throws ParserConfigurationException, IOException, SAXException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        InputStream resourceAsStream = SolrConfigurationTest.class.getResourceAsStream("config.xml");
        Document document = XMLUtils.parseDocument(resourceAsStream);

        SolrIteratorConfig config = SolrConfigParser.parse(XMLUtils.findElement(document.getDocumentElement(), "migration"), "fq");

        ProcessIteratorFactory processIteratorFactory = ProcessIteratorFactory.create(config);
        ProcessIterator processIterator = processIteratorFactory.createProcessIterator(config, null);
        Assert.assertTrue(processIterator instanceof AbstractSolrIterator);

        AbstractSolrIterator abstractSolrIterator = (AbstractSolrIterator) processIterator;
        Assert.assertEquals(abstractSolrIterator.getMasterQuery(), "*:*");
        Assert.assertEquals(abstractSolrIterator.getEndpoint(), "select");
        Assert.assertEquals(abstractSolrIterator.getAddress(), "$iteration.url$");
    }
}
