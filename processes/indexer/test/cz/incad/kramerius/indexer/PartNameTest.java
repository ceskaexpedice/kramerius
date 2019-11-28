package cz.incad.kramerius.indexer;

import cz.incad.kramerius.utils.XMLUtils;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

public class PartNameTest extends TestCase {

    public void testPartName() throws TransformerException, IOException, SAXException, ParserConfigurationException {

        InputStream transform = BiblioModsTest.class.getResourceAsStream("res/_periodical_volume.xslt");
        InputStream is = BiblioModsTest.class.getResourceAsStream("res/volumePartName.xml");
        TransformerFactory tfactory = TransformerFactory.newInstance();
        tfactory.setURIResolver(new BiblioModsTest.TestURIResolver());
        StreamSource docStream = new StreamSource(is);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StreamResult sResult = new StreamResult(bos);
        tfactory.newTransformer(new StreamSource(transform)).transform(docStream, sResult);
        String fields = "<fields>"+new String(bos.toByteArray())+"</fields>";
        Document document = XMLUtils.parseDocument(new StringReader(fields), true);
        NodeList childNodes = document.getDocumentElement().getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element elm = (Element) node;
                String textContent = elm.getTextContent();
                Assert.assertTrue("2015##Mimořádné číslo 3##Sedmkrát z logiky a metodologie vědy".equals(textContent));
            }
        }

    }
}
