package cz.incad.kramerius.services.workers.nullworker;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.Worker;
import cz.incad.kramerius.services.iterators.IterationItem;
import cz.incad.kramerius.services.workers.checkexists.ExistsWorker;
import cz.incad.kramerius.services.workers.kibana.utils.KibanaMessageUtils;
import org.json.JSONObject;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NullWorker extends Worker {

    public static final Logger LOGGER = Logger.getLogger(ExistsWorker.class.getName());


    public NullWorker(String sourceName, Element workerElm, Client client, List<IterationItem> pids) {
        super(sourceName, workerElm, client, pids);

    }

    @Override
    public void run() {
        try {
            LOGGER.info("["+Thread.currentThread().getName()+"] Null worker; processing batch "+this.itemsToBeProcessed.size());
            NullWorkerFactory.COUNTER += this.itemsToBeProcessed.size();
            this.itemsToBeProcessed.stream().forEach(item->{
                JSONObject object = KibanaMessageUtils.basicPIDMessage(item);

                List<JSONObject> confObjects = new ArrayList<>();
                this.onIndexEventUpdateElms.stream().forEach(element -> {
                    NodeList childNodes = element.getChildNodes();
                    for (int i = 0; i < childNodes.getLength(); i++) {
                        if (childNodes.item(i).getNodeType() == Node.CDATA_SECTION_NODE) {
                            CDATASection cdataSection = (CDATASection) childNodes.item(i);
                            String wholeText = cdataSection.getWholeText();
                            JSONObject confOBject = new JSONObject(wholeText);
                            confObjects.add(confOBject);
                        }
                    }
                });

                confObjects.stream().forEach(it-> KibanaMessageUtils.expand(it, object));

                KIBANA_LOGGER.log(Level.INFO, object.toString());
            });
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
}
