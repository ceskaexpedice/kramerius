package org.kramerius.cleanup;

import cz.inovatika.dochub.CleanupStrategy;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.logging.Logger;

/**
 * ExpirationCleanupStrategy
 * @author ppodsednik
 */
public class ExpirationCleanupStrategy implements CleanupStrategy {
    public static final Logger LOGGER = Logger.getLogger(ExpirationCleanupStrategy.class.getName());

    private final int expirationHours;


    public ExpirationCleanupStrategy(int expirationHours) {
        this.expirationHours = expirationHours;
        Instant expirationThreshold = Instant.now().minus(expirationHours, ChronoUnit.HOURS);
        LOGGER.info("ExpirationCleanupStrategy created with expirationHours=" + expirationHours + " and expirationThreshold=" + expirationThreshold +" everything before this time will be deleted");
    }

    @Override
    public boolean shouldDelete(Path file, BasicFileAttributes attrs) {
        Instant expirationThreshold = Instant.now().minus(expirationHours, ChronoUnit.HOURS);
        return attrs.lastModifiedTime().toInstant().isBefore(expirationThreshold);
    }

}