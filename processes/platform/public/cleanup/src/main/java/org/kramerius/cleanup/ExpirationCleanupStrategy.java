package org.kramerius.cleanup;

import cz.inovatika.dochub.CleanupStrategy;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * ExpirationCleanupStrategy
 * @author ppodsednik
 */
public class ExpirationCleanupStrategy implements CleanupStrategy {
    private final int expirationHours;

    public ExpirationCleanupStrategy(int expirationHours) {
        this.expirationHours = expirationHours;
    }

    @Override
    public boolean shouldDelete(Path file, BasicFileAttributes attrs) {
        Instant expirationThreshold = Instant.now().minus(expirationHours, ChronoUnit.HOURS);
        return attrs.lastModifiedTime().toInstant().isBefore(expirationThreshold);
    }

}