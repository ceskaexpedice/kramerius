package cz.incad.kramerius.services.workers.replicate.copy;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.services.SupportedLibraries;
import cz.incad.kramerius.services.transform.K7SourceToDestTransform;
import cz.incad.kramerius.services.utils.ResultsUtils;
import cz.incad.kramerius.services.workers.replicate.records.ExistingConflictRecord;
import cz.incad.kramerius.services.workers.replicate.records.ReplicateRecord;
import cz.incad.kramerius.utils.StringUtils;
import cz.inovatika.kramerius.services.config.ProcessConfig;
import cz.inovatika.kramerius.services.workers.Worker;
import cz.inovatika.kramerius.services.workers.batch.BatchConsumer;
import cz.inovatika.kramerius.services.workers.WorkerFinisher;
import cz.inovatika.kramerius.services.iterators.IterationItem;
import cz.inovatika.kramerius.services.iterators.utils.KubernetesSolrUtils;
import cz.incad.kramerius.services.workers.replicate.*;
import cz.incad.kramerius.services.workers.replicate.records.IndexedRecord;
import cz.incad.kramerius.utils.XMLUtils;
import cz.inovatika.kramerius.services.workers.batch.BatchTransformation;
import cz.inovatika.kramerius.services.workers.config.WorkerConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URLEncoder;
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

public class CDKCopyWorker extends Worker {

    private static SupportedLibraries supportedLibraries = new SupportedLibraries();
    
    private static Logger LOGGER = Logger.getLogger(CDKCopyWorker.class.getName());

    public CDKCopyWorker(ProcessConfig processConfig, Client client, List<IterationItem> items, WorkerFinisher finisher) {
        super(processConfig, client, items, finisher);
    }

    @Override
    public void run() {
        try {
            int batchSize = processConfig.getWorkerConfig().getRequestConfig().getBatchSize();
            String onIndexedFieldList = processConfig.getWorkerConfig().getDestinationConfig().getOnIndexedFieldList();
            String fieldList = processConfig.getWorkerConfig().getRequestConfig().getFieldList();

            boolean compositeId = processConfig.getWorkerConfig().getRequestConfig().isCompositeId();
            String rootOfComposite = processConfig.getWorkerConfig().getRequestConfig().getRootOfComposite();
            String childOfComposite = processConfig.getWorkerConfig().getRequestConfig().getChildOfComposite();

            ReplicateFinisher.WORKERS.addAndGet(this.itemsToBeProcessed.size());
            LOGGER.info("["+Thread.currentThread().getName()+"] processing list of items "+this.itemsToBeProcessed.size());
            int batches = this.itemsToBeProcessed.size() / batchSize + (this.itemsToBeProcessed.size() % batchSize == 0 ? 0 :1);
            LOGGER.info("["+Thread.currentThread().getName()+"] creating  "+batches+" batch ");
            for (int i=0;i<batches;i++) {
                int from = i*batchSize;
                int to = from + batchSize;
                try {
                	List<IterationItem> subitems = itemsToBeProcessed.subList(from, Math.min(to,itemsToBeProcessed.size() ));
                    ReplicateFinisher.BATCHES.addAndGet(subitems.size());

                    // Creates replicate context - notindexed documents; indexed documents; conflicting documents
                    CDKReplicateContext cdkReplicateContext = createReplicateContext(subitems, new K7SourceToDestTransform());

                    /**
                     * Not indexed part; indexing full documents
                     */
                    if (!cdkReplicateContext.getNotIndexed().isEmpty()) {
                        
                        /** Indexing field list; full list of indexing document fields  */
                        String fl = onIndexedFieldList != null ? onIndexedFieldList : fieldList;

                        /** Fetching documents from remote library */
                        Element response = fetchDocsFromSourceSolr( this.client,  cdkReplicateContext.getNotIndexed().stream().map(IterationItem::getPid).collect(Collectors.toList()), fl);
                        Element resultElem = XMLUtils.findElement(response, (elm) -> {
                            return elm.getNodeName().equals("result");
                        });

                        // test conflict
                        Document batch = BatchUtils.batch(cdkReplicateContext, resultElem, compositeId, rootOfComposite, childOfComposite, new K7SourceToDestTransform(), new BatchConsumer() {

                            @Override
                            public ModifyFieldResult modifyField(Element field) {
                                return ModifyFieldResult.none;
                            }

                            @Override
                            public void changeDocument(ProcessConfig processConfig, Element doc) {

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
                                    cdkLicenses.setTextContent(CDKCopyWorker.this.processConfig.getSourceName()+"_"+ licOfAncestors);
                                    doc.appendChild(cdkLicenses);
                                }

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
                                    cdkLicenses.setTextContent(CDKCopyWorker.this.processConfig.getSourceName()+"_"+ licOfAncestors);
                                    doc.appendChild(cdkLicenses);
                                }

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
                                    cdkLicenses.setTextContent(CDKCopyWorker.this.processConfig.getSourceName()+"_"+ license);
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
                        String s = KubernetesSolrUtils.sendToDest(processConfig.getWorkerConfig().getDestinationConfig().getDestinationUrl(), this.client, batch);
                        LOGGER.info(s);
                    }

                    /**
                     * Already indexed part; indexing only part of documents -  licenses, authors, titles, ...
                     */
                    List<Element> onUpdateUpdateElements = config.getDestinationConfig().getOnUpdateUpdateElements();
                    String onUpdateFieldList =  config.getDestinationConfig().getOnUpdateFieldList();

                    if (!cdkReplicateContext.getAlreadyIndexed().isEmpty()) {
                        // On update elements must not be empty
                        if (!onUpdateUpdateElements.isEmpty()) {
                            /** Updating fields */
                            String fl = onUpdateFieldList != null ? onUpdateFieldList : null;
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
                                Element response = fetchDocsFromSourceSolr( this.client,  pids, fl);
                                Element resultElem = XMLUtils.findElement(response, (elm) -> {
                                    return elm.getNodeName().equals("result");
                                });
                                /** Construct final batch */
                                destBatch = BatchUtils.batch(cdkReplicateContext, resultElem, compositeId, rootOfComposite, childOfComposite, new K7SourceToDestTransform(), new BatchConsumer() {

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
                                            public void changeDocument(ProcessConfig processConfig, Element doc) {

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

                                                Map<String, Object> cdkDoc = alreadyIndexedAsMap.get(childOfComposite).getDocument();

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
                                                        return CDKCopyWorker.this.processConfig.getSourceName()+"_"+cnt;
                                                    }).collect(Collectors.toSet());


                                                    Set<String> indexedCDKLicenses = cdkDoc.get(specificCDKField) != null ?  new HashSet<String>((List<String>)cdkDoc.get(specificCDKField)) : new HashSet<>();
                                                    indexedCDKLicenses.removeIf(item -> !item.startsWith(CDKCopyWorker.this.processConfig.getSourceName() + "_"));
                                                    if (!indexedCDKLicenses.equals(newCDKValues)) {
                                                        List<String> newList = new ArrayList<String>( cdkDoc.get(specificCDKField)  != null ?  (List<String>)cdkDoc.get(specificCDKField) : new ArrayList<>() );
                                                        // remove everything what is prefixed
                                                        newList.removeIf(item -> item.startsWith(CDKCopyWorker.this.processConfig.getSourceName() + "_"));

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
                                destBatch = db;
                        	}

                            Element addDocument = destBatch.getDocumentElement();
                            onUpdateEvent(addDocument);
                            ReplicateFinisher.UPDATED.addAndGet(XMLUtils.getElements(addDocument).size());
                            String s = KubernetesSolrUtils.sendToDest(config.getDestinationConfig().getDestinationUrl() , this.client, destBatch);
                        } else {
                            // no update
                            LOGGER.info("No update element ");
                        }
                    }

                    /**  Reharvesting existing conflict */
                    if (!cdkReplicateContext.getExistingConflictRecords().isEmpty()) {
                        cdkReplicateContext.getExistingConflictRecords().forEach(existingConflictRecord -> {
                            existingConflictRecord.reharvestConflict(client, "-reharvest api-");
                        });
                    }

                    /** Reharvest new conflict */
                    if (cdkReplicateContext.getNewConflictRecords().isEmpty()) {
                        cdkReplicateContext.getNewConflictRecords().forEach(newConflictRecord -> {
                            newConflictRecord.reharvestConflict(client, "-reharvest api-");
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

    protected CDKReplicateContext createReplicateContext(List<IterationItem> subitems, BatchTransformation transform)  throws ParserConfigurationException, SAXException, IOException {

        // vsechny pidy
        String reduce = subitems.stream().map(it -> {
            return '"' + it.getPid() + '"';
        }).collect(Collectors.joining(" OR "));

        String collectionField = this.config.getRequestConfig().getCollectionField();
        String checkUrlC = this.config.getRequestConfig().getCheckUrl();
        String checkEndpoint = this.config.getRequestConfig().getCheckEndpoint();
        boolean compositeId = this.config.getRequestConfig().isCompositeId();

        List<String> computedFields = Arrays.asList("cdk.licenses", "cdk.licenses_of_ancestors cdk.contains_licenses");
        String fieldlist = "pid " + collectionField +" cdk.leader cdk.collection "+computedFields.stream().collect(Collectors.joining(" "));
        if (compositeId) {
            fieldlist = fieldlist + " " + " root.pid compositeId";
        }

        String query = "?q=" + "pid" + ":(" + URLEncoder.encode(reduce, "UTF-8")
                + ")&fl=" + URLEncoder.encode(fieldlist, "UTF-8") + "&wt=xml&rows=" + subitems.size();

        String checkUrl = checkUrlC + (checkUrlC.endsWith("/") ? "" : "/") + checkEndpoint;
        Element resultElem = XMLUtils.findElement(KubernetesSolrUtils.executeQueryJersey(client, checkUrl, query),
                (elm) -> {
                    return elm.getNodeName().equals("result");
                });

        List<Element> docElms = XMLUtils.getElements(resultElem);
        List<Map<String, Object>>  docs = docElms.stream().map(d -> {
            Map<String, Object> map = ResultsUtils.doc(d);
            return map;
        }).collect(Collectors.toList());


        List<IndexedRecord> indexedRecordList = new ArrayList<>();
        // Existing conficts - remove from docElms
        List<ExistingConflictRecord> econflicts = findIndexConflict(docs);
        removePids(econflicts, docs);

        List<String> econflictPids = econflicts.stream().map(ExistingConflictRecord::getPid).collect(Collectors.toList());

        // found indexed & not indexed records
        docElms.stream().forEach(d -> {
            Map<String, Object> map = ResultsUtils.doc(d);

            Element collection = XMLUtils.findElement(d, e -> {
                return e.getAttribute("name").equals(collectionField);
            });

            if (collection != null) {
                map.put(collectionField, collection.getTextContent());
            }

            // computed fields
            computedFields.stream().forEach(it-> computedField(d, map,it));

            IndexedRecord record = new IndexedRecord(map);
            if (!econflictPids.contains(record.getPid())) {
                indexedRecordList.add(record);
            }
        });

        List<String> pidsFromLocalSolr = indexedRecordList.stream().map(m -> {
            return m.getPid();
        }).collect(Collectors.toList());

        List<IterationItem> notindexed = new ArrayList<>();
        subitems.forEach(item -> {
            if (!pidsFromLocalSolr.contains(item.getPid()) && !econflictPids.contains(item.getPid()))
                notindexed.add(item);
        });

        return new CDKReplicateContext(indexedRecordList, econflicts, notindexed);
    }


    private static void removePids(List<? extends ReplicateRecord> conflicts, List<Map<String, Object>> docs) {
        Set<String> pids = conflicts.stream()
                .map(ReplicateRecord::getPid)
                .collect(Collectors.toSet());
        docs.removeIf(doc -> pids.contains(doc.get("pid")));
    }

    private List<ExistingConflictRecord> findIndexConflict(List<Map<String,Object>> docs) {
        String childOfComposite = this.config.getRequestConfig().getChildOfComposite();

        Map<String, List<String>> pidToCompositeIds = docs.stream()
                .filter(map -> {
                    String pid = (String) map.get(getTransform(this.config).getField(childOfComposite));
                    String compositeId = (String) map.get("compositeId");
                    return StringUtils.isAnyString(pid) && StringUtils.isAnyString(compositeId);
                })
                .collect(Collectors.groupingBy(
                        map -> (String) map.get(getTransform(this.config).getField(childOfComposite)),
                        Collectors.mapping(map -> (String) map.get("compositeId"), Collectors.toList())
                ));


        return pidToCompositeIds.entrySet().stream()
                .map(entry -> new ExistingConflictRecord(entry.getKey(),
                        entry.getValue().stream().distinct().collect(Collectors.toList())))
                .filter(ExistingConflictRecord::isConflict)
                .collect(Collectors.toList());
    }

    //TODO:  REMOVE
    private static BatchTransformation getTransform(WorkerConfig config) {
        String transform = config.getRequestConfig().getTransform();
        if (transform != null) {
            switch (transform.toLowerCase()) {
                case "copy": return null;
                case "k7": return new K7SourceToDestTransform();
                default: return null;
            }
        }
        return null;
    }


    private void computedField(Element d, Map<String, Object> map, String fieldName) {
        Element cdkLicenses = XMLUtils.findElement(d, e -> {
            return e.getAttribute("name").equals(fieldName);
        });

        if (cdkLicenses != null) {
            List<String> licenses = XMLUtils.getElements(cdkLicenses).stream().map(Element::getTextContent).collect(Collectors.toList());
            map.put(fieldName, licenses);
        }
    }


}
