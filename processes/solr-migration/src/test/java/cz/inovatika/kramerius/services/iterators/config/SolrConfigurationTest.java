package cz.inovatika.kramerius.services.iterators.config;

import cz.incad.kramerius.utils.XMLUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

public class SolrConfigurationTest {

    @Test
    public void testSolrConfiguration() throws ParserConfigurationException, IOException, SAXException {
        InputStream resourceAsStream = SolrConfigurationTest.class.getResourceAsStream("config.xml");
        Document document = XMLUtils.parseDocument(resourceAsStream);

        SolrIteratorConfig build = SolrConfigParser.parse(XMLUtils.findElement(document.getDocumentElement(), "migration"), "fq");

        Assert.assertTrue(build.getFactoryClz() !=null);
        Assert.assertEquals(build.getFactoryClz(), "cz.inovatika.kramerius.services.iterators.factories.SolrIteratorFactory");

        Assert.assertTrue(build.getEndpoint() != null);
        Assert.assertEquals(build.getEndpoint(), "select");

        Assert.assertTrue(build.getUrl() != null);
        Assert.assertEquals(build.getUrl(), "$iteration.url$");

        Assert.assertTrue(build.getIdField() != null);
        Assert.assertEquals(build.getIdField(), "PID");

        Assert.assertTrue(build.getTypeOfIteration() != null);
        Assert.assertEquals(build.getTypeOfIteration(), TypeOfIteration.CURSOR);

    }
}
