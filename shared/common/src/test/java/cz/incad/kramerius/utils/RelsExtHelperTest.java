package cz.incad.kramerius.utils;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import cz.incad.kramerius.utils.pid.LexerException;

public class RelsExtHelperTest {

    /* TODO AK_NEW
    @Test
    public void testDonatorGet() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        URL resource = RelsExtHelperTest.class.getResource("donator.xml");
        Assert.assertNotNull(resource);
        Document document = XMLUtils.parseDocument(resource.openStream(),true);
        String donator = RelsExtUtils.getDonator(document);
        Assert.assertNotNull(donator);
        Assert.assertEquals(donator,"donator:norway");
    }

     */
    

    /* TODO AK_NEW
    @Test
    public void testGetTilesUrl() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        URL resource = RelsExtHelperTest.class.getResource("donator.xml");
        Assert.assertNotNull(resource);
        Document document = XMLUtils.parseDocument(resource.openStream(),true);
        String tilesUrl = RelsExtUtils.getRelsExtTilesUrl(document);
        Assert.assertNotNull(tilesUrl);
        Assert.assertEquals(tilesUrl,"kramerius4://deepZoomCache");
    }

    @Test
    public void testModel() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, LexerException, org.ceskaexpedice.akubra.utils.pid.LexerException {
        URL resource = RelsExtHelperTest.class.getResource("donator.xml");
        Assert.assertNotNull(resource);
        Document document = XMLUtils.parseDocument(resource.openStream(),true);
        String model = RelsExtUtils.getModel(document);
        Assert.assertNotNull(model);
        Assert.assertEquals(model,"monograph");
    }

     */

    
    /* TODO AK_NEW
    @Test
    public void testRelations() throws ParserConfigurationException, SAXException, IOException {
        URL resource = RelsExtHelperTest.class.getResource("donator.xml");
        Assert.assertNotNull(resource);
        Document document = XMLUtils.parseDocument(resource.openStream(),true);
        List<Pair<String,String>> relations = RelsExtUtils.getRelations(document);
        Assert.assertTrue(relations.size() == 16);
        relations.stream().forEach(rel-> {
            Assert.assertTrue(rel.getLeft().equals("hasPage"));
            Assert.assertTrue(rel.getRight().startsWith("uuid:"));
        });
    }

     */
    
}
