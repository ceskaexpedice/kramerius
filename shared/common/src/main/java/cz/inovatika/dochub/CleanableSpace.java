package cz.inovatika.dochub;

import java.io.IOException;

public interface CleanableSpace {

    void cleanup(CleanupStrategy strategy) throws IOException;
}