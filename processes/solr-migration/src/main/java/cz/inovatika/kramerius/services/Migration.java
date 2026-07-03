package cz.inovatika.kramerius.services;

import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.inovatika.kramerius.services.config.MigrationConfig;
import cz.inovatika.kramerius.services.config.MigrationConfigParser;
import cz.inovatika.kramerius.services.iterators.ApacheHTTPRequestEnricher;
import cz.inovatika.kramerius.services.iterators.IterationItem;
import cz.inovatika.kramerius.services.iterators.MigrationIterator;
import cz.inovatika.kramerius.services.iterators.MigrationIteratorFactory;
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
import java.util.Locale;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class for command line utility
 */
public class Migration {

    public static final Logger LOGGER = Logger.getLogger(Migration.class.getName());
    public static final String HTTP_CONNECTION_REUSE = "HTTP_CONNECTION_REUSE";

    //protected Client client;
    protected CloseableHttpClient client;
    protected MigrationIndexFeederFinisher finisher;
    protected MigrationIterator iterator;
    protected MigrationIndexFeederFactory migrationIndexFeederFactory;


    public Migration() throws MigrateSolrIndexException {
        super();
        this.client = buildApacheClient(System.getenv());
    }


    protected CloseableHttpClient buildApacheClient() {
        return buildApacheClient(System.getenv());
    }

    protected CloseableHttpClient buildApacheClient(java.util.Map<String, String> env) {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(30))
                .setResponseTimeout(Timeout.ofSeconds(60))
                .build();

        boolean connectionReuseEnabled = isConnectionReuseEnabled(env);
        LOGGER.info(String.format(
                "HTTP connection reuse is %s (set %s=false to disable it)",
                connectionReuseEnabled ? "enabled" : "disabled",
                HTTP_CONNECTION_REUSE));

        var builder = HttpClients.custom()
                .setDefaultRequestConfig(config);

        if (!connectionReuseEnabled) {
            builder.setConnectionReuseStrategy((request, response, context) -> false);
        }

        return builder.build();
    }

    protected boolean isConnectionReuseEnabled(java.util.Map<String, String> env) {
        return !env.containsKey(HTTP_CONNECTION_REUSE) || Boolean.parseBoolean(env.get(HTTP_CONNECTION_REUSE));
    }

    public void migrate(File configFile) throws MigrateSolrIndexException, IllegalAccessException, InstantiationException, ClassNotFoundException, IOException, ParserConfigurationException, SAXException, NoSuchMethodException {
        LOGGER.info(String.format("Loading from configuration %s", configFile.getAbsolutePath()));
        Document document = XMLUtils.parseDocument(new FileInputStream(configFile));
        MigrationConfig config = MigrationConfigParser.parse(document.getDocumentElement());
        MigrationIteratorFactory migrationIteratorFactory = MigrationIteratorFactory.create(config.getIteratorConfig().getFactoryClz());

        this.iterator = migrationIteratorFactory.createMigrationIterator(config.getIteratorConfig(), this.client);
        this.migrationIndexFeederFactory = MigrationIndexFeederFactory.create(config.getFeederConfig());

        this.finisher = this.migrationIndexFeederFactory.createFinisher(config, this.client);
        ProgressTracker progressTracker = new ProgressTracker(this.iterator.estimateTotalDocuments(this.client));

        try {
            this.iterator.iterate(client, (List<IterationItem> items) -> {
                MigrationIndexFeeder feeder = createFeeder(config, this.iterator, items);
                processFeederWithWorkingTimeCheck(feeder, config.getWorkingTime());
                progressTracker.addProcessed(items.size());
                progressTracker.maybeReport(false);
            }, () -> {
                //finishRestFeeders(worksWhatHasToBeDone, config.getWorkingTime());
                progressTracker.maybeReport(true);
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
            throw new RuntimeException("Feeder processing failed; aborting migration", e);
        }
    }

    private static final class ProgressTracker {
        private final long totalDocuments;
        private final int reportEvery;
        private final long startedAtMillis = System.currentTimeMillis();
        private long processed;
        private long lastReportedProcessed;

        private ProgressTracker(long totalDocuments) {
            this.totalDocuments = totalDocuments;
            this.reportEvery = resolveReportEvery();
        }

        private void addProcessed(int count) {
            processed += count;
        }

        private void maybeReport(boolean force) {
            boolean shouldReport = force || processed - lastReportedProcessed >= reportEvery;
            if (!shouldReport) {
                return;
            }
            lastReportedProcessed = processed;
            long elapsedMillis = System.currentTimeMillis() - startedAtMillis;
            if (totalDocuments > 0) {
                double percent = (processed * 100.0d) / totalDocuments;
                LOGGER.info(String.format(
                        "Migration progress: %s/%s | %.1f%% | %s",
                        formatCount(processed),
                        formatCount(totalDocuments),
                        percent,
                        formatElapsed(elapsedMillis)));
            } else {
                LOGGER.info(String.format(
                        "Migration progress: %s | %s",
                        formatCount(processed),
                        formatElapsed(elapsedMillis)));
            }
        }

        private int resolveReportEvery() {
            String configured = System.getenv("MIGRATION_REPORT_EVERY");
            if (StringUtils.isBlank(configured)) {
                return 5000;
            }
            try {
                int parsed = Integer.parseInt(configured);
                return parsed > 0 ? parsed : 5000;
            } catch (NumberFormatException e) {
                return 5000;
            }
        }

        private String formatCount(long value) {
            String raw = Long.toString(value);
            StringBuilder builder = new StringBuilder(raw.length() + raw.length() / 3);
            int firstGroupLength = raw.length() % 3;
            if (firstGroupLength == 0) {
                firstGroupLength = 3;
            }
            builder.append(raw, 0, firstGroupLength);
            for (int i = firstGroupLength; i < raw.length(); i += 3) {
                builder.append(' ');
                builder.append(raw, i, i + 3);
            }
            return builder.toString();
        }

        private String formatElapsed(long elapsedMillis) {
            long totalSeconds = elapsedMillis / 1000L;
            long hours = totalSeconds / 3600L;
            long minutes = (totalSeconds % 3600L) / 60L;
            long seconds = totalSeconds % 60L;
            return String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, seconds);
        }
    }


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

    protected MigrationIndexFeeder createFeeder(MigrationConfig config, MigrationIterator iteratorInstance, List<IterationItem> identifiers) {
        try {
            ApacheHTTPRequestEnricher enricher = ApacheHTTPRequestEnricher.NO_OP;
            String apiKey = config.getFeederConfig().getRequestConfig().getApiKey();
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

}
