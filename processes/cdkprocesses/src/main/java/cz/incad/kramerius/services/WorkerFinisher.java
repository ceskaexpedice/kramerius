package cz.incad.kramerius.services;

import com.sun.jersey.api.client.Client;

import cz.incad.kramerius.timestamps.TimestampStore;
import cz.incad.kramerius.utils.XMLUtils;
import org.w3c.dom.Element;

import java.util.concurrent.CyclicBarrier;

public abstract class WorkerFinisher {

    protected Client client;
    protected CyclicBarrier barrier;
    protected Element workerElm;
    protected String destinationUrl;
    protected String timestampUrl;

    
    public WorkerFinisher(String timestampUrl, Element workerElm, Client client) {
        super();
        this.client = client;
        this.workerElm = workerElm;
        this.timestampUrl = timestampUrl;
        
        Element destElm = XMLUtils.findElement(workerElm, "destination");
        if (destElm != null) {
            Element urlElm = XMLUtils.findElement(destElm, "url");
            this.destinationUrl = urlElm != null ? urlElm.getTextContent() : "";
        }

    }

    
    public void exceptionDuringCrawl(Exception ex) {}
    
    //public void finishSuccessCrawl() {}
    
    // inform about finish crawl
    public abstract  void finish();

    public CyclicBarrier getBarrier() {
        return barrier;
    }

    public void setBarrier(CyclicBarrier barrier) {
        this.barrier = barrier;
    }
}
