package cz.inovatika.kramerius.services.workers.batch;

import cz.incad.kramerius.utils.XMLUtils;
import cz.inovatika.kramerius.services.config.ProcessConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;
import java.util.List;

public class Batch {

    private ProcessConfig config;
    private BatchTransformation transformation;
    private BatchConsumer consumer;

    public Batch(ProcessConfig processConfig, BatchTransformation transformation, BatchConsumer consumer) {
        this.config = processConfig;
        this.transformation = transformation;
        this.consumer = consumer;
    }

    public Document create(Element resultElem ) throws ParserConfigurationException {
        Document destBatch = XMLUtils.crateDocument("add");
        List<Element> docs = XMLUtils.getElements(resultElem, new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element elm) {
                return  (elm.getNodeName().equals("doc"));
            }
        });

        for (int i = 0; i < docs.size(); i++) {
            Element destDocElement = destBatch.createElement("doc");
            Element sourceDocElm = docs.get(i);

            // basic transform
            transformation.transform(sourceDocElm, destBatch, destDocElement,consumer);

            // dat do workeru
            boolean compositeId = config.getWorkerConfig().getRequestConfig().isCompositeId();
            String root = config.getWorkerConfig().getRequestConfig().getRootOfComposite();
            String child = config.getWorkerConfig().getRequestConfig().getChildOfComposite();


            if (compositeId && root != null && child != null) {
                root = transformation.getField(root) != null ?  transformation.getField(root) : root;
                child = transformation.getField(child) != null ?  transformation.getField(child) : child;
                boolean b = enhanceByCompositeId(destBatch, destDocElement, root, child);
                if (b) {
                    destBatch.getDocumentElement().appendChild(destDocElement);
                }
            } else {
                destBatch.getDocumentElement().appendChild(destDocElement);
            }

            if (consumer != null) consumer.changeDocument(this.config, destDocElement);
        }
        return destBatch;

    }



    private static boolean enhanceByCompositeId(Document ndoc,Element docElm, String root, String child) {
        Element childComponent = XMLUtils.findElement(docElm, new XMLUtils.ElementsFilter() {

            @Override
            public boolean acceptElement(Element paramElement) {
                String attribute = paramElement.getAttribute("name");
                return attribute.equals(child);
            }
        });
        Element rootComponent = XMLUtils.findElement(docElm, new XMLUtils.ElementsFilter() {

            @Override
            public boolean acceptElement(Element paramElement) {
                String attribute = paramElement.getAttribute("name");
                return attribute.equals(root);
            }
        });

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
