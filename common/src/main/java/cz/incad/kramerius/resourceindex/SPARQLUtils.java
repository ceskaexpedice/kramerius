package cz.incad.kramerius.resourceindex;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cz.incad.kramerius.FedoraNamespaceContext;

/**
 * Sparql utility class
 * @author pstastny
 */
public class SPARQLUtils {
    
    /**
     * Returns results from given doc
     * @param doc SPARQL results document
     * @return
     * @throws XPathExpressionException
     */
    public static List<String> sparqlResults(Document doc) throws XPathExpressionException {
        List<String> retvals = new ArrayList<String>();
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();

        xpath.setNamespaceContext(new FedoraNamespaceContext());
        String xPathStr = "/sparql:sparql/sparql:results/sparql:result/sparql:object";
        XPathExpression expr = xpath.compile(xPathStr);
        NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); i++) {
            Node childnode = nodes.item(i);
            String pid = childnode.getAttributes().getNamedItem("uri").getNodeValue();
            retvals.add(pid.replaceAll("info:fedora/", ""));
        }
        return retvals;
    }
}
