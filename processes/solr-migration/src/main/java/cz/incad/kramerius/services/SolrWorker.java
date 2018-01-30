package cz.incad.kramerius.services;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;

import cz.incad.kramerius.service.MigrateSolrIndexException;

public class SolrWorker implements Runnable {


    public static  Logger LOGGER = Logger.getLogger( SolrWorker.class.getName());

    private CyclicBarrier barrier;
    private Client client;
    private int start = 0;
    private int end;

    public SolrWorker(CyclicBarrier barrier, int start, int stop) {
        super();
        this.client = Client.create();
        //this.client.addFilter(new LoggingFilter(LOGGER));
        this.start = start;
        this.end = stop;
        this.barrier = barrier;

    }

    @Override
    public void run() {
        try {
            int number = this.end - this.start;
            LOGGER.info("["+Thread.currentThread().getName()+"] processing interval ("+this.start+","+this.end+")");
            int iterations = (number / MigrationUtils.configuredRowsSize()) + ((number % MigrationUtils.configuredRowsSize()) == 0 ? 0 : 1);
            for (int i = 0; i < iterations; i++) {
                int cursor = this.start+i*MigrationUtils.configuredRowsSize();
                int maximum = Math.min(cursor + MigrationUtils.configuredRowsSize(), this.end);
                
                String url = MigrationUtils.constructedQueryURL();

                Element result = MigrationUtils.querySolr(client, url,maximum - cursor, cursor);
                List<Document> batches = BatchUtils.batches(result, MigrationUtils.configuredBatchSize());
                LOGGER.fine("["+Thread.currentThread().getName()+"] sending batches :"+batches.size());
                for (Document  batch : batches) {
                    MigrationUtils.sendToDest(this.client, batch);
                }
            }
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        } catch (SAXException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        } catch (MigrateSolrIndexException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        } finally {
            try {
                barrier.await();
            } catch (InterruptedException ex) {
                return;
            } catch (BrokenBarrierException ex) {
                return;
            }
        }

    }
}
