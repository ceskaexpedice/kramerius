package cz.inovatika.kramerius.services.workers.copy;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.utils.XMLUtils;
import cz.inovatika.kramerius.services.config.ProcessConfig;
import cz.inovatika.kramerius.services.iterators.IterationItem;
import cz.inovatika.kramerius.services.iterators.utils.KubernetesSolrUtils;
import cz.inovatika.kramerius.services.transform.CopyConsumer;
import cz.inovatika.kramerius.services.transform.CopyTransformation;
import cz.inovatika.kramerius.services.utils.ResultsUtils;
import cz.inovatika.kramerius.services.utils.SolrUtils;
import cz.inovatika.kramerius.services.workers.Worker;
import cz.inovatika.kramerius.services.workers.WorkerFinisher;
import cz.inovatika.kramerius.services.workers.copy.records.SCIndexedRecord;
import cz.inovatika.kramerius.services.workers.copy.utils.BatchUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class SimpleCopyWorker extends Worker {

    public SimpleCopyWorker(ProcessConfig processConfig, Client client, List<IterationItem> items, WorkerFinisher finisher) {
        super(processConfig, client, items, finisher);
    }

    // on update event
    private void onUpdateEvent(Element addDocument) {
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


    private void onIndexUpdateEvent(Element addDocument) {
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


    private void onIndexRemoveEvent(Element addDocument) {
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

        String reduce = pids.stream().reduce("", (i, v) -> {
            if (!i.equals("")) {
                return i + " OR \"" + v + "\"";
            } else {
                return '"' + v + '"';
            }
        });
        String query = "?q=" + idIdentifier + ":(" + URLEncoder.encode(reduce, "UTF-8") + ")&fl="
                + URLEncoder.encode(fieldlist, "UTF-8") + "&wt=xml&rows=" + pids.size();
        LOGGER.info(String.format("Requesting uri %s, %s",requestUrl, query));
        return KubernetesSolrUtils.executeQueryJersey(client,requestUrl, query);
    }

    protected SimpleCopyContext createContext(List<IterationItem> subitems)  throws ParserConfigurationException, SAXException, IOException {

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


        List<SCIndexedRecord> indexedRecordList = new ArrayList<>();
        docElms.stream().forEach(d -> {
            Map<String, Object> map = ResultsUtils.doc(d);

            SCIndexedRecord record = new SCIndexedRecord(config.getRequestConfig().getIdIdentifier(),  map);
            indexedRecordList.add(record);
        });

        List<String> identifierFromOriginalSolr = indexedRecordList.stream().map(m -> {
            return m.getId();
        }).collect(Collectors.toList());

        List<IterationItem> notindexed = new ArrayList<>();
        subitems.forEach(item -> {
            if (!identifierFromOriginalSolr.contains(item.getPid()))
                notindexed.add(item);
        });

        return new SimpleCopyContext(indexedRecordList, notindexed);
    }

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

                    SimpleCopyContext simpleCopyContext = createContext(subitems);

                    if (!simpleCopyContext.getNotIndexed().isEmpty()) {

                        /** Indexing field list; full list of indexing document fields  */
                        String onIndexedFieldList = config.getDestinationConfig().getOnIndexedFieldList();
                        String fieldList = config.getRequestConfig().getFieldList();
                        String fl =  onIndexedFieldList != null ? onIndexedFieldList : fieldList;

                        Element response = fetchDocumentFromRemoteSOLR( this.client,  subitems.stream().map(IterationItem::getPid).collect(Collectors.toList()), fl);
                        Element resultElem = XMLUtils.findElement(response, (elm) -> {
                            return elm.getNodeName().equals("result");
                        });

                        Document batch = BatchUtils.batch(processConfig, resultElem,  new CopyTransformation(), null);

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
                                Map<String, SCIndexedRecord> alreadyIndexedAsMap = simpleCopyContext.getAlreadyIndexedAsMap();
                                /** Fetch documents from source library */
                                Element response2 = fetchDocumentFromRemoteSOLR( this.client,  pids, fl);
                                Element resultElem2 = XMLUtils.findElement(response2, (elm) -> {
                                    return elm.getNodeName().equals("result");
                                });
                                /** Construct final batch */
                                destBatch = BatchUtils.batch(processConfig, resultElem2,  new CopyTransformation(),null);

                            } else {
                                /** If there is no update list, then no update */
                                Document db = XMLUtils.crateDocument("add");
                                destBatch = db;
                            }


                            Element addDocument = destBatch.getDocumentElement();
                            onUpdateEvent(addDocument);
                            String s = KubernetesSolrUtils.sendToDest(config.getDestinationConfig().getDestinationUrl(), this.client, destBatch);
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
            //LOGGER.info(String.format("Worker finished; All work for workers: %d; work in batches: %d; indexed: %d; updated %d, compositeIderror %d" ,  ReplicateFinisher.WORKERS.get(), ReplicateFinisher.BATCHES.get(), ReplicateFinisher.NEWINDEXED.get(), ReplicateFinisher.UPDATED.get(),ReplicateFinisher.NOT_INDEXED_COMPOSITEID.get()));
        }

    }
}
