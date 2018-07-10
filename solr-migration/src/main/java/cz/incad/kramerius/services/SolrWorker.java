package cz.incad.kramerius.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import cz.incad.kramerius.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;

import cz.incad.kramerius.service.MigrateSolrIndexException;

public class SolrWorker implements Runnable {


    public static  Logger LOGGER = Logger.getLogger(SolrWorker.class.getName());

    private Client client;
    private List<String> pidsToBeProcessed;

    private CyclicBarrier barrier;

    public SolrWorker( Client client, List<String> pids) {
        super();
        this.client = client;
        this.pidsToBeProcessed = pids;

    }

    public CyclicBarrier getBarrier() {
        return barrier;
    }

    public void setBarrier(CyclicBarrier barrier) {
        this.barrier = barrier;
    }

    @Override
    public void run() {
        try {

            LOGGER.info("["+Thread.currentThread().getName()+"] processing list of pids "+this.pidsToBeProcessed.size());
            int batchSize = MigrationUtils.configuredBatchSize();
            int batches = this.pidsToBeProcessed.size() / batchSize + (this.pidsToBeProcessed.size() % batchSize == 0 ? 0 :1);
            LOGGER.info("["+Thread.currentThread().getName()+"] creating  "+batches+" batches ");
            for (int i=0;i<batches;i++) {
                int from = i*batchSize;
                int to = from + batchSize;
                try {
                    List<String> subpids = pidsToBeProcessed.subList(from, Math.min(to,pidsToBeProcessed.size() ));
                    Element response = MigrationUtils.fetchDocuments(this.client, MigrationUtils.queryBaseURL(), subpids);
                    Element resultElem = XMLUtils.findElement(response, (elm) -> {
                        return elm.getNodeName().equals("result");
                    });
                    List<Document> batchDocuments = BatchUtils.batches(resultElem, MigrationUtils.configuredBatchSize());
                    for (Document  batch : batchDocuments) {
                        MigrationUtils.sendToDest(this.client, batch);
                    }
                } catch (ParserConfigurationException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                } catch (SAXException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
               }
            }
        } catch (MigrateSolrIndexException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
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
