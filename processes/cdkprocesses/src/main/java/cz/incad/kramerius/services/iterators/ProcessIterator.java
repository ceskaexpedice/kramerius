package cz.incad.kramerius.services.iterators;

import com.sun.jersey.api.client.Client;

/**
 * Process iterator, implemenation is reponsible for iteration over solr collection or file
 */
public interface ProcessIterator {

    public void iterate(Client client, ProcessIterationCallback iterationCallback, ProcessIterationEndCallback endCallback);
}
