package cz.inovatika.kramerius.services.iterators.solr;

import cz.inovatika.kramerius.services.config.ResponseHandlingConfig;
import cz.inovatika.kramerius.services.iterators.ApacheHTTPRequestEnricher;
import cz.inovatika.kramerius.services.iterators.MigrationIterator;

public abstract class AbstractSolrIterator implements MigrationIterator {

    protected String address;
    protected String masterQuery;
    protected int rows;
    protected String filterQuery;
    protected String endpoint;
    protected String id;
    protected String sorting;
    protected String[] fieldList;

    protected ResponseHandlingConfig responseHandlingConfig;
    protected ApacheHTTPRequestEnricher enricher;


    public AbstractSolrIterator(String address, String masterQuery, String filterQuery, String endpoint, String id, String sorting, int rows, String[] fieldList, ResponseHandlingConfig responseHandlingConfig, ApacheHTTPRequestEnricher enricher) {
        this.id = id;
        this.rows = rows;
        this.filterQuery = filterQuery;
        this.sorting = sorting;
        this.endpoint = endpoint;
        this.address = address;
        this.masterQuery = masterQuery;
        this.fieldList = fieldList;
        this.responseHandlingConfig = responseHandlingConfig;
        this.enricher = enricher;
    }

    public AbstractSolrIterator(String address, String masterQuery, String filterQuery, String endpoint, String id, String sorting, int rows, ResponseHandlingConfig responseHandlingConfig, ApacheHTTPRequestEnricher enricher) {
        this( address, masterQuery, filterQuery, endpoint, id, sorting, rows, new String[]{}, responseHandlingConfig,enricher);
    }


    public String getAddress() {
        return address;
    }

    public int getRows() {
        return rows;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getId() {
        return id;
    }

    public String getMasterQuery() {
        return masterQuery;
    }

    public String getFilterQuery() {
        return filterQuery;
    }

    public String getSorting() {
        return sorting;
    }

    public ApacheHTTPRequestEnricher getEnricher() {
        return enricher;
    }

    public String[] getFieldList() {
        return fieldList;
    }
}