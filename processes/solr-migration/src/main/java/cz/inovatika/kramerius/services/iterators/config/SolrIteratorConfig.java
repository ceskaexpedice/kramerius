package cz.inovatika.kramerius.services.iterators.config;

import java.util.Arrays;

/**
 * Configuration class for creating and setting up Solr iterators.
 * This class encapsulates all parameters required for efficient iteration
 * over Solr query results, typically used by a {@code SolrIterator}.
 * It utilizes the Builder pattern for easy and safe instance creation,
 * ensuring the configuration is immutable once built.
 */
public class SolrIteratorConfig {

    /** The URL of the Solr server (e.g., http://localhost:8983/solr/logs). */
    private final String url;
    /** The main Solr query ('q' parameter). Defaults to "*:*" (all documents). */
    private final String masterQuery;
    /** The Solr filter query ('fq' parameter). Can be an empty string. */
    private final String filterQuery;
    /** The Solr request handler endpoint (e.g., 'select', 'search'). Can be an empty string. */
    private final String endpoint;
    /** The name of the field that serves as the unique identifier (e.g., 'id'). Crucial for correct sorting and cursor-based iteration. */
    private final String idField;
    /** The result sorting rule ('sort' parameter). E.g., 'id asc, title desc'. */
    private final String sort;
    /** The number of rows (documents) to retrieve per batch ('rows' parameter). E.g., 100 or 5000 */
    private final int rows;
    /** The type of iteration to be used @see TypeOfIteration */
    private final TypeOfIteration typeOfIteration;
    /** The list of fields to retrieve ('fl' parameter). If empty, all fields might be returned, depending on Solr settings. */
    private final String[] fieldList;

    /**
     * The fully qualified class name of the Factory responsible for creating
     * the specific Solr iterator implementation.
     */
    private final String factoryClz;
    /**
     * Optional URL to fetch a timestamp (or similar marker) that defines the
     * **starting point** for incremental downloading (e.g., the last processed time).
     * This value is typically used to build a query that retrieves only new or updated documents.
     */
    private final String timestampUrl;
    /**
     * Optional name of the Solr field containing the timestamp (or version marker)
     * used for **incremental downloading**. This field is checked against the value
     * obtained from {@code timestampUrl} to filter records.
     */
    private final String timestampField;


    private SolrIteratorConfig(Builder builder) {
        this.url = builder.url;
        this.masterQuery = builder.masterQuery;
        this.filterQuery = builder.filterQuery;
        this.endpoint = builder.endpoint;
        this.idField = builder.idField;
        this.sort = builder.sort;
        this.rows = builder.rows;
        this.typeOfIteration = builder.typeOfIteration;
        this.fieldList = builder.fieldList;
        this.timestampUrl = builder.timestampUrl;
        this.timestampField = builder.timestampField;
        this.factoryClz= builder.factoryClz;
    }

    /**
     * Returns the Solr server URL.
     * @return The Solr server URL.
     */
    public String getUrl() { return url; }
    /**
     * Returns the main Solr query ('q').
     * @return The master query string.
     */
    public String getMasterQuery() { return masterQuery; }
    /**
     * Returns the filter query ('fq').
     * @return The filter query string.
     */
    public String getFilterQuery() { return filterQuery; }
    /**
     * Returns the Solr request handler endpoint.
     * @return The endpoint string.
     */
    public String getEndpoint() { return endpoint; }
    /**
     * Returns the name of the unique identifier field.
     * @return The ID field name.
     */
    public String getIdField() { return idField; }
    /**
     * Returns the result sorting rule ('sort').
     * @return The sort rule string.
     */
    public String getSort() { return sort; }
    /**
     * Returns the number of rows (documents) per iteration step.
     * @return The rows limit.
     */
    public int getRows() { return rows; }
    /**
     * Returns the type of iteration.
     * @return The type of iteration.
     * @see TypeOfIteration
     */
    public TypeOfIteration getTypeOfIteration() { return typeOfIteration; }
    /**
     * Returns the list of fields to retrieve ('fl').
     * @return An array of field names.
     */
    public String[] getFieldList() { return fieldList; }
    /**
     * Returns the optional URL for fetching a timestamp.
     * @return The timestamp URL.
     */
    public String getTimestampUrl() { return timestampUrl; }
    /**
     * Returns the optional name of the timestamp field in Solr.
     * @return The timestamp field name.
     */
    public String getTimestampField() { return timestampField; }

    /**
     * Returns the fully qualified class name of the Factory.
     * @return The Factory class name.
     */
    public String getFactoryClz() { return factoryClz; }

    @Override
    public String toString() {
        return "SolrIteratorConfig{" +
                "url='" + url + '\'' +
                ", masterQuery='" + masterQuery + '\'' +
                ", filterQuery='" + filterQuery + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", idField='" + idField + '\'' +
                ", sort='" + sort + '\'' +
                ", rows=" + rows +
                ", typeOfIteration=" + typeOfIteration +
                ", fieldList=" + Arrays.toString(fieldList) +
                ", factoryClz='" + factoryClz + '\'' +
                ", timestampUrl='" + timestampUrl + '\'' +
                ", timestampField='" + timestampField + '\'' +
                '}';
    }

    // ==========================================================
    // BUILDER PATTERN
    // ==========================================================
    
    public static class Builder {
        private String url;
        private String masterQuery = "*:*";
        private String idField;

        private String filterQuery = "";
        private String endpoint = "";
        private String sort;
        private int rows = 100;
        private TypeOfIteration typeOfIteration = TypeOfIteration.CURSOR;
        private String[] fieldList = new String[0];
        private String timestampUrl;
        private String timestampField;

        private String factoryClz;

        public Builder(String url, String idField) {
            this.url = url;
            this.idField = idField;
            this.sort = idField + " ASC";
        }


        public Builder masterQuery(String masterQuery) {
            this.masterQuery = masterQuery;
            return this;
        }

        public Builder filterQuery(String filterQuery) {
            this.filterQuery = filterQuery;
            return this;
        }

        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }
        
        public Builder sort(String sort) {
            this.sort = sort;
            return this;
        }

        public Builder rows(int rows) {
            this.rows = rows;
            return this;
        }

        public Builder typeOfIteration(TypeOfIteration typeOfIteration) {
            this.typeOfIteration = typeOfIteration;
            return this;
        }

        public Builder fieldList(String fieldList) {
            if (fieldList != null && !fieldList.trim().isEmpty()) {
                this.fieldList = fieldList.split(",");
            }
            return this;
        }

        public SolrIteratorConfig build() {
            if (this.url == null || this.url.trim().isEmpty()) {
                throw new IllegalStateException("URL pro Solr musí být definována.");
            }
            if (this.sort == null || this.sort.trim().isEmpty()) {
                 this.sort = this.idField + " ASC";
            }
            return new SolrIteratorConfig(this);
        }

        public Builder timestampUrl(String timestampUrl) {
            this.timestampUrl = timestampUrl;
            return this;
        }

        public Builder timestampField(String timestampField) {
            this.timestampField = timestampField;
            return this;
        }

        public Builder factoryClz(String factoryClz) {
            this.factoryClz = factoryClz;
            return this;
        }

    }
}