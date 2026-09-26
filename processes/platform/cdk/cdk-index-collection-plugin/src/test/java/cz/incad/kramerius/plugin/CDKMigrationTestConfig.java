package cz.incad.kramerius.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

final class CDKMigrationTestConfig {
    private static final String RESOURCE_NAME = "/cdk-migration-test.properties";

    private final Properties properties;

    private CDKMigrationTestConfig(Properties properties) {
        this.properties = properties;
    }

    static CDKMigrationTestConfig load() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = CDKMigrationTestConfig.class.getResourceAsStream(RESOURCE_NAME)) {
            if (input == null) {
                throw new IOException("Missing test resource " + RESOURCE_NAME);
            }
            properties.load(new InputStreamReader(input, StandardCharsets.UTF_8));
        }
        return new CDKMigrationTestConfig(properties);
    }

    String configSource() {
        return required("configSource");
    }

    String updateConfigSource() {
        return required("updateConfigSource");
    }

    String destinationUrl() {
        return required("destinationUrl");
    }

    String iterationDl() {
        return required("iterationDl");
    }

    String iterationId() {
        return required("iterationId");
    }

    String iterationUrl() {
        return required("iterationUrl");
    }

    String iterationFQuery() {
        return required("iterationFQuery");
    }

    String iterationApiKey() {
        return required("iterationApiKey");
    }

    String iterationWorkingtime() {
        return required("iterationWorkingtime");
    }

    String timestampUrl() {
        return required("timestampUrl");
    }

    String comparingIdentifier() {
        return required("comparingIdentifier");
    }

    private String required(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new IllegalStateException("Missing test property " + key);
        }
        return value;
    }
}
