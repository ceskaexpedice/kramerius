package cz.inovatika.kramerius.fedora;

import java.util.ArrayList;
import java.util.List;

public class ProcessingIndexQueryParameters {
    private final String queryString;
    private final String sortField;
    private final boolean ascending;
    private final int rows;
    private final int pageIndex;
    private final List<String> fieldsToFetch;

    // Private constructor to enforce the use of the Builder
    private ProcessingIndexQueryParameters(Builder builder) {
        this.queryString = builder.queryString;
        this.sortField = builder.sortField;
        this.ascending = builder.ascending;
        this.rows = builder.rows;
        this.pageIndex = builder.pageIndex;
        this.fieldsToFetch = builder.fieldsToFetch;
    }

    // Getters
    public String getQueryString() {
        return queryString;
    }

    public String getSortField() {
        return sortField;
    }

    public boolean isAscending() {
        return ascending;
    }

    public int getRows() {
        return rows;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public List<String> getFieldsToFetch() {
        return fieldsToFetch;
    }

    // Builder class
    public static class Builder {
        private String queryString;
        private String sortField;
        private boolean ascending = true; // Default sort order
        private int rows = 10;            // Default rows
        private int pageIndex = 0;       // Default page index
        private final List<String> fieldsToFetch = new ArrayList<>();

        public Builder queryString(String queryString) {
            this.queryString = queryString;
            return this;
        }

        public Builder sortField(String sortField) {
            this.sortField = sortField;
            return this;
        }

        public Builder ascending(boolean ascending) {
            this.ascending = ascending;
            return this;
        }

        public Builder rows(int rows) {
            this.rows = rows;
            return this;
        }

        public Builder pageIndex(int pageIndex) {
            this.pageIndex = pageIndex;
            return this;
        }

        public Builder addFieldToFetch(String field) {
            this.fieldsToFetch.add(field);
            return this;
        }

        public Builder fieldsToFetch(List<String> fields) {
            this.fieldsToFetch.addAll(fields);
            return this;
        }

        public ProcessingIndexQueryParameters build() {
            return new ProcessingIndexQueryParameters(this);
        }
    }
}
