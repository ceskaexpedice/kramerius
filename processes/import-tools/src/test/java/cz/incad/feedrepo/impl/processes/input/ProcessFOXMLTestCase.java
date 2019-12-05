package cz.incad.feedrepo.impl.processes.input;

import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.utils.XMLUtils;
import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by pstastny on 10/8/2017.
 */
public class ProcessFOXMLTestCase extends TestCase {

    public void testFoxml() throws IOException, SAXException, ParserConfigurationException {
        InputStream is = ProcessFOXMLTestCase.class.getClassLoader().getResourceAsStream("fo1.xml");
        Document document = XMLUtils.parseDocument(is, true);
        System.out.println(is);
        System.out.println(document);
        Element elm = XMLUtils.findElement(document.getDocumentElement(), "objectProperties", FedoraNamespaces.FEDORA_FOXML_URI);

        NodeList childNodes = elm.getChildNodes();
        for (int i = 0,ll=childNodes.getLength(); i <ll ; i++) {
            Node n = childNodes.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element propElm = (Element) n;
                String name = propElm.getAttribute("NAME");
                String value = propElm.getAttribute("VALUE");
                System.out.println(name);
            }
        }
    }
}

