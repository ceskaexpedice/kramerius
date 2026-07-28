package cz.incad.kramerius.plugin;

import cz.inovatika.kramerius.services.Migration;
import org.apache.commons.io.FileUtils;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class CDKUpdateMigrationTest {

    @Test
    public void testUpdateMainUsesUpdateTemplate() throws Exception {
        CDKUpdateMigrationTestConfig config = CDKUpdateMigrationTestConfig.load();
        final String expectedTimestampUrl = config.timestampUrl() + "/" + config.iterationDl() + "/timestamp";

        File tmp = File.createTempFile("cdk-test-update-main", ".xml");
        tmp.deleteOnExit();

        Migration migrationSpy = Mockito.spy(new Migration());

        try (MockedStatic<CDKUpdateMigration> cdkmStatic = Mockito.mockStatic(CDKUpdateMigration.class, Mockito.CALLS_REAL_METHODS)) {
            cdkmStatic.when(CDKUpdateMigration::createTempFile).thenReturn(tmp);
            cdkmStatic.when(CDKUpdateMigration::createMigration).thenReturn(migrationSpy);

            CDKUpdateMigration.updateMain(
                    config.destinationUrl(),
                    config.iterationDl(),
                    config.iterationId(),
                    config.iterationUrl(),
                    config.iterationFQuery(),
                    config.iterationApiKey(),
                    config.iterationWorkingtime(),
                    config.timestampUrl(),
                    config.comparingIdentifier(),
                    null,
                    true,
                    false
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

    @Test
    public void testSingleProcessMethod() {
        assertEquals(1, processMethodCount(CDKUpdateMigration.class));
    }

    private static long processMethodCount(Class<?> clazz) {
        long count = 0;
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(ProcessMethod.class)) {
                count++;
            }
        }
        return count;
    }
}
