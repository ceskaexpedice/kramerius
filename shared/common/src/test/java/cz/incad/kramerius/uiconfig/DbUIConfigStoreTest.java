package cz.incad.kramerius.uiconfig;

import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class DbUIConfigStoreTest {

    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    static DataSource dataSource;
    UIConfigStore store;

    @BeforeAll
    static void startDb() {
        postgres.start();

        org.postgresql.ds.PGSimpleDataSource ds =
                new org.postgresql.ds.PGSimpleDataSource();

        ds.setURL(postgres.getJdbcUrl());
        ds.setUser(postgres.getUsername());
        ds.setPassword(postgres.getPassword());

        dataSource = ds;
    }

    @BeforeEach
    void setUp() throws Exception {
        try (Connection c = dataSource.getConnection();
             Statement st = c.createStatement()) {

            st.execute("""
                CREATE TABLE IF NOT EXISTS ui_config (
                    config_type TEXT PRIMARY KEY,
                    config_json JSONB NOT NULL,
                    updated_at TIMESTAMP NOT NULL
                )
            """);

            st.execute("TRUNCATE ui_config");
        }

        store = new DbUIConfigStore(dataSource);
    }

    @AfterAll
    static void stopDb() {
        postgres.stop();
    }

    @Test
    void saveAndLoadConfig() throws Exception {
        String json = """
            { "theme": "dark", "features": ["search", "export"] }
            """;

        store.save(
                UIConfigType.GENERAL,
                new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))
        );

        assertTrue(store.exists(UIConfigType.GENERAL));

        try (InputStream in = store.load(UIConfigType.GENERAL)) {
            String loaded = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(json.trim(), loaded.trim());
        }
    }

    @Test
    void overwriteExistingConfig() throws Exception {
        store.save(
                UIConfigType.LICENSES,
                new ByteArrayInputStream("{\"v\":1}".getBytes())
        );

        store.save(
                UIConfigType.LICENSES,
                new ByteArrayInputStream("{\"v\":2}".getBytes())
        );

        try (InputStream in = store.load(UIConfigType.LICENSES)) {
            String loaded = new String(in.readAllBytes());
            assertTrue(loaded.contains("\"v\":2"));
        }
    }

    @Test
    void loadNonExistingConfigFails() {
        assertThrows(Exception.class, () ->
                store.load(UIConfigType.CURATOR_LISTS)
        );
    }

    @Test
    void existsWorksCorrectly() throws Exception {
        assertFalse(store.exists(UIConfigType.GENERAL));

        store.save(
                UIConfigType.GENERAL,
                new ByteArrayInputStream("{}".getBytes())
        );

        assertTrue(store.exists(UIConfigType.GENERAL));
    }
}
