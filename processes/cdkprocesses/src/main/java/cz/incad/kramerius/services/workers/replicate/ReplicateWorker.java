package cz.incad.kramerius.services.workers.replicate;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.services.Worker;
import cz.incad.kramerius.services.iterators.IterationItem;
import cz.incad.kramerius.services.utils.SolrUtils;
import cz.incad.kramerius.utils.XMLUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.kramerius.Replicate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Replicate index
 */
public class ReplicateWorker extends Worker {

    // DEFAULT index fields; K5 index
    public static final String DEFAULT_FIELDLIST = "PID timestamp fedora.model document_type handle status created_date modified_date parent_model " +
            "parent_pid parent_pid parent_title root_model root_pid root_title text_ocr pages_count " +
            "datum_str datum rok datum_begin datum_end datum_page issn mdt ddt dostupnost keywords " +
            "geographic_names collection sec model_path pid_path rels_ext_index level dc.title title_sort " +
            "title_sort dc.creator dc.identifier language dc.description details facet_title browse_title browse_autor img_full_mime viewable " +
            "virtual location range mods.shelfLocator mods.physicalLocation text dnnt";

    // Default pid; K5 index
    public static final String DEFAULT_PID_FIELD = "PID";
    // Default collection; K5 index
    public static final String COLLECTION_FIELD = "collection";


    public static Logger LOGGER = Logger.getLogger(ReplicateWorker.class.getName());

    private String fieldList = DEFAULT_FIELDLIST;
    private String idIdentifier = DEFAULT_PID_FIELD;
    private String collectionField = COLLECTION_FIELD;

    private boolean compositeId = false;
    private String rootOfComposite = null;
    private String childOfComposite = null;

    private String checkUrl = null;
    private String checkEndpoint = null;



    public ReplicateWorker(Element workerElm, Client client, List<IterationItem> pids) {
        super(workerElm, client, pids);

        Element requestElm = XMLUtils.findElement(workerElm, "request");
        if (requestElm != null) {
            // Field list to retrieve
            Element fieldlistElm = XMLUtils.findElement(requestElm, "fieldlist");
            if (fieldlistElm != null) {
                fieldList = fieldlistElm.getTextContent();
            }
            // Id
            Element idElm = XMLUtils.findElement(requestElm, "id");
            if (idElm != null) {
                idIdentifier = idElm.getTextContent();
            }

            // collection
            Element collectionElm = XMLUtils.findElement(requestElm, "collection");
            if (collectionElm != null) {
                collectionField = collectionElm.getTextContent();
            }

            // Composite id if there is solr cloud
            Element compositeIdElm = XMLUtils.findElement(requestElm, "composite.id");
            if (compositeIdElm != null) {
                compositeId = Boolean.parseBoolean(compositeIdElm.getTextContent());

                Element compositeRootElm = XMLUtils.findElement(requestElm, "composite.root");
                if (compositeRootElm != null) {
                    this.rootOfComposite = compositeRootElm.getTextContent();
                }
                Element compositeChildElm = XMLUtils.findElement(requestElm, "composite.child");
                if (compositeChildElm != null) {
                    this.childOfComposite = compositeChildElm.getTextContent();
                }
            }
            // Check url; for checking if the document is already indexed
            Element checkUrlElm = XMLUtils.findElement(requestElm, "checkUrl");
            if (checkUrlElm != null) {
                this.checkUrl = checkUrlElm.getTextContent();
            }

            // Check url endpoint; for checking if the document is already indexed
            Element checkEndpointElm = XMLUtils.findElement(requestElm, "checkEndpoint");
            if (checkEndpointElm != null) {
                this.checkEndpoint = checkEndpointElm.getTextContent();
            }
        }
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
                    // Detect if documents are new documents or already indexed documents
                    PidsToReplicate pidsToReplicate = findPidsAlreadyIndexed(subpids);

                    // not indexed => onIndeRemoveElms + onIndexUpdate
                    if (!pidsToReplicate.getNotIndexed().isEmpty()) {

                        // fetch document
                        Element response = fetchDocumentFromRemoteSOLR( this.client,  pidsToReplicate.getNotIndexed());
                        Element resultElem = XMLUtils.findElement(response, (elm) -> {
                            return elm.getNodeName().equals("result");
                        });

                        // create batch
                        Document batch = BatchUtils.batch(resultElem, this.compositeId, this.rootOfComposite, this.childOfComposite);
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

                        ReplicateFinisher.NEWINDEXED.addAndGet(XMLUtils.getElements(addDocument).size());

                        String s = SolrUtils.sendToDest(this.destinationUrl, this.client, batch);
                        LOGGER.info(s);

                    }

                    //
                    if (!pidsToReplicate.getAlreadyIndexed().isEmpty()) {
                        // un update
                        if (!this.onUpdateUpdateElements.isEmpty()) {

                            Document destBatch = XMLUtils.crateDocument("add");
                            pidsToReplicate.getAlreadyIndexed().stream().forEach(pair->{
                                Element doc = destBatch.createElement("doc");
                                Element field = destBatch.createElement("field");

                                // pid +
                                field.setAttribute("name", idIdentifier);

                                field.setTextContent(pair.getLeft());
                                doc.appendChild(field);
                                destBatch.getDocumentElement().appendChild(doc);

                            });

                            Element addDocument = destBatch.getDocumentElement();
                            this.onUpdateUpdateElements.stream().forEach(f->{
                                synchronized (f.getOwnerDocument()) {
                                    String name = f.getAttribute("name");
                                    // collection ?? not do it for everything... how to do that
                                    // iterating over doc
                                    List<Element> docs = XMLUtils.getElements(addDocument);
                                    for (int j = 0,ll=docs.size(); j < ll; j++) {
                                        Element doc = docs.get(j);
                                        Node node = f.cloneNode(true);
                                        doc.getOwnerDocument().adoptNode(node);
                                        doc.appendChild(node);

                                    }
                                }
                            });

                            List<Element> docs = XMLUtils.getElements(destBatch.getDocumentElement());
                            docs.stream().forEach(doc->{
                                List<Element> fields = XMLUtils.getElements(doc);
                                int size = fields.size();

                                if (size <2) {
                                    StringWriter writer = new StringWriter();
                                    try {
                                        XMLUtils.print(doc, writer);
                                    } catch (TransformerException e) { }

                                    throw new IllegalStateException("Cannot index document "+writer.toString());
                                }

                            });

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

            LOGGER.info(String.format("Worker finished; All work for workers: %d; work in batches: %d; indexed: %d; updated %d" ,  ReplicateFinisher.WORKERS.get(), ReplicateFinisher.BATCHES.get(), ReplicateFinisher.NEWINDEXED.get(), ReplicateFinisher.UPDATED.get()));

        }


    }

    private PidsToReplicate findPidsAlreadyIndexed(List<String> subpids) throws ParserConfigurationException, SAXException, IOException {
        String reduce = subpids.stream().map(it->{return '"'+it+'"';}).collect(Collectors.joining(" OR "));
        String fieldlist = idIdentifier+" "+collectionField;

        String query =   "?q="+idIdentifier+":(" + URLEncoder.encode(reduce, "UTF-8") + ")&fl=" + URLEncoder.encode(fieldlist, "UTF-8")+"&wt=xml&rows="+subpids.size();

        String checkUrl = this.checkUrl + (this.checkUrl.endsWith("/") ? "": "/") + this.checkEndpoint;
        Element resultElem = XMLUtils.findElement(SolrUtils.executeQuery(client, checkUrl , query), (elm) -> {
            return elm.getNodeName().equals("result");
        });

        List<Element> docs = XMLUtils.getElements(resultElem);

        List<Pair<String, String>> pidsAndCollections = docs.stream().map(d -> {
            Element pid = XMLUtils.findElement(d, e -> {
                return e.getAttribute("name").equals(idIdentifier);
            });
            Element collection = XMLUtils.findElement(d, e -> {
                return e.getAttribute("name").equals(collectionField);
            });
            return Pair.of(pid.getTextContent(), collection != null ? collection.getTextContent().trim() : "");
        }).collect(Collectors.toList());


        List<String> pidsFromLocalSolr = pidsAndCollections.stream().map(Pair::getLeft).collect(Collectors.toList());
        List<String> notindexed = new ArrayList<>();
        subpids.forEach(pid-> {  if (!pidsFromLocalSolr.contains(pid)) notindexed.add(pid); });

        return new PidsToReplicate(pidsAndCollections,  notindexed);
    }


    public  Element fetchDocumentFromRemoteSOLR(Client client, List<String> pids) throws IOException, SAXException, ParserConfigurationException {
        String reduce = pids.stream().reduce("", (i, v) -> {
            if (!i.equals("")) {
                return i + " OR \"" + v+"\"";
            } else {
                return '"'+v+'"';
            }
        });

        String query =  "?q="+idIdentifier+":(" + URLEncoder.encode(reduce, "UTF-8") + ")&fl=" + URLEncoder.encode(this.fieldList, "UTF-8")+"&wt=xml&rows="+pids.size();
        return SolrUtils.executeQuery(client, this.requestUrl , query);
    }


}
