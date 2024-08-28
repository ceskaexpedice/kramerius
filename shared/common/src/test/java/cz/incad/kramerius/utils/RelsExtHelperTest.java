package cz.incad.kramerius.utils;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import cz.incad.kramerius.utils.pid.LexerException;

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
    

    @Test
    public void testGetTilesUrl() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        URL resource = RelsExtHelperTest.class.getResource("donator.xml");
        Assert.assertNotNull(resource);
        Document document = XMLUtils.parseDocument(resource.openStream(),true);
        String tilesUrl = RelsExtHelper.getRelsExtTilesUrl(document);
        Assert.assertNotNull(tilesUrl);
        Assert.assertEquals(tilesUrl,"kramerius4://deepZoomCache");
    }

    @Test
    public void testModel() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, LexerException {
        URL resource = RelsExtHelperTest.class.getResource("donator.xml");
        Assert.assertNotNull(resource);
        Document document = XMLUtils.parseDocument(resource.openStream(),true);
        String model = RelsExtHelper.getModel(document.getDocumentElement());
        Assert.assertNotNull(model);
        Assert.assertEquals(model,"monograph");
    }

    
    @Test
    public void testRelations() throws ParserConfigurationException, SAXException, IOException {
        URL resource = RelsExtHelperTest.class.getResource("donator.xml");
        Assert.assertNotNull(resource);
        Document document = XMLUtils.parseDocument(resource.openStream(),true);
        List<Pair<String,String>> relations = RelsExtHelper.getRelations(document.getDocumentElement());
        Assert.assertTrue(relations.size() == 16);
        relations.stream().forEach(rel-> {
            Assert.assertTrue(rel.getLeft().equals("hasPage"));
            Assert.assertTrue(rel.getRight().startsWith("uuid:"));
        });
    }
    
}
