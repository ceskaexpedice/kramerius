package cz.incad.kramerius.plugin;

import cz.inovatika.kramerius.services.Migration;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class CDKMIgrationTest {

    @Test
    public void testShowConfigOnly() throws Exception {
        CDKMigrationTestConfig config = CDKMigrationTestConfig.load();

        File tmp = File.createTempFile("cdk-test", ".xml");
        tmp.deleteOnExit();

        Migration migrationSpy = Mockito.spy(new Migration());

        try (MockedStatic<CDKMigration> cdkmStatic = Mockito.mockStatic(CDKMigration.class, Mockito.CALLS_REAL_METHODS)) {

            // Only stub the factories. All other static methods, including migrateMain, are real.
            cdkmStatic.when(CDKMigration::createTempFile).thenReturn(tmp);
            cdkmStatic.when(CDKMigration::createMigration).thenReturn(migrationSpy);


            CDKMigration.migrateMain(
                    config.configSource(),
                    config.destinationUrl(),
                    config.iterationDl(),
                    config.iterationId(),
                    config.iterationUrl(),
                    config.iterationFQuery(),
                    config.iterationApiKey(),
                    config.iterationWorkingtime(),
                    true
            );

            verify(migrationSpy, never()).migrate(any(File.class));

            String written = FileUtils.readFileToString(tmp, StandardCharsets.UTF_8);
            assertTrue(written.contains(config.destinationUrl()));
            assertTrue(written.contains(config.iterationDl()));
            assertTrue(written.contains(config.iterationId()));
            assertTrue(written.contains(config.iterationUrl()));
        }
    }

    @Test
    public void testShowConfigOnlyWithExtendedParameters() throws Exception {
        CDKMigrationTestConfig config = CDKMigrationTestConfig.load();

        File tmp = File.createTempFile("cdk-test-extended", ".xml");
        tmp.deleteOnExit();

        try (MockedStatic<CDKMigration> cdkmStatic = Mockito.mockStatic(CDKMigration.class, Mockito.CALLS_REAL_METHODS)) {
            cdkmStatic.when(CDKMigration::createTempFile).thenReturn(tmp);

            CDKMigration.migrateMain(
                    config.configSource(),
                    config.destinationUrl(),
                    config.iterationDl(),
                    config.iterationId(),
                    config.iterationUrl(),
                    config.iterationFQuery(),
                    config.iterationApiKey(),
                    config.iterationWorkingtime(),
                    config.timestampUrl(),
                    config.comparingIdentifier(),
                    true
            );

            String written = FileUtils.readFileToString(tmp, StandardCharsets.UTF_8);
            assertTrue(written.contains(config.destinationUrl()));
            assertTrue(written.contains(config.iterationId()));
            assertTrue(written.contains(config.timestampUrl()));
            assertTrue(written.contains("<id>" + config.comparingIdentifier() + "</id>"));
        }
    }

    @Test
    public void testShowUpdateConfigOnly() throws Exception {
        CDKMigrationTestConfig config = CDKMigrationTestConfig.load();
        final String expectedTimestampUrl = config.timestampUrl() + "/" + config.iterationDl() + "/timestamp";

        File tmp = File.createTempFile("cdk-test-update", ".xml");
        tmp.deleteOnExit();

        Migration migrationSpy = Mockito.spy(new Migration());

        try (MockedStatic<CDKMigration> cdkmStatic = Mockito.mockStatic(CDKMigration.class, Mockito.CALLS_REAL_METHODS)) {
            cdkmStatic.when(CDKMigration::createTempFile).thenReturn(tmp);
            cdkmStatic.when(CDKMigration::createMigration).thenReturn(migrationSpy);

            CDKMigration.migrateMain(
                    config.updateConfigSource(),
                    config.destinationUrl(),
                    config.iterationDl(),
                    config.iterationId(),
                    config.iterationUrl(),
                    config.iterationFQuery(),
                    config.iterationApiKey(),
                    config.iterationWorkingtime(),
                    config.timestampUrl(),
                    config.comparingIdentifier(),
                    true
            );

            verify(migrationSpy, never()).migrate(any(File.class));

            String written = FileUtils.readFileToString(tmp, StandardCharsets.UTF_8);
            assertTrue(written.contains("<name>" + config.iterationDl() + "-update</name>"));
            assertTrue(written.contains("<type>update</type>"));
            assertTrue(written.contains("<timestamp>" + expectedTimestampUrl + "</timestamp>"));
            assertTrue(written.contains("<timestamp_field>indexed</timestamp_field>"));
            assertTrue(written.contains("<url>" + config.iterationUrl() + "</url>"));
            assertTrue(written.contains("<apikey>" + config.iterationApiKey() + "</apikey>"));
            assertTrue(written.contains("<id>" + config.comparingIdentifier() + "</id>"));
        }
    }
}
