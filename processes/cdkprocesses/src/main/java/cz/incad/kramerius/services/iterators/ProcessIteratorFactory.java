package cz.incad.kramerius.services.iterators;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.Worker;
import cz.incad.kramerius.services.WorkerFactory;
import cz.incad.kramerius.services.WorkerFinisher;
import org.w3c.dom.Element;

import java.util.List;
import java.util.logging.Logger;

public abstract class ProcessIteratorFactory {

    public static final Logger LOGGER = Logger.getLogger(ProcessIteratorFactory.class.getName());

    public abstract ProcessIterator createProcessIterator(String timestamp,Element iterator, Client client);

    public static ProcessIteratorFactory create(String instanceName) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        LOGGER.info(String.format("Creating factory %s", instanceName));
        Class<ProcessIteratorFactory> aClass = (Class<ProcessIteratorFactory>) Class.forName(instanceName);
        return aClass.newInstance();
    }

}
