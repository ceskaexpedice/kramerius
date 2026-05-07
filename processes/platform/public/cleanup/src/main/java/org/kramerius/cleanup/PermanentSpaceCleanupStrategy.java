package org.kramerius.cleanup;

import cz.inovatika.dochub.CleanupStrategy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class PermanentSpaceCleanupStrategy implements CleanupStrategy {
    private final long maxSpaceBytes;
    private final Set<Path> filesToDelete = new HashSet<>();

    public PermanentSpaceCleanupStrategy(long maxSpaceBytes, Path rootPath) throws IOException {
        this.maxSpaceBytes = maxSpaceBytes;
        calculateDeletions(rootPath);
    }

    private void calculateDeletions(Path rootPath) throws IOException {
        List<FileMeta> allFiles = new ArrayList<>();

        // Rychlý průchod pro zjištění velikostí a časů
        Files.walk(rootPath)
                .filter(Files::isRegularFile)
                .forEach(p -> {
                    try {
                        BasicFileAttributes attrs = Files.readAttributes(p, BasicFileAttributes.class);
                        allFiles.add(new FileMeta(p, attrs.size(), attrs.lastModifiedTime().toMillis()));
                    } catch (IOException ignored) {}
                });

        long currentTotalSize = allFiles.stream().mapToLong(f -> f.size).sum();

        if (currentTotalSize > maxSpaceBytes) {
            // Seřadíme od nejstarších
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