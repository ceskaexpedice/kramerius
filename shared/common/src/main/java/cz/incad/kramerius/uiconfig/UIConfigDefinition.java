package cz.incad.kramerius.uiconfig;

/**
 * UIConfigDefinition
 */
public class UIConfigDefinition {

    private final UIConfigType type;
    private final String key;
    private final String endpoint;
    private final String title;
    private final String schemaType;

    public UIConfigDefinition(UIConfigType type, String key, String endpoint, String title, String schemaType) {
        this.type = type;
        this.key = key;
        this.endpoint = endpoint;
        this.title = title;
        this.schemaType = schemaType;
    }

    public UIConfigType getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getTitle() {
        return title;
    }

    public String getSchemaType() {
        return schemaType;
    }
}
