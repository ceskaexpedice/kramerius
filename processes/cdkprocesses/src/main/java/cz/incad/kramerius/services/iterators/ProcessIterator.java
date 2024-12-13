package cz.incad.kramerius.services.iterators;

import com.sun.jersey.api.client.Client;

import cz.incad.kramerius.timestamps.TimestampStore;

/**
 * Process iterator, implemenation is reponsible for iteration over solr collection or file
 */
public interface ProcessIterator {

	public void iterate(Client client, ProcessIterationCallback iterationCallback, ProcessIterationEndCallback endCallback);

}
