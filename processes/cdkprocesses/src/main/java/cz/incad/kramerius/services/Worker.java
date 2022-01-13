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
import java.util.stream.Collectors;

public abstract class Worker implements Runnable {

    public static final Logger LOGGER = Logger.getLogger(Worker.class.getName());
    protected static Logger KIBANA_LOGGER = null;


    protected Client client;
    protected List<String> pidsToBeProcessed;
    protected List<IterationItem> itemsToBeProcessed;

    protected CyclicBarrier barrier;

    protected Element workerElm;

    // default properties
    protected int batchSize;
    protected String requestUrl;
    protected String requestEndpoint;

    protected String destinationUrl;

    protected String user;
    protected String pass;


    static AtomicBoolean _LOGGER_INITIALIZED = new AtomicBoolean(false);
    static  ReentrantLock _LOCK = new ReentrantLock();
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



    public Worker(Element workerElm, Client client, List<IterationItem> items) {
        super();
        this.client = client;
        this.itemsToBeProcessed = items;
        this.pidsToBeProcessed = items.stream().map(IterationItem::getPid).collect(Collectors.toList());
        this.workerElm = workerElm;

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

            if (userElm != null && passElm != null) {
                this.user = userElm.getTextContent();
                this.pass = passElm.getTextContent();
            }
        }

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


            // on update
            Element onupdate = XMLUtils.findElement(destElm, "onupdate");
            if (onupdate != null) {
                Element updateFieldElement = XMLUtils.findElement(onupdate, "update.dest.field");
                if (updateFieldElement!=null) this.onUpdateUpdateElements = XMLUtils.getElements(updateFieldElement);
            }

        }


    }


    public CyclicBarrier getBarrier() {
        return barrier;
    }

    public void setBarrier(CyclicBarrier barrier) {
        this.barrier = barrier;
    }



}
