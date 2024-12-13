package cz.incad.kramerius.timestamps;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;

import com.sun.jersey.api.client.Client;



/** Retrieve timestamp from store */
public interface TimestampStore {
	
	public List<Timestamp> retrieveTimestamps(String source) throws SolrServerException, IOException;
	
	public void storeTimestamp(Timestamp timestamp) throws SolrServerException, IOException;
	
	
	public void storeTimestamp(String type, String name, Date date, long indexed, long updated, long batches, long workers) throws SolrServerException, IOException;

	public Timestamp findLatest(String source) throws SolrServerException, IOException;
}
