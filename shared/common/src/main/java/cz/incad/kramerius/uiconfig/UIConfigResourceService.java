package cz.incad.kramerius.uiconfig;

import java.io.InputStream;
import java.util.List;

/**
 * UIConfigResourceService
 *
 * @author ppodsednik
 */
public interface UIConfigResourceService {

    UIConfigResourceContent load(String resourceKey);

    List<UIConfigResourceInfo> list();

    void save(String resourceKey, String contentType, InputStream content);

    void delete(String resourceKey);

    boolean exists(String resourceKey);
}
