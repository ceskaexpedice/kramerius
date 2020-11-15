package cz.incad.kramerius.services.iterators;

import com.sun.jersey.api.client.Client;

public interface ProcessIterator {

    public void iterate(Client client, ProcessIterationCallback iterationCallback, ProcessIterationEndCallback endCallback);
}
