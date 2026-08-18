package cz.incad.kramerius.uiconfig;

/**
 * UIConfigResourceInfo
 *
 * @author ppodsednik
 */
public class UIConfigResourceInfo {

    private final String resourceKey;
    private final String contentType;

    public UIConfigResourceInfo(String resourceKey, String contentType) {
        this.resourceKey = resourceKey;
        this.contentType = contentType;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public String getContentType() {
        return contentType;
    }
}
