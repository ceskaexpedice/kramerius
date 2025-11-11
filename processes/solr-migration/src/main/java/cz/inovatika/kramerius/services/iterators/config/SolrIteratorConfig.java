package cz.inovatika.kramerius.services.iterators.config;

import java.util.Arrays;

public class SolrIteratorConfig {

    private final String url;
    private final String masterQuery;
    private final String filterQuery;
    private final String endpoint;
    private final String idField;
    private final String sort;
    private final int rows;
    private final TypeOfIteration typeOfIteration;
    private final String[] fieldList;

    private final String factoryClz;

    private final String timestampUrl;
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

    public String getUrl() { return url; }
    public String getMasterQuery() { return masterQuery; }
    public String getFilterQuery() { return filterQuery; }
    public String getEndpoint() { return endpoint; }
    public String getIdField() { return idField; }
    public String getSort() { return sort; }
    public int getRows() { return rows; }
    public TypeOfIteration getTypeOfIteration() { return typeOfIteration; }
    public String[] getFieldList() { return fieldList; }
    public String getTimestampUrl() { return timestampUrl; }
    public String getTimestampField() { return timestampField; }
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