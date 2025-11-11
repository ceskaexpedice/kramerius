package cz.inovatika.kramerius.services.workers.config.request;


/**
 * Immutable configuration for the request source (e.g., remote Solr).
 */
public class RequestConfig {


    private final String fieldList;
    private final String idIdentifier;
    private final String transform;
    private final String collectionField;
    private final boolean compositeId;
    private final String rootOfComposite;
    private final String childOfComposite;
    private final String checkUrl;
    private final String checkEndpoint;

    private final String url;
    private final String endpoint;
    private final int batchSize;

    private RequestConfig(Builder builder) {
        this.fieldList = builder.fieldList;
        this.idIdentifier = builder.idIdentifier;
        this.transform = builder.transform;
        this.collectionField = builder.collectionField;
        this.compositeId = builder.compositeId;
        this.rootOfComposite = builder.rootOfComposite;
        this.childOfComposite = builder.childOfComposite;
        this.checkUrl = builder.checkUrl;
        this.checkEndpoint = builder.checkEndpoint;

        // Inicializace nových polí
        this.url = builder.url;
        this.endpoint = builder.endpoint;
        this.batchSize = builder.batchSize;
    }

    // --- Getters ---
    public String getFieldList() { return fieldList; }
    public String getIdIdentifier() { return idIdentifier; }
    public String getTransform() { return transform; }
    public String getCollectionField() { return collectionField; }
    public boolean isCompositeId() { return compositeId; }
    public String getRootOfComposite() { return rootOfComposite; }
    public String getChildOfComposite() { return childOfComposite; }
    public String getCheckUrl() { return checkUrl; }
    public String getCheckEndpoint() { return checkEndpoint; }

    public String getUrl() { return url; }
    public String getEndpoint() { return endpoint; }
    public int getBatchSize() { return batchSize; }


    // ==========================================================
    // BUILDER PATTERN
    // ==========================================================

    public static class Builder {
        // --- Defaults ---
        private String fieldList = "*";
        private String idIdentifier = "pid";
        private String transform = null; // Default
        private String collectionField = null;
        private boolean compositeId = false;
        private String rootOfComposite = null;
        private String childOfComposite = null;
        private String checkUrl = null;
        private String checkEndpoint = null;

        private String url;
        private String endpoint;
        private int batchSize;

        public Builder fieldList(String fieldList) { this.fieldList = fieldList; return this; }
        public Builder idIdentifier(String idIdentifier) { this.idIdentifier = idIdentifier; return this; }

        public Builder url(String url) { this.url = url; return this; }
        public Builder endpoint(String endpoint) { this.endpoint = endpoint; return this; }
        public Builder batchSize(int batchSize) { this.batchSize = batchSize; return this; }

        public Builder transform(String transform) {
            this.transform = transform;
            return this;
        }
        

        public Builder collectionField(String collectionField) { this.collectionField = collectionField; return this; }
        public Builder compositeId(boolean compositeId) { this.compositeId = compositeId; return this; }
        public Builder rootOfComposite(String rootOfComposite) { this.rootOfComposite = rootOfComposite; return this; }
        public Builder childOfComposite(String childOfComposite) { this.childOfComposite = childOfComposite; return this; }
        public Builder checkUrl(String checkUrl) { this.checkUrl = checkUrl; return this; }
        public Builder checkEndpoint(String checkEndpoint) { this.checkEndpoint = checkEndpoint; return this; }

        public RequestConfig build() {
            return new RequestConfig(this);
        }
    }

    @Override
    public String toString() {
        return "RequestConfig{" +
                "fieldList='" + fieldList + '\'' +
                ", idIdentifier='" + idIdentifier + '\'' +
                ", transform='" + transform + '\'' +
                ", collectionField='" + collectionField + '\'' +
                ", compositeId=" + compositeId +
                ", rootOfComposite='" + rootOfComposite + '\'' +
                ", childOfComposite='" + childOfComposite + '\'' +
                ", checkUrl='" + checkUrl + '\'' +
                ", checkEndpoint='" + checkEndpoint + '\'' +
                '}';
    }
}