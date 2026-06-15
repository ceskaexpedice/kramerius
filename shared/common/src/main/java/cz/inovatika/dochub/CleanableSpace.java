package cz.inovatika.dochub;


import java.io.IOException;
import java.nio.file.Path;

public interface CleanableSpace {

    public Path getRootPath();

    public double getConfiguredMaxLimit();

    public int getConfiguredMaxAge();

    void cleanup(CleanupStrategy strategy) throws IOException;
}