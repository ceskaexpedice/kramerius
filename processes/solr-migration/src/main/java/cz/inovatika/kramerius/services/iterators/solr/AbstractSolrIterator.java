package cz.inovatika.kramerius.services.iterators.solr;

import cz.inovatika.kramerius.services.iterators.ProcessIterator;

public abstract class AbstractSolrIterator implements ProcessIterator {

    protected String address;
    protected String masterQuery;
    protected int rows;
    protected String filterQuery;
    protected String endpoint;
    protected String id;
    protected String sorting;
    protected String[] fieldList;



    public AbstractSolrIterator(String address, String masterQuery, String filterQuery, String endpoint, String id, String sorting, int rows, String[] fieldList) {
        this.id = id;
        this.rows = rows;

        this.filterQuery = filterQuery;
        this.sorting = sorting;
        this.endpoint = endpoint;
        this.address = address;
        this.masterQuery = masterQuery;
        this.fieldList = fieldList;

    }

    public AbstractSolrIterator(String address, String masterQuery, String filterQuery, String endpoint, String id, String sorting, int rows) {
        this( address,masterQuery, filterQuery, endpoint, id, sorting, rows, new String[]{});
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

    public String[] getFieldList() {
        return fieldList;
    }
}