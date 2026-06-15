package cz.incad.kramerius.plugin;

import cz.inovatika.kramerius.services.Migration;
import org.apache.commons.io.FileUtils;
import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
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
        final String ITERATION_FQUERY = "model:page";
        final String ITERATION_APIKEY = "apikey";
        final String ITERATION_WORKING_TIME = "workingtime";
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
                    ITERATION_FQUERY,
                    ITERATION_APIKEY,
                    ITERATION_WORKING_TIME,
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

    @Test
    public void testShowConfigOnlyWithExtendedParameters() throws Exception {
        final String CONFIG_SOURCE = "/cz/incad/kramerius/services/workers/copy/K7.xml";
        final String DESTINATION_URL = "http://dev-solrcloud-common.cdk-devel-solr:8983/solr/search_v2";
        final String ITERATION_DL = "knav";
        final String ITERATION_ID = "compositeId";
        final String ITERATION_URL = "http://knav-tunnel.cdk-proxy.svc.cluster.local/search/api/cdk/v7.0/forward/sync/solr";
        final String TIMESTAMP_URL = "https://api.val.ceskadigitalniknihovna.cz/search/api/admin/v7.0/connected";
        final String COMPARING_IDENTIFIER = "pid";

        File tmp = File.createTempFile("cdk-test-extended", ".xml");
        tmp.deleteOnExit();

        try (MockedStatic<CDKMigration> cdkmStatic = Mockito.mockStatic(CDKMigration.class, Mockito.CALLS_REAL_METHODS)) {
            cdkmStatic.when(CDKMigration::createTempFile).thenReturn(tmp);

            CDKMigration.migrateMain(
                    CONFIG_SOURCE,
                    DESTINATION_URL,
                    ITERATION_DL,
                    ITERATION_ID,
                    ITERATION_URL,
                    "",
                    "",
                    "",
                    TIMESTAMP_URL,
                    COMPARING_IDENTIFIER,
                    true
            );

            String written = FileUtils.readFileToString(tmp, StandardCharsets.UTF_8);
            assertTrue(written.contains(DESTINATION_URL));
            assertTrue(written.contains(ITERATION_ID));
            assertTrue(written.contains(TIMESTAMP_URL));
            assertTrue(written.contains("<id>" + COMPARING_IDENTIFIER + "</id>"));
        }
    }

    @Test
    public void testShowUpdateConfigOnly() throws Exception {
        final String CONFIG_SOURCE = "/cz/incad/kramerius/services/workers/replicate/configurations/default_k7_v2_update.xml";
        final String DESTINATION_URL = "http://localhost:8983/solr/search_cdk_v1";
        final String ITERATION_DL = "tul";
        final String ITERATION_ID = "pid";
        final String ITERATION_URL = "https://kramerius.tul.cz/search/api/cdk/v7.0/forward/sync/solr";
        final String ITERATION_APIKEY = "apikey";
        final String TIMESTAMP_URL = "http://localhost:8080/search/api/admin/v7.0/connected";
        final String EXPECTED_TIMESTAMP_URL = TIMESTAMP_URL + "/" + ITERATION_DL + "/timestamp";
        final String COMPARING_IDENTIFIER = "pid";

        File tmp = File.createTempFile("cdk-test-update", ".xml");
        tmp.deleteOnExit();

        Migration migrationSpy = Mockito.spy(new Migration());

        try (MockedStatic<CDKMigration> cdkmStatic = Mockito.mockStatic(CDKMigration.class, Mockito.CALLS_REAL_METHODS)) {
            cdkmStatic.when(CDKMigration::createTempFile).thenReturn(tmp);
            cdkmStatic.when(CDKMigration::createMigration).thenReturn(migrationSpy);

            CDKMigration.migrateMain(
                    CONFIG_SOURCE,
                    DESTINATION_URL,
                    ITERATION_DL,
                    ITERATION_ID,
                    ITERATION_URL,
                    "",
                    ITERATION_APIKEY,
                    "",
                    TIMESTAMP_URL,
                    COMPARING_IDENTIFIER,
                    true
            );

            verify(migrationSpy, never()).migrate(any(File.class));

            String written = FileUtils.readFileToString(tmp, StandardCharsets.UTF_8);
            assertTrue(written.contains("<name>" + ITERATION_DL + "-update</name>"));
            assertTrue(written.contains("<type>update</type>"));
            assertTrue(written.contains("<timestamp>" + EXPECTED_TIMESTAMP_URL + "</timestamp>"));
            assertTrue(written.contains("<timestamp_field>indexed</timestamp_field>"));
            assertTrue(written.contains("<url>" + ITERATION_URL + "</url>"));
            assertTrue(written.contains("<apikey>" + ITERATION_APIKEY + "</apikey>"));
            assertTrue(written.contains("<id>" + COMPARING_IDENTIFIER + "</id>"));
        }
    }
}
