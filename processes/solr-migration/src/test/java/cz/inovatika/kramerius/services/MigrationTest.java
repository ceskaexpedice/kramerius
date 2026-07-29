package cz.inovatika.kramerius.services;

import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.inovatika.kramerius.services.config.MigrationConfig;
import cz.inovatika.kramerius.services.iterators.ApacheHTTPRequestEnricher;
import cz.inovatika.kramerius.services.iterators.IterationItem;
import cz.inovatika.kramerius.services.iterators.MigrationIterationCallback;
import cz.inovatika.kramerius.services.iterators.MigrationIterationEndCallback;
import cz.inovatika.kramerius.services.iterators.MigrationIterator;
import cz.inovatika.kramerius.services.iterators.MigrationIteratorFactory;
import cz.inovatika.kramerius.services.iterators.config.SolrIteratorConfig;
import cz.inovatika.kramerius.services.iterators.factories.SolrIteratorFactory;
import cz.inovatika.kramerius.services.workers.MigrationIndexFeeder;
import cz.inovatika.kramerius.services.workers.MigrationIndexFeederContext;
import cz.inovatika.kramerius.services.workers.MigrationIndexFeederFinisher;
import cz.inovatika.kramerius.services.workers.factories.MigrationIndexFeederFactory;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MigrationTest {

    @Before
    public void resetRecordingFactories() {
        RecordingIteratorFactory.reset();
        RecordingFeederFactory.reset();
    }

    @Test
    public void migrateRunsIteratorFeedersAndFinisher() throws Exception {
        File config = writeConfig(false);

        new Migration().migrate(config);

        Assert.assertEquals(1, RecordingIteratorFactory.createdIterators);
        Assert.assertEquals(1, RecordingFeederFactory.createdFinishers);
        Assert.assertEquals(2, RecordingFeederFactory.createdFeeders);
        Assert.assertEquals(2, RecordingFeederFactory.processedBatches.size());
        Assert.assertEquals(Arrays.asList("uuid:1", "uuid:2"), RecordingFeederFactory.processedBatches.get(0));
        Assert.assertEquals(Arrays.asList("uuid:3"), RecordingFeederFactory.processedBatches.get(1));
        Assert.assertEquals(1, RecordingFeederFactory.finished);
        Assert.assertEquals(1, RecordingIteratorFactory.endCallbacks);
        Assert.assertEquals(0, RecordingFeederFactory.exceptions);
        Assert.assertEquals("secret-api-key", RecordingFeederFactory.enrichedApiKey);
    }

    @Test
    public void migrateReportsFeederExceptionFinishesAndRethrows() throws Exception {
        File config = writeConfig(true);

        try {
            new Migration().migrate(config);
            Assert.fail("Expected MigrateSolrIndexException");
        } catch (MigrateSolrIndexException expected) {
            Assert.assertTrue(expected.getCause() instanceof RuntimeException);
            Assert.assertEquals("Feeder processing failed; aborting migration", expected.getCause().getMessage());
        }

        Assert.assertEquals(1, RecordingFeederFactory.createdFeeders);
        Assert.assertEquals(1, RecordingFeederFactory.processAttempts);
        Assert.assertEquals(2, RecordingFeederFactory.exceptions);
        Assert.assertEquals(1, RecordingFeederFactory.finished);
    }

    @Test
    public void migrateReportsIteratorExceptionFinishesAndRethrows() throws Exception {
        RecordingIteratorFactory.throwDuringIteration = true;
        File config = writeConfig(false);

        try {
            new Migration().migrate(config);
            Assert.fail("Expected MigrateSolrIndexException");
        } catch (MigrateSolrIndexException expected) {
            Assert.assertTrue(expected.getCause() instanceof RuntimeException);
            Assert.assertEquals("iterator failure", expected.getCause().getMessage());
        }

        Assert.assertEquals(1, RecordingFeederFactory.createdFinishers);
        Assert.assertEquals(1, RecordingFeederFactory.exceptions);
        Assert.assertEquals(1, RecordingFeederFactory.finished);
    }

    @Test
    public void workingTimeHandlesIntervalsInsideOneDayAndAcrossMidnight() throws Exception {
        Migration migration = new Migration();

        Assert.assertTrue(migration.isWorkingTimeImpl("8:00", "16:00",
                java.time.LocalDate.of(2026, 6, 9),
                java.time.LocalDateTime.of(2026, 6, 9, 12, 0)));
        Assert.assertFalse(migration.isWorkingTimeImpl("8:00", "16:00",
                java.time.LocalDate.of(2026, 6, 9),
                java.time.LocalDateTime.of(2026, 6, 9, 7, 59)));
        Assert.assertTrue(migration.isWorkingTimeImpl("22:00", "6:00",
                java.time.LocalDate.of(2026, 6, 9),
                java.time.LocalDateTime.of(2026, 6, 9, 23, 0)));
        Assert.assertTrue(migration.isWorkingTimeImpl("22:00", "6:00",
                java.time.LocalDate.of(2026, 6, 9),
                java.time.LocalDateTime.of(2026, 6, 9, 5, 0)));
        Assert.assertFalse(migration.isWorkingTimeImpl("22:00", "6:00",
                java.time.LocalDate.of(2026, 6, 9),
                java.time.LocalDateTime.of(2026, 6, 9, 12, 0)));
    }

    @Test
    public void connectionReuseIsEnabledByDefaultAndCanBeDisabledViaEnv() throws Exception {
        Migration migration = new Migration();

        Assert.assertTrue(migration.isConnectionReuseEnabled(new HashMap<>()));

        Map<String, String> env = new HashMap<>();
        env.put(Migration.HTTP_CONNECTION_REUSE, "false");

        Assert.assertFalse(migration.isConnectionReuseEnabled(env));
    }

    private File writeConfig(boolean feederThrows) throws IOException {
        File config = File.createTempFile("migration-test", ".xml");
        config.deleteOnExit();
        FileWriter writer = new FileWriter(config);
        try {
            writer.write(
                    "<migration>" +
                            "<source-name>test-source</source-name>" +
                            "<name>test-migration</name>" +
                            "<type>full</type>" +
                            "<iteration>" +
                            "<iteratorFactory class='" + RecordingIteratorFactory.class.getName() + "'/>" +
                            "<url>http://iteration.example/solr</url>" +
                            "<endpoint>select</endpoint>" +
                            "<id>pid</id>" +
                            "<rows>2</rows>" +
                            "<type>CURSOR</type>" +
                            "</iteration>" +
                            "<threads>1</threads>" +
                            "<feeder>" +
                            "<feederFactory class='" + RecordingFeederFactory.class.getName() + "'/>" +
                            "<request>" +
                            "<url>http://source.example/solr</url>" +
                            "<endpoint>select</endpoint>" +
                            "<batchsize>2</batchsize>" +
                            "<apikey>secret-api-key</apikey>" +
                            "<fieldlist>pid title</fieldlist>" +
                            "<trasfrom>" + feederThrows + "</trasfrom>" +
                            "</request>" +
                            "<destination><url>http://destination.example/solr/update</url></destination>" +
                            "</feeder>" +
                    "</migration>");
        } finally {
            writer.close();
        }
        return config;
    }

    public static class RecordingIteratorFactory extends MigrationIteratorFactory {

        static int createdIterators;
        static int endCallbacks;
        static boolean throwDuringIteration;

        static void reset() {
            createdIterators = 0;
            endCallbacks = 0;
            throwDuringIteration = false;
        }

        @Override
        public MigrationIterator createMigrationIterator(SolrIteratorConfig config, CloseableHttpClient client) {
            createdIterators++;
            Assert.assertEquals("http://iteration.example/solr", config.getUrl());
            Assert.assertEquals("pid", config.getIdField());
            return new RecordingIterator();
        }
    }

    public static class RecordingIterator implements MigrationIterator {

        @Override
        public void iterate(CloseableHttpClient client, MigrationIterationCallback iterationCallback, MigrationIterationEndCallback endCallback) {
            if (RecordingIteratorFactory.throwDuringIteration) {
                throw new RuntimeException("iterator failure");
            }

            try {
                iterationCallback.call(Arrays.asList(
                        new IterationItem("uuid:1", "test-source"),
                        new IterationItem("uuid:2", "test-source")));
                iterationCallback.call(Arrays.asList(new IterationItem("uuid:3", "test-source")));
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            endCallback.end();
            RecordingIteratorFactory.endCallbacks++;
        }
    }

    public static class RecordingFeederFactory extends MigrationIndexFeederFactory {

        static int createdFeeders;
        static int createdFinishers;
        static int processAttempts;
        static int exceptions;
        static int finished;
        static String enrichedApiKey;
        static List<List<String>> processedBatches;

        static void reset() {
            createdFeeders = 0;
            createdFinishers = 0;
            processAttempts = 0;
            exceptions = 0;
            finished = 0;
            enrichedApiKey = null;
            processedBatches = new ArrayList<>();
        }

        @Override
        public MigrationIndexFeeder createFeeder(MigrationConfig migrationConfig, MigrationIterator iteratorInstance,
                                                 CloseableHttpClient client, ApacheHTTPRequestEnricher enricher,
                                                 List<IterationItem> pids, MigrationIndexFeederFinisher finisher) {
            createdFeeders++;
            HttpGet request = new HttpGet("http://source.example/solr/select");
            enricher.enrich(request);
            try {
                enrichedApiKey = request.getHeader(SolrIteratorFactory.X_API_KEY).getValue();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return new RecordingFeeder(migrationConfig, client, enricher, pids, finisher);
        }

        @Override
        public MigrationIndexFeederFinisher createFinisher(MigrationConfig migrationConfig, CloseableHttpClient client) {
            createdFinishers++;
            return new RecordingFinisher(migrationConfig, client);
        }
    }

    public static class RecordingFinisher extends MigrationIndexFeederFinisher {

        public RecordingFinisher(MigrationConfig config, CloseableHttpClient client) {
            super(config, client);
        }

        @Override
        public void exceptionDuringCrawl(Exception ex) {
            RecordingFeederFactory.exceptions++;
        }

        @Override
        public void finish() {
            RecordingFeederFactory.finished++;
        }
    }

    public static class RecordingFeeder extends MigrationIndexFeeder<TestContext> {

        public RecordingFeeder(MigrationConfig migrationConfig, CloseableHttpClient client,
                               ApacheHTTPRequestEnricher enricher, List<IterationItem> items,
                               MigrationIndexFeederFinisher finisher) {
            super(migrationConfig, client, enricher, items, finisher);
        }

        @Override
        public void process() {
            RecordingFeederFactory.processAttempts++;
            if (Boolean.parseBoolean(config.getRequestConfig().getTransform())) {
                throw new RuntimeException("feeder failure");
            }
            List<String> ids = new ArrayList<>();
            for (IterationItem item : itemsToBeProcessed) {
                ids.add(item.getId());
            }
            RecordingFeederFactory.processedBatches.add(ids);
        }

        @Override
        protected TestContext createContext(List<IterationItem> subitems) throws UnsupportedEncodingException {
            return new TestContext(subitems);
        }
    }

    public static class TestContext extends MigrationIndexFeederContext {

        public TestContext(List<IterationItem> allItems) {
            super(allItems);
        }
    }
}
