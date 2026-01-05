package cz.inovatika.kramerius.services.workers.copy.simple;

import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.inovatika.kramerius.services.config.ProcessConfig;
import cz.inovatika.kramerius.services.iterators.IterationItem;
import cz.inovatika.kramerius.services.utils.ResultsUtils;
import cz.inovatika.kramerius.services.utils.SolrUtils;
import cz.inovatika.kramerius.services.workers.*;
import cz.inovatika.kramerius.services.workers.batch.UpdateSolrBatchCreator;
import cz.inovatika.kramerius.services.workers.copy.CopyWorker;
import cz.inovatika.kramerius.services.workers.copy.CopyWorkerContext;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class SimpleCopyWorker extends CopyWorker<WorkerIndexedItem, SimpleCopyWorkerContext> {

    public SimpleCopyWorker(ProcessConfig processConfig, CloseableHttpClient client, List<IterationItem> items, WorkerFinisher finisher) {
        super(processConfig, client, items, finisher);
    }

    @Override
    protected SimpleCopyWorkerContext createContext(List<IterationItem> subItems) {
        String identifierField = this.config.getRequestConfig().getIdIdentifier() != null ? this.config.getRequestConfig().getIdIdentifier() : this.processConfig.getIteratorConfig().getIdField();

        String reduce = subItems.stream().map(it -> {
            Map<String, Object> doc = it.getDoc();
            if (doc.containsKey(identifierField)) {
                return '"' + (String) doc.get(identifierField) + '"';
            } else {
                return '"' + it.getId() + '"';
            }
        }).collect(Collectors.joining(" OR "));


        String checkUrlC = this.config.getRequestConfig().getCheckUrl();
        String checkEndpoint = this.config.getRequestConfig().getCheckEndpoint();
        boolean compositeId = this.config.getRequestConfig().isCompositeId();

        String childOfComposite = this.config.getRequestConfig().getChildOfComposite();
        String rootOfComposite = this.config.getRequestConfig().getRootOfComposite();

        String fieldList = this.config.getRequestConfig().getFieldList();
        if (compositeId) {
            if (!fieldList.contains(childOfComposite)) {
                throw new IllegalArgumentException("Field list must contain '" + childOfComposite + "'");
            }
            if (!fieldList.contains(rootOfComposite)) {
                throw new IllegalArgumentException("Field list must contain '" + rootOfComposite + "'");
            }
        }

        //
        String query = "?q=" + identifierField + ":(" + URLEncoder.encode(reduce, StandardCharsets.UTF_8)
                + ")&fl=" + URLEncoder.encode(fieldList, StandardCharsets.UTF_8) + "&wt=xml&rows=" + subItems.size();

        List<Element> docElms = solrResult(checkUrlC, checkEndpoint, query);
        List<WorkerIndexedItem> workerIndexedItemList = new ArrayList<>();
        docElms.forEach(d -> {
            Map<String, Object> map = ResultsUtils.doc(d);
            String id = map.containsKey(identifierField) ? (String) map.get(identifierField) : null;
            WorkerIndexedItem record = new WorkerIndexedItem(id, map);
            workerIndexedItemList.add(record);
        });

        List<String> identifierFromOriginalSolr = workerIndexedItemList.stream().map(WorkerIndexedItem::getId).toList();

        List<IterationItem> notIndexed = new ArrayList<>();
        subItems.forEach(item -> {
            if (!identifierFromOriginalSolr.contains(item.getId()))
                notIndexed.add(item);
        });
        return new SimpleCopyWorkerContext(subItems, workerIndexedItemList, notIndexed);
    }

    // Basic implemenation
    @Override
    public void run() {
        try {
            int batchSize = this.config.getRequestConfig().getBatchSize();
            LOGGER.info("[" + Thread.currentThread().getName() + "] processing list of items " + this.itemsToBeProcessed.size());
            int batches = this.itemsToBeProcessed.size() / batchSize + (this.itemsToBeProcessed.size() % batchSize == 0 ? 0 : 1);
            LOGGER.info("[" + Thread.currentThread().getName() + "] creating  " + batches + " batch ");
            for (int i = 0; i < batches; i++) {
                int from = i * batchSize;
                int to = from + batchSize;
                try {
                    List<IterationItem> subitems = itemsToBeProcessed.subList(from, Math.min(to, itemsToBeProcessed.size()));
                    CopyWorkerContext<WorkerIndexedItem> simpleCopyContext = createContext(subitems);

                    if (!simpleCopyContext.getNotIndexed().isEmpty()) {

                        String onIndexedFieldList = config.getDestinationConfig().getOnIndexedFieldList();
                        String fieldList = config.getRequestConfig().getFieldList();
                        String fl = onIndexedFieldList != null ? onIndexedFieldList : fieldList;

                        Element response = fetchDocumentFromRemoteSOLR(this.client, getNotIndexedIdentifiers(simpleCopyContext), fl);
                        Element resultElem = XMLUtils.findElement(response, (elm) -> {
                            return elm.getNodeName().equals("result");
                        });

                        UpdateSolrBatchCreator batchFact = new UpdateSolrBatchCreator(processConfig, resultElem, null);
                        Document batch = batchFact.createBatchForInsert();

                        Element addDocument = batch.getDocumentElement();
                        // on index - remove element
                        onIndexRemoveEvent(addDocument);
                        // on index update element
                        onIndexUpdateEvent(addDocument);

                        String destinationUrl = processConfig.getWorkerConfig().getDestinationConfig().getDestinationUrl();
                        String s = SolrUtils.sendToDest(destinationUrl, this.client, batch);
                        LOGGER.info(s);
                    }

                    if (!simpleCopyContext.getAlreadyIndexed().isEmpty()) {

                        String fl = config.getDestinationConfig().getOnUpdateFieldList() != null ? config.getDestinationConfig().getOnUpdateFieldList() : null;
                        Document destBatch = null;
                        if (fl != null) {
                            List<String> identifiers = getIndexedIdentifiers(simpleCopyContext);
                            Element response2 = fetchDocumentFromRemoteSOLR(this.client, identifiers, fl);
                            Element resultElem2 = XMLUtils.findElement(response2, (elm) -> {
                                return elm.getNodeName().equals("result");
                            });
                            UpdateSolrBatchCreator batch = new UpdateSolrBatchCreator(processConfig, resultElem2, null);
                            destBatch = batch.createBatchForUpdate();

                        } else {
                            destBatch = XMLUtils.crateDocument("add");
                        }

                        // do only if fl != null ||
                        boolean doUpdate = !config.getDestinationConfig().getOnUpdateUpdateElements().isEmpty() || StringUtils.isAnyString(fl);
                        if (doUpdate) {
                            Element addDocument = destBatch.getDocumentElement();
                            onUpdateEvent(addDocument);
                            String s = SolrUtils.sendToDest(this.config.getDestinationConfig().getDestinationUrl(), this.client, destBatch);
                            LOGGER.info(s);
                        } else {
                            LOGGER.info("No update");
                        }
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Informing about exception");
                    finisher.exceptionDuringCrawl(e);
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Informing about exception");
            finisher.exceptionDuringCrawl(ex);
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                this.barrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }
}
