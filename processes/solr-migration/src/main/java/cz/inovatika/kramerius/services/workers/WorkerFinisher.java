package cz.inovatika.kramerius.services.workers;

import com.sun.jersey.api.client.Client;

import cz.incad.kramerius.utils.XMLUtils;
import cz.inovatika.kramerius.services.config.ProcessConfig;
import cz.inovatika.kramerius.services.workers.config.WorkerConfig;
import org.w3c.dom.Element;

import java.util.concurrent.CyclicBarrier;

public abstract class WorkerFinisher {

    protected Client client;
    protected CyclicBarrier barrier;
    protected ProcessConfig processConfig;

    
    public WorkerFinisher(ProcessConfig config,  /*String timestampUrl, Element workerElm,*/ Client client) {
        super();
        this.client = client;
        this.processConfig = config;
    }

    public void exceptionDuringCrawl(Exception ex) {}
    

    // inform about finish crawl
    public abstract  void finish();

    public CyclicBarrier getBarrier() {
        return barrier;
    }

    public void setBarrier(CyclicBarrier barrier) {
        this.barrier = barrier;
    }
}
