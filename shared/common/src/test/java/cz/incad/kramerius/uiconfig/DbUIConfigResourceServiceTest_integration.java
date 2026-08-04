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

/**
 * DbUIResourceServiceTest_integration
 *
 * @author ppodsednik
 */
public class DbUIConfigResourceServiceTest_integration {

    private static Properties testsProperties;
    private static DbUIConfigResourceService dbUIConfigResourceService;
    private static DbConnectionProvider dbConnectionProvider;

    @BeforeClass
    public static void beforeAll() {
        testsProperties = IntegrationTestsUtils.loadProperties();
        dbConnectionProvider = new DbConnectionProvider(testsProperties);
        dbUIConfigResourceService = new DbUIConfigResourceService(dbConnectionProvider);
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
    public void testSaveAndLoadHtmlResource() {
        String key = "licenses/dnnto/page/unauthenticated/cs";
        String html = "<html><body>Hello</body></html>";

        dbUIConfigResourceService.save(key, "text/html", new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8)));
        assertTrue(dbUIConfigResourceService.exists(key));

        UIConfigResourceContent resource = dbUIConfigResourceService.load(key);
        assertNotNull(resource);
        assertEquals(key, resource.getResourceKey());
        assertEquals("text/html", resource.getContentType());
        String loaded = new String(resource.getContent(), StandardCharsets.UTF_8);
        assertEquals(html, loaded);
    }

    @Test
    public void testOverwriteExistingResource() {
        String key = "licenses/dnnto/image/logo";
        dbUIConfigResourceService.save(key, "image/png", new ByteArrayInputStream(new byte[]{1, 2, 3}));
        dbUIConfigResourceService.save(key, "image/png", new ByteArrayInputStream(new byte[]{4, 5, 6, 7}));

        UIConfigResourceContent resource = dbUIConfigResourceService.load(key);
        assertNotNull(resource);
        assertArrayEquals(new byte[]{4, 5, 6, 7}, resource.getContent());
    }

    @Test
    public void testLoadNonExistingResourceReturnsNull() {
        UIConfigResourceContent resource = dbUIConfigResourceService.load("licenses/unknown/page/cs");
        assertNull(resource);
    }

    @Test
    public void testExistsWorksCorrectly() {
        String key = "licenses/test/page/cs";
        assertFalse(dbUIConfigResourceService.exists(key));

        dbUIConfigResourceService.save(key, "text/html", new ByteArrayInputStream("<html/>".getBytes(StandardCharsets.UTF_8)));

        assertTrue(dbUIConfigResourceService.exists(key));
    }

    @Test
    public void testOverwriteContentType() {
        String key = "licenses/dnnto/page/unauthenticated/cs";
        dbUIConfigResourceService.save(key, "text/html", new ByteArrayInputStream("<html/>".getBytes(StandardCharsets.UTF_8)));
        dbUIConfigResourceService.save(key, "text/plain", new ByteArrayInputStream("plain text".getBytes(StandardCharsets.UTF_8)));

        UIConfigResourceContent resource = dbUIConfigResourceService.load(key);

        assertNotNull(resource);
        assertEquals("text/plain", resource.getContentType());
    }

    @Test
    public void testBinaryResource() {
        String key = "licenses/dnnto/image/logo";
        byte[] image = new byte[]{
                0x01, 0x02, 0x03, 0x7F, (byte) 0xFF
        };

        dbUIConfigResourceService.save(key, "image/png", new ByteArrayInputStream(image));
        UIConfigResourceContent resource = dbUIConfigResourceService.load(key);

        assertNotNull(resource);
        assertEquals("image/png", resource.getContentType());
        assertArrayEquals(image, resource.getContent());
    }

    private static void createTables(DbConnectionProvider dbConnectionProvider) {
        Connection connection = dbConnectionProvider.get();
        try {
            PreparedStatement ps = connection.prepareStatement("DROP TABLE IF EXISTS ui_config_resource");
            ps.executeUpdate();
            InputStream is = DbUIConfigResourceServiceTest_integration.class.getResourceAsStream("ui_config_resource.sql");
            String sql = IOUtils.toString(is, StandardCharsets.UTF_8);
            ps = connection.prepareStatement(sql);
            ps.executeUpdate();
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