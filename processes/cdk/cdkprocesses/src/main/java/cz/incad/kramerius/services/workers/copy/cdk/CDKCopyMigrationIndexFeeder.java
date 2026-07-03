package cz.incad.kramerius.services.workers.copy.cdk;

import cz.incad.kramerius.services.utils.ResultsUtils;
import cz.incad.kramerius.services.workers.batch.CDKUpdateSolrBatchCreator;
import cz.incad.kramerius.services.workers.copy.cdk.model.CDKExistingConflictFeederItem;
import cz.incad.kramerius.services.workers.copy.cdk.model.CDKWorkerIndexedItem;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.kramerius.services.config.MigrationConfig;
import cz.inovatika.kramerius.services.iterators.ApacheHTTPRequestEnricher;
import cz.inovatika.kramerius.services.workers.MigrationIndexFeederFinisher;
import cz.inovatika.kramerius.services.iterators.IterationItem;
import cz.inovatika.kramerius.services.iterators.utils.HTTPSolrUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.inovatika.kramerius.services.workers.copy.CopyMigrationIndexFeeder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CDKCopyMigrationIndexFeeder extends CopyMigrationIndexFeeder<CDKWorkerIndexedItem, CDKCopyContext> {

    private static final Logger LOGGER = Logger.getLogger(CDKCopyMigrationIndexFeeder.class.getName());

    public CDKCopyMigrationIndexFeeder(MigrationConfig migrationConfig, CloseableHttpClient client, ApacheHTTPRequestEnricher enricher, List<IterationItem> items, MigrationIndexFeederFinisher finisher) {
        super(migrationConfig, client, enricher, items, finisher);
    }


    @Override
    public void process() {
        try {
            int batchSize = migrationConfig.getFeederConfig().getRequestConfig().getBatchSize();
            String onIndexedFieldList = migrationConfig.getFeederConfig().getDestinationConfig().getOnIndexedFieldList();
            String fieldList = migrationConfig.getFeederConfig().getRequestConfig().getFieldList();

            CDKCopyFinisher.FEEDERS.addAndGet(this.itemsToBeProcessed.size());
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
                        Element response = fetchDocumentFromRemoteSOLR( this.client,  cdkReplicateContext.getNotIndexed().stream().map(IterationItem::getPid).collect(Collectors.toList()), fl);
                        Element resultElem = XMLUtils.findElement(response, (elm) -> {
                            return elm.getNodeName().equals("result");
                        });

                        // Create batch; no trasnformers
                        CDKUpdateSolrBatchCreator updateSolrBatch = new CDKUpdateSolrBatchCreator(cdkReplicateContext, migrationConfig,resultElem);
                        Document batch = updateSolrBatch.createBatchForInsert();

                        Element addDocument = batch.getDocumentElement();
                        // on index - remove element
                        onIndexRemoveEvent(addDocument);
                        // on index update element
                        onIndexUpdateEvent(addDocument);

                        CDKCopyFinisher.NEWINDEXED.addAndGet(XMLUtils.getElements(addDocument).size());
                        String s = HTTPSolrUtils.sendToDest(migrationConfig.getFeederConfig().getDestinationConfig().getDestinationUrl(), this.client, batch);
                        LOGGER.fine(s);
                    }

                    /**
                     * Already indexed part; indexing only part of documents -  licenses, authors, titles, ...
                     */
                    List<Element> onUpdateUpdateElements = config.getDestinationConfig().getOnUpdateUpdateElements();
                    String onUpdateFieldList =  config.getDestinationConfig().getOnUpdateFieldList();

                    if (!cdkReplicateContext.getAlreadyIndexed().isEmpty()) {
                        if (!onUpdateUpdateElements.isEmpty() || StringUtils.isAnyString(onUpdateFieldList)) {
                            /** Updating fields */
                            String fl = onUpdateFieldList;
                            /** Destinatination batch */
                            Document destBatch = null;
                            if (fl != null) {
                                /** already indexed pids */
                                List<String> pids = cdkReplicateContext.getAlreadyIndexed().stream().map(ir->{
                                    return ir.getPid();
                                }).collect(Collectors.toList());
                                /** Fetch documents from source library */
                                Element response = fetchDocumentFromRemoteSOLR( this.client,  pids, fl);
                                Element resultElem = XMLUtils.findElement(response, (elm) -> {
                                    return elm.getNodeName().equals("result");
                                });

                                CDKUpdateSolrBatchCreator updateSolrBatch = new CDKUpdateSolrBatchCreator(cdkReplicateContext, migrationConfig,resultElem);
                                destBatch = updateSolrBatch.createBatchForUpdate();
                            } else {
                                /** If there is no update list, then no update */
                                Document db = XMLUtils.crateDocument("add");
                                destBatch = db;
                        	}

                            if (destBatch != null) {
                                Element addDocument = destBatch.getDocumentElement();
                                onUpdateEvent(addDocument);
                                CDKCopyFinisher.UPDATED.addAndGet(XMLUtils.getElements(addDocument).size());
                                String s = HTTPSolrUtils.sendToDest(config.getDestinationConfig().getDestinationUrl() , this.client, destBatch);
                                LOGGER.fine(s);
                            } else {
                                LOGGER.warning("No batch for update");
                            }
                        } else {
                            // no update
                            LOGGER.info("No update element ");
                        }
                    }
                    ///admin/v7.0/reharvest
                    String reharvestApi = KConfiguration.getInstance().getConfiguration().getString("cdk.api.reharvest.point");

                    /**  Reharvesting existing conflict */
                    if (!cdkReplicateContext.getExistingConflictRecords().isEmpty()) {

                        cdkReplicateContext.getExistingConflictRecords().forEach(existingConflictRecord -> {
                            try {
                                existingConflictRecord.reharvestConflict(client, reharvestApi);
                            } catch (Exception e) {
                                LOGGER.log(Level.SEVERE, "Reharvest failed for " + existingConflictRecord.getPid(), e);
                            }
                        });
                    }

                    /** Reharvest new conflict */
                    if (!cdkReplicateContext.getNewConflictRecords().isEmpty()) {
                        cdkReplicateContext.getNewConflictRecords().forEach(newConflictRecord -> {
                            try {
                                newConflictRecord.reharvestConflict(client, reharvestApi);
                            } catch (Exception e) {
                                LOGGER.log(Level.SEVERE, "Reharvest failed for " + newConflictRecord.getPid(), e);
                            }
                        });
                    }
                } catch (ParserConfigurationException | SAXException | IOException e) {
                    abortHarvest("CDK feeder batch failed; aborting harvest", e);
                }
            }
        } catch(Exception ex) {
            if (ex instanceof HarvestAbortedException) {
                throw (HarvestAbortedException) ex;
            }
            abortHarvest("CDK feeder failed; aborting harvest", ex);
        } finally {
//            try {
//                this.barrier.await();
//            } catch (InterruptedException | BrokenBarrierException e) {
//                LOGGER.log(Level.SEVERE, e.getMessage(),e);
//            }
            LOGGER.info(String.format("Feeder finished; All work for feeders: %d; work in batches: %d; indexed: %d; updated %d, compositeIderror %d" ,  CDKCopyFinisher.FEEDERS.get(), CDKCopyFinisher.BATCHES.get(), CDKCopyFinisher.NEWINDEXED.get(), CDKCopyFinisher.UPDATED.get(), CDKCopyFinisher.NOT_INDEXED_COMPOSITEID.get()));
        }
    }

    private void abortHarvest(String message, Exception ex) {
        LOGGER.log(Level.SEVERE, message, ex);
        finisher.exceptionDuringCrawl(ex);
        throw new HarvestAbortedException(message, ex);
    }

    private static class HarvestAbortedException extends RuntimeException {
        HarvestAbortedException(String message, Throwable cause) {
            super(message, cause);
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
        String childOfComposite = this.config.getRequestConfig().getChildOfComposite();
        boolean compositeId = this.config.getRequestConfig().isCompositeId();

        List<String> computedFields = Arrays.asList("cdk.licenses", "cdk.licenses_of_ancestors cdk.contains_licenses");
        String fieldlist = "pid " + collectionField +" cdk.leader cdk.collection "+ String.join(" ", computedFields);
        if (compositeId) {
            fieldlist = fieldlist + " root.pid compositeId model root.model own_model_path own_parent.pid";
        }

        String query = "?q=" + "pid" + ":(" + URLEncoder.encode(reduce, StandardCharsets.UTF_8)
                + ")&fl=" + URLEncoder.encode(fieldlist, StandardCharsets.UTF_8) + "&wt=xml&rows=" + subitems.size();

        String checkUrl = checkUrlC + (checkUrlC.endsWith("/") ? "" : "/") + checkEndpoint;
        Element resultElem = XMLUtils.findElement(HTTPSolrUtils.executeQueryApache(client, this.enricher, checkUrl, query),
                (elm) -> {
                    return elm.getNodeName().equals("result");
        });

        List<Element> docElms = XMLUtils.getElements(resultElem);
        List<Map<String, Object>>  docs = docElms.stream().map(ResultsUtils::doc).collect(Collectors.toList());


        List<CDKWorkerIndexedItem> indexedRecordList = new ArrayList<>();
        List<CDKExistingConflictFeederItem> econflicts = findIndexConflict(childOfComposite, subitems, docs);
        removePids(econflicts, docs);

        List<String> econflictPids = econflicts.stream().map(CDKExistingConflictFeederItem::getPid).toList();

        docElms.forEach(d -> {
            Map<String, Object> map = ResultsUtils.doc(d);

            Element collection = XMLUtils.findElement(d, e -> {
                return e.getAttribute("name").equals(collectionField);
            });

            if (collection != null) {
                map.put(collectionField, collection.getTextContent());
            }

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


    private static void removePids(List<CDKExistingConflictFeederItem> conflicts, List<Map<String, Object>> docs) {
        Set<String> pids = conflicts.stream()
                .map(CDKExistingConflictFeederItem::getPid)
                .collect(Collectors.toSet());
        docs.removeIf(doc -> pids.contains(doc.get("pid")));
    }

    static List<CDKExistingConflictFeederItem> findIndexConflict(String childOfComposite, List<IterationItem> subitems, List<Map<String,Object>> docs) {
        // 1. Vytvorime si rychlou mapu pro vyhledavani zdrojovych polozek podle PID (O(1) pristup)
        Map<String, IterationItem> sourceByPid = subitems.stream()
                .filter(item -> StringUtils.isAnyString(item.getPid()))
                .collect(Collectors.toMap(IterationItem::getPid, item -> item, (left, right) -> left, LinkedHashMap::new));

        Map<String, ConflictAccumulator> conflicts = new LinkedHashMap<>();

        // 2. Projdeme dokumenty vracene z lokalniho Solru
        for (Map<String, Object> indexedDoc : docs) {
            String pid = getString(indexedDoc, childOfComposite); // V tvem pripade "pid"
            if (!StringUtils.isAnyString(pid)) continue;

            IterationItem sourceItem = sourceByPid.get(pid);
            if (sourceItem == null) continue; // Polozka je sice v Solru, ale neni v aktualni migrovane davce

            // pid
            ConflictAccumulator accumulator = conflicts.computeIfAbsent(pid, ConflictAccumulator::new);
            // pridam compositeId
            accumulator.addCompositeId(getString(indexedDoc, "compositeId"));

            // 3. Porovname lokalni index se zdrojem (zda nedoslo k presunu v hierarchii)
            if (findConflict(indexedDoc, sourceItem.getDoc())) {
                accumulator.conflict = true;
                accumulator.addCompositeId(resolveCompositeId(sourceItem.getDoc(), pid));
            }
        }

        // 4. Vyfiltrujeme pouze ty akumulatory, kde se skutecne potvrdil konflikt
        return conflicts.values().stream()
                .filter(accumulator -> accumulator.conflict)
                .map(ConflictAccumulator::toConflictItem)
                .collect(Collectors.toList());
    }

    static boolean findConflict(Map<String, Object> indexedDoc, Map<String, Object> sourceDoc) {
        // zjistim compositeId
        String indexedCompositeId = getString(indexedDoc, "compositeId");
        // pid - indexovany
        String indexedPid = getString(indexedDoc, "pid");
        // resolvuje na zaklade root.pidu
        String sourceCompositeId = resolveCompositeId(sourceDoc, indexedPid);

        if (StringUtils.isAnyString(indexedCompositeId) && StringUtils.isAnyString(sourceCompositeId)
                && !indexedCompositeId.equals(sourceCompositeId)) {
            return true;
        }

        return different(indexedDoc, sourceDoc, "root.pid")
                || different(indexedDoc, sourceDoc, "model")
                || different(indexedDoc, sourceDoc, "root.model")
                || different(indexedDoc, sourceDoc, "own_model_path")
                || different(indexedDoc, sourceDoc, "own_parent.pid");
    }

    private static boolean different(Map<String, Object> indexedDoc, Map<String, Object> sourceDoc, String fieldName) {
        String indexedValue = getString(indexedDoc, fieldName);
        String sourceValue = getString(sourceDoc, fieldName);
        if (!StringUtils.isAnyString(indexedValue) || !StringUtils.isAnyString(sourceValue)) {
            return false;
        }
        return !indexedValue.equals(sourceValue);
    }

    private static String resolveCompositeId(Map<String, Object> sourceDoc, String pid) {
        String compositeId = getString(sourceDoc, "compositeId");
        if (StringUtils.isAnyString(compositeId)) {
            return compositeId;
        }

        String rootPid = getString(sourceDoc, "root.pid");
        if (StringUtils.isAnyString(rootPid) && StringUtils.isAnyString(pid)) {
            return rootPid + "!" + pid;
        }

        return null;
    }

    private static String getString(Map<String, Object> doc, String fieldName) {
        if (doc == null || !doc.containsKey(fieldName)) {
            return null;
        }

        Object value = doc.get(fieldName);
        return value != null ? value.toString() : null;
    }

    private static final class ConflictAccumulator {
        private final String pid;
        private final LinkedHashSet<String> compositeIds = new LinkedHashSet<>();
        private boolean conflict;

        private ConflictAccumulator(String pid) {
            this.pid = pid;
        }

        private void addCompositeId(String compositeId) {
            if (StringUtils.isAnyString(compositeId)) {
                compositeIds.add(compositeId);
            }
        }

        private CDKExistingConflictFeederItem toConflictItem() {
            return new CDKExistingConflictFeederItem(pid, new ArrayList<>(compositeIds), conflict);
        }
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
