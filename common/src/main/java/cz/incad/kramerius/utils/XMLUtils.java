package cz.incad.kramerius.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

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

/**
 * Handle XML; parsing, find, etc..
 * @author pavels
 *
 * <b>Implementation note: All DOM access methods are synchronized on Document object </b>
 *
 */
public class XMLUtils {

    public static final Logger LOGGER = Logger.getLogger(XMLUtils.class.getName());
    
    
    /**
     * PArse document from reader
     * @param reader Reader
     * @return DOM
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static Document parseDocument(Reader reader) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory newInstance = DocumentBuilderFactory.newInstance();
        LOGGER.log(Level.FINE, "builder factory instance :"+newInstance.getClass().getResource(newInstance.getClass().getSimpleName()+".class"));
        DocumentBuilder builder = newInstance.newDocumentBuilder();
        return builder.parse(new InputSource(reader));
    }

    /**
     * Parse document from reader with namespace aware flag
     * @param reader Reader
     * @param namespaceaware namespace aware flag
     * @return DOM
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static Document parseDocument(Reader reader, boolean namespaceaware) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        LOGGER.log(Level.FINE, "builder factory instance :"+factory.getClass().getResource(factory.getClass().getSimpleName()+".class"));
        factory.setNamespaceAware(namespaceaware);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(reader));
    }

    /**
     * Parse document from input stream
     * @param is InputStream
     * @return DOM
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static Document parseDocument(InputStream is) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory newInstance = DocumentBuilderFactory.newInstance();
        LOGGER.log(Level.FINE, "builder factory instance :"+newInstance.getClass().getResource(newInstance.getClass().getSimpleName()+".class"));
        DocumentBuilder builder = newInstance.newDocumentBuilder();
        return builder.parse(is);
    }

    /**
     * Parse document form inputstream with namespace aware flag
     * @param is Inputstream
     * @param namespaceaware namespace aware flag
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static Document parseDocument(InputStream is, boolean namespaceaware) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        LOGGER.log(Level.FINE, "builder factory instance :"+factory.getClass().getResource(factory.getClass().getSimpleName()+".class"));
        factory.setNamespaceAware(namespaceaware);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(is);
    }

    /**
     * Returns child elements from given top element
     * @param topElm
     * @return
     */
    public static List<Element> getElements(Element topElm) {
        if (topElm == null) throw new IllegalArgumentException("topElm cannot be null");
        synchronized(topElm.getOwnerDocument()) {
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
    }

    /**
     * Returns child elements from given top element and accepted by given filter
     * @param topElm Top element
     * @param filter Filter
     * @return
     */
    public static List<Element> getElements(Element topElm, ElementsFilter filter ) {
        if (topElm == null) throw new IllegalArgumentException("topElm cannot be null");
        synchronized(topElm.getOwnerDocument()) {
            List<Element> retVals = new ArrayList<Element>();
            NodeList childNodes = topElm.getChildNodes();
            for (int i = 0, ll = childNodes.getLength(); i < ll; i++) {
                Node n = childNodes.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    Element elm = (Element) n;
                    if (filter.acceptElement(elm)) {
                        retVals.add(elm);
                    }
                }
            }
            return retVals;
        }
    }
    
    public static List<Element> getElementsRecursive(Element topElm, ElementsFilter filter) {
        if (topElm == null) throw new IllegalArgumentException("topElm cannot be null");
        synchronized(topElm.getOwnerDocument()) {
            List<Element> elms = new ArrayList<Element>();
            Stack<Element> stack = new Stack<Element>();
            stack.push(topElm);
            while (!stack.isEmpty()) {
                Element curElm = stack.pop();
                if (filter.acceptElement(curElm)) {
                    elms.add(curElm);
                }
                NodeList childNodes = curElm.getChildNodes();
                for (int i = 0, ll = childNodes.getLength(); i < ll; i++) {
                    Node item = childNodes.item(i);
                    if (item.getNodeType() == Node.ELEMENT_NODE) {
                        stack.push((Element) item);
                    }
                }
            }
            return elms;
        }
    }
    
    
    private static boolean namespacesAreSame(String fNamespace, String sNamespace) {
        if ((fNamespace == null) && (sNamespace == null)) {
            return true;
        } else if (fNamespace != null) {
            return fNamespace.equals(sNamespace);
        } else
            return false;
    }

    /**
     * Finds element in DOM tree
     * @param topElm Top element
     * @param nodeName Node name
     * @return returns found node
     */
    public static Element findElement(Element topElm, String nodeName) {
        if (topElm == null) throw new IllegalArgumentException("topElm cannot be null");
        synchronized(topElm.getOwnerDocument()) {
            Stack<Element> stack = new Stack<Element>();
            stack.push(topElm);
            while (!stack.isEmpty()) {
                Element curElm = stack.pop();
                if (curElm.getNodeName().equals(nodeName)) {
                    return curElm;
                }
                List<Node> nodesToProcess = new ArrayList<Node>();

                NodeList childNodes = curElm.getChildNodes();
                for (int i = 0, ll = childNodes.getLength(); i < ll; i++) {
                    Node item = childNodes.item(i);
                    if (item.getNodeType() == Node.ELEMENT_NODE) {
                        //stack.push((Element) item);
                        nodesToProcess.add(item);
                    }
                }
                Collections.reverse(nodesToProcess);
                for (Node node : nodesToProcess) {
                    stack.push((Element) node);
                }
            }
            return null;
        }
    }

    /**
     * Finds element in DOM tree
     * @param topElm Top element
     * @param nodeName Node name
     * @return returns found node
     */
    public static List<Node> findNodesByType(Element topElm, int type) {
        List<Node> retvals = new ArrayList<Node>();
        if (topElm == null) throw new IllegalArgumentException("topElm cannot be null");
        synchronized(topElm.getOwnerDocument()) {
            Stack<Node> stack = new Stack<Node>();
            stack.push(topElm);
            while (!stack.isEmpty()) {
                Node curElm = stack.pop();
                if (curElm.getNodeType() == type) {
                    retvals.add(curElm);
                }
                List<Node> nodesToProcess = new ArrayList<Node>();

                NodeList childNodes = curElm.getChildNodes();
                for (int i = 0, ll = childNodes.getLength(); i < ll; i++) {
                    Node item = childNodes.item(i);
                    //stack.push((Element) item);
                    nodesToProcess.add(item);
                }
                Collections.reverse(nodesToProcess);
                for (Node node : nodesToProcess) {
                    stack.push(node);
                }
            }
            return retvals;
        }
    }

    /**
     * Finds element in DOM tree
     * @param topElm Root node
     * @param localName Local element name
     * @param namespace Element namespace
     * @return found element
     */
    public static Element findElement(Element topElm, String localName, String namespace) {
        if (topElm == null) throw new IllegalArgumentException("topElm cannot be null");
        synchronized(topElm.getOwnerDocument()) {
            Stack<Element> stack = new Stack<Element>();
            stack.push(topElm);
            while (!stack.isEmpty()) {
                Element curElm = stack.pop();
                if ((curElm.getLocalName().equals(localName)) && (namespacesAreSame(curElm.getNamespaceURI(), namespace))) {
                    return curElm;
                }
                List<Node> nodesToProcess = new ArrayList<Node>();
                NodeList childNodes = curElm.getChildNodes();
                for (int i = 0, ll = childNodes.getLength(); i < ll; i++) {
                    Node item = childNodes.item(i);
                    if (item.getNodeType() == Node.ELEMENT_NODE) {
                        nodesToProcess.add(item);
                    }
                }
                // because of stack
                Collections.reverse(nodesToProcess);
                for (Node node : nodesToProcess) {
                    stack.push((Element) node);
                    
                }
            }
            return null;
        }
    }


    
    public static Element findElement(Element topElm, ElementsFilter filter) {
        if (topElm == null) throw new IllegalArgumentException("topElm cannot be null");
        synchronized(topElm.getOwnerDocument()) {
            Stack<Element> stack = new Stack<Element>();
            stack.push(topElm);
            while (!stack.isEmpty()) {
                Element curElm = stack.pop();
                if (filter.acceptElement(curElm)) {
                    return curElm;
                }

                List<Node> nodesToProcess = new ArrayList<Node>();
                NodeList childNodes = curElm.getChildNodes();
                for (int i = 0, ll = childNodes.getLength(); i < ll; i++) {
                    Node item = childNodes.item(i);
                    if (item.getNodeType() == Node.ELEMENT_NODE) {
                        //stack.push((Element) item);
                        nodesToProcess.add(item);
                    }
                }
                Collections.reverse(nodesToProcess);
                for (Node node : nodesToProcess) {
                    stack.push((Element)node);
                }
            }
            return null;
        }
    }
    
    
    /**
     * Serialize W3C document into given output stream
     * @param doc W3C document
     * @param out OutputStream 
     * @throws TransformerException
     */
    public static void print(Document doc, OutputStream out) throws TransformerException {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(out);
        transformer.transform(source, result);
    }

    /**
     * Print document to given writer
     * @param doc Printing document
     * @param out Writer
     * @throws TransformerException
     */
    public static void print(Document doc, Writer out) throws TransformerException {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(out);
        transformer.transform(source, result);
    }

    /**
     * Print part of document 
     * @param elm Root element which should be written
     * @param out OuputStream
     * @throws TransformerException
     */
    public static void print(Element elm, OutputStream out) throws TransformerException {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();

        DOMSource source = new DOMSource(elm);
        StreamResult result = new StreamResult(out);
        transformer.transform(source, result);
    }

    /**
     * Print part of document 
     * @param elm Root element which should be written
     * @param out Writer
     * @throws TransformerException
     */
    public static void print(Element elm, Writer out) throws TransformerException {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();

        DOMSource source = new DOMSource(elm);
        StreamResult result = new StreamResult(out);
        transformer.transform(source, result);
    }
    
    /**
     * Create new empty document 
     * @param rootName Name of root element
     * @return
     * @throws ParserConfigurationException
     */
    public static Document crateDocument(String rootName) throws ParserConfigurationException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = builderFactory.newDocumentBuilder();
        Document document = docBuilder.newDocument();
        Element rootElement = document.createElement(rootName);
        document.appendChild(rootElement);
        return document;
    }
    
    /**
     * Elements filter 
     * @author pavels
     *
     */
    public static interface ElementsFilter {
        /**
         * Returns true if given element should be accepted
         * @param element
         * @return
         */
        public boolean acceptElement(Element element);
    }
}
