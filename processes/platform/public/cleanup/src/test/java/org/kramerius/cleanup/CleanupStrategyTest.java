package org.kramerius.cleanup;

import cz.inovatika.dochub.CleanupStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.nio.file.attribute.BasicFileAttributes;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CleanupStrategyTest
 * @author ppodsednik
 */
class CleanupStrategyTest {

    @TempDir
    Path tempDir;

    @Test
    void testUserContextExpirationStrategy() throws IOException {
        // GIVEN: Create one fresh file and one old file
        Path freshFile = tempDir.resolve("fresh.txt");
        Path oldFile = tempDir.resolve("old.txt");

        Files.createFile(freshFile);
        Files.createFile(oldFile);

        // Set old file modification time to 3 days ago
        FileTime threeDaysAgo = FileTime.from(Instant.now().minus(3, ChronoUnit.DAYS));
        Files.setLastModifiedTime(oldFile, threeDaysAgo);

        // WHEN: Strategy is configured for 48 hours (2 days)
        ExpirationCleanupStrategy strategy = new ExpirationCleanupStrategy(48);

        // THEN:
        assertTrue(strategy.shouldDelete(oldFile, getAttrs(oldFile)), "Old file should be deleted");
        assertFalse(strategy.shouldDelete(freshFile, getAttrs(freshFile)), "Fresh file should be kept");
    }

    @Test
    void testPermanentSpaceSizeLimitStrategy() throws IOException {
        // GIVEN: Create three files of 1KB each
        Path file1 = tempDir.resolve("file1.txt");
        Path file2 = tempDir.resolve("file2.txt");
        Path file3 = tempDir.resolve("file3.txt");

        // Write 1024 bytes to each
        byte[] data = new byte[1024];
        Files.write(file1, data);
        Files.write(file2, data);
        Files.write(file3, data);

        // Set modification times so file1 is oldest, file3 is newest
        Instant now = Instant.now();
        Files.setLastModifiedTime(file1, FileTime.from(now.minus(10, ChronoUnit.MINUTES)));
        Files.setLastModifiedTime(file2, FileTime.from(now.minus(5, ChronoUnit.MINUTES)));
        Files.setLastModifiedTime(file3, FileTime.from(now));

        // WHEN: Strategy limit is set to 1500 bytes (should delete oldest to fit)
        // file1 (1KB) + file2 (1KB) + file3 (1KB) = 3KB total.
        // To get under 1.5KB, it must delete file1 and file2.
        SizeLimitCleanupStrategy strategy = new SizeLimitCleanupStrategy(1500, tempDir);

        // THEN:
        assertTrue(strategy.shouldDelete(file1, getAttrs(file1)), "Oldest file should be deleted");
        assertTrue(strategy.shouldDelete(file2, getAttrs(file2)), "Second oldest should be deleted to fit limit");
        assertTrue(strategy.shouldDelete(file3, getAttrs(file3)), "Newest file should be deleted as well");
    }

    @Test
    void testForceEmptyStrategy() throws IOException {
        Path someFile = tempDir.resolve("any.txt");
        Files.createFile(someFile);

        // Lambda strategy: always return true
        CleanupStrategy strategy = (file, attrs) -> true;

        assertTrue(strategy.shouldDelete(someFile, getAttrs(someFile)));
    }

    // Helper method to get attributes for the strategy
    private BasicFileAttributes getAttrs(Path path) throws IOException {
        return Files.readAttributes(path, BasicFileAttributes.class);
    }
}