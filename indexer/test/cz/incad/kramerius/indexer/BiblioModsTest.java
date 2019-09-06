package cz.incad.kramerius.indexer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.activemq.util.ByteArrayInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import cz.incad.kramerius.utils.XMLUtils;
import junit.framework.Assert;
import junit.framework.TestCase;

public class BiblioModsTest extends TestCase {

    static class TestURIResolver implements URIResolver {
        @Override
        public Source resolve(String href, String base) throws TransformerException {
            InputStream transform = BiblioModsTest.class.getResourceAsStream(href);
            StreamSource ssource = new StreamSource(transform);
            return ssource;
        }
    }

    public void testTransform() throws ParserConfigurationException, SAXException, IOException, TransformerConfigurationException, TransformerException {
        
        InputStream is = BiblioModsTest.class.getResourceAsStream("res/mods.xml");
        Assert.assertNotNull(is);
        InputStream transform = BiblioModsTest.class.getResourceAsStream("res/_test_wrap.xslt");
        Assert.assertNotNull(transform);
        
        TransformerFactory tfactory = TransformerFactory.newInstance();
        tfactory.setURIResolver(new TestURIResolver());
        StreamSource docStream = new StreamSource(is);
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StreamResult sResult = new StreamResult(bos);
        
        tfactory.newTransformer(new StreamSource(transform)).transform(docStream, sResult);
        String parsingSource = "<?xml version=\"1.1\" encoding=\"UTF-8\"?><doc>" + new String(bos.toByteArray())+"</doc>";
        Document doc = XMLUtils.parseDocument(new StringReader(parsingSource), false);
        List<Element> elements = XMLUtils.getElements(doc.getDocumentElement());
        Assert.assertFalse(elements.stream().filter((element) -> {return element.getAttribute("name").equals("language"); }).collect(Collectors.toList()).isEmpty());

        List<String> sLocators = elements.stream().filter((element) -> {return element.getAttribute("name").equals("mods.shelfLocator"); }).map((element)->{return element.getTextContent();}).collect(Collectors.toList());
        Assert.assertTrue(sLocators.size() == 1);
        Assert.assertTrue(sLocators.get(0).equals("2-0918.388"));

        List<String> pLocators = elements.stream().filter((element) -> {return element.getAttribute("name").equals("mods.physicalLocation"); }).map((element)->{return element.getTextContent();}).collect(Collectors.toList());
        Assert.assertTrue(pLocators.size() == 1);
        Assert.assertTrue(pLocators.get(0).equals("BOA001"));
    }

}
