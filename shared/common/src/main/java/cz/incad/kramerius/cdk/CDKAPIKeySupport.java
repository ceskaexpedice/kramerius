package cz.incad.kramerius.cdk;


import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Support class for API key management with disk persistence.
 * The key is stored in ~/.kramerius4/cdk.api.key
 */
public class CDKAPIKeySupport {

    private static final Logger LOGGER = Logger.getLogger(CDKAPIKeySupport.class.getName());
    private static final int KEY_LENGTH = 32;

    private final Path keyFilePath;

    public CDKAPIKeySupport() {
        String userHome = System.getProperty("user.home");
        this.keyFilePath = Paths.get(userHome, ".kramerius4", "cdk.api.key");
    }

    /**
     * Initializes the API key support.
     * Ensures the directory exists and the key is loaded or generated.
     */
    public final synchronized void init() {
        try {
            if (!Files.exists(keyFilePath)) {
                LOGGER.info("Initializing key & creating key file: " + keyFilePath.toString());
                String key = generateNewApiKey();
                saveApiKey(key);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize API key at: " + keyFilePath, e);
            throw new RuntimeException("API key initialization failed", e);
        }
    }

    public synchronized String getApiKey() {
        try {
            if (Files.exists(keyFilePath)) {
                String key = new String(Files.readAllBytes(keyFilePath), StandardCharsets.UTF_8).trim();
                if (!key.isEmpty()) {
                    return key;
                }
            }
            return null;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to handle API key file at: " + keyFilePath, e);
            throw new RuntimeException("Could not retrieve or create API key", e);
        }
    }

    /**
     * Validates if the provided key matches the one stored on disk.
     * * @param providedKey Key received from the request header.
     * @return true if valid, false otherwise.
     */
    public boolean isValidKey(String providedKey) {
        if (providedKey == null || providedKey.isEmpty()) {
            return false;
        }
        return getApiKey() != null && getApiKey().equals(providedKey);
    }

    /**
     * Generates a cryptographically secure random key.
     */
    private String generateNewApiKey() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[KEY_LENGTH];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Persists the key to the filesystem and ensures directory structure exists.
     */
    private void saveApiKey(String key) throws IOException {
        File parentDir = keyFilePath.getParent().toFile();
        if (!parentDir.exists()) {
            if (parentDir.mkdirs()) {
                LOGGER.info("Created directory: " + parentDir.getAbsolutePath());
            }
        }
        Files.write(keyFilePath, key.getBytes(StandardCharsets.UTF_8));
        File file = keyFilePath.toFile();
        if (file.exists()) {
            file.setReadable(false, false);
            file.setReadable(true, true);
            file.setWritable(false, false);
            file.setWritable(true, true);
        }
        LOGGER.info("New API key successfully saved to: " + keyFilePath.toAbsolutePath());
    }
}