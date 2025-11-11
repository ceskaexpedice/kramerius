package cz.inovatika.kramerius.services.workers;

import com.sun.jersey.api.client.Client;
import cz.inovatika.kramerius.services.config.ProcessConfig;
import cz.inovatika.kramerius.services.iterators.IterationItem;
import cz.incad.kramerius.utils.XMLUtils;
import cz.inovatika.kramerius.services.iterators.ProcessIterator;
import cz.inovatika.kramerius.services.workers.config.WorkerConfig;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Worker implements Runnable {

    public static final Logger LOGGER = Logger.getLogger(Worker.class.getName());

    /** Client for external service communication. */
    //TODO: Replace by apache client
    protected Client client;

    /** List of iteration items to be processed by this worker. */
    protected List<IterationItem> itemsToBeProcessed;

    /** Barrier for synchronizing workers before processing new tasks. */
    protected CyclicBarrier barrier;

    protected ProcessConfig processConfig;
    protected WorkerConfig config;


    /** Finalization handler triggered when the worker completes its task. */
    protected WorkerFinisher finisher;

    public Worker(ProcessConfig processConfig,  Client client, List<IterationItem> items, WorkerFinisher finisher) {
        super();
        this.finisher = finisher;
        this.client = client;
        this.itemsToBeProcessed = items;
        this.config = processConfig.getWorkerConfig();
        this.processConfig = processConfig;
    }

    /**
     * Gets the barrier used for synchronizing workers before processing new tasks.
     *
     * @return The {@link CyclicBarrier} instance.
     */
    public CyclicBarrier getBarrier() {
        return barrier;
    }

    /**
     * Sets the barrier used for synchronizing workers before processing new tasks.
     *
     * @param barrier The {@link CyclicBarrier} instance.
     */
    public void setBarrier(CyclicBarrier barrier) {
        this.barrier = barrier;
    }
}
