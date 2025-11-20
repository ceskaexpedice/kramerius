package cz.inovatika.kramerius.services.workers;

import com.sun.jersey.api.client.*;
import cz.incad.kramerius.utils.StringUtils;
import cz.inovatika.kramerius.services.config.ProcessConfig;
import cz.inovatika.kramerius.services.iterators.IterationItem;
import cz.incad.kramerius.utils.XMLUtils;
import cz.inovatika.kramerius.services.iterators.utils.KubernetesSolrUtils;
import cz.inovatika.kramerius.services.workers.batch.UpdateSolrBatch;
import cz.inovatika.kramerius.services.workers.batch.BatchConsumer;
import cz.inovatika.kramerius.services.utils.ResultsUtils;
import cz.inovatika.kramerius.services.utils.SolrUtils;
import cz.inovatika.kramerius.services.workers.config.WorkerConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class Worker implements Runnable {

    public static final Logger LOGGER = Logger.getLogger(Worker.class.getName());

    /** Client for external service communication. */
    //TODO: Replace by apache client
    protected Client client;

    /** List of iteration items to be processed by this worker. */
    protected List<IterationItem> itemsToBeProcessed;

    /** Barrier for synchronizing workers before processing new tasks. */
    protected CyclicBarrier barrier;

    protected ProcessConfig processConfig;
    protected WorkerConfig config;

    /** Finalization handler triggered when the worker completes its task. */
    protected WorkerFinisher finisher;

    public Worker(ProcessConfig processConfig,  Client client, List<IterationItem> items, WorkerFinisher finisher) {
        super();
        this.finisher = finisher;
        this.client = client;
        this.itemsToBeProcessed = items;
        this.config = processConfig.getWorkerConfig();
        this.processConfig = processConfig;
    }

    /**
     * Gets the barrier used for synchronizing workers before processing new tasks.
     *
     * @return The {@link CyclicBarrier} instance.
     */
    public CyclicBarrier getBarrier() {
        return barrier;
    }

    /**
     * Sets the barrier used for synchronizing workers before processing new tasks.
     *
     * @param barrier The {@link CyclicBarrier} instance.
     */
    public void setBarrier(CyclicBarrier barrier) {
        this.barrier = barrier;
    }

    // --- Events --
    protected void onUpdateEvent(Element addDocument) {
        List<Element> onUpdateUpdateElements = config.getDestinationConfig().getOnUpdateUpdateElements();
        onUpdateUpdateElements.stream().forEach(f->{
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

    protected void onIndexUpdateEvent(Element addDocument) {
        List<Element> onIndexEventUpdateElms = config.getDestinationConfig().getOnIndexEventUpdateElms();
        onIndexEventUpdateElms.stream().forEach(f->{
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

    protected void onIndexRemoveEvent(Element addDocument) {
        List<Element> onIndexEventRemoveElms = config.getDestinationConfig().getOnIndexEventRemoveElms();
        onIndexEventRemoveElms.stream().forEach(f->{
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

    protected Element fetchDocumentFromRemoteSOLR(Client client, List<String> pids, String fieldlist)
            throws IOException, SAXException, ParserConfigurationException {
        String idIdentifier = this.config.getRequestConfig().getIdIdentifier() != null ?  this.config.getRequestConfig().getIdIdentifier() :  this.processConfig.getIteratorConfig().getIdField();

        String requestUrl = this.config.getRequestConfig().getUrl();
        String requestEndpoint =  this.config.getRequestConfig().getEndpoint();
        String reduce = pids.stream().reduce("", (i, v) -> {
            if (!i.equals("")) {
                return i + " OR \"" + v + "\"";
            } else {
                return '"' + v + '"';
            }
        });
        String query = "?q=" + idIdentifier + ":(" + URLEncoder.encode(reduce, "UTF-8") + ")&fl="
                + URLEncoder.encode(fieldlist, "UTF-8") + "&wt=xml&rows=" + pids.size();
        LOGGER.info(String.format("Requesting uri %s, %s",requestUrl.endsWith("/") ? requestUrl + requestEndpoint : requestUrl +"/"+ requestEndpoint, query));
        return KubernetesSolrUtils.executeQueryJersey(client,requestUrl.endsWith("/") ? requestUrl + requestEndpoint : requestUrl +"/"+ requestEndpoint , query);
    }

    protected WorkerContext createContext(List<IterationItem> subitems)  throws ParserConfigurationException, SAXException, IOException {
        String identifierField = this.config.getRequestConfig().getIdIdentifier() != null ?  this.config.getRequestConfig().getIdIdentifier() :  this.processConfig.getIteratorConfig().getIdField();

        // iterovat pres composite id ale pro dotazovani pouzit jinou polozku z docu
        // pri iterovani ziskavat jeste jine polozky
        String reduce = subitems.stream().map(it -> {
            Map<String, Object> doc = it.getDoc();
            if (doc.containsKey(identifierField)) {
                return  '"'+(String) doc.get(identifierField) + '"';
            } else {
                return '"' + it.getId() + '"';
            }
        }).collect(Collectors.joining(" OR "));


        String checkUrlC = this.config.getRequestConfig().getCheckUrl();
        String checkEndpoint = this.config.getRequestConfig().getCheckEndpoint();
        boolean compositeId = this.config.getRequestConfig().isCompositeId();

        String childOfComposite = this.config.getRequestConfig().getChildOfComposite();
        String rootOfComposite = this.config.getRequestConfig().getRootOfComposite();

        String fieldlist = this.config.getRequestConfig().getFieldList();
        if (compositeId) {
            if (!fieldlist.contains(childOfComposite)) {
                throw new IllegalArgumentException("Field list must contain '"+childOfComposite+"'");
            }
            if (!fieldlist.contains(rootOfComposite)) {
                throw new IllegalArgumentException("Field list must contain '"+rootOfComposite+"'");
            }
        }

        String query = "?q=" + identifierField + ":(" + URLEncoder.encode(reduce, "UTF-8")
                + ")&fl=" + URLEncoder.encode(fieldlist, "UTF-8") + "&wt=xml&rows=" + subitems.size();

        String checkUrl = checkUrlC + (checkUrlC.endsWith("/") ? "" : "/") + checkEndpoint;
        Element resultElem = XMLUtils.findElement(KubernetesSolrUtils.executeQueryJersey(client, checkUrl, query),
                (elm) -> {
                    return elm.getNodeName().equals("result");
        });


        List<Element> docElms = XMLUtils.getElements(resultElem);
        List<WorkerIndexedItem> workerIndexedItemList = new ArrayList<>();
        docElms.stream().forEach(d -> {
            Map<String, Object> map = ResultsUtils.doc(d);

            WorkerIndexedItem record = new WorkerIndexedItem(identifierField,  map);
            workerIndexedItemList.add(record);
        });

        List<String> identifierFromOriginalSolr = workerIndexedItemList.stream().map(m -> {
            return m.getId();
        }).collect(Collectors.toList());

        List<IterationItem> notindexed = new ArrayList<>();
        subitems.forEach(item -> {
            if (!identifierFromOriginalSolr.contains(item.getId()))
                notindexed.add(item);
        });
        return new WorkerContext(workerIndexedItemList, notindexed);
    }


    // Basic implemenation
    @Override
    public void run() {
        try {
            int batchSize = this.config.getRequestConfig().getBatchSize();
            LOGGER.info("["+Thread.currentThread().getName()+"] processing list of items "+this.itemsToBeProcessed.size());
            int batches = this.itemsToBeProcessed.size() / batchSize + (this.itemsToBeProcessed.size() % batchSize == 0 ? 0 :1);
            LOGGER.info("["+Thread.currentThread().getName()+"] creating  "+batches+" batch ");
            for (int i=0;i<batches;i++) {
                int from = i*batchSize;
                int to = from + batchSize;
                try {
                    List<IterationItem> subItems = itemsToBeProcessed.subList(from, Math.min(to,itemsToBeProcessed.size() ));
                    /** Fetching documents from remote library */
                    WorkerContext simpleCopyContext = createContext(subItems);

                    if (!simpleCopyContext.getNotIndexed().isEmpty()) {

                        /** Indexing field list; full list of indexing document fields  */
                        String onIndexedFieldList = config.getDestinationConfig().getOnIndexedFieldList();
                        String fieldList = config.getRequestConfig().getFieldList();
                        String fl =  onIndexedFieldList != null ? onIndexedFieldList : fieldList;

                        //List<String> identifiers = getNotIndexedIdentifiers(simpleCopyContext);

                        Element response = fetchDocumentFromRemoteSOLR( this.client,  getNotIndexedIdentifiers(simpleCopyContext) , fl);
                        Element resultElWithDocsToAdd = XMLUtils.findElement(response, (elm) -> {
                            return elm.getNodeName().equals("result");
                        });

                        UpdateSolrBatch updateSolrBatch = new UpdateSolrBatch(processConfig, resultElWithDocsToAdd, createNewIndexedBatchConsumer());
                        Document batchForInsert = updateSolrBatch.createBatchForInsert();

                        Element addDocument = batchForInsert.getDocumentElement();
                        // on index - remove element
                        onIndexRemoveEvent(addDocument);
                        // on index update element
                        onIndexUpdateEvent(addDocument);

                        String destinationUrl = processConfig.getWorkerConfig().getDestinationConfig().getDestinationUrl();
                        String s = SolrUtils.sendToDest(destinationUrl, this.client, batchForInsert);
                        LOGGER.info(s);
                    }

                    /**
                     * Already indexed part; indexing only part of documents -  licenses, authors, titles, ...
                     */
                    if (!simpleCopyContext.getAlreadyIndexed().isEmpty()) {

                        /** Updating fields */
                        String fl = config.getDestinationConfig().getOnUpdateFieldList() != null ? config.getDestinationConfig().getOnUpdateFieldList() : null;
                        /** Destinatination batch */
                        Document batchForUpdate = null;
                        if (fl != null) {
                            /** already indexed pids */
                            List<String> identifiers = getIndexedIdentifiers(simpleCopyContext);
                            /** Fetch documents from source library */
                            Element response2 = fetchDocumentFromRemoteSOLR( this.client,  identifiers, fl);
                            Element resultElWithDocsToUpdate = XMLUtils.findElement(response2, (elm) -> {
                                return elm.getNodeName().equals("result");
                            });
                            /** Construct final batch */
                            UpdateSolrBatch updateSolrBatch = new UpdateSolrBatch(processConfig, resultElWithDocsToUpdate, createAlreadyIndexedBatchConsumer());
                            batchForUpdate = updateSolrBatch.createBatchForUpdate();

                        } else {
                            /** If there is no update list, then no update */
                            Document db = XMLUtils.crateDocument("add");
                            batchForUpdate = db;
                        }

                        // do only if fl != null ||
                        boolean doUpdate = !config.getDestinationConfig().getOnUpdateUpdateElements().isEmpty() || StringUtils.isAnyString(fl);
                        if (doUpdate) {
                            Element addDocument = batchForUpdate.getDocumentElement();
                            onUpdateEvent(addDocument);
                            String s = SolrUtils.sendToDest(this.config.getDestinationConfig().getDestinationUrl(), this.client, batchForUpdate);
                            LOGGER.info(s);
                        } else {
                            LOGGER.info("No update");
                        }
                    }
                } catch (Exception e) {
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
        }
    }

    protected List<String> getIndexedIdentifiers(WorkerContext simpleCopyContext) {
        List<String> identifiers = simpleCopyContext.getAlreadyIndexed().stream().map(ir->{
            String string = ir.getId();
            return string;
        }).collect(Collectors.toList());
        return identifiers;
    }

    protected List<String> getNotIndexedIdentifiers(WorkerContext simpleCopyContext) {
        return simpleCopyContext.getNotIndexed().stream().map(IterationItem::getId).collect(Collectors.toList());
    }

    protected BatchConsumer createAlreadyIndexedBatchConsumer() {
        return new BatchConsumer() {
            @Override
            public ModifyFieldResult modifyField(Element field) {
                String name = field.getAttribute("name");

                boolean compositeId = processConfig.getWorkerConfig().getRequestConfig().isCompositeId();
                String idIdentifier = processConfig.getWorkerConfig().getRequestConfig().getIdIdentifier();

                if (compositeId && name.equals("compositeId")) {
                    return ModifyFieldResult.none;
                }
                if (!compositeId && name.equals(idIdentifier)) {
                    return ModifyFieldResult.none;
                }
                // edit
                field.setAttribute("update", "set");
                return ModifyFieldResult.edit;
            }

            @Override
            public void changeDocument(ProcessConfig processConfig, Element doc) {

            }
        };
    }

    protected BatchConsumer createNewIndexedBatchConsumer() {
        return null;
    }



}
