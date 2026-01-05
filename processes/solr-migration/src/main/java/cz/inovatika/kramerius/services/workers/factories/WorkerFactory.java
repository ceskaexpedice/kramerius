package cz.inovatika.kramerius.services.workers.factories;

import cz.inovatika.kramerius.services.config.ProcessConfig;
import cz.inovatika.kramerius.services.workers.WorkerFinisher;
import cz.inovatika.kramerius.services.iterators.IterationItem;
import cz.inovatika.kramerius.services.iterators.ProcessIterator;

import cz.inovatika.kramerius.services.workers.Worker;
import cz.inovatika.kramerius.services.workers.config.WorkerConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.w3c.dom.Element;

import java.util.List;
import java.util.logging.Logger;

public abstract class WorkerFactory {

    public static final Logger LOGGER = Logger.getLogger(WorkerFactory.class.getName());

    public abstract Worker createWorker(ProcessConfig processConfig, ProcessIterator iteratorInstance, CloseableHttpClient client, List<IterationItem> pids, WorkerFinisher finisher);

    public abstract WorkerFinisher createFinisher(ProcessConfig processConfig, CloseableHttpClient client);

    public static WorkerFactory create(String instanceName) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        LOGGER.info(String.format("Creating factory %s", instanceName));
        Class<WorkerFactory> aClass = (Class<WorkerFactory>) Class.forName(instanceName);
        return aClass.newInstance();
    }

    public static WorkerFactory create(WorkerConfig config) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        return create(config.getFactoryClz());
    }

}
