package cz.incad.kramerius.uiconfig;

import java.io.InputStream;

/**
 * UIResourceService
 *
 * @author ppodsednik
 */
public interface UIResourceService {

    UIResourceContent load(String resourceKey);

    void save(String resourceKey, String contentType, InputStream content);

    boolean exists(String resourceKey);
}