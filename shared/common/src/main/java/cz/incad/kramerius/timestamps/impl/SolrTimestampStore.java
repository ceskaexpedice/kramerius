package cz.incad.kramerius.timestamps.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.timestamps.Timestamp;
import cz.incad.kramerius.timestamps.TimestampStore;

public class SolrTimestampStore implements TimestampStore {

	public static Logger LOGGER = Logger.getLogger(SolrTimestamp.class.getName());
	
    private SolrClient solrClient;

    @Inject
    public SolrTimestampStore(@Named("proxyUpdate") @Nullable SolrClient solrClient) {
    	this.solrClient = solrClient;
    }
    
	@Override
	public List<Timestamp> retrieveTimestamps(String source) {
		try {
			List<Timestamp> list = new ArrayList<>();
			SolrQuery solrQuery = new SolrQuery("name:"+source);
			int offset = 0;
			int rows = 100;
			long numFound = Integer.MAX_VALUE;
			solrQuery.setStart(offset).setRows(rows);
			solrQuery.setSort("date", ORDER.asc);
			QueryResponse response = this.solrClient.query(solrQuery);
			
			while (offset < numFound) {
			    response.getResults().forEach((doc) -> {
			    	Timestamp tmsmtp = SolrTimestamp.fromSolrDoc(doc);
			    	list.add(tmsmtp);
			    });
			    
			    offset += rows;
			    solrQuery.setStart(offset).setRows(rows);
			    response = this.solrClient.query(solrQuery);
			    numFound = response.getResults().getNumFound();
			}
			
			return list;
		} catch (SolrServerException | IOException  e) {
			LOGGER.log(Level.SEVERE,e.getMessage(),e);
			return new ArrayList<>();
		}
	}

	@Override
	public void storeTimestamp(Timestamp timestamp) throws SolrServerException, IOException {
		SolrInputDocument solrDoc = timestamp.toSolrDoc();
		this.solrClient.add(solrDoc);
		this.solrClient.commit();
	}
	

	@Override
	public void storeTimestamp(String type, String name, Date date, long indexed, long updated, long batches, long workers) throws SolrServerException, IOException {
		SolrTimestamp tmpstmp = new SolrTimestamp(name,type, date, indexed, updated, batches, workers);
		this.solrClient.add(tmpstmp.toSolrDoc());
		this.solrClient.commit();
	}
	
	

	@Override
	public Timestamp findLatest(String source) throws SolrServerException, IOException {
		SolrQuery solrQuery = new SolrQuery("name:"+source);
		int offset = 0;
		int rows = 1;
		solrQuery.setStart(offset).setRows(rows);
		solrQuery.setSort("date", ORDER.desc);
		QueryResponse response = this.solrClient.query(solrQuery);
		long numFound = response.getResults().getNumFound();
		if (numFound > 0) {
			SolrDocument solrDocument = response.getResults().get(0);
			return SolrTimestamp.fromSolrDoc(solrDocument);
		}
		return null;
	}

	
	/*
	public static void main(String[] args) throws SolrServerException, IOException, InterruptedException {
        HttpSolrClient client = new HttpSolrClient.Builder("http://localhost:8983/solr/updates").build();
        SolrTimestampStore store = new SolrTimestampStore(client);
        Timestamp findLatest = store.findLatest("knav");
        System.out.println(findLatest.getDate());
        System.out.println(findLatest.getId());
	}*/
}
