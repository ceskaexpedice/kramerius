package cz.inovatika.kramerius.services.workers;

import com.sun.jersey.api.client.*;
import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.utils.IOUtils;
import cz.inovatika.kramerius.services.config.ProcessConfig;
import cz.inovatika.kramerius.services.iterators.IterationItem;
import cz.incad.kramerius.utils.XMLUtils;
import cz.inovatika.kramerius.services.iterators.utils.KubernetesSolrUtils;
import cz.inovatika.kramerius.services.workers.batch.Batch;
import cz.inovatika.kramerius.services.workers.batch.impl.CopyTransformation;
import cz.inovatika.kramerius.services.utils.ResultsUtils;
import cz.inovatika.kramerius.services.utils.SolrUtils;
import cz.inovatika.kramerius.services.workers.config.WorkerConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
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

    // on update event
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
        List<Element> onIndexEventRemoveElms = config.getDestinationConfig().getOnIndexEventUpdateElms();
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

    public Element fetchDocumentFromRemoteSOLR(Client client, List<String> pids, String fieldlist)
            throws IOException, SAXException, ParserConfigurationException {
        String idIdentifier = this.config.getRequestConfig().getIdIdentifier();
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


        List<WorkerIndexedItem> workerIndexedItemList = new ArrayList<>();
        docElms.stream().forEach(d -> {
            Map<String, Object> map = ResultsUtils.doc(d);

            WorkerIndexedItem record = new WorkerIndexedItem(config.getRequestConfig().getIdIdentifier(),  map);
            workerIndexedItemList.add(record);
        });

        List<String> identifierFromOriginalSolr = workerIndexedItemList.stream().map(m -> {
            return m.getId();
        }).collect(Collectors.toList());

        List<IterationItem> notindexed = new ArrayList<>();
        subitems.forEach(item -> {
            if (!identifierFromOriginalSolr.contains(item.getPid()))
                notindexed.add(item);
        });

        return new WorkerContext(workerIndexedItemList, notindexed);
    }

    protected String sendToDestination(Document destBatch) {
        try {
            StringWriter writer = new StringWriter();
            XMLUtils.print(destBatch, writer);
            return sendBatchToDestJersey(this.config.getDestinationConfig().getDestinationUrl(), this.client, destBatch, writer);
        } catch (UniformInterfaceException | ClientHandlerException | TransformerException | IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    protected  String sendBatchToDestJersey(String destSolr, Client client, Document batchDoc, StringWriter writer) throws TransformerException, IOException {
        WebResource r = client.resource(destSolr);
        ClientResponse resp = r.accept(MediaType.TEXT_XML).type(MediaType.TEXT_XML).entity(writer.toString(), MediaType.TEXT_XML).post(ClientResponse.class);
        if (resp.getStatus() != ClientResponse.Status.OK.getStatusCode()) {

            StringWriter stringWriter = new StringWriter();
            XMLUtils.print(batchDoc,stringWriter);
            LOGGER.warning("Problematic batch: ");
            LOGGER.warning(stringWriter.toString());

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            InputStream entityInputStream = resp.getEntityInputStream();
            IOUtils.copyStreams(entityInputStream, bos);
            return new String(bos.toByteArray(), "UTF-8");
        } else {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            InputStream entityInputStream = resp.getEntityInputStream();
            IOUtils.copyStreams(entityInputStream, bos);
            return new String(bos.toByteArray(), "UTF-8");
        }
    }

    // Basic implemenation
    @Override
    public void run() {
        try {
            int batchSize = this.config.getRequestConfig().getBatchSize();
            LOGGER.info("["+Thread.currentThread().getName()+"] processing list of pids "+this.itemsToBeProcessed.size());
            int batches = this.itemsToBeProcessed.size() / batchSize + (this.itemsToBeProcessed.size() % batchSize == 0 ? 0 :1);
            LOGGER.info("["+Thread.currentThread().getName()+"] creating  "+batches+" batch ");
            for (int i=0;i<batches;i++) {
                int from = i*batchSize;
                int to = from + batchSize;
                try {
                    List<IterationItem> subitems = itemsToBeProcessed.subList(from, Math.min(to,itemsToBeProcessed.size() ));
                    /** Fetching documents from remote library */
                    WorkerContext simpleCopyContext = createContext(subitems);

                    if (!simpleCopyContext.getNotIndexed().isEmpty()) {

                        /** Indexing field list; full list of indexing document fields  */
                        String onIndexedFieldList = config.getDestinationConfig().getOnIndexedFieldList();
                        String fieldList = config.getRequestConfig().getFieldList();
                        String fl =  onIndexedFieldList != null ? onIndexedFieldList : fieldList;

                        Element response = fetchDocumentFromRemoteSOLR( this.client,  subitems.stream().map(IterationItem::getPid).collect(Collectors.toList()), fl);
                        Element resultElem = XMLUtils.findElement(response, (elm) -> {
                            return elm.getNodeName().equals("result");
                        });

                        ////        Document batch = BatchUtils.batch(processConfig, resultElem, createBatchTransformation(), null);
                        Batch batchFact = new Batch(processConfig, createBatchTransformation(), null);
                        Document batch = batchFact.create(resultElem);

                        Element addDocument = batch.getDocumentElement();
                        // on index - remove element
                        onIndexRemoveEvent(addDocument);
                        // on index update element
                        onIndexUpdateEvent(addDocument);

                        String destinationUrl = processConfig.getWorkerConfig().getDestinationConfig().getDestinationUrl();
                        String s = SolrUtils.sendToDest(destinationUrl, this.client, batch);
                        LOGGER.info(s);
                    }

                    /**
                     * Already indexed part; indexing only part of documents -  licenses, authors, titles, ...
                     */
                    if (!simpleCopyContext.getAlreadyIndexed().isEmpty()) {
                        // On update elements must not be empty
                        List<Element> onUpdateUpdateElements = config.getDestinationConfig().getOnUpdateUpdateElements();
                        if (!onUpdateUpdateElements.isEmpty()) {
                            /** Updating fields */
                            String fl = config.getDestinationConfig().getOnUpdateFieldList() != null ? config.getDestinationConfig().getOnUpdateFieldList() : null;
                            /** Destinatination batch */
                            Document destBatch = null;
                            if (fl != null) {
                                /** already indexed pids */
                                List<String> pids = simpleCopyContext.getAlreadyIndexed().stream().map(ir->{
                                    String string = ir.getId();
                                    return string;
                                }).collect(Collectors.toList());
                                /** Indexed records as map */
                                Map<String, WorkerIndexedItem> alreadyIndexedAsMap = simpleCopyContext.getAlreadyIndexedAsMap();
                                /** Fetch documents from source library */
                                Element response2 = fetchDocumentFromRemoteSOLR( this.client,  pids, fl);
                                Element resultElem2 = XMLUtils.findElement(response2, (elm) -> {
                                    return elm.getNodeName().equals("result");
                                });
                                /** Construct final batch */
                                Batch batch = new Batch(processConfig, createBatchTransformation(), null);
                                destBatch = batch.create(resultElem2);

                            } else {
                                /** If there is no update list, then no update */
                                Document db = XMLUtils.crateDocument("add");
                                destBatch = db;
                            }


                            Element addDocument = destBatch.getDocumentElement();
                            onUpdateEvent(addDocument);
                            sendToDestination(destBatch);
                        } else {
                            // no update
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
            //LOGGER.info(String.format("Worker finished; All work for workers: %d; work in batches: %d; indexed: %d; updated %d, compositeIderror %d" ,  ReplicateFinisher.WORKERS.get(), ReplicateFinisher.BATCHES.get(), ReplicateFinisher.NEWINDEXED.get(), ReplicateFinisher.UPDATED.get(),ReplicateFinisher.NOT_INDEXED_COMPOSITEID.get()));
        }
    }

    private static CopyTransformation createBatchTransformation() {
        return new CopyTransformation();
    }

    // intended to
//    protected Document createBatchToIndex(Element resultElem) throws ParserConfigurationException, MigrateSolrIndexException {
//        Document batch = BatchUtils.batch(processConfig, resultElem, createBatchTransformation(), null);
//        return batch;
//    }
}
