package cz.incad.kramerius.uiconfig;

/**
 * UIResource
 * @author ppodsednik
 */
public class UIResourceContent {

    private final String resourceKey;
    private final String contentType;
    private final byte[] content;

    public UIResourceContent(String resourceKey, String contentType, byte[] content) {
        this.resourceKey = resourceKey;
        this.contentType = contentType;
        this.content = content;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getContent() {
        return content;
    }

}