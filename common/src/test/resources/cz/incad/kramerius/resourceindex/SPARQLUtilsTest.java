package cz.incad.kramerius.resourceindex;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import cz.incad.kramerius.utils.XMLUtils;
import junit.framework.Assert;
import junit.framework.TestCase;

public class SPARQLUtilsTest extends TestCase {

    public void testSparql() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        URL resource = SPARQLUtilsTest.class.getResource("res/resource.xml");
        Document document = XMLUtils.parseDocument(resource.openStream(),true);
        List<String> sparqlResults = SPARQLUtils.sparqlResults(document);
        Assert.assertTrue(sparqlResults.size() == 100);
    }
}
