package cz.inovatika.dochub.impl;

import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.dochub.CleanupStrategy;
import cz.inovatika.dochub.DocumentType;
import cz.inovatika.dochub.UserContentSpace;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.Optional;

public class FileUserContentSpaceImpl implements UserContentSpace {

    private final Path rootPath;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public FileUserContentSpaceImpl() {
        String userRoot = KConfiguration.getInstance()
                .getConfiguration()
                .getString("dochub.storage.user");

        if (userRoot == null || userRoot.isEmpty()) {
            throw new IllegalStateException("Configuration key 'dochub.storage.user' is missing or empty.");
        }
        this.rootPath = Paths.get(userRoot);
        try {
            Files.createDirectories(this.rootPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create user storage directory: " + userRoot, e);
        }
    }
    public boolean isExpired(String token) throws IOException {
        Path expirePath = resolveTokenPath(token).resolve("expires");
        if (!Files.exists(expirePath)) {
            return true;
        }

        String dateStr = Files.readString(expirePath, StandardCharsets.UTF_8);
        Instant expiryDate = Instant.parse(dateStr);

        return Instant.now().isAfter(expiryDate);
    }
    public String generateHash(String user, String pid) {
        try {
            String input = user + "|" + pid;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hashBytes).substring(0, 24);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
    @Override
    public String storeBundle(InputStream is, String user, String pid, DocumentType type, String auditInfo) throws IOException {
        String token = generateHash(user, pid);
        Path targetDir = resolveTokenPath(token);
        Files.createDirectories(targetDir);

        int expirationHours = KConfiguration.getInstance()
                .getConfiguration()
                .getInt("dochub.user.expiration.hours", 24);
        Instant expiresAt = Instant.now().plus(expirationHours, ChronoUnit.HOURS);

        Path filePath = targetDir.resolve("content." + type.name().toLowerCase());
        try (OutputStream os = Files.newOutputStream(filePath)) {
            is.transferTo(os);
        }

        Files.writeString(targetDir.resolve("stamp.txt"), auditInfo, StandardCharsets.UTF_8);
        Files.writeString(targetDir.resolve("expires"), expiresAt.toString(), StandardCharsets.UTF_8);

        return token;
    }

    private Path resolveTokenPath(String token) {
        String p1 = token.substring(0, Math.min(2, token.length()));
        String p2 = token.substring(Math.min(2, token.length()), Math.min(4, token.length()));
        String p3 = token.substring(Math.min(4, token.length()), Math.min(6, token.length()));
        return rootPath.resolve(p1).resolve(p2).resolve(p3).resolve(token);
    }

    @Override
    public Optional<InputStream> getBundle(String token, String user) throws IOException {
        Path filePath = resolveTokenPath(token).resolve("content.pdf");
        if (Files.exists(filePath)) {
            return Optional.of(Files.newInputStream(filePath));
        }
        if (isExpired(token)) {
            throw new IOException("Doesnt exist or is expired");
        }
        checkAndIncrementDailyLimit(user,2);
        return Optional.empty();
    }

    @Override
    public boolean exists(String token) {

        return Files.exists(resolveTokenPath(token));
    }

    @Override
    public void deleteBundle(String token) throws IOException {
        Path bundlePath = resolveTokenPath(token);

        if (Files.exists(bundlePath)) {
            try (var stream = Files.walk(bundlePath)) {
                stream.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        });
            } catch (UncheckedIOException e) {
                throw e.getCause();
            }
        }
    }

    @Override
    public String getToken(String pid, String user) {
        return generateHash(user, pid);
    }

    @Override
    public Optional<String> getAuditInfo(String token) throws IOException {
        Path stampPath = resolveTokenPath (token).resolve("stamp.txt");
        if (Files.exists(stampPath)) {
            return Optional.of(Files.readString(stampPath, StandardCharsets.UTF_8));
        }
        return Optional.empty();
    }


    public void cleanup(CleanupStrategy strategy) throws IOException {
        if (!Files.exists(rootPath)) return;

        Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (strategy.shouldDelete(file, attrs)) {
                    Files.delete(file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (!dir.equals(rootPath)) {
                    try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
                        if (!ds.iterator().hasNext()) {
                            Files.delete(dir);
                        }
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private Path resolveUserStatsPath(String user) {
        String userHash = generateHash(user, "salt-for-stats");
        String p1 = userHash.substring(0, 2);
        String p2 = userHash.substring(2, 4);

        return rootPath.resolve("users")
                .resolve(p1)
                .resolve(p2)
                .resolve(user)
                .resolve(LocalDate.now().toString() + ".count");
    }

    private Path resolveDailyCounterPath(String user) {
        String datePart = LocalDate.now().toString();
        return rootPath.resolve("counters").resolve(datePart).resolve(user + ".count");
    }

    private void checkAndIncrementDailyLimit(String user, int limit) throws IOException {
        Path counterFile = resolveDailyCounterPath(user);
        Files.createDirectories(counterFile.getParent());

        int currentCount = 0;
        if (Files.exists(counterFile)) {
            try {
                currentCount = Integer.parseInt(Files.readString(counterFile, StandardCharsets.UTF_8).trim());
            } catch (NumberFormatException e) {
                currentCount = 0;
            }
        }

        if (currentCount >= limit) {
            throw new IOException("Denní limit 2 stažení byl pro uživatele " + user + " vyčerpán.");
        }

        Files.writeString(counterFile, String.valueOf(currentCount + 1), StandardCharsets.UTF_8);
    }
}