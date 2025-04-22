package cz.incad.kramerius.services;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.iterators.IterationItem;
import cz.incad.kramerius.services.iterators.ProcessIterator;
import cz.incad.kramerius.timestamps.TimestampStore;

import org.w3c.dom.Element;

import java.util.List;
import java.util.logging.Logger;

public abstract class WorkerFactory {

    public static final Logger LOGGER = Logger.getLogger(WorkerFactory.class.getName());

    public abstract Worker createWorker(String sourceName, String reharvestUrl, ProcessIterator iteratorInstance, Element worker, Client client, List<IterationItem> pids, WorkerFinisher finisher);

    public abstract WorkerFinisher createFinisher(String timestampUrl, Element worker, Client client);

    public static WorkerFactory create(String instanceName) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        LOGGER.info(String.format("Creating factory %s", instanceName));
        Class<WorkerFactory> aClass = (Class<WorkerFactory>) Class.forName(instanceName);
        return aClass.newInstance();
    }


}
