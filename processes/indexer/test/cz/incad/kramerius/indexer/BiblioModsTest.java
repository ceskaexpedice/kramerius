package cz.incad.kramerius.indexer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
import org.xml.sax.SAXException;

import cz.incad.kramerius.utils.XMLUtils;
import junit.framework.Assert;
import junit.framework.TestCase;

public class BiblioModsTest extends TestCase {

    private class TestURIResolver implements URIResolver {
        @Override
        public Source resolve(String href, String base) throws TransformerException {
            InputStream transform = BiblioModsTest.class.getResourceAsStream(href);
            StreamSource ssource = new StreamSource(transform);
            return ssource;
        }
    }

    public void testTransform() throws ParserConfigurationException, SAXException, IOException, TransformerConfigurationException, TransformerException {
        
        InputStream is = BiblioModsTest.class.getResourceAsStream("mods.xml");
        Assert.assertNotNull(is);
        InputStream transform = BiblioModsTest.class.getResourceAsStream("res/_test_wrap.xslt");
        Assert.assertNotNull(transform);
        
        TransformerFactory tfactory = TransformerFactory.newInstance();
        tfactory.setURIResolver(new TestURIResolver());
        StreamSource docStream = new StreamSource(is);
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StreamResult sResult = new StreamResult(bos);
        
        tfactory.newTransformer(new StreamSource(transform)).transform(docStream, sResult);
        Assert.assertEquals("<field name=\"mods.shelfLocator\">54 G 000887</field><field name=\"mods.physicalLocation\">ABA000</field>", new String(bos.toByteArray()).trim());
    }
}
