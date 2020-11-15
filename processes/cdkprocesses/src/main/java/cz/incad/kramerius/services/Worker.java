package cz.incad.kramerius.services;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.utils.XMLUtils;
import org.w3c.dom.Element;

import java.util.List;
import java.util.concurrent.CyclicBarrier;

public abstract class Worker implements Runnable {

    protected Client client;
    protected List<String> pidsToBeProcessed;
    protected CyclicBarrier barrier;
    protected Element workerElm;
    protected int batchSize;
    protected String requestUrl;
    protected String requestEndpoint;

    protected String destinationUrl;

    public Worker(Element workerElm, Client client, List<String> pids) {
        super();
        this.client = client;
        this.pidsToBeProcessed = pids;
        this.workerElm = workerElm;

        Element requestElm = XMLUtils.findElement(workerElm, "request");
        if (requestElm != null) {
            Element batchsizeElm = XMLUtils.findElement(requestElm, "batchsize");
            this.batchSize = (batchsizeElm != null  && !batchsizeElm.getTextContent().trim().equals("")) ?  Integer.parseInt(batchsizeElm.getTextContent()) : 10;

            Element urlElm = XMLUtils.findElement(requestElm, "url");
            this.requestUrl = urlElm != null ? urlElm.getTextContent() : "";

            Element endpointElm = XMLUtils.findElement(requestElm, "endpoint");
            this.requestEndpoint = endpointElm != null ? endpointElm.getTextContent() : "";

            this.requestUrl = requestUrl.endsWith("/") ? requestUrl +this.requestEndpoint :  requestUrl+ "/" +requestEndpoint;
        }

        Element destElm = XMLUtils.findElement(workerElm, "destination");
        if (destElm != null) {
            Element urlElm = XMLUtils.findElement(destElm, "url");
            this.destinationUrl = urlElm != null ? urlElm.getTextContent() : "";
        }
    }

    public CyclicBarrier getBarrier() {
        return barrier;
    }

    public void setBarrier(CyclicBarrier barrier) {
        this.barrier = barrier;
    }



}
