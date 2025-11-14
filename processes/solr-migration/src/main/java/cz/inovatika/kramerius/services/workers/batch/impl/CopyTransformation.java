package cz.inovatika.kramerius.services.workers.batch.impl;

import cz.inovatika.kramerius.services.workers.batch.BatchConsumer;
import cz.inovatika.kramerius.services.workers.batch.BatchConsumer.ModifyFieldResult;
import cz.incad.kramerius.utils.XMLUtils;
import cz.inovatika.kramerius.services.workers.batch.BatchTransformation;
import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Arrays;
import java.util.List;


//TODO: Change it in future; copied from K5 cdk download
public class CopyTransformation extends BatchTransformation {


    public static final String NAME = "COPY";

    public static void simpleValue(Document feedDoc, Element feedDocElm, Node node, String derivedName, BatchConsumer consumer) {
        String attributeName = derivedName != null ? derivedName : ((Element)node).getAttribute("name");
 
        Element strElm = feedDoc.createElement("field");
        strElm.setAttribute("name", attributeName);
        String textContent = node.getTextContent();
        String content = StringEscapeUtils.escapeXml(textContent);

        // add to context to process
        strElm.setTextContent(textContent);

        ModifyFieldResult result = ModifyFieldResult.none;
        if (consumer != null) {
        	result = consumer.modifyField(strElm);
        }
        if (!result.equals(ModifyFieldResult.delete)) {
            feedDocElm.appendChild(strElm);
        }
    }

    public static void arrayValue( Element sourceDocElement, Document feedDoc, Element feedDocElement, Node node, BatchConsumer consumer) {
        String attributeName = ((Element) node).getAttribute("name");
        NodeList childNodes = node.getChildNodes();
        for (int i = 0,ll=childNodes.getLength(); i < ll; i++) {
            Node n = childNodes.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                simpleValue( feedDoc,feedDocElement, n, attributeName,  consumer);
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

    @Override
    public String getName() {
        return NAME;
    }

    /** */
    public void transform(Element sourceDocElm, Document destDocument, Element destDocElem, BatchConsumer consumer)  {
        //String pid = pid(sourceDocElm);
        if (sourceDocElm.getNodeName().equals("doc")) {
            NodeList childNodes = sourceDocElm.getChildNodes();
            for (int j = 0,lj=childNodes.getLength(); j < lj; j++) {
                Node node = childNodes.item(j);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    List<String> primitiveVals = Arrays.asList("str","int","bool", "date");
                    if (primitiveVals.contains(node.getNodeName())) {
                        simpleValue(destDocument,destDocElem, node,null,  consumer);
                    } else {
                        arrayValue(sourceDocElm, destDocument,destDocElem,node, consumer);
                    }
                }
            }
        }

    }

    



    @Override
    public String getField(String fieldId) {
        return fieldId;
    }

    @Override
    public String resolveSourcePid(String cdkField) {
        return cdkField;
    }
}
