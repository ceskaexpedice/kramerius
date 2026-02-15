package cz.inovatika.dochub.impl;

import cz.incad.kramerius.security.licenses.limits.LimitInterval;
import cz.inovatika.dochub.UsageCounter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class FileUsageCounterImpl implements UsageCounter {

    public static final Logger LOGGER = Logger.getLogger(FileUsageCounterImpl.class.getName());

    private final Path countersRoot;

    public FileUsageCounterImpl(Path rootPath) {
        this.countersRoot = rootPath.resolve("audit");
        try {
            Files.createDirectories(this.countersRoot);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create auditing folder", e);
        }
    }

    @Override
    public void logUsage(String pid, String user) {
        Path userLicenseDir = resolveUserLicenseDir(user, pid);
        String fileName = Instant.now().toString().replace(":", "-") + ".log";
        Path logFile = userLicenseDir.resolve(fileName);

        try {
            Files.createDirectories(userLicenseDir);
            Files.createFile(logFile);
            Files.writeString(logFile, user+"\n"+pid, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Cannot create log file", e);
        }
    }

    @Override
    public long getUsageCount(String user, String pid, LimitInterval interval, int value) {
        Path userLicenseDir = resolveUserLicenseDir(user, pid);
        if (!auditFolderExists(userLicenseDir)) {
            return 0;
        }

        Instant threshold = calculateThreshold(interval, value);
        LOGGER.log(Level.FINE, String.format("Current time %s &  threshold: %s", Instant.now().toString(), threshold.toString()));

        StringWriter writer = new StringWriter();

        try (Stream<Path> stream = streamFiles(userLicenseDir)) {

            List<Path> p =  stream
                    .filter(path -> {
                        try {
                                String fileName = path.getFileName().toString();

                                int tIndex = fileName.indexOf('T');
                                if (tIndex > -1) {
                                String timeStr = fileName.substring(0, tIndex + 1) +
                                        fileName.substring(tIndex + 1).replace("-", ":").replace(".log", "");
                                Instant fileTime = Instant.parse(timeStr);
                                boolean retval = fileTime.isAfter(threshold);
                                if (retval) {
                                    String line = String.format( "\tparsedTime(%s) < thresholdTime(%s)\n", fileTime.toString(),threshold.toString());
                                    writer.write(line);
                                } else {
                                    String line = String.format( "\tparsedTime(%s) > thresholdTime(%s)\n", fileTime.toString(),threshold.toString());
                                    writer.write(line);
                                }
                                return retval;
                            } else {
                                return false;
                            }
                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE, "Cannot read time from file", e);
                            return false;
                        }
                    }).toList();

            LOGGER.log(Level.FINE, String.format("Comparision result is  %s", writer.toString()));
            LOGGER.log(Level.FINE, String.format("Usage count for user '%s': %d", p, p.size()));
            return p.size();
                    //.count();
        } catch (IOException e) {
            return 0;
        }
    }

    boolean auditFolderExists(Path userLicenseDir) {
        boolean exists = Files.exists(userLicenseDir);
        LOGGER.log(Level.FINE,String.format("Testing exists %b", exists));
        return exists;
    }

    Stream<Path> streamFiles(Path userLicenseDir) throws IOException {
        return Files.list(userLicenseDir);
    }

    private Instant calculateThreshold(LimitInterval interval, int value) {
        Instant now = Instant.now();
        switch (interval) {
            case PER_MINUTE: return now.minus(value, ChronoUnit.MINUTES);
            case PER_HOUR: return now.minus(value, ChronoUnit.HOURS);
            case PER_DAY: return now.minus(value, ChronoUnit.DAYS);
            case PER_WEEK: return now.minus(value * 7L, ChronoUnit.DAYS);
            default: return now;
        }
    }

    private Path resolveUserLicenseDir(String user, String pid) {
        String folderName = generateHash(user, pid);
        String p1 = folderName.substring(0, Math.min(2, folderName.length()));
        String p2 = folderName.substring(Math.min(2, folderName.length()), Math.min(4, folderName.length()));
        String p3 = folderName.substring(Math.min(4, folderName.length()), Math.min(6, folderName.length()));
        return countersRoot.resolve(p1).resolve(p2).resolve(p3).resolve(folderName);
    }


    private String generateHash(String user, String pid) {
        try {
            String input = user + "|" + pid;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes).substring(0, 24);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not found", e);
        }
    }

}