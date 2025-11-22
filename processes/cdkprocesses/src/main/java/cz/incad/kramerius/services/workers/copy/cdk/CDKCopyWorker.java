package cz.incad.kramerius.services.workers.copy.cdk;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.utils.ResultsUtils;
import cz.incad.kramerius.services.workers.batch.CDKUpdateSolrBatchCreator;
import cz.incad.kramerius.services.workers.copy.cdk.model.CDKExistingConflictWorkerItem;
import cz.incad.kramerius.services.workers.copy.cdk.model.CDKWorkerIndexedItem;
import cz.incad.kramerius.utils.StringUtils;
import cz.inovatika.kramerius.services.config.ProcessConfig;
import cz.inovatika.kramerius.services.workers.WorkerFinisher;
import cz.inovatika.kramerius.services.iterators.IterationItem;
import cz.inovatika.kramerius.services.iterators.utils.KubernetesSolrUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.inovatika.kramerius.services.workers.copy.CopyWorker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CDKCopyWorker extends CopyWorker<CDKWorkerIndexedItem, CDKCopyContext> {

    private static final Logger LOGGER = Logger.getLogger(CDKCopyWorker.class.getName());

    public CDKCopyWorker(ProcessConfig processConfig, Client client, List<IterationItem> items, WorkerFinisher finisher) {
        super(processConfig, client, items, finisher);
    }

    @Override
    public void run() {
        try {
            int batchSize = processConfig.getWorkerConfig().getRequestConfig().getBatchSize();
            String onIndexedFieldList = processConfig.getWorkerConfig().getDestinationConfig().getOnIndexedFieldList();
            String fieldList = processConfig.getWorkerConfig().getRequestConfig().getFieldList();

            CDKCopyFinisher.WORKERS.addAndGet(this.itemsToBeProcessed.size());
            LOGGER.info("["+Thread.currentThread().getName()+"] processing list of items "+this.itemsToBeProcessed.size());
            int batches = this.itemsToBeProcessed.size() / batchSize + (this.itemsToBeProcessed.size() % batchSize == 0 ? 0 :1);
            LOGGER.info("["+Thread.currentThread().getName()+"] creating  "+batches+" batch ");
            for (int i=0;i<batches;i++) {
                int from = i*batchSize;
                int to = from + batchSize;
                try {
                	List<IterationItem> subitems = itemsToBeProcessed.subList(from, Math.min(to,itemsToBeProcessed.size() ));
                    CDKCopyFinisher.BATCHES.addAndGet(subitems.size());

                    // Creates replicate context - notindexed documents; indexed documents; conflicting documents
                    CDKCopyContext cdkReplicateContext = createContext(subitems);

                    /**
                     * Not indexed part; indexing full documents
                     */
                    if (!cdkReplicateContext.getNotIndexed().isEmpty()) {
                        
                        /** Indexing field list; full list of indexing document fields  */
                        String fl = onIndexedFieldList != null ? onIndexedFieldList : fieldList;

                        /** Fetching documents from remote library */
                        Element response = fetchDocumentFromRemoteSOLR( this.client,  cdkReplicateContext.getAlreadyIndexed().stream().map(CDKWorkerIndexedItem::getPid).collect(Collectors.toList()), fl);
                        Element resultElem = XMLUtils.findElement(response, (elm) -> {
                            return elm.getNodeName().equals("result");
                        });

                        // Create batch; no trasnformers
                        CDKUpdateSolrBatchCreator updateSolrBatch = new CDKUpdateSolrBatchCreator(cdkReplicateContext, processConfig,resultElem);
                        Document batch = updateSolrBatch.createBatchForInsert();

                        Element addDocument = batch.getDocumentElement();
                        // on index - remove element
                        onIndexRemoveEvent(addDocument);
                        // on index update element
                        onIndexUpdateEvent(addDocument);

                        CDKCopyFinisher.NEWINDEXED.addAndGet(XMLUtils.getElements(addDocument).size());
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
                            String fl = onUpdateFieldList;
                            /** Destinatination batch */
                            Document destBatch = null;
                            if (fl != null) {
                                /** already indexed pids */
                                List<String> pids = cdkReplicateContext.getAlreadyIndexed().stream().map(ir->{
                                    return ir.getPid();
                                }).collect(Collectors.toList());
                                /** Indexed records as map */
                                Map<String, CDKWorkerIndexedItem> alreadyIndexedAsMap = cdkReplicateContext.getAlreadyIndexedAsMap();
                                /** Fetch documents from source library */
                                Element response = fetchDocumentFromRemoteSOLR( this.client,  pids, fl);
                                Element resultElem = XMLUtils.findElement(response, (elm) -> {
                                    return elm.getNodeName().equals("result");
                                });

                                CDKUpdateSolrBatchCreator updateSolrBatch = new CDKUpdateSolrBatchCreator(cdkReplicateContext, processConfig,resultElem);
                            } else {
                                /** If there is no update list, then no update */
                                Document db = XMLUtils.crateDocument("add");
                                destBatch = db;
                        	}

                            if (destBatch != null) {
                                Element addDocument = destBatch.getDocumentElement();
                                onUpdateEvent(addDocument);
                                CDKCopyFinisher.UPDATED.addAndGet(XMLUtils.getElements(addDocument).size());
                                String s = KubernetesSolrUtils.sendToDest(config.getDestinationConfig().getDestinationUrl() , this.client, destBatch);
                            } else {
                                LOGGER.warning("No batch for update");
                            }
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
                } catch (ParserConfigurationException | SAXException | IOException e) {
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
            } catch (InterruptedException | BrokenBarrierException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(),e);
            }
            LOGGER.info(String.format("Worker finished; All work for workers: %d; work in batches: %d; indexed: %d; updated %d, compositeIderror %d" ,  CDKCopyFinisher.WORKERS.get(), CDKCopyFinisher.BATCHES.get(), CDKCopyFinisher.NEWINDEXED.get(), CDKCopyFinisher.UPDATED.get(), CDKCopyFinisher.NOT_INDEXED_COMPOSITEID.get()));
        }
    }

    @Override
    protected CDKCopyContext createContext(List<IterationItem> subitems) throws UnsupportedEncodingException {
        // vsechny pidy
        String reduce = subitems.stream().map(it -> {
            return '"' + it.getPid() + '"';
        }).collect(Collectors.joining(" OR "));

        String collectionField = this.config.getRequestConfig().getCollectionField();
        String checkUrlC = this.config.getRequestConfig().getCheckUrl();
        String checkEndpoint = this.config.getRequestConfig().getCheckEndpoint();
        boolean compositeId = this.config.getRequestConfig().isCompositeId();

        List<String> computedFields = Arrays.asList("cdk.licenses", "cdk.licenses_of_ancestors cdk.contains_licenses");
        String fieldlist = "pid " + collectionField +" cdk.leader cdk.collection "+ String.join(" ", computedFields);
        if (compositeId) {
            fieldlist = fieldlist + " " + " root.pid compositeId";
        }

        String query = "?q=" + "pid" + ":(" + URLEncoder.encode(reduce, StandardCharsets.UTF_8)
                + ")&fl=" + URLEncoder.encode(fieldlist, StandardCharsets.UTF_8) + "&wt=xml&rows=" + subitems.size();

        String checkUrl = checkUrlC + (checkUrlC.endsWith("/") ? "" : "/") + checkEndpoint;
        Element resultElem = XMLUtils.findElement(KubernetesSolrUtils.executeQueryJersey(client, checkUrl, query),
                (elm) -> {
                    return elm.getNodeName().equals("result");
                });

        List<Element> docElms = XMLUtils.getElements(resultElem);
        List<Map<String, Object>>  docs = docElms.stream().map(ResultsUtils::doc).collect(Collectors.toList());


        List<CDKWorkerIndexedItem> indexedRecordList = new ArrayList<>();
        // Existing conficts - remove from docElms
        List<CDKExistingConflictWorkerItem> econflicts = findIndexConflict(docs);
        removePids(econflicts, docs);

        List<String> econflictPids = econflicts.stream().map(CDKExistingConflictWorkerItem::getPid).toList();

        // found indexed & not indexed records
        docElms.forEach(d -> {
            Map<String, Object> map = ResultsUtils.doc(d);

            Element collection = XMLUtils.findElement(d, e -> {
                return e.getAttribute("name").equals(collectionField);
            });

            if (collection != null) {
                map.put(collectionField, collection.getTextContent());
            }

            // computed fields
            computedFields.forEach(it-> computedField(d, map,it));

            CDKWorkerIndexedItem record = new CDKWorkerIndexedItem((String)map.get("pid"), map);
            if (!econflictPids.contains(record.getPid())) {
                indexedRecordList.add(record);
            }
        });

        List<String> pidsFromLocalSolr = indexedRecordList.stream().map(CDKWorkerIndexedItem::getPid).toList();

        List<IterationItem> notindexed = new ArrayList<>();
        subitems.forEach(item -> {
            if (!pidsFromLocalSolr.contains(item.getPid()) && !econflictPids.contains(item.getPid()))
                notindexed.add(item);
        });

        return new CDKCopyContext(subitems, indexedRecordList, econflicts, notindexed);
    }


    private static void removePids(List<CDKExistingConflictWorkerItem> conflicts, List<Map<String, Object>> docs) {
        Set<String> pids = conflicts.stream()
                .map(CDKExistingConflictWorkerItem::getPid)
                .collect(Collectors.toSet());
        docs.removeIf(doc -> pids.contains(doc.get("pid")));
    }

    private List<CDKExistingConflictWorkerItem> findIndexConflict(List<Map<String,Object>> docs) {
        String childOfComposite = this.config.getRequestConfig().getChildOfComposite();

        Map<String, List<String>> pidToCompositeIds = docs.stream()
                .filter(map -> {
                    String pid = (String) map.get(childOfComposite);
                    String compositeId = (String) map.get("compositeId");
                    return StringUtils.isAnyString(pid) && StringUtils.isAnyString(compositeId);
                })
                .collect(Collectors.groupingBy(
                        map -> (String) map.get(childOfComposite),
                        Collectors.mapping(map -> (String) map.get("compositeId"), Collectors.toList())
                ));


        return pidToCompositeIds.entrySet().stream()
                .map(entry -> new CDKExistingConflictWorkerItem(entry.getKey(),
                        entry.getValue().stream().distinct().collect(Collectors.toList())))
                .filter(CDKExistingConflictWorkerItem::isConflict)
                .collect(Collectors.toList());
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
