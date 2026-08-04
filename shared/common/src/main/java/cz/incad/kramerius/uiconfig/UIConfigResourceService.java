package cz.incad.kramerius.uiconfig;

import java.io.InputStream;

/**
 * UIConfigResourceService
 *
 * @author ppodsednik
 */
public interface UIConfigResourceService {

    UIConfigResourceContent load(String resourceKey);

    void save(String resourceKey, String contentType, InputStream content);

    boolean exists(String resourceKey);
}