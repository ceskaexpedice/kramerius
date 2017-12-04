package cz.incad.kramerius.services;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cz.incad.kramerius.indexer.FedoraOperations;
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

        int numberOfDocs = (elms.size() / batchSize) + ((elms.size() % batchSize) > 0 ? 1 : 0);
        for (int i = 0; i < numberOfDocs; i++) {
            Document destBatch = XMLUtils.crateDocument("add");
            int max = Math.min((i+1)*batchSize, elms.size());
            for (int j = i*batchSize; j < max; j++) {
                Element destDocElement = destBatch.createElement("doc");
                destBatch.getDocumentElement().appendChild(destDocElement);
                transform(elms.get(j), destBatch, destDocElement);
            }
            batches.add(destBatch);
        }
        
        return batches;
    }
    
    public static void transform(Element sourceDocElm, Document destDocument,Element destDocElem) throws MigrateSolrIndexException  {
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
            browseAuthorsAndTitles(sourceDocElm, destDocument, destDocElem);
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
    
    public static void simpleValue(Document ndoc, Element docElm, Node node, String derivedName) {
        Element strElm = ndoc.createElement("field");
        strElm.setAttribute("name", derivedName != null ? derivedName : ((Element)node).getAttribute("name"));
        docElm.appendChild(strElm);
        String content = StringEscapeUtils.escapeXml(node.getTextContent());
        strElm.setTextContent(content);
    }

    public static void arrayValue(Document ndoc, Element docElm, Node node) {
        NodeList childNodes = node.getChildNodes();
        for (int i = 0,ll=childNodes.getLength(); i < ll; i++) {
            Node n = childNodes.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                simpleValue(ndoc,docElm, n, ((Element)node).getAttribute("name"));
            }
        }
    }

    // special not stored fields  browse_autor, browse_title
    public static void browseAuthorsAndTitles(Element sourceDocElm,Document ndoc, Element docElm)  {
        try {
            FedoraOperations operations = new FedoraOperations(null,null);
            Element dcCreators = XMLUtils.findElement(sourceDocElm, new XMLUtils.ElementsFilter() {
                
                @Override
                public boolean acceptElement(Element e) {
                    String attribute = e.getAttribute("name");
                    return attribute.equals("dc.creator");
                }
            });
            if (dcCreators != null) {
                List<Element> dcCreatorsStrings = XMLUtils.getElements(dcCreators);
                for (Element author : dcCreatorsStrings) {
                    //<xsl:value-of select="exts:prepareCzech($generic, text())"/>##<xsl:value-of select="text()"/>
                    String textContent = author.getTextContent();
                    String prepared = operations.prepareCzech(textContent)+"##"+textContent;
                    Element strElm = ndoc.createElement("field");
                    strElm.setAttribute("name", "browse_autor");
                    docElm.appendChild(strElm);
                    strElm.setTextContent(prepared);
                }
            }

            Element model = XMLUtils.findElement(sourceDocElm, new XMLUtils.ElementsFilter() {
                
                @Override
                public boolean acceptElement(Element e) {
                    String attribute = e.getAttribute("name");
                    return attribute.equals("fedora.model");
                }
            });

            
            Element dcTitle = XMLUtils.findElement(sourceDocElm, new XMLUtils.ElementsFilter() {
                
                @Override
                public boolean acceptElement(Element e) {
                    String attribute = e.getAttribute("name");
                    return attribute.equals("dc.title");
                }
            });
            
            
            if (dcTitle != null && model != null &&  Arrays.asList(KConfiguration.getInstance().getConfiguration().getStringArray("indexer.browseModels")).contains(model.getTextContent().trim())) {
                Element strElm = ndoc.createElement("field");
                strElm.setAttribute("name", "browse_title");
                docElm.appendChild(strElm);
                String textContent = dcTitle.getTextContent();
                String prepared = operations.prepareCzech(textContent)+"##"+textContent;
                strElm.setTextContent(prepared);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
}
