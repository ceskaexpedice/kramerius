package cz.incad.kramerius.services;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.*;
import com.sun.jersey.api.json.*;
import com.sun.jersey.api.json.JSONConfiguration;
import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.services.iterators.IterationItem;
import cz.incad.kramerius.services.iterators.ProcessIterator;
import cz.incad.kramerius.services.iterators.ProcessIteratorFactory;
import cz.incad.kramerius.services.iterators.timestamps.TimestampStore;
import cz.incad.kramerius.services.iterators.timestamps.solr.SolrTimestampChecks;
import cz.incad.kramerius.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class for command line utility
 */
public class ParallelProcessImpl {

    public static final Logger LOGGER = Logger.getLogger(ParallelProcessImpl.class.getName());
    
    private Client client;
    private WorkerFactory workerFactory;

    private int threads;

    private Element workerElem;
    private WorkerFinisher finisher;
    private ProcessIterator iterator;


    public ParallelProcessImpl() throws MigrateSolrIndexException {
        super();
        this.client = buildClient();
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Client getClient() {
        return client;
    }

    protected Client buildClient() {
    	//Client client = Client.create();
    	ClientConfig cc = new DefaultClientConfig();
    	cc.getProperties().put(
    	        ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
    	cc.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
    	return Client.create(cc);
    }	

    private void startWorkers(List<Worker> worksWhasHasToBeDone) throws BrokenBarrierException, InterruptedException {
        CyclicBarrier barrier = new CyclicBarrier(worksWhasHasToBeDone.size()+1);
        worksWhasHasToBeDone.stream().forEach(th->{
            th.setBarrier(barrier);
            new Thread(th).start();
        });
        barrier.await();
    }

    public void migrate(File config) throws MigrateSolrIndexException, IllegalAccessException, InstantiationException, ClassNotFoundException, IOException, ParserConfigurationException, SAXException, NoSuchMethodException {
        LOGGER.info(String.format("Loading from configuration %s", config.getAbsolutePath()));
        Document document = XMLUtils.parseDocument(new FileInputStream(config));
        // initialize whole process properties
        this.initialize(document.getDocumentElement());

        Element timestamps = XMLUtils.findElement(document.getDocumentElement(),"timestamp");
        TimestampStore timestampStore  = null;
        if (timestamps != null) {
        	timestampStore = null; //new SolrTimestampChecks(client, null, null)
        }

        
        // Iterator factory
        Element iteratorFactory = XMLUtils.findElement(document.getDocumentElement(),"iteratorFactory");
        String iteratorClass = iteratorFactory.getAttribute("class");
        ProcessIteratorFactory processIteratorFactory = ProcessIteratorFactory.create(iteratorClass);

        
        
        // Iterator instance
        Element iterationElm = XMLUtils.findElement(document.getDocumentElement(), "iteration");
    	this.iterator =processIteratorFactory.createProcessIterator(iterationElm, this.client);

        Element workerFactory = XMLUtils.findElement(document.getDocumentElement(),"workerFactory");
        String workerClass = workerFactory.getAttribute("class");
        this.workerFactory = WorkerFactory.create(workerClass);

        this.workerElem = XMLUtils.findElement(document.getDocumentElement(), "worker");
        this.finisher = this.workerFactory.createFinisher( this.iterator.getTimestampStore(),  workerElem,this.client);


        final List<Worker>  worksWhatHasToBeDone = new ArrayList<>();
        try {
            this.iterator.iterate(this.client, (List<IterationItem> idents)->{
                addNewWorkToWorkers(this.iterator, worksWhatHasToBeDone, idents);
            }, ()-> {
                finishRestWorkers(worksWhatHasToBeDone);
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new MigrateSolrIndexException(e);
        } finally {
                if (finisher != null)  this.finisher.finish();
        }
    }

    private void initialize(Element iteration) {
        Element threadsElm = XMLUtils.findElement(iteration, "threads");
        this.threads = threadsElm != null ? Integer.parseInt(threadsElm.getTextContent()) : 2;
    }

    // musi zustat tady
    private void addNewWorkToWorkers(ProcessIterator processIterator, List<Worker> worksWhatHasToBeDone, List<IterationItem> identifiers) {
        try {
            worksWhatHasToBeDone.add(createWorker(processIterator, this.workerElem, identifiers));
            if (worksWhatHasToBeDone.size() >= threads) {
                startWorkers(worksWhatHasToBeDone);
                worksWhatHasToBeDone.clear();
            }
        } catch ( BrokenBarrierException | InterruptedException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }



    private Worker createWorker(ProcessIterator iteratorInstance, Element workerElm, List<IterationItem> identifiers) {
        try {
            return this.workerFactory.createWorker(iteratorInstance, workerElm, this.client,identifiers);
        } catch ( IllegalStateException  e ) {
            throw new RuntimeException("Cannot create worker instance "+e.getMessage());
        }
    }

    private void finishRestWorkers(List<Worker> worksWhatHasToBeDone) {
        try {
            if (!worksWhatHasToBeDone.isEmpty()) {
                startWorkers(worksWhatHasToBeDone);
            }
        } catch (BrokenBarrierException | InterruptedException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    public static void main(String[] args) throws MigrateSolrIndexException, ClassNotFoundException, IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, SAXException, ParserConfigurationException {
        if (args.length > 0 ) {
            for (String arg : args) {
                ParallelProcessImpl migr = new ParallelProcessImpl();
                migr.migrate(new File(arg));
            }
        }
    }
}
