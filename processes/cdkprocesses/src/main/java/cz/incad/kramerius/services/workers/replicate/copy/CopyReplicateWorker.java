package cz.incad.kramerius.services.workers.replicate.copy;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.services.Worker;
import cz.incad.kramerius.services.iterators.IterationItem;
import cz.incad.kramerius.services.utils.SolrUtils;
import cz.incad.kramerius.services.workers.replicate.AbstractReplicateWorker;
import cz.incad.kramerius.services.workers.replicate.BatchUtils;
import cz.incad.kramerius.services.workers.replicate.ReplicateContext;
import cz.incad.kramerius.services.workers.replicate.ReplicateFinisher;
import cz.incad.kramerius.utils.XMLUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Replicate index; 1-1 copy + enhancing compositeID if there is solr cloud
 */
public class CopyReplicateWorker extends AbstractReplicateWorker {

    public static Logger LOGGER = Logger.getLogger(CopyReplicateWorker.class.getName());

    public CopyReplicateWorker(Element workerElm, Client client, List<IterationItem> pids) {
        super(workerElm, client, pids);
        config(workerElm);
    }


	@Override
    public void run() {
        try {
            ReplicateFinisher.WORKERS.addAndGet(this.itemsToBeProcessed.size());

            LOGGER.info("["+Thread.currentThread().getName()+"] processing list of pids "+this.pidsToBeProcessed.size());
            int batches = this.pidsToBeProcessed.size() / batchSize + (this.pidsToBeProcessed.size() % batchSize == 0 ? 0 :1);
            LOGGER.info("["+Thread.currentThread().getName()+"] creating  "+batches+" batch ");
            for (int i=0;i<batches;i++) {
                int from = i*batchSize;
                int to = from + batchSize;
                try {
                    List<String> subpids = pidsToBeProcessed.subList(from, Math.min(to,pidsToBeProcessed.size() ));
                    ReplicateFinisher.BATCHES.addAndGet(subpids.size());
                    ReplicateContext pidsToReplicate = findPidsAlreadyIndexed(subpids, this.transform);
                    if (!pidsToReplicate.getNotIndexed().isEmpty()) {

                        // fetch document
                        Element response = fetchDocumentFromRemoteSOLR( this.client,  pidsToReplicate.getNotIndexed());
                        Element resultElem = XMLUtils.findElement(response, (elm) -> {
                            return elm.getNodeName().equals("result");
                        });

                        // create batch
                        Document batch = BatchUtils.batch(resultElem, this.compositeId, this.rootOfComposite, this.childOfComposite, this.transform);

                        Element addDocument = batch.getDocumentElement();
                        // on index - remove element
                        this.onIndexEventRemoveElms.stream().forEach(f->{
                            synchronized (f.getOwnerDocument()) {
                                String name = f.getAttribute("name");
                                // iterating over doc
                                List<Element> docs = XMLUtils.getElements(addDocument);
                                for (int j = 0,ll=docs.size(); j < ll; j++) {
                                    Element doc = docs.get(j);
                                    List<Element> fields = XMLUtils.getElements(doc);
                                    for (Element fe : fields) {
                                        String fName = fe.getAttribute("name");
                                        if (name.equals(fName)) {
                                            doc.removeChild(fe);
                                        }
                                    }
                                }
                            }
                        });
                        // on index update element
                        this.onIndexEventUpdateElms.stream().forEach(f->{
                            synchronized (f.getOwnerDocument()) {
                                List<Element> docs = XMLUtils.getElements(addDocument);
                                for (int j = 0,ll=docs.size(); j < ll; j++) {
                                    Element doc = docs.get(j);
                                    Node node = f.cloneNode(true);
                                    doc.getOwnerDocument().adoptNode(node);
                                    doc.appendChild(node);
                                }
                            }
                        });
                        ReplicateFinisher.NEWINDEXED.addAndGet(XMLUtils.getElements(addDocument).size());
                        String s = SolrUtils.sendToDest(this.destinationUrl, this.client, batch);
                        LOGGER.info(s);

                    }

                    //
                    if (!pidsToReplicate.getAlreadyIndexed().isEmpty()) {
                        // un update
                        if (!this.onUpdateUpdateElements.isEmpty()) {

                            Document destBatch = XMLUtils.crateDocument("add");
                            pidsToReplicate.getAlreadyIndexed().stream().forEach(pair->{

                                Element doc = destBatch.createElement("doc");
                                Element field = destBatch.createElement("field");

                                if (compositeId) {
                                    String compositeId = pair.get("compositeId");

                                    String root = pair.get(transform.getField(rootOfComposite));
                                    String child = pair.get(transform.getField(childOfComposite));

                                    field.setAttribute("name", "compositeId");
                                    field.setTextContent(root +"!"+child);

                                } else {
                                    String idname = transform.getField(idIdentifier);
                                    String identifier = pair.get(idname);
                                    // if compositeid
                                    field.setAttribute("name", idname);
                                    // formal name from hashmap
                                    field.setTextContent(identifier);
                                }
                                doc.appendChild(field);
                                destBatch.getDocumentElement().appendChild(doc);

                            });

                            Element addDocument = destBatch.getDocumentElement();
                            this.onUpdateUpdateElements.stream().forEach(f->{
                                synchronized (f.getOwnerDocument()) {
                                    String name = f.getAttribute("name");
                                    // collection ?? not do it for everything... how to do that
                                    // iterating over doc
                                    List<Element> docs = XMLUtils.getElements(addDocument);
                                    for (int j = 0,ll=docs.size(); j < ll; j++) {
                                        Element doc = docs.get(j);
                                        Node node = f.cloneNode(true);
                                        doc.getOwnerDocument().adoptNode(node);
                                        doc.appendChild(node);

                                    }
                                }
                            });

                            List<Element> docs = XMLUtils.getElements(destBatch.getDocumentElement());
                            docs.stream().forEach(doc->{
                                List<Element> fields = XMLUtils.getElements(doc);
                                int size = fields.size();

                            });


                            ReplicateFinisher.UPDATED.addAndGet(XMLUtils.getElements(addDocument).size());
                            String s = SolrUtils.sendToDest(this.destinationUrl, this.client, destBatch);
                            LOGGER.info(s);
                        } else {
                            LOGGER.info("No update element ");
                        }
                    }

                } catch (ParserConfigurationException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                } catch (SAXException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
               } catch (MigrateSolrIndexException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                }
            }
        } finally {
            try {
                this.barrier.await();
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(),e);
            } catch (BrokenBarrierException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(),e);
            }
            LOGGER.info(String.format("Worker finished; All work for workers: %d; work in batches: %d; indexed: %d; updated %d, compositeIderror %d" ,  ReplicateFinisher.WORKERS.get(), ReplicateFinisher.BATCHES.get(), ReplicateFinisher.NEWINDEXED.get(), ReplicateFinisher.UPDATED.get(),ReplicateFinisher.NOT_INDEXED_COMPOSITEID.get()));
        }


    }

}
