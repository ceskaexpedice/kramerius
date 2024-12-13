package cz.incad.kramerius.services.workers.replicate.copy;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.services.SupportedLibraries;
import cz.incad.kramerius.services.Worker;
import cz.incad.kramerius.services.WorkerFinisher;
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
import java.io.StringWriter;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Replicate index; 1-1 copy + enhancing compositeID if there is solr cloud
 */
public class CopyReplicateWorker extends AbstractReplicateWorker {

    public static SupportedLibraries supportedLibraries = new SupportedLibraries();
    
    public static Logger LOGGER = Logger.getLogger(CopyReplicateWorker.class.getName());

    public CopyReplicateWorker(String sourceName, Element workerElm, Client client, List<IterationItem> pids, WorkerFinisher finisher) {
        super(sourceName, workerElm, client, pids, finisher);
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

                                List<Element> indexed = XMLUtils.getElements(doc, new XMLUtils.ElementsFilter() {
                                    @Override
                                    public boolean acceptElement(Element element) {
                                        String attribute = element.getAttribute("name");
                                        return "indexed".equals(attribute);
                                    }
                                }).stream().collect(Collectors.toList());
                                

                                if (indexed.size() > 0) {
                                  Instant instant = new Date().toInstant();
                                  indexed.get(0).setTextContent(DateTimeFormatter.ISO_INSTANT.format(instant));
                                }
                                
                                List<String> licensesOfAncestors = XMLUtils.getElements(doc, new XMLUtils.ElementsFilter() {
                                    
                                    @Override
                                    public boolean acceptElement(Element element) {
                                        String attribute = element.getAttribute("name");
                                        return "licenses_of_ancestors".equals(attribute);
                                    }
                                }).stream().map(Element::getTextContent).collect(Collectors.toList());
                                
                                for (String licOfAncestors : licensesOfAncestors) {
                                    Document document = doc.getOwnerDocument();
                                    Element cdkLicenses = document.createElement("field");
                                    cdkLicenses.setAttribute("name", "cdk.licenses_of_ancestors");
                                    cdkLicenses.setTextContent(sourceName+"_"+ licOfAncestors);
                                    doc.appendChild(cdkLicenses);
                                }

                                
                                List<String> containsLicenses = XMLUtils.getElements(doc, new XMLUtils.ElementsFilter() {
                                    
                                    @Override
                                    public boolean acceptElement(Element element) {
                                        String attribute = element.getAttribute("name");
                                        return "contains_licenses".equals(attribute);
                                    }
                                }).stream().map(Element::getTextContent).collect(Collectors.toList());
                                
                                for (String licOfAncestors : containsLicenses) {
                                    Document document = doc.getOwnerDocument();
                                    Element cdkLicenses = document.createElement("field");
                                    cdkLicenses.setAttribute("name", "cdk.contains_licenses");
                                    cdkLicenses.setTextContent(sourceName+"_"+ licOfAncestors);
                                    doc.appendChild(cdkLicenses);
                                }
                                
                                
                                
                                List<String> licenses = XMLUtils.getElements(doc, new XMLUtils.ElementsFilter() {
                                    
                                    @Override
                                    public boolean acceptElement(Element element) {
                                        String attribute = element.getAttribute("name");
                                        return "licenses".equals(attribute);
                                    }
                                }).stream().map(Element::getTextContent).collect(Collectors.toList());
                                for (String license : licenses) {
                                    Document document = doc.getOwnerDocument();
                                    Element cdkLicenses = document.createElement("field");
                                    cdkLicenses.setAttribute("name", "cdk.licenses");
                                    cdkLicenses.setTextContent(sourceName+"_"+ license);
                                    doc.appendChild(cdkLicenses);
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
                                        List<String> addValues = Arrays.asList(
                                                "licenses",
                                                "licenses_of_ancestors",
                                                "contains_licenses",
                                                
                                                "in_collections",
                                                "in_collections.direct",

                                                "titles.search",
                                                "authors",
                                                "authors.search",
                                                "authors.facet",
                                                
                                                "cdk.k5.license.translated",
                                                "cdk.licenses");
                                        
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
                                        // setreseni dat cdk.licenses 
                                        
                                        //<field name="indexed" update="set">2023-06-26T12:56:07.400Z</field>
                                        

                                        Instant instant = new Date().toInstant();
                                        //DateTimeFormatter.ISO_INSTANT.format(instant);
                                        Element fieldDate = doc.getOwnerDocument().createElement("field");
                                        fieldDate.setAttribute("name", "indexed");
                                        fieldDate.setAttribute("update", "set");
                                        fieldDate.setTextContent(DateTimeFormatter.ISO_INSTANT.format(instant));
                                        doc.appendChild(fieldDate);
                                        
                                        
                                        List<Pair<String,String>> comparingFields = Arrays.asList(
                                            Pair.of("licenses", "cdk.licenses"),
                                            Pair.of("contains_licenses", "cdk.contains_licenses"),
                                            Pair.of("licenses_of_ancestors", "cdk.licenses_of_ancestors")
                                        );
                                        
                                        //System.out.println("Docs for pid "+pid);
                                        Map<String, Object> cdkDoc = docs.get(pid);
                                        for (Pair<String,String> cpField : comparingFields) {
                                            
                                            String sourceField = cpField.getLeft();
                                            String specificCDKField = cpField.getRight();
                                            List<Element> newIndexedField = XMLUtils.getElements(doc, new XMLUtils.ElementsFilter() {
                                                @Override
                                                public boolean acceptElement(Element element) {
                                                    String attribute = element.getAttribute("name");
                                                    return sourceField.equals(attribute);
                                                }
                                            });
                                            
                                            Set<String> newCDKValues = new HashSet<>();
                                            newCDKValues =  newIndexedField.stream().map(Element::getTextContent).map(cnt-> {
                                                return sourceName+"_"+cnt;
                                            }).collect(Collectors.toSet());
                                            
                                            
                                            Set<String> indexedCDKLicenses = cdkDoc.get(specificCDKField) != null ?  new HashSet<String>((List<String>)cdkDoc.get(specificCDKField)) : new HashSet<>();
                                            indexedCDKLicenses.removeIf(item -> !item.startsWith(sourceName + "_"));
                                            if (!indexedCDKLicenses.equals(newCDKValues)) {
                                                List<String> newList = new ArrayList<String>( cdkDoc.get(specificCDKField)  != null ?  (List<String>)cdkDoc.get(specificCDKField) : new ArrayList<>() );
                                                // remove everything what is prefixed 
                                                newList.removeIf(item -> item.startsWith(sourceName + "_"));
                                                
                                                // add new indexed values
                                                newList.addAll(newCDKValues);

                                                
                                                for (Element nIF : newIndexedField) {
                                                    doc.removeChild(nIF);
                                                }

                                                Set<String> tempSet = new HashSet<>();
                                                Document document = doc.getOwnerDocument();
                                                if (newList.size() > 0) {
                                                    newList.stream().forEach(lic-> {

                                                        Element cdkSpecific = document.createElement("field");
                                                        cdkSpecific.setAttribute("name", specificCDKField);
                                                        cdkSpecific.setAttribute("update", "set");
                                                        cdkSpecific.setTextContent(lic);
                                                        doc.appendChild(cdkSpecific);

                                                        Pair<String, String> divided = supportedLibraries.divideLibraryAndLicense(lic);
                                                        if (divided != null) {
                                                            
                                                            String rv = divided.getRight();
                                                            if (!tempSet.contains(rv)) {
                                                                Element changedField = document.createElement("field");
                                                                changedField.setAttribute("name", sourceField);
                                                                changedField.setAttribute("update", "set");
                                                                
                                                                changedField.setTextContent(rv);
                                                                doc.appendChild(changedField);
                                                                
                                                                tempSet.add(rv);
                                                            }
                                                        }
                                                    });
                                                    
                                                } else {
                                                    

                                                    Element cdkSpecific = document.createElement("field");
                                                    cdkSpecific.setAttribute("name", specificCDKField);
                                                    cdkSpecific.setAttribute("update", "set");
                                                    cdkSpecific.setAttribute("null", "true");
                                                    doc.appendChild(cdkSpecific);
 
                                                    Element changedField = document.createElement("field");
                                                    changedField.setAttribute("name", sourceField);
                                                    changedField.setAttribute("update", "set");
                                                    changedField.setAttribute("null", "true");
                                                    doc.appendChild(changedField);

                                                }
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
                                        //String compositeId = pair.get("compositeId").toString();

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
                            //LOGGER.info(s);
                        } else {
                            LOGGER.info("No update element ");
                        }
                    }

                } catch (ParserConfigurationException e) {
                    LOGGER.log(Level.SEVERE,"Informing about exception");
                    finisher.exceptionDuringCrawl(e);
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                } catch (SAXException e) {
                    LOGGER.log(Level.SEVERE,"Informing about exception");
                    finisher.exceptionDuringCrawl(e);
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE,"Informing about exception");
                    finisher.exceptionDuringCrawl(e);
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
               } catch (MigrateSolrIndexException e) {
                    LOGGER.log(Level.SEVERE,"Informing about exception");
                    finisher.exceptionDuringCrawl(e);
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                }
            }
        } catch(Exception ex) {
            LOGGER.log(Level.SEVERE,"Informing about exception");
            finisher.exceptionDuringCrawl(ex);
            LOGGER.log(Level.SEVERE,ex.getMessage(),ex);
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
