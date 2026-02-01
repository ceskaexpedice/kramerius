package cz.incad.kramerius.uiconfig;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class DbUIConfigServiceTest_integration {

    private static Properties testsProperties;
    private static DbUIConfigService dbUIConfigService;
    private static DbConnectionProvider dbConnectionProvider;

    @BeforeClass
    public static void beforeAll() {
        testsProperties = IntegrationTestsUtils.loadProperties();
        dbConnectionProvider = new DbConnectionProvider(testsProperties);
        dbUIConfigService = new DbUIConfigService(dbConnectionProvider, null);
    }

    @Before
    public void beforeEach() {
        IntegrationTestsUtils.checkIntegrationTestsIgnored(testsProperties);
        createTables(dbConnectionProvider);
    }

    @AfterClass
    public static void afterAll() {
        dbConnectionProvider.close();
    }

    @Test
    public void testSaveAndLoadConfig() throws Exception {
        String json = """
            {"theme": "dark", "features": ["search", "export"]}
            """;
        dbUIConfigService.save(
                UIConfigType.GENERAL,
                new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))
        );
        assertTrue(dbUIConfigService.exists(UIConfigType.GENERAL));
        try (InputStream in = dbUIConfigService.load(UIConfigType.GENERAL)) {
            String loaded = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(json.trim(), loaded.trim());
        }
    }

    @Test
    public void testOverwriteExistingConfig() throws Exception {
        dbUIConfigService.save(
                UIConfigType.LICENSES,
                new ByteArrayInputStream("{\"v\": 1}".getBytes())
        );
        dbUIConfigService.save(
                UIConfigType.LICENSES,
                new ByteArrayInputStream("{\"v\": 2}".getBytes())
        );
        try (InputStream in = dbUIConfigService.load(UIConfigType.LICENSES)) {
            String loaded = new String(in.readAllBytes());
            assertTrue(loaded.contains("\"v\": 2"));
        }
    }

    @Test
    public void testLoadNonExistingConfigFails() {
        assertThrows(Exception.class, () ->
                dbUIConfigService.load(UIConfigType.CURATOR_LISTS)
        );
    }

    @Test
    public void testExistsWorksCorrectly() {
        assertFalse(dbUIConfigService.exists(UIConfigType.GENERAL));
        dbUIConfigService.save(
                UIConfigType.GENERAL,
                new ByteArrayInputStream("{}".getBytes())
        );
        assertTrue(dbUIConfigService.exists(UIConfigType.GENERAL));
    }

    private static void createTables(DbConnectionProvider dbConnectionProvider) {
        Connection connection = dbConnectionProvider.get();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("DROP TABLE IF EXISTS ui_config");
            preparedStatement.executeUpdate();

            InputStream is = DbUIConfigServiceTest_integration.class.getResourceAsStream("ui_config.sql");
            String sql = IOUtils.toString(is, StandardCharsets.UTF_8);
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            tryClose(connection);
        }
    }

    private static void tryClose(Connection c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

}
