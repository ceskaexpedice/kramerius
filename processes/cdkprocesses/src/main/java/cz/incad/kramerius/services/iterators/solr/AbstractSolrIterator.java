package cz.incad.kramerius.services.iterators.solr;

import cz.incad.kramerius.services.iterators.ProcessIterator;
import cz.incad.kramerius.services.iterators.timestamps.TimestampStore;

public abstract class AbstractSolrIterator implements ProcessIterator {
	
	protected TimestampStore timestampStore;

    protected String address;
    protected String masterQuery;
    protected int rows;
    protected String filterQuery;
    protected String endpoint;
    protected String id;
    protected String sorting;

    protected String user;
    protected String pass;


    public AbstractSolrIterator(TimestampStore store, String address, String masterQuery, String filterQuery, String endpoint, String id, String sorting, int rows, String user, String pass) {
        this.id = id;
        this.rows = rows;

        this.filterQuery = filterQuery;
        this.sorting = sorting;
        this.endpoint = endpoint;
        this.address = address;
        this.masterQuery = masterQuery;

        this.user = user;
        this.pass = pass;
        
        this.timestampStore = store;
    }

    public AbstractSolrIterator(TimestampStore timestampCheck,String address, String masterQuery, String filterQuery, String endpoint, String id, String sorting, int rows) {
        this(timestampCheck, address,masterQuery, filterQuery, endpoint, id, sorting, rows, null, null);
    }

	@Override
	public TimestampStore getTimestampStore() {
		return this.timestampStore;
	}
}