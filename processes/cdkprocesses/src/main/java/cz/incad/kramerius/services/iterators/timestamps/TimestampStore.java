package cz.incad.kramerius.services.iterators.timestamps;

import com.sun.jersey.api.client.Client;


/** Retrieve timestamp from store */
public interface TimestampStore {
	
	/** Return timestamp from store */
	public String retrieveTimestamp(Client cl);
	
	/** Store timestamp */
	public void storeTimestamp(Client cl, String timestamp);
}
