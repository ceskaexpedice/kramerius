package cz.incad.kramerius.services.workers.checkindex;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.Worker;
import cz.incad.kramerius.services.iterators.IterationItem;
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
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 */
public class CheckIndexWorker extends Worker {

    public static final Logger KIBANA_LOGGER = Logger.getLogger("kibana."+CheckIndexWorker.class.getName());

    public static final Logger LOGGER = Logger.getLogger(CheckIndexWorker.class.getName());

    private String checkingIndexType;

    public CheckIndexWorker(Element workerElm, Client client, List<IterationItem> pids) {
        super(workerElm, client, pids);

        Element destinationElm = XMLUtils.findElement(workerElm, "destination");
        if (destinationElm != null) {
            Element elementChkIndexType = XMLUtils.findElement(destinationElm, "checkindex.type");
            this.checkingIndexType = elementChkIndexType.getTextContent();
        }
    }

    @Override
    public void run() {
        try {
            LOGGER.info("["+Thread.currentThread().getName()+"] Check Imported  processing list of pids "+this.pidsToBeProcessed.size());
            int batches = this.pidsToBeProcessed.size() / batchSize + (this.pidsToBeProcessed.size() % batchSize == 0 ? 0 :1);
            LOGGER.info("["+Thread.currentThread().getName()+"] creating  "+batches+" batch ");
            for (int i=0;i<batches;i++) {
                int from = i*batchSize;
                int to = from + batchSize;


                    // create big batch - contains all subprocessed pids
                List<String> subpids = pidsToBeProcessed.subList(from, Math.min(to,pidsToBeProcessed.size() ));
                subpids = subpids.stream().map(pid->{
                    if (pid.contains("|")) {
                        String[] vals = pid.split("\\|");
                        if (vals.length > 0) return vals[1];
                        else return null;
                    } else return pid;

                }).collect(Collectors.toList());

                String reduce = subpids.stream().reduce("", (identity, v) -> {
                    if (!identity.equals("")) {
                        return identity + " OR \"" + v+"\"";
                    } else {
                        return '"'+v+'"';
                    }
                });
                String fieldlist = "PID collection";
                String query =   "?q=PID:(" + URLEncoder.encode(reduce, "UTF-8") + ")&fl=" + URLEncoder.encode(fieldlist, "UTF-8")+"&wt=xml";
                // query

                Element resultElem = XMLUtils.findElement(SolrUtils.executeQuery(client, this.requestUrl.endsWith("/") ? this.requestUrl+this.requestEndpoint :this.requestUrl+"/"+this.requestEndpoint , query), (elm) -> {
                    return elm.getNodeName().equals("result");
                });
                List<Pair<String, List<String>>> processedSubPids = ResultsUtils.pidAndCollectionFromResult(resultElem);
                processedSubPids.stream().forEach(this::logPid);
            }
        } catch (ParserConfigurationException | SAXException | IOException  e)  {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
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


    private JSONObject logObject(Pair<String, List<String>> pair) {
        JSONObject jObject = new JSONObject();
        jObject.put("pid",pair.getLeft());
        JSONArray jsonArray = new JSONArray();
        pair.getRight().stream().forEach(jsonArray::put);
        jObject.put("collections", jsonArray);
        jObject.put("type", this.checkingIndexType);
        return jObject;
    }

    private void logPid(Pair<String, List<String>> pair) {
        KIBANA_LOGGER.log(Level.INFO, logObject(pair).toString());
    }
}
