package cz.inovatika.kramerius.services.workers;

import com.sun.jersey.api.client.*;
import cz.inovatika.kramerius.services.config.ProcessConfig;
import cz.inovatika.kramerius.services.iterators.IterationItem;
import cz.incad.kramerius.utils.XMLUtils;
import cz.inovatika.kramerius.services.iterators.utils.KubernetesSolrUtils;
import cz.inovatika.kramerius.services.workers.config.WorkerConfig;
import cz.inovatika.kramerius.services.workers.copy.CopyWorkerContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class Worker<C extends WorkerContext>  implements Runnable  {

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

    protected abstract C createContext(List<IterationItem> subitems) throws UnsupportedEncodingException;
}
