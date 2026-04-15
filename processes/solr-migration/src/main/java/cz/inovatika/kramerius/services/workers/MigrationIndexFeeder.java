package cz.inovatika.kramerius.services.workers;

import cz.inovatika.kramerius.services.config.MigrationConfig;
import cz.inovatika.kramerius.services.iterators.ApacheHTTPRequestEnricher;
import cz.inovatika.kramerius.services.iterators.IterationItem;
import cz.incad.kramerius.utils.XMLUtils;
import cz.inovatika.kramerius.services.workers.config.FeederConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Logger;

public abstract class MigrationIndexFeeder<C extends MigrationIndexFeederContext> /* implements Runnable */ {

    public static final Logger LOGGER = Logger.getLogger(MigrationIndexFeeder.class.getName());

    /** Client for external service communication. */
    //TODO: Replace by apache client
    protected CloseableHttpClient client;

    /** List of iteration items to be processed by this worker. */
    protected List<IterationItem> itemsToBeProcessed;

    /** Barrier for synchronizing workers before processing new tasks. */
    //protected CyclicBarrier barrier;

    protected MigrationConfig migrationConfig;
    protected FeederConfig config;

    /** Finalization handler triggered when the worker completes its task. */
    protected MigrationIndexFeederFinisher finisher;

    /** Apache http request enricher */
    protected ApacheHTTPRequestEnricher enricher;

    public MigrationIndexFeeder(MigrationConfig migrationConfig, CloseableHttpClient client, ApacheHTTPRequestEnricher enricher, List<IterationItem> items, MigrationIndexFeederFinisher finisher) {
        super();
        this.finisher = finisher;
        this.client = client;
        this.itemsToBeProcessed = items;
        this.enricher = enricher;
        this.config = migrationConfig.getFeederConfig();
        this.migrationConfig = migrationConfig;
    }


    public abstract void process();

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

    public void setEnricher(ApacheHTTPRequestEnricher enricher) {
        this.enricher = enricher;
    }

    public ApacheHTTPRequestEnricher getEnricher() {
        return enricher;
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
