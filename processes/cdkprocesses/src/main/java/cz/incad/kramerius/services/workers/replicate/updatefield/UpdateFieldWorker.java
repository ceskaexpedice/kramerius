package cz.incad.kramerius.services.workers.replicate.updatefield;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.logging.Level;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;

import cz.incad.kramerius.services.iterators.IterationItem;
import cz.incad.kramerius.services.utils.SolrUtils;
import cz.incad.kramerius.services.workers.replicate.AbstractReplicateWorker;
import cz.incad.kramerius.services.workers.replicate.ReplicateContext;
import cz.incad.kramerius.services.workers.replicate.ReplicateFinisher;
import cz.incad.kramerius.utils.XMLUtils;

public class UpdateFieldWorker extends AbstractReplicateWorker {

    private String updateField = null;
    private String updateOperation = null;

    public UpdateFieldWorker(String sourceName, Element worker, Client client, List<IterationItem> items) {
        super(sourceName, worker, client, items);

        config(workerElm);

        Element requestElm = XMLUtils.findElement(workerElm, "destination");
        if (requestElm != null) {
            Element updateFieldElm = XMLUtils.findElement(requestElm, "updateField");
            if (updateFieldElm != null) {
                updateField = updateFieldElm.getTextContent();
            }

            Element operationElm = XMLUtils.findElement(requestElm, "updateFieldOperation");
            if (operationElm != null) {
                updateOperation = operationElm.getTextContent();
            }
        }
    }

    @Override
    public void run() {
        try {
            LOGGER.info("[" + Thread.currentThread().getName() + "] processing list of pids "
                    + this.pidsToBeProcessed.size());
            int batches = this.pidsToBeProcessed.size() / batchSize
                    + (this.pidsToBeProcessed.size() % batchSize == 0 ? 0 : 1);
            LOGGER.info("[" + Thread.currentThread().getName() + "] creating  " + batches + " update ocr batches ");
            for (int i = 0; i < batches; i++) {
                int from = i * batchSize;
                int to = from + batchSize;
                try {
                    List<String> subpids = pidsToBeProcessed.subList(from, Math.min(to, pidsToBeProcessed.size()));
                    ReplicateFinisher.BATCHES.addAndGet(subpids.size());
                    ReplicateContext pidsToReplicate = findPidsAlreadyIndexed(subpids, this.transform);
                    //
                    if (!pidsToReplicate.getAlreadyIndexed().isEmpty()) {

                        List<String> pids = pidsToReplicate.getAlreadyIndexed().stream().map(m -> {
                            return m.get(this.idIdentifier).toString();
                        }).collect(Collectors.toList());

                        Element response = fetchFields(pids);
                        Element resultElem = XMLUtils.findElement(response, (elm) -> {
                            return elm.getNodeName().equals("result");
                        });

                        Map<String, Pair<List<String>, String>> map = new HashMap<>();
                        List<Element> docs = XMLUtils.getElements(resultElem);
                        for (int j = 0, ll = docs.size(); j < ll; j++) {
                            Element doc = docs.get(j);

                            Element updateFieldElm = XMLUtils.findElement(doc, e -> {
                                return e.getAttribute("name").equals(updateField);
                            });

                            Element idElm = XMLUtils.findElement(doc, e -> {
                                return e.getAttribute("name").equals(idIdentifier);
                            });

                            if (updateFieldElm != null && idElm != null) {
                                if (updateFieldElm.getNodeName().equals("arr")) {
                                    List<Element> elements = XMLUtils.getElements(updateFieldElm);
                                    List<String> collected = elements.stream().map(Element::getTextContent)
                                            .collect(Collectors.toList());
                                    if (idElm != null) {
                                        map.put(idElm.getTextContent(), Pair.of(collected, idElm.getTextContent()));
                                    }
                                } else {
                                    map.put(idElm.getTextContent(), Pair.of(
                                            Arrays.asList(updateFieldElm.getTextContent()), idElm.getTextContent()));
                                }
                            } else {
                                ReplicateFinisher.NOT_INDEXED_SKIPPED.addAndGet(1);
                            }
                        }

                        Document destBatch = XMLUtils.crateDocument("add");
                        pidsToReplicate.getAlreadyIndexed().stream().forEach(pair -> {

                            Element doc = destBatch.createElement("doc");
                            Element field = destBatch.createElement("field");

                            if (compositeId) {
                                // String compositeId = pair.get("compositeId");

                                String root = pair.get(transform.getField(rootOfComposite)).toString();
                                String child = pair.get(transform.getField(childOfComposite)).toString();

                                field.setAttribute("name", "compositeId");
                                field.setTextContent(root + "!" + child);

                            } else {
                                String idname = transform.getField(idIdentifier);
                                String identifier = pair.get(idname).toString();
                                // if compositeid
                                field.setAttribute("name", idname);
                                // formal name from hashmap
                                field.setTextContent(identifier);
                            }
                            doc.appendChild(field);

                            String idname = transform.getField(idIdentifier);
                            String identifier = pair.get(idname).toString();
                            if (map.containsKey(identifier)) {
                                List<String> values = map.get(identifier).getLeft();
                                for (String value : values) {

                                    Element updatingField = destBatch.createElement("field");
                                    String transformedUpdateField = transform.getField(updateField);
                                    updatingField.setAttribute("name", transformedUpdateField);
                                    updatingField.setAttribute("update", this.updateOperation);

                                    updatingField.setTextContent(value);
                                    doc.appendChild(updatingField);

                                    destBatch.getDocumentElement().appendChild(doc);
                                }
                            }
                        });

                        ReplicateFinisher.UPDATED
                                .addAndGet(XMLUtils.getElements(destBatch.getDocumentElement()).size());
                        String s = SolrUtils.sendToDest(this.destinationUrl, this.client, destBatch);
                        LOGGER.info(s);
                    } else {
                        LOGGER.info("No update element ");
                    }

                } catch (ParserConfigurationException | SAXException | IOException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        } finally {
            try {
                this.barrier.await();
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            } catch (BrokenBarrierException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }

            LOGGER.info(String.format(
                    "Worker finished; All work for workers: %d; work in batches: %d; indexed: %d; updated %d, compositeIderror %d, skipped %d",
                    ReplicateFinisher.WORKERS.get(), ReplicateFinisher.BATCHES.get(),
                    ReplicateFinisher.NEWINDEXED.get(), ReplicateFinisher.UPDATED.get(),
                    ReplicateFinisher.NOT_INDEXED_COMPOSITEID.get(), ReplicateFinisher.NOT_INDEXED_SKIPPED.get()));

        }
    }

    protected Element fetchFields(List<String> subpids) {
        try {
            String reduce = subpids.stream().reduce("", (i, v) -> {
                if (!i.equals("")) {
                    return i + " OR \"" + v + "\"";
                } else {
                    return '"' + v + '"';
                }
            });
            String query = "?q=" + this.idIdentifier + ":(" + URLEncoder.encode(reduce, "UTF-8") + ")&fl="
                    + URLEncoder.encode(this.fieldList, "UTF-8") + "&wt=xml&rows=" + subpids.size();
            Element element = SolrUtils.executeQuery(client, this.requestUrl, query, this.user, this.pass);
            return element;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
    }
}
