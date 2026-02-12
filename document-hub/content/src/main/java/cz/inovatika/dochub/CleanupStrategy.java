package cz.inovatika.dochub;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public interface CleanupStrategy {

    boolean shouldDelete(Path file, BasicFileAttributes attrs);
}