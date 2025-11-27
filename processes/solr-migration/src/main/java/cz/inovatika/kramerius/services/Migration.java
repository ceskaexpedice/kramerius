    package cz.inovatika.kramerius.services;

    import com.sun.jersey.api.client.Client;
    import com.sun.jersey.api.client.config.ClientConfig;
    import com.sun.jersey.api.client.config.DefaultClientConfig;
    import com.sun.jersey.api.json.JSONConfiguration;
    import cz.incad.kramerius.service.MigrateSolrIndexException;
    import cz.inovatika.kramerius.services.config.ProcessConfig;
    import cz.inovatika.kramerius.services.config.ProcessConfigParser;
    import cz.inovatika.kramerius.services.iterators.IterationItem;
    import cz.inovatika.kramerius.services.iterators.ProcessIterator;
    import cz.inovatika.kramerius.services.iterators.ProcessIteratorFactory;
    import cz.incad.kramerius.utils.XMLUtils;
    import cz.inovatika.kramerius.services.workers.Worker;
    import cz.inovatika.kramerius.services.workers.WorkerFinisher;
    import cz.inovatika.kramerius.services.workers.factories.WorkerFactory;
    import org.w3c.dom.Document;
    import org.xml.sax.SAXException;

    import javax.xml.parsers.ParserConfigurationException;
    import java.io.File;
    import java.io.FileInputStream;
    import java.io.IOException;
    import java.time.Duration;
    import java.time.LocalDate;
    import java.time.LocalDateTime;
    import java.time.LocalTime;
    import java.time.format.DateTimeFormatter;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.concurrent.BrokenBarrierException;
    import java.util.concurrent.CyclicBarrier;
    import java.util.logging.Level;
    import java.util.logging.Logger;

    /**
     * Main class for command line utility
     */
    public class Migration {

        public static final Logger LOGGER = Logger.getLogger(Migration.class.getName());

        protected Client client;
        protected WorkerFinisher finisher;
        protected ProcessIterator iterator;
        protected WorkerFactory workerFactory;


        public Migration() throws MigrateSolrIndexException {
            super();
            this.client = buildJerseyClient();
        }

        protected Client buildJerseyClient() {
            ClientConfig cc = new DefaultClientConfig();
            cc.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
            cc.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
            return Client.create(cc);
        }


        public void migrate(File configFile) throws MigrateSolrIndexException, IllegalAccessException, InstantiationException, ClassNotFoundException, IOException, ParserConfigurationException, SAXException, NoSuchMethodException {
            LOGGER.info(String.format("Loading from configuration %s", configFile.getAbsolutePath()));
            Document document = XMLUtils.parseDocument(new FileInputStream(configFile));
            ProcessConfig config = ProcessConfigParser.parse(document.getDocumentElement());

            ProcessIteratorFactory processIteratorFactory = ProcessIteratorFactory.create(config.getIteratorConfig().getFactoryClz());

            this.iterator = processIteratorFactory.createProcessIterator( config.getIteratorConfig(), this.client);
            this.workerFactory = WorkerFactory.create(config.getWorkerConfig());

            this.finisher = this.workerFactory.createFinisher(config, this.client);

            final List<Worker>  worksWhatHasToBeDone = new ArrayList<>();
            try {
                this.iterator.iterate(client, (List<IterationItem> idents)->{
                    addNewWorkToWorkers(config, this.iterator, worksWhatHasToBeDone, idents, config.getWorkingTime());
                }, ()-> {
                    finishRestWorkers(worksWhatHasToBeDone, config.getWorkingTime());
                });

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                this.finisher.exceptionDuringCrawl(e);
                throw new MigrateSolrIndexException(e);
            } finally {
                // stop process
                if (finisher != null)  this.finisher.finish();
            }
        }

        public static void main(String[] args)  {
            if (args.length > 0 ) {
                for (String arg : args) {
                    try {
                        Migration migr = new Migration();
                        migr.migrate(new File(arg));
                    } catch (MigrateSolrIndexException | IllegalAccessException | InstantiationException |
                             ClassNotFoundException | IOException | ParserConfigurationException | SAXException |
                             NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else {
                LOGGER.log(Level.INFO, "Usage: java -jar processes.jar <config-file1> <config-file2> ... ");
            }
        }

        protected void startWorkers(List<Worker> worksWhasHasToBeDone, String workingtime) throws BrokenBarrierException, InterruptedException {
            if (workingtime != null && workingtime.contains("-")) {
                LOGGER.info(String.format("Working time for this worker %s", workingtime));
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

        public boolean isInWorkingTime( String startTime, String endTime) {
            try {
                LocalDate now = LocalDate.now();
                LocalDateTime current = LocalDateTime.now();
                return isWorkingTimeImpl(startTime, endTime, now, current);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
                return false;
            }
        }

        public  boolean isWorkingTimeImpl(String startTime, String endTime, LocalDate now, LocalDateTime current) {

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

        public  void waitUntilStartWorkingTime(String startTime, String endTime) {
            try {
                LocalDate now = LocalDate.now();
                LocalDateTime current = LocalDateTime.now();

                long time =  waitUntilStartWorkingTimeImpl(startTime, endTime, now, current);
                Thread.sleep(time);

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        public long  waitUntilStartWorkingTimeImpl(String startTime, String endTime, LocalDate now,
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
            } else {
                startDateTimeLT = startDateTimeLT.plusDays(1);
                LOGGER.info("I'm shifting day to :"+startDateTimeLT);
                duration = Duration.between(current, startDateTimeLT);
                long millis = duration.toMillis();

                waitingInfo(startDateTimeLT, millis);

                return millis;
            }
        }

        protected void waitingInfo(LocalDateTime startDateTimeLT, long millis) {
            long hours = millis / 3600000;
            long minutes = (millis % 3600000) / 60000;
            long seconds = ((millis % 3600000) % 60000) / 1000;
            LOGGER.info("The date is "+startDateTimeLT+". The calculated wait time is : " + hours + " hours, " + minutes + " minutes a " + seconds + " seconds.");
        }

        protected void addNewWorkToWorkers(ProcessConfig config, ProcessIterator processIterator, List<Worker> worksWhatHasToBeDone, List<IterationItem> identifiers, String workingtime) {
            try {
                worksWhatHasToBeDone.add(createWorker(config,  processIterator,  identifiers));
                if (worksWhatHasToBeDone.size() >= config.getThreads()) {
                    startWorkers(worksWhatHasToBeDone, workingtime);
                    worksWhatHasToBeDone.clear();
                }
            } catch ( BrokenBarrierException | InterruptedException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
            }
        }

        protected Worker createWorker(ProcessConfig config, ProcessIterator iteratorInstance,List<IterationItem> identifiers) {
            try {
                return this.workerFactory.createWorker(config, iteratorInstance,  this.client,identifiers, this.finisher);
            } catch ( IllegalStateException  e ) {
                throw new RuntimeException("Cannot create worker instance "+e.getMessage());
            }
        }

        protected void finishRestWorkers(List<Worker> worksWhatHasToBeDone, String workingtime) {
            try {
                if (!worksWhatHasToBeDone.isEmpty()) {
                    startWorkers(worksWhatHasToBeDone, workingtime);
                }
            } catch (BrokenBarrierException | InterruptedException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
            }
        }
    }
