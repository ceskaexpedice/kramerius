package cz.incad.kramerius.services.workers.nullworker;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.Worker;
import cz.incad.kramerius.services.workers.checkexists.ExistsWorker;
import org.w3c.dom.Element;

import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NullWorker extends Worker {

    public static final Logger LOGGER = Logger.getLogger(ExistsWorker.class.getName());


    public NullWorker(Element workerElm, Client client, List<String> pids) {
        super(workerElm, client, pids);

    }

    @Override
    public void run() {
        try {
            LOGGER.info("["+Thread.currentThread().getName()+"] Null worker; processing batch "+this.pidsToBeProcessed.size());
            NullWorkerFactory.COUNTER += this.pidsToBeProcessed.size();
        } finally {
            try {
                this.barrier.await();
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(),e);
            } catch (BrokenBarrierException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(),e);
            }
        }
    }
}
