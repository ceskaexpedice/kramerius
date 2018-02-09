package cz.incad.kramerius.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import cz.incad.kramerius.utils.ALTOUtils;
import cz.incad.kramerius.utils.ALTOUtils.AltoDisected;
import cz.incad.kramerius.utils.XMLUtils;
import junit.framework.Assert;
import junit.framework.TestCase;

public class AltoSupportServletTest extends TestCase {

    public void testFindTerms() throws IOException, ParserConfigurationException, SAXException, TransformerException {
        URL url = AltoSupportServletTest.class.getResource("solr.xml");
        InputStream openStream = url.openStream();
        Document document = XMLUtils.parseDocument(openStream);
        Set<String> terms = AltoSupportServlet.findHighlightTerm(document.getDocumentElement(),"uuid:5c243d40-3425-11e3-bd38-5ef3fc9ae867");
        Assert.assertTrue(terms.size() == 1);
        Assert.assertTrue(terms.iterator().next().equals("PROSA"));
    }

    public void testFindAltos() throws ParserConfigurationException, SAXException, IOException {
        URL solr = AltoSupportServletTest.class.getResource("solr.xml");
        URL alto = AltoSupportServletTest.class.getResource("alto.xml");
        InputStream openStream = solr.openStream();
        Document solrDocument = XMLUtils.parseDocument(openStream);

        openStream = alto.openStream();
        Document altoDocument = XMLUtils.parseDocument(openStream);

        Set<String> terms = AltoSupportServlet.findHighlightTerm(solrDocument.getDocumentElement(),"uuid:5c243d40-3425-11e3-bd38-5ef3fc9ae867");
        for (String sterm : terms) {
            AltoDisected disected = ALTOUtils.disectAlto(sterm, altoDocument);
            Assert.assertNotNull(disected);
            Assert.assertNotNull(disected.getBoxes().size() > 0);
        }
    }

    public void testZeroAltos() throws ParserConfigurationException, SAXException, IOException {
        URL solr = AltoSupportServletTest.class.getResource("solr.xml");
        URL alto = AltoSupportServletTest.class.getResource("alto.xml");
        InputStream openStream = solr.openStream();

        openStream = alto.openStream();
        Document altoDocument = XMLUtils.parseDocument(openStream);

        AltoDisected disected = ALTOUtils.disectAlto(null, altoDocument);
        Assert.assertTrue(disected.getBoxes().size() == 0);

        disected = ALTOUtils.disectAlto("", altoDocument);
        Assert.assertTrue(disected.getBoxes().size() == 0);
        
    }

}
