package cz.incad.kramerius.services.iterators.solr;

import cz.incad.kramerius.services.iterators.ProcessIterator;
import cz.incad.kramerius.timestamps.TimestampStore;

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

}