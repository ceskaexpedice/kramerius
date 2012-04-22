package cz.incad.kramerius.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XMLUtils {

    public static Document parseDocument(Reader reader) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        return builder.parse(new InputSource(reader));
    }

    public static Document parseDocument(Reader reader, boolean namespaceaware) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(namespaceaware);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(reader));
    }

    public static Document parseDocument(InputStream is) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        return builder.parse(is);
    }

    public static Document parseDocument(InputStream is, boolean namespaceaware) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(namespaceaware);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(is);
    }

    public static List<Element> getElements(Element topElm) {
        List<Element> retVals = new ArrayList<Element>();
        NodeList childNodes = topElm.getChildNodes();
        for (int i = 0, ll = childNodes.getLength(); i < ll; i++) {
            Node n = childNodes.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                retVals.add((Element) n);
            }
        }
        return retVals;
    }

    private static boolean namespacesAreSame(String fNamespace, String sNamespace) {
        if ((fNamespace == null) && (sNamespace == null)) {
            return true;
        } else if (fNamespace != null) {
            return fNamespace.equals(sNamespace);
        } else
            return false;
    }

    public static Element findElement(Element topElm, String nodeName) {
        Stack<Element> stack = new Stack<Element>();
        stack.push(topElm);
        while (!stack.isEmpty()) {
            Element curElm = stack.pop();
            if (curElm.getNodeName().equals(nodeName)) {
                return curElm;
            }
            NodeList childNodes = curElm.getChildNodes();
            for (int i = 0, ll = childNodes.getLength(); i < ll; i++) {
                Node item = childNodes.item(i);
                if (item.getNodeType() == Node.ELEMENT_NODE) {
                    stack.push((Element) item);
                }
            }
        }
        return null;
    }

    public static Element findElement(Element topElm, String localName, String namespace) {
        Stack<Element> stack = new Stack<Element>();
        stack.push(topElm);
        while (!stack.isEmpty()) {
            Element curElm = stack.pop();
            if ((curElm.getLocalName().equals(localName)) && (namespacesAreSame(curElm.getNamespaceURI(), namespace))) {
                return curElm;
            }
            NodeList childNodes = curElm.getChildNodes();
            for (int i = 0, ll = childNodes.getLength(); i < ll; i++) {
                Node item = childNodes.item(i);
                if (item.getNodeType() == Node.ELEMENT_NODE) {
                    stack.push((Element) item);
                }
            }
        }
        return null;
    }

    public static void print(Document doc, OutputStream out) throws TransformerException {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(out);
        transformer.transform(source, result);
    }
}
