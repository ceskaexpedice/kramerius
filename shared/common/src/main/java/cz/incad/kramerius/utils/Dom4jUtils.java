package cz.incad.kramerius.utils;

import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dom4jUtils {

    private static Map<String, String> NAMESPACE_URIS = new HashMap<>();

    static {
        NAMESPACE_URIS.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        NAMESPACE_URIS.put("foxml", "info:fedora/fedora-system:def/foxml#");
        //RELS-EXT
        NAMESPACE_URIS.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        NAMESPACE_URIS.put("model", "info:fedora/fedora-system:def/model#");
        NAMESPACE_URIS.put("rel", "http://www.nsdl.org/ontologies/relationships#");
        NAMESPACE_URIS.put("oai", "http://www.openarchives.org/OAI/2.0/");
        //BIBLIO_MODS
        NAMESPACE_URIS.put("mods", "http://www.loc.gov/mods/v3");
        //DC
        NAMESPACE_URIS.put("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
        NAMESPACE_URIS.put("dc", "http://purl.org/dc/elements/1.1/");
        //NAMESPACE_URIS.put("", "");
    }

    public static Document parseXmlFromFile(File xmlFile) throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(xmlFile);
        return document;
    }

    public static Document parseXmlFromW3cDoc(org.w3c.dom.Document doc) throws DocumentException, IOException {
        return parseXmlFromString(w3cDocumentToString(doc));
    }

    public static Document parseXmlFromString(String xmlString) throws DocumentException {
        InputStream stream = null;
        try {
            SAXReader reader = new SAXReader();
            stream = new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8));
            Document document = reader.read(stream);
            return document;
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static XPath buildXpath(String xpathExpr) {
        XPath xPath = DocumentHelper.createXPath(xpathExpr);
        xPath.setNamespaceURIs(NAMESPACE_URIS);
        return xPath;
    }

    public static Element firstElementByXpath(Element root, String xpathExpr) {
        XPath xPath = buildXpath(xpathExpr);
        List<Node> result = xPath.selectNodes(root);
        if (result.size() > 0) {
            Node firstNode = result.get(0);
            if (firstNode instanceof Element) {
                return (Element) firstNode;
            }
        }
        return null;
    }

    public static List<Element> elementsByXpath(Element root, String xpathExpr) {
        XPath xPath = buildXpath(xpathExpr);
        List<Element> retvals = new ArrayList<>();
        List<Node> result = xPath.selectNodes(root);
        for (Node node : result) {
            if (node instanceof Element) {
            	retvals.add((Element)node);
            }
		}
        return retvals;
    }

    public static String stringOrNullFromFirstElementByXpath(Element root, String xpathExpr) {
        XPath xPath = buildXpath(xpathExpr);
        List<Node> result = xPath.selectNodes(root);
        if (result.size() > 0) {
            Node firstNode = result.get(0);
            if (firstNode instanceof Element) {
                String value = ((Element) firstNode).getStringValue();
                if (value != null) {
                    String trimmed = value.trim();
                    if (!trimmed.isEmpty()) {
                        return trimmed;
                    }
                }
            }
        }
        return null;
    }

    public static String stringOrNullFromAttributeByXpath(Element root, String xpathExpr) {
        XPath xPath = buildXpath(xpathExpr);
        List<Node> result = xPath.selectNodes(root);
        if (result.size() == 1) {
            Node firstNode = result.get(0);
            if (firstNode instanceof Attribute) {
                String value = ((Attribute) firstNode).getValue();
                if (value != null) {
                    String trimmed = value.trim();
                    if (!trimmed.isEmpty()) {
                        return trimmed;
                    }
                }
            }
        }
        return null;
    }

    public static Integer integerOrNullFromAttributeByXpath(Element root, String xpathExpr) {
        String value = stringOrNullFromAttributeByXpath(root, xpathExpr);
        if (value != null) {
            return Integer.valueOf(value);
        } else {
            return null;
        }
    }

    public static String stringOrNullFromAttributeByName(Element element, String attributeName) {
        Attribute attribute = element.attribute(attributeName);
        if (attribute != null) {
            String value = attribute.getValue();
            if (value != null) {
                String trimmed = value.trim();
                if (!trimmed.isEmpty()) {
                    return trimmed;
                }
            }
        }
        return null;
    }

    public static Float floatOrNullFromAttributeByName(Element element, String attributeName) {
        String value = stringOrNullFromAttributeByName(element, attributeName);
        if (value != null) {
            return Float.valueOf(value);
        } else {
            return null;
        }
    }

    public static Integer integerOrNullFromAttributeByName(Element element, String attributeName) {
        String value = stringOrNullFromAttributeByName(element, attributeName);
        if (value != null) {
            return Integer.valueOf(value);
        } else {
            return null;
        }
    }

    private static String w3cDocumentToString(org.w3c.dom.Document doc) throws IOException {
        try {
            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            return writer.toString();
        } catch (TransformerException e) {
            throw new IOException(e);
        }
    }

    public static String toStringOrNull(Node node) {
        return toStringOrNull(node, true);
    }

    public static String toStringOrNull(Node node, boolean trim) {
        if (node != null) {
            String value = node.getStringValue();
            if (value != null) {
                //replace multiple white spaces with single space
                value = value.replaceAll("\\s+", " ");
                if (trim) {
                    value = value.trim();
                }
                if (!value.isEmpty()) {
                    return value;
                }
            }
        }
        return null;
    }

    public static String docToPrettyString(Document doc) throws IOException {
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("UTF-8");
        format.setIndent(true);
        format.setIndentSize(4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XMLWriter xmlWriter = new XMLWriter(out, format);
        xmlWriter.write(doc);
        xmlWriter.flush();
        out.close();
        return out.toString();
    }

    public static String getNamespaceUri(String prefix) {
        return NAMESPACE_URIS.get(prefix);
    }
}
