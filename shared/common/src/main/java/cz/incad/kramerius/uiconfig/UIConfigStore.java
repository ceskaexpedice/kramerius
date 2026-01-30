package cz.incad.kramerius.uiconfig;

import java.io.IOException;
import java.io.InputStream;

public interface UIConfigStore {

    InputStream load(UIConfigType type) throws IOException;

    void save(UIConfigType type, InputStream json) throws IOException;

    boolean exists(UIConfigType type);
}
