package cz.incad.kramerius.services.iterators.solr;

import cz.incad.kramerius.services.iterators.ProcessIterator;

public abstract class AbstractSolrIterator implements ProcessIterator {


    protected String address;
    protected String masterQuery;
    protected int rows;
    protected String filterQuery;
    protected String endpoint;
    protected String id;
    protected String sorting;

    public AbstractSolrIterator(String address, String masterQuery, String filterQuery, String endpoint, String id, String sorting, int rows) {
        this.id = id;
        this.rows = rows;
        this.filterQuery = filterQuery;
        this.sorting = sorting;
        this.endpoint = endpoint;
        this.address = address;
        this.masterQuery = masterQuery;
    }
}
