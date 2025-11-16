package cz.incad.kramerius.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class BatchUtils {
    
    public static final Logger LOGGER = Logger.getLogger(BatchUtils.class.getName());

    public static List<Document> batches(Element resultElem,int batchSize) throws ParserConfigurationException, MigrateSolrIndexException  {
        List<Document> batches = new ArrayList<Document>();
        List<Element> elms = XMLUtils.getElements(resultElem, new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element elm) {
                return  (elm.getNodeName().equals("doc"));
            }
        });

        int numberOfBatches = (elms.size() / batchSize) + ((elms.size() % batchSize) > 0 ? 1 : 0);
        for (int i = 0; i < numberOfBatches; i++) {
            Document destBatch = XMLUtils.crateDocument("add");
            int max = Math.min((i+1)*batchSize, elms.size());
            for (int j = i*batchSize; j < max; j++) {
                Element destDocElement = destBatch.createElement("doc");
                destBatch.getDocumentElement().appendChild(destDocElement);
                Element sourceDocElm = elms.get(j);
                transform(sourceDocElm, destBatch, destDocElement);

                // array  field
                Element field = destBatch.createElement("field");
                field.setAttribute("name", "collection");
                field.setTextContent("vc:44679769-b5bb-4ac7-ad27-a0c44698c2ea");

                destDocElement.appendChild(field);

            }
//            try {
//                XMLUtils.print(destBatch, System.out);
//            } catch (TransformerException e) {
//                e.printStackTrace();
//            }

            batches.add(destBatch);
        }


        return batches;
    }


    /** find element by attribute */
    static Element findByAttribute(Element sourceDocElm, String attName) {
        Element elemName = XMLUtils.findElement(sourceDocElm,  new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                return element.getAttribute("name").equals(attName);
            }
        });
        return elemName;
    }

    /** find pid in source doc */
    static String pid(Element sourceDocElm) {
        Element pidElm = XMLUtils.findElement(sourceDocElm,  new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                if (element.getNodeName().equals("str")) {
                    return element.getAttribute("name").equals("PID");
                }
                return false;
            }
        });
        if (pidElm != null) {
            return pidElm.getTextContent().trim();
        } else return "";
    }


    public static void transform(Element sourceDocElm, Document destDocument,Element destDocElem) throws MigrateSolrIndexException  {
        String pid = pid(sourceDocElm);
        if (sourceDocElm.getNodeName().equals("doc")) {
            NodeList childNodes = sourceDocElm.getChildNodes();
            for (int j = 0,lj=childNodes.getLength(); j < lj; j++) {
                Node node = childNodes.item(j);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    List<String> primitiveVals = Arrays.asList("str","int","bool", "date");
                    if (primitiveVals.contains(node.getNodeName())) {
                        simpleValue(destDocument,destDocElem, node,null);
                    } else {
                        arrayValue(destDocument,destDocElem,node);
                    }
                }
            }
            if (MigrationUtils.configuredBuildCompositeId()) {
                enhanceByCompositeId(destDocument, destDocElem);
            }
        }
        
    }
    
    public static void enhanceByCompositeId(Document ndoc,Element docElm) {
        Element pidElm = XMLUtils.findElement(docElm, new XMLUtils.ElementsFilter() {
            
            @Override
            public boolean acceptElement(Element paramElement) {
                String attribute = paramElement.getAttribute("name");
                return attribute.equals("PID");
            }
        });
        Element rootPidElm = XMLUtils.findElement(docElm, new XMLUtils.ElementsFilter() {
            
            @Override
            public boolean acceptElement(Element paramElement) {
                String attribute = paramElement.getAttribute("name");
                return attribute.equals("root_pid");
            }
        });
        
            
        
        String txt = rootPidElm.getTextContent().trim()+"!"+pidElm.getTextContent().trim();
        Element compositeIdElm = ndoc.createElement("field");
        String compositeIdName = System.getProperty("compositeId.field.name","compositeId");
        compositeIdElm.setAttribute("name", compositeIdName);
        compositeIdElm.setTextContent(txt);
        docElm.appendChild(compositeIdElm);

    }
    
    public static void simpleValue(Document feedDoc, Element feedDocElm, Node node, String derivedName) {
        String attributeName = derivedName != null ? derivedName : ((Element)node).getAttribute("name");
        Element strElm = feedDoc.createElement("field");
        strElm.setAttribute("name", attributeName);
        feedDocElm.appendChild(strElm);
        String content = StringEscapeUtils.escapeXml(node.getTextContent());
        strElm.setTextContent(content);
    }

    public static void arrayValue( Document feedDoc, Element feedDocElement, Node node) {
        String attributeName = ((Element) node).getAttribute("name");
        NodeList childNodes = node.getChildNodes();
        for (int i = 0,ll=childNodes.getLength(); i < ll; i++) {
            Node n = childNodes.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                simpleValue( feedDoc,feedDocElement, n, attributeName);
            }
        }
    }
}
