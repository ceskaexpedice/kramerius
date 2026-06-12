package cz.inovatika.dochub;


import java.io.IOException;
import java.nio.file.Path;

public interface CleanableSpace {

    public Path getRootPath();

    void cleanup(CleanupStrategy strategy) throws IOException;
}