package cz.incad.kramerius.rest.api.k5.client.item.decorators;

import cz.incad.kramerius.utils.XMLUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class DonatorDecorateTest {

    @Test
    public void testDonatorGet() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        URL resource = DonatorDecorateTest.class.getResource("donator.xml");
        Assert.assertNotNull(resource);
        Document document = XMLUtils.parseDocument(resource.openStream(),true);
        List<Element> donators = DonatorDecorate.findDonators(document);
        Assert.assertEquals(donators.size(),1);
        Assert.assertEquals(DonatorDecorate.getDonatorString(donators.get(0)),"donator:norway");
    }

    public void testDonatorGet2() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        URL resource = DonatorDecorateTest.class.getResource("donator2.xml");
        Assert.assertNotNull(resource);
        Document document = XMLUtils.parseDocument(resource.openStream(),true);
        List<Element> donators = DonatorDecorate.findDonators(document);
        Assert.assertEquals(donators.size(),2);
        Assert.assertEquals(DonatorDecorate.getDonatorString(donators.get(0)),"donator:norway");
        Assert.assertEquals(DonatorDecorate.getDonatorString(donators.get(1)),"donator:k3tok5");
    }
}
