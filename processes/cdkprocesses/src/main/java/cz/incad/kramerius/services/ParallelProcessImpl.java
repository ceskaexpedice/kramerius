package cz.incad.kramerius.services;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.*;
import com.sun.jersey.api.json.*;
import com.sun.jersey.api.json.JSONConfiguration;
import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.services.iterators.IterationItem;
import cz.incad.kramerius.services.iterators.ProcessIterator;
import cz.incad.kramerius.services.iterators.ProcessIteratorFactory;
import cz.incad.kramerius.timestamps.TimestampStore;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class for command line utility
 */
public class ParallelProcessImpl {

    public static final Logger LOGGER = Logger.getLogger(ParallelProcessImpl.class.getName());
    
    private Client client;
    private WorkerFactory workerFactory;

    private String sourceName;
    private String name;
    
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

    
    public static boolean isInWorkingTime( String startTime, String endTime) {
        try {
            LocalDate now = LocalDate.now();
            LocalDateTime current = LocalDateTime.now();

            return isWorkingTimeImpl(startTime, endTime, now, current);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return false;
        }
    }

    public static boolean isWorkingTimeImpl(String startTime,String endTime,  LocalDate now, LocalDateTime current) {

        LocalTime startTimeLT = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("H:mm"));
        LocalDateTime startDateTimeLT = startTimeLT.atDate(now);
        
        LocalTime endTimeLT = LocalTime.parse(endTime, DateTimeFormatter.ofPattern("H:mm"));
        LocalDateTime endDateTimeLT = endTimeLT.atDate(now);

        if (startDateTimeLT.isAfter(endDateTimeLT)) {
            if (current.isAfter(startDateTimeLT.minusDays(1)) && current.isBefore(endDateTimeLT)) {
                return true;
            } else  if (current.isAfter(startDateTimeLT) && current.isBefore(endDateTimeLT.plusDays(1))) {
                return true;
            }
        } else {
            if ( current.isAfter(startDateTimeLT) && current.isBefore(endDateTimeLT)) {
                return true;
            } else {
                return false;
            }
        }
        
        return false;
    }
    
    public static void waitUntilStartWorkingTime(String startTime, String endTime) {
        try {
            LocalDate now = LocalDate.now();
            LocalDateTime current = LocalDateTime.now();

            long time =  waitUntilStartWorkingTimeImpl(startTime, endTime, now, current);
            Thread.sleep(time);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public static long  waitUntilStartWorkingTimeImpl(String startTime, String endTime, LocalDate now,
            LocalDateTime current) {
        LocalTime startTimeLT = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("H:mm"));
        LocalDateTime startDateTimeLT = startTimeLT.atDate(now);
        
        LocalTime endTimeLT = LocalTime.parse(endTime, DateTimeFormatter.ofPattern("H:mm"));
        LocalDateTime endDateTimeLT = endTimeLT.atDate(now);
        
        if (startDateTimeLT.isAfter(endDateTimeLT)) {
            if (current.isAfter(startDateTimeLT.minusDays(1)) && current.isBefore(endDateTimeLT)) {
                startDateTimeLT = startDateTimeLT.minusDays(1);
            } else  if (current.isAfter(startDateTimeLT) && current.isBefore(endDateTimeLT.plusDays(1))) {
                endDateTimeLT = endDateTimeLT.plusDays(1);
            }
        }

        
        
        Duration duration = Duration.between(current, startDateTimeLT);
        if (!duration.isNegative()) {
            long millis = duration.toMillis();

            waitingInfo(startDateTimeLT, millis);

            return millis;
            //Thread.sleep(millis);
        } else {
            startDateTimeLT = startDateTimeLT.plusDays(1);
            endDateTimeLT = endDateTimeLT.plusDays(1);
            LOGGER.info("I'm shifting day to :"+startDateTimeLT);
            duration = Duration.between(current, startDateTimeLT);
            long millis = duration.toMillis();

            waitingInfo(startDateTimeLT, millis);
            
            return millis;
            //Thread.sleep(millis);
            
            //LOGGER.log(Level.SEVERE, "Negative waiting");
        }
    }

    private static void waitingInfo(LocalDateTime startDateTimeLT, long millis) {
        long hours = millis / 3600000; // 3600000 ms v hodině
        long minutes = (millis % 3600000) / 60000; // 60000 ms v minutě
        long seconds = ((millis % 3600000) % 60000) / 1000; // 1000 ms v sekundě

        LOGGER.info("The date is "+startDateTimeLT+". The calculated wait time is : " + hours + " hours, " + minutes + " minutes a " + seconds + " seconds.");
    }
    
    private void startWorkers(List<Worker> worksWhasHasToBeDone, String workingtime) throws BrokenBarrierException, InterruptedException {
        if (workingtime != null && workingtime.contains("-")) {
            String[] intervalParts = workingtime.split("-");
            String startTime = intervalParts[0];
            String endTime = intervalParts[1];
            
            while (true) {
                if (!isInWorkingTime( startTime, endTime)) {
                    waitUntilStartWorkingTime(startTime, endTime);
                } else {
                    break; 
                }
            }
        }
        
        // check if sleep hours 
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


        Element timestampElm = XMLUtils.findElement(document.getDocumentElement(),"timestamp");
        String timestamp = null;
        if (timestampElm != null) {
            timestamp = StringUtils.isAnyString(timestampElm.getTextContent().trim()) ? timestampElm.getTextContent() : null ;
        }

        Element workingTimeElm = XMLUtils.findElement(document.getDocumentElement(),"workingtime");
        final AtomicReference<String> workingTime = new AtomicReference<>();
        if (workingTimeElm != null) {
            workingTime.set(workingTimeElm.getTextContent());
            //workingtime = workingTimeElm.getTextContent();
        }

        Element sourceNameElm = XMLUtils.findElement(document.getDocumentElement(), "source-name");
        if (sourceNameElm != null) {
            this.sourceName = sourceNameElm.getTextContent();
            if (timestamp != null) {
                if (timestamp.endsWith("connected") || timestamp.endsWith("connected/")) {
                    timestamp = timestamp+(timestamp.endsWith("/") ? "" : "/")+String.format("%s/timestamp",this.sourceName);
                }
            }
        } else {
            throw new IllegalStateException("expecting source name !! ");
        }

        // Iterator factory
        Element iteratorFactory = XMLUtils.findElement(document.getDocumentElement(),"iteratorFactory");
        String iteratorClass = iteratorFactory.getAttribute("class");
        ProcessIteratorFactory processIteratorFactory = ProcessIteratorFactory.create(iteratorClass);

        // Iterator instance
        Element iterationElm = XMLUtils.findElement(document.getDocumentElement(), "iteration");

        this.iterator =processIteratorFactory.createProcessIterator(timestamp, iterationElm, this.client);

        Element workerFactory = XMLUtils.findElement(document.getDocumentElement(),"workerFactory");
        String workerClass = workerFactory.getAttribute("class");
        this.workerFactory = WorkerFactory.create(workerClass);

        this.workerElem = XMLUtils.findElement(document.getDocumentElement(), "worker");
        this.finisher = this.workerFactory.createFinisher(timestamp,  workerElem,this.client);


        Element nameElm = XMLUtils.findElement(document.getDocumentElement(), "name");
        if (nameElm != null) {
            this.name = nameElm.getTextContent();
        }

        final List<Worker>  worksWhatHasToBeDone = new ArrayList<>();
        try {
            this.iterator.iterate(this.client, (List<IterationItem> idents)->{
                addNewWorkToWorkers(this.iterator, worksWhatHasToBeDone, idents, workingTime.get());
            }, ()-> {
                finishRestWorkers(worksWhatHasToBeDone,workingTime.get());
            });
            
        } catch (Exception e) {
            this.finisher.exceptionDuringCrawl(e);
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new MigrateSolrIndexException(e);
        } finally {
            // stop process 
            if (finisher != null)  this.finisher.finish();
        }
    }

    private void initialize(Element iteration) {
        Element threadsElm = XMLUtils.findElement(iteration, "threads");
        this.threads = threadsElm != null ? Integer.parseInt(threadsElm.getTextContent()) : 2;
    }

    private void addNewWorkToWorkers(ProcessIterator processIterator, List<Worker> worksWhatHasToBeDone, List<IterationItem> identifiers, String workingtime) {
        try {
            worksWhatHasToBeDone.add(createWorker(processIterator, this.workerElem, identifiers));
            if (worksWhatHasToBeDone.size() >= threads) {
                startWorkers(worksWhatHasToBeDone, workingtime);
                worksWhatHasToBeDone.clear();
            }
        } catch ( BrokenBarrierException | InterruptedException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }



    private Worker createWorker(ProcessIterator iteratorInstance, Element workerElm, List<IterationItem> identifiers) {
        try {
            return this.workerFactory.createWorker(this.sourceName, iteratorInstance, workerElm, this.client,identifiers, this.finisher);
        } catch ( IllegalStateException  e ) {
            throw new RuntimeException("Cannot create worker instance "+e.getMessage());
        }
    }

    private void finishRestWorkers(List<Worker> worksWhatHasToBeDone,String workingtime) {
        try {
            if (!worksWhatHasToBeDone.isEmpty()) {
                startWorkers(worksWhatHasToBeDone, workingtime);
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
