package cz.incad.kramerius.services.workers.replicate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import cz.incad.kramerius.services.utils.ResultsUtils;
import cz.incad.kramerius.services.workers.replicate.records.IndexedRecord;
import cz.incad.kramerius.services.workers.replicate.records.NewConflictRecord;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.inovatika.kramerius.services.workers.batch.BatchTransformation;
import cz.inovatika.kramerius.services.workers.batch.BatchConsumer;
import cz.incad.kramerius.utils.XMLUtils;

public class BatchUtils {


    private BatchUtils() {}

    public static final Logger LOGGER = Logger.getLogger(BatchUtils.class.getName());

    /**
     * Creates batch document
     * @param cdkRepContext ReplicateContext class
     * @param resultElem Result element from source library
     * @param compositeId CompoisteId
     * @param root Root PID
     * @param child  Child PID
     * @param srcTransform SourceToDestTransformation
     * @param consumer Consumer for additional modification
     * @return
     * @throws ParserConfigurationException
     * @throws MigrateSolrIndexException
     */
    public static Document batch(CDKReplicateContext cdkRepContext, Element resultElem, boolean compositeId, String root, String child, BatchTransformation srcTransform, BatchConsumer consumer ) throws ParserConfigurationException, MigrateSolrIndexException  {
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

            NewConflictRecord newConflict = checkModelConflict(cdkRepContext, sourceDocElm, child, srcTransform);
            if (newConflict != null) {
                cdkRepContext.addConflictRecord(newConflict);
            } else {
                // basic transform
                srcTransform.transform(sourceDocElm, destBatch, destDocElement,consumer);
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


                Element rootPidElm = XMLUtils.findElement(destDocElement, new XMLUtils.ElementsFilter() {
                    @Override
                    public boolean acceptElement(Element element) {
                        String name = element.getAttribute("name");
                        return name.equals("root.pid");
                    }
                });


                Element pidElm = XMLUtils.findElement(destDocElement, new XMLUtils.ElementsFilter() {

                    @Override
                    public boolean acceptElement(Element element) {
                        String name = element.getAttribute("name");
                        return name.equals("pid");
                    }
                });

                if (consumer != null) consumer.changeDocument( null, destDocElement);
            }

        }
        return destBatch;
    }


    /** find conflicting record */
    private static NewConflictRecord checkModelConflict(CDKReplicateContext cdkReplicateContext, Element sourceDocElm, String childComposite, BatchTransformation srcTransform) {
        String transformedComposite = srcTransform.resolveSourcePid(childComposite);
        Map<String, Object> doc = ResultsUtils.doc(sourceDocElm);
        Object pid = doc.get(transformedComposite);
        IndexedRecord indexedRecord = cdkReplicateContext.getAlreadyIndexedAsMap().get(pid.toString());
        if (indexedRecord != null) {
            Object indexedModel = indexedRecord.getDocument().get("model");
            Object fetchedModel = doc.get(srcTransform.getField( "model"));
            if (!indexedModel.toString().toLowerCase().equals(fetchedModel.toString().toLowerCase())) {
                return new NewConflictRecord(pid.toString(), Arrays.asList((String)srcTransform.getField( "root.pid"), (String)indexedRecord.getDocument().get("root.pid")));
            } else {
                return null;
            }
        } else return null;
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
