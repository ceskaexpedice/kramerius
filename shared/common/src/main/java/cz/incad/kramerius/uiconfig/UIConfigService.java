package cz.incad.kramerius.uiconfig;

import java.io.IOException;
import java.io.InputStream;

/**
 * UIConfigService
 * @author ppodsednik
 */
public interface UIConfigService {

    InputStream load(UIConfigType type);

    void save(UIConfigType type, InputStream json);

    boolean exists(UIConfigType type);
}
