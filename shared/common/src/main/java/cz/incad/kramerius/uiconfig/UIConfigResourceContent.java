package cz.incad.kramerius.uiconfig;

/**
 * UIConfigResourceContent
 * @author ppodsednik
 */
public class UIConfigResourceContent {

    private final String resourceKey;
    private final String contentType;
    private final byte[] content;

    public UIConfigResourceContent(String resourceKey, String contentType, byte[] content) {
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