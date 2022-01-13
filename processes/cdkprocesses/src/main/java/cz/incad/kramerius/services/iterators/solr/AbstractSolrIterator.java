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

    protected String user;
    protected String pass;


    public AbstractSolrIterator(String address, String masterQuery, String filterQuery, String endpoint, String id, String sorting, int rows, String user, String pass) {
        this.id = id;
        this.rows = rows;
        this.filterQuery = filterQuery;
        this.sorting = sorting;
        this.endpoint = endpoint;
        this.address = address;
        this.masterQuery = masterQuery;

        this.user = user;
        this.pass = pass;
    }

    public AbstractSolrIterator(String address, String masterQuery, String filterQuery, String endpoint, String id, String sorting, int rows) {
        this(address,masterQuery, filterQuery, endpoint, id, sorting, rows, null, null);
    }
}