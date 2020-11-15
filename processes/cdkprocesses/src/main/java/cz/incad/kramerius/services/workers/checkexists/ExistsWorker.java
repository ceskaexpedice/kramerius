package cz.incad.kramerius.services.workers.checkexists;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.Worker;
import cz.incad.kramerius.services.utils.ResultsUtils;
import cz.incad.kramerius.services.utils.SolrUtils;
import cz.incad.kramerius.utils.XMLUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


/**
 * Check if indexed documents exists in remote solr
 *
 */
public class ExistsWorker extends Worker {

    public static enum LogExistsCondition {
        EXISTS, NOT_EXISTS, BOTH
    }


    public static final Logger KIBANA_LOGGER = Logger.getLogger("kibana."+ExistsWorker.class.getName());
    public static final Logger LOGGER = Logger.getLogger(ExistsWorker.class.getName());

    private Map<String, String> collections;
    //private boolean logExisting = false;
    private LogExistsCondition typeOfLogging = LogExistsCondition.BOTH;

    public ExistsWorker(Element workerElm, Client client, List<String> pids, Map<String, String> cols) {
        super(workerElm, client, pids);

        this.collections = cols;
        ExistsFinisher.BATCH_COUNTER.addAndGet(pids.size());

        Element collectionsElem = XMLUtils.findElement(workerElm, "collections.url");
        if (collectionsElem != null) {
            XMLUtils.getElements(collectionsElem).forEach(element -> {
                String key = XMLUtils.findElement(element, "key").getTextContent();
                String value = XMLUtils.findElement(element, "value").getTextContent();
                collections.put(key, value);
            });
        }

        Element logExistingElm = XMLUtils.findElement(workerElm, "kibana.log.logexisting");
        if (logExistingElm != null) {
            String logExistingContent = logExistingElm.getTextContent();
            this.typeOfLogging = LogExistsCondition.valueOf(logExistingContent);
        }


    }

    @Override
    public void run() {
        try {
            LOGGER.info("["+Thread.currentThread().getName()+"] CHECK exists processing list of pids "+this.pidsToBeProcessed.size());
            int batches = this.pidsToBeProcessed.size() / batchSize + (this.pidsToBeProcessed.size() % batchSize == 0 ? 0 :1);
            LOGGER.info("["+Thread.currentThread().getName()+"] creating  "+batches+" migrateBatches ");
            for (int i=0;i<batches;i++) {
                int from = i*batchSize;
                int to = from + batchSize;
                try {
                    // create big batch - contains all subprocessed pids
                    List<String> subpids = pidsToBeProcessed.subList(from, Math.min(to,pidsToBeProcessed.size() ));
                    String reduce = subpids.stream().reduce("", (identity, v) -> {
                        if (!identity.equals("")) {
                            return identity + " OR \"" + v+"\"";
                        } else {
                            return '"'+v+'"';
                        }
                    });
                    String fieldlist = "PID collection";
                    String query =   "?q=PID:(" + URLEncoder.encode(reduce, "UTF-8") + ")&fl=" + URLEncoder.encode(fieldlist, "UTF-8")+"&wt=xml";

                    Element resultElem = XMLUtils.findElement(SolrUtils.executeQuery(client, this.requestUrl , query), (elm) -> {
                        return elm.getNodeName().equals("result");
                    });
                    List<Pair<String, List<String>>> pairs = ResultsUtils.pidAndCollectionFromResult(resultElem);

                    // divide into collections
                    Map<String, List<String>> mappingCollectionsToPids = new HashMap<>();
                    pairs.stream().forEach(pair-> {
                        List<String> collections = pair.getRight();
                        for (String c :  collections) {
                            if (!mappingCollectionsToPids.containsKey(c)) {
                                mappingCollectionsToPids.put(c, new ArrayList<>());
                            }
                            mappingCollectionsToPids.get(c).add(pair.getLeft());
                        }
                    });

                    mappingCollectionsToPids.keySet().stream().forEach(col-> {
                        Map<String,Boolean> exists = new HashMap<>();
                        try {
                            String q = mappingCollectionsToPids.get(col).stream().reduce("", (identity, v) -> {
                                if (!identity.equals("")) {
                                    return identity + " OR \"" + v+"\"";
                                } else {
                                    return '"'+v+'"';
                                }
                            });
                            String colURl = this.collections.get(col);
                            colURl += (colURl.endsWith("/") ?  "" :"/");

                            Element collectionResponse = SolrUtils.executeQuery(client, colURl, "api/v5.0/search?q=PID:(" + URLEncoder.encode(q, "UTF-8") + ")&fl=PID&wt=xml");
                            Element collectionResponseResultElm = XMLUtils.findElement(collectionResponse, (elm) -> {
                                return elm.getNodeName().equals("result");
                            });

                            List<String> resultPids = ResultsUtils.pidFromResult(collectionResponseResultElm);
                            resultPids = resultPids.stream().map(it -> {
                                return (it.contains("@") && !it.contains("/@")) ? it.replaceAll("@", "/@") : it;
                            }).collect(Collectors.toList());

                            final List<String> resultPidsF = resultPids;

                            mappingCollectionsToPids.get(col).stream().forEach(spid->{
                                exists.put(spid, resultPidsF.contains(spid));
                            });

                            exists.keySet().forEach(key->{
                                logExists(col, key, exists.get(key));
                            });

                        } catch (ParserConfigurationException | SAXException |  IOException e) {
                            throw new RuntimeException(e);
                        }
                    });

                } catch (ParserConfigurationException | SAXException | IOException  e) {
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
        }

    }

    private JSONObject logObject(String col, String key, boolean aBoolean) {
        JSONObject jObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(col);
        jObject.put("collections", jsonArray);
        jObject.put("pid",key);
        jObject.put("exists",aBoolean);
        jObject.put("type","exists");
        return jObject;
    }


    private void logExists(String col, String key, boolean aBoolean) {
        ExistsFinisher.COUNTER.incrementAndGet();
        boolean emitLog = false;
        switch (this.typeOfLogging) {
            case BOTH:
                emitLog = true;
                break;
            case EXISTS:
                emitLog = aBoolean;
                break;
            case NOT_EXISTS:
                emitLog = !aBoolean;
                break;
        }
        if (emitLog)  KIBANA_LOGGER.log(Level.INFO, logObject(col,key,aBoolean).toString());
    }
}
