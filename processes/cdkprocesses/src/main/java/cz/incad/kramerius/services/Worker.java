package cz.incad.kramerius.services;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.iterators.IterationItem;
import cz.incad.kramerius.services.logging.OnlyMessageFormatter;
import cz.incad.kramerius.utils.XMLUtils;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Abstract worker class that processes tasks distributed by an {@link  cz.incad.kramerius.services.iterators.ProcessIterator}.
 * <p>
 * A {@code Worker} instance is always associated with an iterator that assigns it
 * a batch of tasks. The worker processes the assigned workload and synchronizes
 * with other workers using a {@link CyclicBarrier} before receiving a new batch.
 * </p>
 * <p>
 * This class can be extended to define specific processing behaviors while maintaining
 * a generic framework for task execution and synchronization.
 * </p>
 */
public abstract class Worker implements Runnable {

    public static final Logger LOGGER = Logger.getLogger(Worker.class.getName());
    protected static Logger KIBANA_LOGGER = null;


    /** Client for external service communication. */
    //TODO: Replace by apache client
    protected Client client;

    /** List of persistent identifiers (PIDs) to be processed. */
    //protected List<IterationItem> pidsToBeProcessed;

    /** List of iteration items to be processed by this worker. */
    protected List<IterationItem> itemsToBeProcessed;

    /** Barrier for synchronizing workers before processing new tasks. */
    protected CyclicBarrier barrier;

    /** Name of the source providing data for processing. */
    protected String sourceName;

    /** XML configuration element for the worker. */
    protected Element workerElm;

    // Default properties
    /** Batch size for processing items. */
    protected int batchSize;

    /** Base URL for making requests. */
    protected String requestUrl;

    /** Endpoint associated with the request. */
    protected String requestEndpoint;

    /** URL of the destination system where processed data will be sent. */
    protected String destinationUrl;

    /** Reharvest api */
    protected String reharvestApi;

    /** Finalization handler triggered when the worker completes its task. */
    protected WorkerFinisher finisher;

    //TODO: Remove kibana stuff
    static AtomicBoolean _LOGGER_INITIALIZED = new AtomicBoolean(false);
    static  ReentrantLock _LOCK = new ReentrantLock();

    /** XML elements defining updates triggered upon indexing events. */
    protected List<Element> onIndexEventUpdateElms = new ArrayList<>();
    protected List<Element> onIndexEventRemoveElms = new ArrayList<>();
    protected List<Element> onUpdateUpdateElements = new ArrayList<>();

    private static void logConfigure(String logFileName, String loggerName) {
        try{
            _LOCK.lock();
            if (!_LOGGER_INITIALIZED.get()) {

                FileHandler fileHandler = new FileHandler(logFileName);
                fileHandler.setFormatter(new OnlyMessageFormatter());

                KIBANA_LOGGER =  Logger.getLogger(loggerName);
                KIBANA_LOGGER.addHandler(fileHandler);

                LogManager.getLogManager().addLogger(KIBANA_LOGGER);

                _LOGGER_INITIALIZED.set(true);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        } finally {
            _LOCK.unlock();
        }
    }



    /**
     * Constructs a new worker instance.
     *
     * @param sourceName The name of the data source.
     * @param workerElm  XML configuration for the worker.
     * @param client     A Jersey client used for external communication.
     * @param items      List of iteration items to be processed.
     * @param finisher   Callback invoked when processing is completed.
     */
    public Worker(String sourceName, String reharvestApi, Element workerElm, Client client, List<IterationItem> items, WorkerFinisher finisher) {
        super();
        this.finisher = finisher;
        this.client = client;
        this.itemsToBeProcessed = items;
        //this.pidsToBeProcessed = items.stream().map(IterationItem::getPid).collect(Collectors.toList());
        this.workerElm = workerElm;
        this.sourceName = sourceName;
        this.reharvestApi = reharvestApi;

        // Load request-related properties from XML configuration
         Element requestElm = XMLUtils.findElement(workerElm, "request");
        if (requestElm != null) {

            Element batchsizeElm = XMLUtils.findElement(requestElm, "batchsize");
            this.batchSize = (batchsizeElm != null  && !batchsizeElm.getTextContent().trim().equals("")) ?  Integer.parseInt(batchsizeElm.getTextContent()) : 10;

            Element urlElm = XMLUtils.findElement(requestElm, "url");
            this.requestUrl = urlElm != null ? urlElm.getTextContent() : "";

            Element endpointElm = XMLUtils.findElement(requestElm, "endpoint");
            this.requestEndpoint = endpointElm != null ? endpointElm.getTextContent() : "";

            this.requestUrl = requestUrl.endsWith("/") ? requestUrl +this.requestEndpoint :  requestUrl+ "/" +requestEndpoint;

            Element userElm = XMLUtils.findElement(requestElm, "user");
            Element passElm = XMLUtils.findElement(requestElm, "pass");

        }
        // Load destination-related properties from XML configuration
        Element destElm = XMLUtils.findElement(workerElm, "destination");
        if (destElm != null) {
            // solr destination
            Element urlElm = XMLUtils.findElement(destElm, "url");
            this.destinationUrl = urlElm != null ? urlElm.getTextContent() : "";
            // logger destination
            Element kibanaLoggerNameElm = XMLUtils.findElement(workerElm, "kibana.log.loggername");
            String loggerName = kibanaLoggerNameElm != null ? kibanaLoggerNameElm.getTextContent() : "kibana."+Worker.class.getName();


            Element kibanaLogfileNameElm = XMLUtils.findElement(workerElm, "kibana.log.logfile");
            String logFileName = kibanaLogfileNameElm != null ? kibanaLogfileNameElm.getTextContent() : "kibana.log";

            logConfigure(logFileName, loggerName);

            // on index event
            Element onindex = XMLUtils.findElement(destElm, "onindex");
            if (onindex != null) {
                Element updateFieldElement = XMLUtils.findElement(onindex, "update.dest.field");
                if(updateFieldElement!=null) this.onIndexEventUpdateElms = XMLUtils.getElements(updateFieldElement);

                Element removeFieldElement = XMLUtils.findElement(onindex, "remove.dest.field");
                if (removeFieldElement!=null) this.onIndexEventRemoveElms = XMLUtils.getElements(removeFieldElement);
            }

            // Load on-update event configurations
            Element onupdate = XMLUtils.findElement(destElm, "onupdate");
            if (onupdate != null) {
                Element updateFieldElement = XMLUtils.findElement(onupdate, "update.dest.field");
                if (updateFieldElement!=null) this.onUpdateUpdateElements = XMLUtils.getElements(updateFieldElement);
            }

        }
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
}
