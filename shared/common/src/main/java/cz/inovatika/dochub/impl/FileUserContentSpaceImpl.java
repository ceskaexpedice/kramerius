package cz.inovatika.dochub.impl;

import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.dochub.*;
import org.json.JSONObject;

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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * #Sample configuration (configuration.properties)
 * <p>
 * dochub.storage.user=
 * dochub.user.expiration.hours=96
 * dochub.user.download.daily_limit=100
 */
public class FileUserContentSpaceImpl implements UserContentSpace, CleanableSpace {

    Logger LOGGER = Logger.getLogger(UserContentSpace.class.getName());

    public static final String DATA_FOLDER = "data";

    private final Path rootPath;
    private final UsageCounter usageCounter;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public FileUserContentSpaceImpl() {
        String userRoot = KConfiguration.getInstance().getConfiguration().getString("dochub.storage.user");
        if (userRoot == null || userRoot.isEmpty()) {
            throw new IllegalStateException("Configuration key 'dochub.storage.user' is missing or empty.");
        }
        Path userContentRoot = Paths.get(userRoot);
        try {
            Files.createDirectories(userContentRoot);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create user storage directory: " + userRoot, e);
        }
        this.usageCounter = new FileUsageCounterImpl(userContentRoot);
        this.rootPath = userContentRoot.resolve(DATA_FOLDER);
    }

    private void checkExpiration(String token, DocumentType type) throws IOException {
        Path targetDir = resolveTokenPath(token, type);
        Path referenceFile = targetDir.resolve("info.json");
        if (!Files.exists(referenceFile)) {
            throw new UsageException("Token information is missing for: " + token);
        }
        Instant creationTime = Files.getLastModifiedTime(referenceFile).toInstant();
        int expirationHours = KConfiguration.getInstance().getConfiguration().getInt("dochub.user.expiration.hours", 48);
        Instant expiryDate = creationTime.plus(expirationHours, ChronoUnit.HOURS);
        if (Instant.now().isAfter(expiryDate)) {
            throw new UsageException("Token is expired");
        }
    }

    @Override
    public Path getRootPath() {
        return this.rootPath;
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

    private Path resolveTokenPath(String token, DocumentType type) {
        String p1 = token.substring(0, Math.min(2, token.length()));
        String p2 = token.substring(Math.min(2, token.length()), Math.min(4, token.length()));
        String p3 = token.substring(Math.min(4, token.length()), Math.min(6, token.length()));
        return rootPath.resolve(p1).resolve(p2).resolve(p3).resolve(token).resolve(type.name().toLowerCase());
    }

    private Path resolveUserBundlesPath(String user) {
        String userHash = generateHash(user, "salt-for-user-content");
        String p1 = userHash.substring(0, Math.min(2, userHash.length()));
        String p2 = userHash.substring(Math.min(2, userHash.length()), Math.min(4, userHash.length()));
        return rootPath.resolve("users").resolve(p1).resolve(p2).resolve(userHash);
    }

    private Path resolveUserBundleInfoPath(String user, String token, DocumentType type) {
        return resolveUserBundlesPath(user).resolve(token).resolve(type.name().toLowerCase() + ".json");
    }

    private void writeBundleInfo(String user, String pid, String token, DocumentType type, Path contentPath) throws IOException {
        JSONObject infoJson = new JSONObject();
        infoJson.put("pid", pid);
        infoJson.put("user", user);
        infoJson.put("token", token);
        infoJson.put("type", type.name());
        infoJson.put("created", Instant.now().toString());
        infoJson.put("size", Files.size(contentPath));

        Files.writeString(resolveTokenPath(token, type).resolve("info.json"), infoJson.toString(), StandardCharsets.UTF_8);

        Path userBundleInfoPath = resolveUserBundleInfoPath(user, token, type);
        Files.createDirectories(userBundleInfoPath.getParent());
        Files.writeString(userBundleInfoPath, infoJson.toString(), StandardCharsets.UTF_8);
    }

    @Override
    public String storeBundle(InputStream is, String user, String pid, DocumentType type, String auditInfo) throws IOException {
        LOGGER.info("Storing bundle for user: " + user);
        String token = generateHash(user, pid);
        Path targetDir = resolveTokenPath(token, type);
        LOGGER.info("targetDir: " + targetDir);
        Files.createDirectories(targetDir);
        Path filePath = targetDir.resolve("content." + type.name().toLowerCase());
        LOGGER.info("File path: " + filePath);
        try (OutputStream os = Files.newOutputStream(filePath)) {
            is.transferTo(os);
        }
        writeBundleInfo(user, pid, token, type, filePath);
        this.usageCounter.logUsage(user, pid);

        return token;
    }

    @Override
    public List<UserContentBundle> listBundles(String user) throws IOException {
        Path userBundlesPath = resolveUserBundlesPath(user);
        List<UserContentBundle> bundles = new ArrayList<>();
        if (!Files.exists(userBundlesPath)) {
            return bundles;
        }

        try (Stream<Path> stream = Files.walk(userBundlesPath)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .forEach(path -> {
                        try {
                            JSONObject infoJson = new JSONObject(Files.readString(path, StandardCharsets.UTF_8));
                            String token = infoJson.getString("token");
                            DocumentType type = DocumentType.valueOf(infoJson.getString("type"));
                            Path contentPath = resolveTokenPath(token, type).resolve("content." + type.name().toLowerCase());
                            boolean available = Files.exists(contentPath);
                            bundles.add(new UserContentBundle(
                                    token,
                                    infoJson.getString("pid"),
                                    type,
                                    Instant.parse(infoJson.getString("created")),
                                    available ? Files.size(contentPath) : 0,
                                    available));
                        } catch (Exception e) {
                            LOGGER.warning("Cannot read user bundle info: " + path + ", " + e.getMessage());
                        }
                    });
        }

        bundles.sort(Comparator.comparing(UserContentBundle::getCreated).reversed());
        return bundles;
    }

    @Override
    public Optional<InputStream> getBundle(String token, String user, DocumentType type) throws IOException {
        String filename = "content." + type.name().toLowerCase();
        LOGGER.fine("Getting bundle for token: " + token + ", user: " + user + ", type: " + type);
        Path filePath = resolveTokenPath(token, type).resolve(filename);
        LOGGER.fine("Resolved file path: " + filePath);
        if (Files.exists(filePath)) {
            return Optional.of(Files.newInputStream(filePath));
        }
        checkAndIncrementDailyLimit(user);
        return Optional.empty();
    }

    @Override
    public boolean exists(String token, DocumentType type) {
        Path path = resolveTokenPath(token, type);
        return Files.exists(path);
    }

    @Override
    public void deleteBundle(String token, DocumentType type) throws IOException {
        Path bundlePath = resolveTokenPath(token, type);
        Path infoPath = bundlePath.resolve("info.json");
        if (Files.exists(infoPath)) {
            try {
                JSONObject infoJson = new JSONObject(Files.readString(infoPath, StandardCharsets.UTF_8));
                if (infoJson.has("user")) {
                    Files.deleteIfExists(resolveUserBundleInfoPath(infoJson.getString("user"), token, type));
                }
            } catch (Exception e) {
                LOGGER.warning("Cannot delete user bundle index for token: " + token + ", " + e.getMessage());
            }
        }
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
    public void cleanup(CleanupStrategy strategy) throws IOException {
        LOGGER.info("Starting cleanup of user content space "+rootPath.toString());
        if (!Files.exists(rootPath)) return;

        Path usersPath = rootPath.resolve("users");
        Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (dir.equals(usersPath)) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }

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

    private void checkAndIncrementDailyLimit(String user) throws UsageException, IOException {
        int limit = KConfiguration.getInstance().getConfiguration().getInt("dochub.user.download.daily_limit", 10);

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
            throw new UsageException("Denní limit " + limit + " stažení byl pro uživatele " + user + " vyčerpán.");
        }

        Files.writeString(counterFile, String.valueOf(currentCount + 1), StandardCharsets.UTF_8);
    }

    @Override
    public UsageCounter getUsageCounter() {
        return usageCounter;
    }
}
