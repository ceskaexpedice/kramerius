package cz.inovatika.kramerius.services;

import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.inovatika.kramerius.services.config.ProcessConfig;
import cz.inovatika.kramerius.services.config.ProcessConfigParser;
import cz.inovatika.kramerius.services.iterators.ApacheHTTPRequestEnricher;
import cz.inovatika.kramerius.services.iterators.IterationItem;
import cz.inovatika.kramerius.services.iterators.ProcessIterator;
import cz.inovatika.kramerius.services.iterators.ProcessIteratorFactory;
import cz.incad.kramerius.utils.XMLUtils;
import cz.inovatika.kramerius.services.iterators.factories.SolrIteratorFactory;
import cz.inovatika.kramerius.services.workers.MigrationIndexFeeder;
import cz.inovatika.kramerius.services.workers.MigrationIndexFeederFinisher;
//import cz.inovatika.kramerius.services.workers.config.request.RequestConfig;
import cz.inovatika.kramerius.services.workers.factories.MigrationIndexFeederFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class for command line utility
 */
public class Migration {

    public static final Logger LOGGER = Logger.getLogger(Migration.class.getName());

    //protected Client client;
    protected CloseableHttpClient client;
    protected MigrationIndexFeederFinisher finisher;
    protected ProcessIterator iterator;
    protected MigrationIndexFeederFactory migrationIndexFeederFactory;


    public Migration() throws MigrateSolrIndexException {
        super();
        this.client = buildApacheClient();
    }

//    protected Client buildJerseyClient() {
//        ClientConfig cc = new DefaultClientConfig();
//        cc.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
//        cc.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
//        return Client.create(cc);
//    }

    protected CloseableHttpClient buildApacheClient() {
        // Nastavení timeoutů (v Jersey bylo default, zde je dobré je definovat)
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(30))
                .setResponseTimeout(Timeout.ofSeconds(60))
                .build();

        return HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build();
    }

    public void migrate(File configFile) throws MigrateSolrIndexException, IllegalAccessException, InstantiationException, ClassNotFoundException, IOException, ParserConfigurationException, SAXException, NoSuchMethodException {
        LOGGER.info(String.format("Loading from configuration %s", configFile.getAbsolutePath()));
        Document document = XMLUtils.parseDocument(new FileInputStream(configFile));
        ProcessConfig config = ProcessConfigParser.parse(document.getDocumentElement());
        ProcessIteratorFactory processIteratorFactory = ProcessIteratorFactory.create(config.getIteratorConfig().getFactoryClz());

        this.iterator = processIteratorFactory.createProcessIterator(config.getIteratorConfig(), this.client);
        this.migrationIndexFeederFactory = MigrationIndexFeederFactory.create(config.getWorkerConfig());

        this.finisher = this.migrationIndexFeederFactory.createFinisher(config, this.client);

        try {
            this.iterator.iterate(client, (List<IterationItem> idents) -> {
                MigrationIndexFeeder feeder = createFeeder(config, this.iterator, idents);
                processFeederWithWorkingTimeCheck(feeder, config.getWorkingTime());
            }, () -> {
                //finishRestFeeders(worksWhatHasToBeDone, config.getWorkingTime());
            });

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            this.finisher.exceptionDuringCrawl(e);
            throw new MigrateSolrIndexException(e);
        } finally {
            // stop process
            if (finisher != null) this.finisher.finish();
        }
    }

    public static void startMigration(String configFile) {
        try {
            Migration migr = new Migration();
            migr.migrate(new File(configFile));
        } catch (MigrateSolrIndexException | IllegalAccessException | InstantiationException |
                 ClassNotFoundException | IOException | ParserConfigurationException | SAXException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        if (args.length > 0) {
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

    protected void processFeederWithWorkingTimeCheck(MigrationIndexFeeder feeder, String workingtime) {
        if (workingtime != null && workingtime.contains("-")) {
            LOGGER.info(String.format("Working time for this worker %s", workingtime));
            String[] intervalParts = workingtime.split("-");
            String startTime = intervalParts[0];
            String endTime = intervalParts[1];

            while (!isInWorkingTime(startTime, endTime)) {
                waitUntilStartWorkingTime(startTime, endTime);
            }
        }

        try {
            feeder.process();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            if (finisher != null) {
                finisher.exceptionDuringCrawl(e);
            }
        }
    }

//    protected void startWorkers(List<Worker> worksWhasHasToBeDone, String workingtime) throws BrokenBarrierException, InterruptedException {
//        if (workingtime != null && workingtime.contains("-")) {
//            LOGGER.info(String.format("Working time for this worker %s", workingtime));
//            String[] intervalParts = workingtime.split("-");
//            String startTime = intervalParts[0];
//            String endTime = intervalParts[1];
//            while (true) {
//                if (!isInWorkingTime(startTime, endTime)) {
//                    waitUntilStartWorkingTime(startTime, endTime);
//                } else {
//                    break;
//                }
//            }
//        }
//
//        // check if sleep hours
//        CyclicBarrier barrier = new CyclicBarrier(worksWhasHasToBeDone.size() + 1);
//        worksWhasHasToBeDone.stream().forEach(th -> {
//            th.setBarrier(barrier);
//            new Thread(th).start();
//        });
//        barrier.await();
//    }

    public boolean isInWorkingTime(String startTime, String endTime) {
        try {
            LocalDate now = LocalDate.now();
            LocalDateTime current = LocalDateTime.now();
            return isWorkingTimeImpl(startTime, endTime, now, current);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return false;
        }
    }

    public boolean isWorkingTimeImpl(String startTime, String endTime, LocalDate now, LocalDateTime current) {

        LocalTime startTimeLT = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("H:mm"));
        LocalDateTime startDateTimeLT = startTimeLT.atDate(now);

        LocalTime endTimeLT = LocalTime.parse(endTime, DateTimeFormatter.ofPattern("H:mm"));
        LocalDateTime endDateTimeLT = endTimeLT.atDate(now);

        if (startDateTimeLT.isAfter(endDateTimeLT)) {
            if (current.isAfter(startDateTimeLT.minusDays(1)) && current.isBefore(endDateTimeLT)) {
                return true;
            } else if (current.isAfter(startDateTimeLT) && current.isBefore(endDateTimeLT.plusDays(1))) {
                return true;
            }
        } else {
            if (current.isAfter(startDateTimeLT) && current.isBefore(endDateTimeLT)) {
                return true;
            } else {
                return false;
            }
        }

        return false;
    }

    public void waitUntilStartWorkingTime(String startTime, String endTime) {
        try {
            LocalDate now = LocalDate.now();
            LocalDateTime current = LocalDateTime.now();

            long time = waitUntilStartWorkingTimeImpl(startTime, endTime, now, current);
            Thread.sleep(time);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public long waitUntilStartWorkingTimeImpl(String startTime, String endTime, LocalDate now,
                                              LocalDateTime current) {
        LocalTime startTimeLT = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("H:mm"));
        LocalDateTime startDateTimeLT = startTimeLT.atDate(now);

        LocalTime endTimeLT = LocalTime.parse(endTime, DateTimeFormatter.ofPattern("H:mm"));
        LocalDateTime endDateTimeLT = endTimeLT.atDate(now);

        if (startDateTimeLT.isAfter(endDateTimeLT)) {
            if (current.isAfter(startDateTimeLT.minusDays(1)) && current.isBefore(endDateTimeLT)) {
                startDateTimeLT = startDateTimeLT.minusDays(1);
            } else if (current.isAfter(startDateTimeLT) && current.isBefore(endDateTimeLT.plusDays(1))) {
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
            LOGGER.info("I'm shifting day to :" + startDateTimeLT);
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
        LOGGER.info("The date is " + startDateTimeLT + ". The calculated wait time is : " + hours + " hours, " + minutes + " minutes a " + seconds + " seconds.");
    }

    protected MigrationIndexFeeder createFeeder(ProcessConfig config, ProcessIterator iteratorInstance, List<IterationItem> identifiers) {
        try {
            ApacheHTTPRequestEnricher enricher = ApacheHTTPRequestEnricher.NO_OP;
            String apiKey = config.getWorkerConfig().getRequestConfig().getApiKey();
            if (!StringUtils.isEmpty(apiKey)) {
                enricher = new ApacheHTTPRequestEnricher() {
                    @Override
                    public void enrich(HttpUriRequestBase request) {
                        request.setHeader(SolrIteratorFactory.X_API_KEY, apiKey);
                    }
                };
            }
            return this.migrationIndexFeederFactory.createFeeder(config, iteratorInstance, this.client, enricher, identifiers, this.finisher);
        } catch (IllegalStateException e) {
            throw new RuntimeException("Cannot create worker instance " + e.getMessage());
        }
    }

//    protected void finishRestFeeders(List<MigrationIndexFeeder> worksWhatHasToBeDone, String workingtime) {
//        if (!worksWhatHasToBeDone.isEmpty()) {
//            processFeederWithWorkingTimeCheck(worksWhatHasToBeDone, workingtime);
//        }
//    }
}
