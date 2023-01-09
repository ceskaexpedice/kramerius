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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Replicate index; 1-1 copy + enhancing compositeID if there is solr cloud
 */
public class CopyReplicateWorker extends AbstractReplicateWorker {

    public static Logger LOGGER = Logger.getLogger(CopyReplicateWorker.class.getName());

    public CopyReplicateWorker(String sourceName, Element workerElm, Client client, List<IterationItem> pids) {
        super(sourceName, workerElm, client, pids);
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
                    // rozdeleni na indexovane a neindexovane
                    ReplicateContext pidsToReplicate = findPidsAlreadyIndexed(subpids, this.transform);
                    
                    if (!pidsToReplicate.getNotIndexed().isEmpty()) {
                        
                        // ziskat dokumenty a vytvorit davku
                        String fl = this.onIndexedFieldList != null ? this.onIndexedFieldList : this.fieldList;
                        Document batch = retrieveAndCretebatch(pidsToReplicate.getNotIndexed(), fl, new CopyReplicateConsumer() {
                            
                            @Override
                            public ModifyFieldResult modifyField(Element field) {
                                return ModifyFieldResult.none;
                            }
                            
                            
                            @Override
                            public void changeDocument(String rootPid, String pid,Element doc) {
                                List<Element> foundElements = XMLUtils.getElements(doc, new XMLUtils.ElementsFilter() {
                                    
                                    @Override
                                    public boolean acceptElement(Element element) {
                                        String attribute = element.getAttribute("name");
                                        return "licenses".equals(attribute);
                                    }
                                });
                                
                                if (!foundElements.isEmpty()) {
                                    for (Element fElm : foundElements) {
                                        Document document = fElm.getOwnerDocument();
                                        Element cdkLicenses = document.createElement("field");
                                        cdkLicenses.setAttribute("name", "cdk.licenses");
                                        cdkLicenses.setTextContent(sourceName+"_"+ fElm.getTextContent());
                                        
                                        doc.appendChild(cdkLicenses);
                                    }
                                }
                            }
                        });

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
                        onIndexEvent(addDocument);

                        ReplicateFinisher.NEWINDEXED.addAndGet(XMLUtils.getElements(addDocument).size());
                        String s = SolrUtils.sendToDest(this.destinationUrl, this.client, batch);
                        LOGGER.info(s);
                    }

                    // Jiz indexovane pidy 
                    if (!pidsToReplicate.getAlreadyIndexed().isEmpty()) {
                        // un update udalost
                        if (!this.onUpdateUpdateElements.isEmpty()) {
                            // pokud je definovany fieldlist pak musime ziskat data ze zdrojoveho krameria
                            String fl = this.onUpdateFieldList != null ? this.onUpdateFieldList : null;
                            Document destBatch = null;
                            if (fl != null) {
                                // indexovane pidy & a mapovani pid <=> dokument
                                List<String> pids = pidsToReplicate.getAlreadyIndexed().stream().map(pair->{
                                    String string = pair.get("pid").toString();
                                    return string;
                                }).collect(Collectors.toList());
                                Map<String,Map<String, Object>> docs = pidsToReplicate.getAlreadyIndexedAsMap();

                                destBatch =  retrieveAndCretebatch(pids, fl, new CopyReplicateConsumer() {
                                    
                                    /**
                                     * Modifikovani pole  
                                     * @param field
                                     * @return
                                     */
                                    @Override
                                    public ModifyFieldResult modifyField(Element field) {

                                        List<String> deleteFields = Arrays.asList();
                                        List<String> addValues = Arrays.asList("licenses","licenses_of_ancestors","contains_licenses");
                                        
                                        String name = field.getAttribute("name");
                                        if (deleteFields.contains(name)) {
                                            return ModifyFieldResult.delete;
                                        }
                                        else {
                                            // pridavani poli
                                            if (addValues.contains(name)) {
                                                field.setAttribute("update", "add-distinct");
                                            } else {
                                                field.setAttribute("update", "set");
                                            }
                                            return ModifyFieldResult.edit;
                                        }
                                        
                                    }
                                    
                                    @Override
                                    public void changeDocument(String root, String pid, Element doc) {
                                        
                                        // cdk dokument
                                        Map<String, Object> cdkDoc = docs.get(pid);
                                        
                                        List<Element> newIndexedLicenses = XMLUtils.getElements(doc, new XMLUtils.ElementsFilter() {
                                            
                                            @Override
                                            public boolean acceptElement(Element element) {
                                                String attribute = element.getAttribute("name");
                                                return "licenses".equals(attribute);
                                            }
                                        });
                                        

                                        List<String> newCDKLicenses = new ArrayList<>();
                                        newCDKLicenses =  newIndexedLicenses.stream().map(Element::getTextContent).map(lic-> {
                                            return sourceName+"_"+lic;
                                        }).collect(Collectors.toList());
                                        
                                        List<String> indexedCDKLicenses = (List<String>) cdkDoc.get("cdk.licenses");
                                        if (indexedCDKLicenses == null) {
                                            indexedCDKLicenses = new ArrayList<>();
                                        }

                                        List<String> toRemoveCDKLicenses = new ArrayList<>(indexedCDKLicenses);
                                        toRemoveCDKLicenses.removeAll(newCDKLicenses);
                                        
                                        if (!newCDKLicenses.isEmpty()) {
                                            for (Element fElm : newIndexedLicenses) {
                                                Document document = fElm.getOwnerDocument();
                                                Element cdkLicenses = document.createElement("field");
                                                cdkLicenses.setAttribute("name", "cdk.licenses");
                                                cdkLicenses.setTextContent(sourceName+"_"+fElm.getTextContent());
                                                
                                                cdkLicenses.setAttribute("update", "add-distinct");
                                                doc.appendChild(cdkLicenses);
                                            }
                                        }
                                        
                                        if (toRemoveCDKLicenses!=null && toRemoveCDKLicenses.size() > 0) {
                                            for (String remove : toRemoveCDKLicenses) {
                                                Document document = doc.getOwnerDocument();
                                                Element cdkLicenses = document.createElement("field");
                                                cdkLicenses.setAttribute("name", "cdk.licenses");
                                                cdkLicenses.setTextContent(sourceName+"_"+remove);
                                                cdkLicenses.setAttribute("update", "remove");
                                                doc.appendChild(cdkLicenses);
                                            }
                                        }
                                        
                                    }
                                });
                            } else {
                                Document db = XMLUtils.crateDocument("add");
                                pidsToReplicate.getAlreadyIndexed().stream().forEach(pair->{
                                    Element doc = db.createElement("doc");
                                    Element field = db.createElement("field");
                                    if (compositeId) {
                                        String compositeId = pair.get("compositeId").toString();

                                        String root = pair.get(transform.getField(rootOfComposite)).toString();
                                        String child = pair.get(transform.getField(childOfComposite)).toString();

                                        field.setAttribute("name", "compositeId");
                                        field.setTextContent(root +"!"+child);

                                    } else {
                                        
                                        String idname = transform.getField(idIdentifier);
                                        String identifier = pair.get(idname).toString();
                                        // if compositeid
                                        field.setAttribute("name", idname);
                                        // formal name from hashmap
                                        field.setTextContent(identifier);
                                    }
                                    doc.appendChild(field);
                                    db.getDocumentElement().appendChild(doc);
                                });
                                destBatch = db;
                        	}
                        	

                            Element addDocument = destBatch.getDocumentElement();
                            onUpdateEvent(addDocument);
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
	
	
	private Document retrieveAndCretebatch(List<String> pids, String fl, CopyReplicateConsumer consumer)
			throws IOException, SAXException, ParserConfigurationException, MigrateSolrIndexException {
		
		Element response = fetchDocumentFromRemoteSOLR( this.client,  pids, fl);
		Element resultElem = XMLUtils.findElement(response, (elm) -> {
		    return elm.getNodeName().equals("result");
		});

		// create batch
		Document batch = BatchUtils.batch(resultElem, this.compositeId, this.rootOfComposite, this.childOfComposite, this.transform, consumer);
		return batch;
	}

	// on update event
	private void onUpdateEvent(Element addDocument) {
		this.onUpdateUpdateElements.stream().forEach(f->{
		    synchronized (f.getOwnerDocument()) {
		        String name = f.getAttribute("name");
		        List<Element> docs = XMLUtils.getElements(addDocument);
		        for (int j = 0,ll=docs.size(); j < ll; j++) {
		            Element doc = docs.get(j);
		            Node node = f.cloneNode(true);
		            doc.getOwnerDocument().adoptNode(node);
		            doc.appendChild(node);
		        }
		    }
		});
	}

	
	// on index event
	private void onIndexEvent(Element addDocument) {
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
	}

}
