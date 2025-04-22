package cz.incad.kramerius.services.workers.replicate.copy;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.services.SupportedLibraries;
import cz.incad.kramerius.services.WorkerFinisher;
import cz.incad.kramerius.services.iterators.IterationItem;
import cz.incad.kramerius.services.utils.KubernetesSolrUtils;
import cz.incad.kramerius.services.workers.replicate.*;
import cz.incad.kramerius.services.workers.replicate.records.IndexedRecord;
import cz.incad.kramerius.utils.XMLUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Replicate index; 1-1 copy + enhancing compositeID if there is solr cloud
 */
public class CopyReplicateWorker extends AbstractReplicateWorker {

    public static SupportedLibraries supportedLibraries = new SupportedLibraries();
    
    public static Logger LOGGER = Logger.getLogger(CopyReplicateWorker.class.getName());

    public CopyReplicateWorker(String sourceName, String introspectUrl, Element workerElm, Client client, List<IterationItem> pids, WorkerFinisher finisher) {
        super(sourceName, introspectUrl, workerElm, client, pids, finisher);
        config(workerElm);
    }


	@Override
    public void run() {
        try {
            ReplicateFinisher.WORKERS.addAndGet(this.itemsToBeProcessed.size());
            LOGGER.info("["+Thread.currentThread().getName()+"] processing list of pids "+this.itemsToBeProcessed.size());
            int batches = this.itemsToBeProcessed.size() / batchSize + (this.itemsToBeProcessed.size() % batchSize == 0 ? 0 :1);
            LOGGER.info("["+Thread.currentThread().getName()+"] creating  "+batches+" batch ");
            for (int i=0;i<batches;i++) {
                int from = i*batchSize;
                int to = from + batchSize;
                try {
                	List<IterationItem> subitems = itemsToBeProcessed.subList(from, Math.min(to,itemsToBeProcessed.size() ));
                    ReplicateFinisher.BATCHES.addAndGet(subitems.size());

                    // Creates replicate context - notindexed documents; indexed documents; conflicting documents
                    CDKReplicateContext cdkReplicateContext = createReplicateContext(subitems, this.transform);

                    /**
                     * Not indexed part; indexing full documents
                     */
                    if (!cdkReplicateContext.getNotIndexed().isEmpty()) {
                        
                        /** Indexing field list; full list of indexing document fields  */
                        String fl = this.onIndexedFieldList != null ? this.onIndexedFieldList : this.fieldList;

                        /** Fetching documents from remote library */
                        Element response = fetchDocumentFromRemoteSOLR( this.client,  cdkReplicateContext.getNotIndexed().stream().map(IterationItem::getPid).collect(Collectors.toList()), fl);
                        Element resultElem = XMLUtils.findElement(response, (elm) -> {
                            return elm.getNodeName().equals("result");
                        });

                        // test conflict
                        Document batch = BatchUtils.batch(cdkReplicateContext, resultElem, this.compositeId, this.rootOfComposite, this.childOfComposite, this.transform, new CopyReplicateConsumer() {

                            @Override
                            public ModifyFieldResult modifyField(Element field) {
                                return ModifyFieldResult.none;
                            }


                            @Override
                            public void changeDocument(String rootPid, String pid,Element doc) {

                                // --- Indexed field modification ---
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
                                // ----

                                //--- License of ancestors; preparing data for cdk.licenses_of_ancestors ---
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
                                // ----

                                //--- contains_licenses; preparing data for cdk.contains_licenses ---
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
                                // ----



                                //--- Licenses; preparing data for cdk.licenses ---
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
                                // ----

                            }
                        });

                        Element addDocument = batch.getDocumentElement();
                        // on index - remove element
                        onIndexRemoveEvent(addDocument);
                        // on index update element
                        onIndexUpdateEvent(addDocument);

                        ReplicateFinisher.NEWINDEXED.addAndGet(XMLUtils.getElements(addDocument).size());
                        String s = KubernetesSolrUtils.sendToDest(this.destinationUrl, this.client, batch);
                        LOGGER.info(s);
                    }

                    /**
                     * Already indexed part; indexing only part of documents -  licenses, authors, titles, ...
                     */
                    if (!cdkReplicateContext.getAlreadyIndexed().isEmpty()) {
                        // On update elements must not be empty
                        if (!this.onUpdateUpdateElements.isEmpty()) {
                            /** Updating fields */
                            String fl = this.onUpdateFieldList != null ? this.onUpdateFieldList : null;
                            /** Destinatination batch */
                            Document destBatch = null;
                            if (fl != null) {
                                /** already indexed pids */
                                List<String> pids = cdkReplicateContext.getAlreadyIndexed().stream().map(ir->{
                                    String string = ir.getPid();
                                    return string;
                                }).collect(Collectors.toList());
                                /** Indexed records as map */
                                Map<String, IndexedRecord> alreadyIndexedAsMap = cdkReplicateContext.getAlreadyIndexedAsMap();
                                /** Fetch documents from source library */
                                Element response = fetchDocumentFromRemoteSOLR( this.client,  pids, fl);
                                Element resultElem = XMLUtils.findElement(response, (elm) -> {
                                    return elm.getNodeName().equals("result");
                                });
                                /** Construct final batch */
                                destBatch = BatchUtils.batch(cdkReplicateContext, resultElem, this.compositeId, this.rootOfComposite, this.childOfComposite, this.transform, new CopyReplicateConsumer() {

                                            /**
                                             * Fields mofidication method; allows modification after
                                             * @param field
                                             * @return
                                             */
                                            @Override
                                            public ModifyFieldResult modifyField(Element field) {
                                                /** Delete fields */
                                                List<String> deleteFields = Arrays.asList();
                                                /** Adding fields */
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

                                                Instant instant = new Date().toInstant();
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

                                                Map<String, Object> cdkDoc = alreadyIndexedAsMap.get(pid).getDocument();

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
                                /** If there is no update list, then no update */
                                Document db = XMLUtils.crateDocument("add");
//                                replicateContext.getAlreadyIndexed().stream().forEach(ir->{
//                                    Element doc = db.createElement("doc");
//                                    Element field = db.createElement("field");
//                                    if (compositeId) {
//                                        //String compositeId = pair.get("compositeId").toString();
//
//                                        /*
//                                        String root = pair.get(transform.getField(rootOfComposite)).toString();
//                                        String child = pair.get(transform.getField(childOfComposite)).toString();
//                                         */
//
//                                        field.setAttribute("name", "compositeId");
//                                        field.setTextContent(root +"!"+child);
//
//                                    } else {
//
//                                        String idname = transform.getField(idIdentifier);
//                                        String identifier = pair.get(idname).toString();
//                                        // if compositeid
//                                        field.setAttribute("name", idname);
//                                        // formal name from hashmap
//                                        field.setTextContent(identifier);
//                                    }
//                                    doc.appendChild(field);
//                                    db.getDocumentElement().appendChild(doc);
//                                });
                                destBatch = db;
                        	}


                            Element addDocument = destBatch.getDocumentElement();
                            onUpdateEvent(addDocument);
                            ReplicateFinisher.UPDATED.addAndGet(XMLUtils.getElements(addDocument).size());
                            String s = KubernetesSolrUtils.sendToDest(this.destinationUrl, this.client, destBatch);
                        } else {
                            // no update
                            LOGGER.info("No update element ");
                        }
                    }

                    /**  Reharvesting existing conflict */
                    if (!cdkReplicateContext.getExistingConflictRecords().isEmpty()) {
                        cdkReplicateContext.getExistingConflictRecords().forEach(existingConflictRecord -> {
                            existingConflictRecord.reharvestConflict(client, reharvestApi);
                        });
                    }

                    /** Reharvest new conflict */
                    if (cdkReplicateContext.getNewConflictRecords().isEmpty()) {
                        cdkReplicateContext.getNewConflictRecords().forEach(newConflictRecord -> {
                            newConflictRecord.reharvestConflict(client, reharvestApi);
                        });
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

    /**
     * Handles a field removal event during the indexing process.
     * <p>
     * This method is triggered during indexing when certain fields should be excluded
     * from the final indexing batch. It iterates over a predefined list of field definitions
     * (stored in {@code onIndexEventRemoveElms}) and removes matching fields from the given
     * {@code addDocument} element.
     * <p>
     * For each field to be removed, it compares its name with field elements inside the document.
     * If a match is found, that field is removed from the respective <doc> element.
     *
     * @param addDocument The XML element representing the indexing batch,
     *                    typically containing one or more <doc> elements with fields to be processed.
     */
    private void onIndexRemoveEvent(Element addDocument) {
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


    /**
     * Handles a field addition event during the indexing process.
     * <p>
     * This method is triggered during indexing to inject additional fields into each <doc> element
     * in the current indexing batch. The fields to be added are pre-defined in the list
     * {@code onIndexEventUpdateElms}, typically representing fixed or dynamically generated metadata.
     *
     * @param addDocument The XML element representing the indexing batch,
     *                    which contains one or more <doc> elements to be enriched with new fields.
     */
    private void onIndexUpdateEvent(Element addDocument) {
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
