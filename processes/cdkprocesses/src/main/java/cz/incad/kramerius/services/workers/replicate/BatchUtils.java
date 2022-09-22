package cz.incad.kramerius.services.workers.replicate;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.utils.XMLUtils;

public class BatchUtils {


    private BatchUtils() {}

    public static final Logger LOGGER = Logger.getLogger(BatchUtils.class.getName());


    public static Document batch(Element resultElem, boolean compositeId, String root, String child, SourceToDestTransform srcTransform) throws ParserConfigurationException, MigrateSolrIndexException  {
        //List<String> removalSourceElements = itemsToRemove();
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
            srcTransform.transform(sourceDocElm, destBatch, destDocElement);

            // composite id is not supported
            if (compositeId && root != null && child != null) {


                root = srcTransform.getField(root) != null ?  srcTransform.getField(root) : root;
                child = srcTransform.getField(child) != null ?  srcTransform.getField(child) : child;

                //srcTransform.
                boolean b = enhanceByCompositeId(destBatch, destDocElement, root, child);
                if (b) {
                	destBatch.getDocumentElement().appendChild(destDocElement);
                } else {
                	ReplicateFinisher.NOT_INDEXED_COMPOSITEID.addAndGet(1);
                }
            } else {
                destBatch.getDocumentElement().appendChild(destDocElement);
            }
        }

        return destBatch;
    }


    public static boolean enhanceByCompositeId(Document ndoc,Element docElm, String root, String child) {
        Element pidElm = XMLUtils.findElement(docElm, new XMLUtils.ElementsFilter() {
            
            @Override
            public boolean acceptElement(Element paramElement) {
                String attribute = paramElement.getAttribute("name");
                return attribute.equals(child);
            }
        });
        Element rootPidElm = XMLUtils.findElement(docElm, new XMLUtils.ElementsFilter() {
            
            @Override
            public boolean acceptElement(Element paramElement) {
                String attribute = paramElement.getAttribute("name");
                return attribute.equals(root);
            }
        });

        if (rootPidElm != null && pidElm != null) {

            String txt = rootPidElm.getTextContent().trim()+"!"+pidElm.getTextContent().trim();
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
