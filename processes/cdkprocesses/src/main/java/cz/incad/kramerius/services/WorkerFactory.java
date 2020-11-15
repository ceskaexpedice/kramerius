package cz.incad.kramerius.services;

import com.sun.jersey.api.client.Client;
import org.w3c.dom.Element;

import java.util.List;
import java.util.logging.Logger;

public abstract class WorkerFactory {

    public static final Logger LOGGER = Logger.getLogger(WorkerFactory.class.getName());


    public abstract Worker createWorker(Element worker, Client client, List<String> pids);

    public abstract WorkerFinisher createFinisher(Element worker, Client client);

    public static WorkerFactory create(String instanceName) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        LOGGER.info(String.format("Creating factory %s", instanceName));
        Class<WorkerFactory> aClass = (Class<WorkerFactory>) Class.forName(instanceName);
        return aClass.newInstance();
    }


}
