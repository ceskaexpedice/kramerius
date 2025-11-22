package cz.inovatika.kramerius.services.workers.batch;

import cz.incad.kramerius.utils.XMLUtils;
import cz.inovatika.kramerius.services.config.ProcessConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.Arrays;
import java.util.List;

public class UpdateSolrBatch {

    private static final List<String> PRIMITIVE_FIELD_TYPES = Arrays.asList("str", "int", "bool", "date");

    protected ProcessConfig config;
    protected Element resultElement;
    protected BatchConsumer consumer;

    public UpdateSolrBatch(ProcessConfig processConfig, Element resultElem, BatchConsumer consumer) {
        this.config = processConfig;
        this.resultElement = resultElem;
        this.consumer = consumer;
    }

    public Document createBatchForInsert() throws ParserConfigurationException {
        return createBatch(false);
    }

    public Document createBatchForUpdate() throws ParserConfigurationException {
        return createBatch(true);
    }

    protected Document createBatch(boolean editMode) throws ParserConfigurationException {
        Document destBatch = XMLUtils.crateDocument("add");
        List<Element> docs = XMLUtils.getElements(this.resultElement, new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element elm) {
                return  (elm.getNodeName().equals("doc"));
            }
        });

        for (Element sourceDocElm : docs) {
            Element destDocElement = destBatch.createElement("doc");
            this.convertSourceToTargetFields(editMode, sourceDocElm, destBatch, destDocElement,consumer);

            boolean compositeId = config.getWorkerConfig().getRequestConfig().isCompositeId();
            String root = config.getWorkerConfig().getRequestConfig().getRootOfComposite();
            String child = config.getWorkerConfig().getRequestConfig().getChildOfComposite();

            if (compositeId && root != null && child != null) {
                boolean b = enhanceByCompositeId(destBatch, destDocElement, root, child);
                destBatch.getDocumentElement().appendChild(destDocElement);
            } else {
                destBatch.getDocumentElement().appendChild(destDocElement);
            }

            if (consumer != null) consumer.changeDocument(this.config, destDocElement);
        }
        return destBatch;
    }

    public void simpleValue(boolean edit, Document feedDoc, Element feedDocElm, Node node, String derivedName, BatchConsumer consumer) {
        boolean compositeId = this.config.getWorkerConfig().getRequestConfig().isCompositeId();
        String idIdentifier = this.config.getWorkerConfig().getRequestConfig().getIdIdentifier();

        String attributeName = derivedName != null ? derivedName : ((Element)node).getAttribute("name");

        Element strElm = feedDoc.createElement("field");
        strElm.setAttribute("name", attributeName);
        String textContent = node.getTextContent();
        strElm.setTextContent(textContent);
        if (edit) {
            boolean isIgnoredId = (compositeId && attributeName.equals("compositeId")) ||
                    (!compositeId && attributeName.equals(idIdentifier));
            if (!isIgnoredId) {
                fieldModifierInUpdate(strElm);
            }
        }

        BatchConsumer.ModifyFieldResult result = BatchConsumer.ModifyFieldResult.none;
        if (consumer != null) {
            result = consumer.modifyField(strElm);
        }
        if (!result.equals(BatchConsumer.ModifyFieldResult.delete)) {
            feedDocElm.appendChild(strElm);
        }
    }

    protected void fieldModifierInUpdate(Element strElm) {
        strElm.setAttribute("update", "set");
    }

    public void arrayValue(boolean edit,  Element sourceDocElement, Document feedDoc, Element feedDocElement, Node node, BatchConsumer consumer) {
        String attributeName = ((Element) node).getAttribute("name");
        NodeList childNodes = node.getChildNodes();
        for (int i = 0,ll=childNodes.getLength(); i < ll; i++) {
            Node n = childNodes.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                simpleValue(edit, feedDoc,feedDocElement, n, attributeName,  consumer);
            }
        }
    }

    public void convertSourceToTargetFields(boolean edit, Element sourceDocElm, Document destBatch, Element destDocElem, BatchConsumer consumer)  {
        if (sourceDocElm.getNodeName().equals("doc")) {
            NodeList childNodes = sourceDocElm.getChildNodes();
            for (int j = 0,lj=childNodes.getLength(); j < lj; j++) {
                Node node = childNodes.item(j);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    List<String> primitiveVals = Arrays.asList("str","int","bool", "date");
                    if (primitiveVals.contains(node.getNodeName())) {
                        simpleValue(edit, destBatch,destDocElem, node,null,  consumer);
                    } else {
                        arrayValue(edit, sourceDocElm, destBatch,destDocElem,node, consumer);
                    }
                }
            }
        }
    }



    private static boolean enhanceByCompositeId(Document ndoc,Element docElm, String root, String child) {
        Element childComponent = XMLUtils.findElement(docElm, paramElement -> paramElement.getAttribute("name").equals(child));
        Element rootComponent = XMLUtils.findElement(docElm, paramElement -> paramElement.getAttribute("name").equals(root));

        if (rootComponent != null && childComponent != null) {

            String txt = rootComponent.getTextContent().trim()+"!"+childComponent.getTextContent().trim();
            Element compositeIdElm = ndoc.createElement("field");
            String compositeIdName = System.getProperty("compositeId.field.name","compositeId");
            compositeIdElm.setAttribute("name", compositeIdName);
            compositeIdElm.setTextContent(txt);
            docElm.appendChild(compositeIdElm);
            return true;
        }  else {
            return  false;
        }
    }
}
