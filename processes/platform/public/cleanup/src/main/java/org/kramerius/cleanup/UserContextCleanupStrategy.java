package org.kramerius.cleanup;

import cz.inovatika.dochub.CleanupStrategy;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class UserContextCleanupStrategy implements CleanupStrategy {
    private final int expirationDays;

    public UserContextCleanupStrategy(int expirationDays) {
        this.expirationDays = expirationDays;
    }

    @Override
    public boolean shouldDelete(Path file, BasicFileAttributes attrs) {
        Instant expirationThreshold = Instant.now().minus(expirationDays, ChronoUnit.DAYS);
        // Pokud je čas poslední změny starší než threshold, smažeme
        return attrs.lastModifiedTime().toInstant().isBefore(expirationThreshold);
    }
}