package cz.incad.kramerius.utils;

import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class RelsExtHelperTest {

    @Test
    public void testDonatorGet() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        URL resource = RelsExtHelperTest.class.getResource("donator.xml");
        Assert.assertNotNull(resource);
        Document document = XMLUtils.parseDocument(resource.openStream(),true);
        String donator = RelsExtHelper.getDonator(document);
        Assert.assertNotNull(donator);
        Assert.assertEquals(donator,"donator:norway");
    }
}
