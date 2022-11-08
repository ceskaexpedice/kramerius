package cz.incad.kramerius.services.iterators.timestamps;

import java.util.Date;

import com.sun.jersey.api.client.Client;

import cz.incad.kramerius.services.iterators.timestamps.objects.TimestampType;


/** Retrieve timestamp from store */
public interface TimestampStore {
	
	/** Return timestamp from store */
	public String retrieveTimestamp(Client cl, TimestampType type, String source);
	
	/** Store timestamp */
	public void storeTimestamp(Client cl, Date date, TimestampType type, String source);
}
