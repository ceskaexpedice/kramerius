package cz.incad.kramerius.services;

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
        final String CONFIG_SOURCE = "/cz/incad/kramerius/services/workers/copy/K7.xml";
        final String DESTINATION_URL = "http://solr-proxy.cdk-val-kramerius.svc.cluster.local:8983/solr/search_v2";
        final String ITERATION_DL = "knav";
        final String ITERATION_ID = "compositeId";
        final String ITERATION_URL = "http://knav-tunnel.cdk-proxy.svc.cluster.local/search/api/cdk/v7.0/forward/sync/solr";
        final boolean ONLY_SHOW_CONFIGURATION = true;

        File tmp = File.createTempFile("cdk-test", ".xml");
        tmp.deleteOnExit();

        Migration migrationSpy = Mockito.spy(new Migration());

        try (MockedStatic<CDKMigration> cdkmStatic = Mockito.mockStatic(CDKMigration.class, Mockito.CALLS_REAL_METHODS)) {

            // Only stub the factories. All other static methods, including migrateMain, are real.
            cdkmStatic.when(CDKMigration::createTempFile).thenReturn(tmp);
            cdkmStatic.when(CDKMigration::createMigration).thenReturn(migrationSpy);

            CDKMigration.migrateMain(
                    CONFIG_SOURCE,
                    DESTINATION_URL,
                    ITERATION_DL,
                    ITERATION_ID,
                    ITERATION_URL,
                    ONLY_SHOW_CONFIGURATION
            );

            verify(migrationSpy, never()).migrate(any(File.class));

            String written = FileUtils.readFileToString(tmp, StandardCharsets.UTF_8);
            assertTrue(written.contains(DESTINATION_URL));
            assertTrue(written.contains(ITERATION_DL));
            assertTrue(written.contains(ITERATION_ID));
            assertTrue(written.contains(ITERATION_URL));
        }
    }
}
