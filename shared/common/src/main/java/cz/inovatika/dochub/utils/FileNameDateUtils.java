package cz.inovatika.dochub.utils;


import java.nio.file.Path;
import java.time.Instant;

public class FileNameDateUtils {

    private FileNameDateUtils() {}


    public static Instant instantFromFileName(Path path) {
        String fileName = path.getFileName().toString();

        int tIndex = fileName.indexOf('T');
        if (tIndex == -1) {
            throw new IllegalArgumentException("Invalid format: Missing 'T'");
        }
        String timeStr = fileName.substring(0, tIndex + 1) +
                fileName.substring(tIndex + 1)
                        .replace("-", ":")
                        .replace(".log", "");
        Instant fileTime = Instant.parse(timeStr);
        return fileTime;
    }
}
