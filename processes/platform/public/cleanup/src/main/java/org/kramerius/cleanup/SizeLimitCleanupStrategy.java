package org.kramerius.cleanup;

import cz.inovatika.dochub.CleanupStrategy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.logging.Logger;

/**
 * SizeLimitCleanupStrategy
 * @author ppodsednik
 */
public class SizeLimitCleanupStrategy implements CleanupStrategy {

    public static final Logger LOGGER = Logger.getLogger(SizeLimitCleanupStrategy.class.getName());

    private final long maxSpaceBytes;
    private final Set<Path> filesToDelete = new HashSet<>();

    public SizeLimitCleanupStrategy(long maxSpaceBytes, Path rootPath) throws IOException {
        this.maxSpaceBytes = maxSpaceBytes;
        calculateDeletions(rootPath);
    }

    /**
     * Initial scan to collect file sizes and modification times
     * @param rootPath
     * @throws IOException
     */
    private void calculateDeletions(Path rootPath) throws IOException {
        List<FileMeta> allFiles = new ArrayList<>();
        Files.walk(rootPath)
                .filter(Files::isRegularFile)
                .forEach(p -> {
                    try {
                        BasicFileAttributes attrs = Files.readAttributes(p, BasicFileAttributes.class);
                        allFiles.add(new FileMeta(p, attrs.size(), attrs.lastModifiedTime().toMillis()));
                    } catch (IOException ignored) {
                        LOGGER.warning("Cannot read file attributes for " + p);
                    }
                });

        long currentTotalSize = allFiles.stream().mapToLong(f -> f.size).sum();
        LOGGER.info("Total size of files in " + rootPath + ": " + currentTotalSize + " and max allowed: " + maxSpaceBytes + "");
        if (currentTotalSize > maxSpaceBytes) {
            // sort from the oldest
            allFiles.sort(Comparator.comparingLong(f -> f.lastModified));
            for (FileMeta file : allFiles) {
                if (currentTotalSize > maxSpaceBytes) {
                    filesToDelete.add(file.path);
                    currentTotalSize -= file.size;
                } else {
                    break;
                }
            }
        }
    }

    @Override
    public boolean shouldDelete(Path file, BasicFileAttributes attrs) {
        return filesToDelete.contains(file);
    }

    private record FileMeta(Path path, long size, long lastModified) {}
}