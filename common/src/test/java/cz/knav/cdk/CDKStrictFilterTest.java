package cz.knav.cdk;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import cz.incad.kramerius.utils.XMLUtils;

public class CDKStrictFilterTest {

    @Test
    public void testFilter() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        URL resource = CDKStrictFilterTest.class.getResource("rels-ext.xml");
        InputStream is = resource.openStream();
        Assert.assertNotNull(is);
        
        Document document = XMLUtils.parseDocument(is, true);
        XPathFactory factory = XPathFactory.newInstance();
        String testURL = CDKStrictFilter.disectURL(factory, document);
        Assert.assertEquals("http://cdk-test.lib.cas.cz/search/img?uuid=uuid:e15bdb43-41d4-48c7-bb64-7d3d1e2f769c&stream=TEXT_OCR&action=GETRAW", testURL);
    }
}
