package cz.incad.kramerius.services.transform;

import cz.incad.kramerius.services.workers.replicate.BatchUtils;
import cz.incad.kramerius.services.workers.replicate.copy.CopyReplicateConsumer;
import cz.incad.kramerius.services.workers.replicate.copy.CopyReplicateConsumer.ModifyFieldResult;
import cz.incad.kramerius.utils.UTFSort;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;


//TODO: Change it in future; copied from K5 cdk download
public class BasicSourceToDestTransform extends SourceToDestTransform{



    /** find element by attribute */
    public static Element findByAttribute(Element sourceDocElm, String attName) {
        Element elemName = XMLUtils.findElement(sourceDocElm,  new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                return element.getAttribute("name").equals(attName);
            }
        });
        return elemName;
    }



    public static void simpleValue(String pid, Document feedDoc, Element feedDocElm, Node node, String derivedName, boolean dontCareAboutNonCopiingFields, CopyReplicateConsumer consumer) {
        //String attributeName = ((Element)node).getAttribute("name");
        String attributeName = derivedName != null ? derivedName : ((Element)node).getAttribute("name");
        if (dontCareAboutNonCopiingFields) {

        	Element strElm = feedDoc.createElement("field");
            strElm.setAttribute("name", attributeName);
            String content = StringEscapeUtils.escapeXml(node.getTextContent());
            // add to context to process
            strElm.setTextContent(content);

            ModifyFieldResult result = ModifyFieldResult.none;
            if (consumer != null) {
            	result = consumer.modifyField(strElm);
            }
            if (!result.equals(ModifyFieldResult.delete)) {
                feedDocElm.appendChild(strElm);
            }
        }
    }

    public static void arrayValue(String pid, Element sourceDocElement, Document feedDoc, Element feedDocElement, Node node, CopyReplicateConsumer consumer) {
        String attributeName = ((Element) node).getAttribute("name");
        NodeList childNodes = node.getChildNodes();
        for (int i = 0,ll=childNodes.getLength(); i < ll; i++) {
            Node n = childNodes.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                //simpleValue(feedDoc,feedDocElement, n, false);
                simpleValue(pid, feedDoc,feedDocElement, n, attributeName, false, consumer);
            }
        }
    }


    /** find pid in source doc */
    public static String pid(Element sourceDocElm) {
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

    /** */
    public void transform(Element sourceDocElm, Document destDocument, Element destDocElem, CopyReplicateConsumer consumer)  {
        String pid = pid(sourceDocElm);
        if (sourceDocElm.getNodeName().equals("doc")) {
            NodeList childNodes = sourceDocElm.getChildNodes();
            for (int j = 0,lj=childNodes.getLength(); j < lj; j++) {
                Node node = childNodes.item(j);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    List<String> primitiveVals = Arrays.asList("str","int","bool", "date");
                    if (primitiveVals.contains(node.getNodeName())) {
                        simpleValue(pid, destDocument,destDocElem, node,null, false, consumer);
                    } else {
                        arrayValue(pid,sourceDocElm, destDocument,destDocElem,node, consumer);
                    }
                }
            }
        }
    }


    @Override
    public String getField(String fieldId) {
        return fieldId;
    }
}
