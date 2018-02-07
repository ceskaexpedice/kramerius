package cz.incad.kramerius.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;

import cz.incad.kramerius.service.MigrateSolrIndex;
import cz.incad.kramerius.service.MigrateSolrIndexException;

public class ParallelMigrateSolrIndexImpl implements MigrateSolrIndex{
    
    public static final Logger LOGGER = Logger.getLogger(ParallelMigrateSolrIndexImpl.class.getName());
    
    private Client client;
    private CyclicBarrier barrier;
    
    public ParallelMigrateSolrIndexImpl() {
        super();
        this.client = Client.create();
        this.barrier = null;
    }

    public int init() throws ParserConfigurationException, SAXException, IOException, MigrateSolrIndexException {
        Element result = MigrationUtils.querySolr(this.client, MigrationUtils.constructedQueryURL(), 0, 0);
        String attribute = result.getAttribute("numFound");
        return Integer.parseInt(attribute);
    }
    
    
    
    @Override
    public void migrate() throws MigrateSolrIndexException {
        long start = System.currentTimeMillis();
        try {
            int numberOfResults = this.init();

            int threads = MigrationUtils.configuredNumberOfThreads();
            int forOneThread = numberOfResults/threads;

            this.barrier = new CyclicBarrier(threads+1);

            for (int i = 0; i < threads; i++) {
                int from = i*forOneThread;
                int to = (i == threads-1 ? numberOfResults :   (i+1)*forOneThread);
                SolrWorker worker = new SolrWorker(barrier, from,to);
                new Thread(worker, "migrate-"+i).start();
            }
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new MigrateSolrIndexException(e);
        } catch (SAXException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new MigrateSolrIndexException(e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new MigrateSolrIndexException(e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new MigrateSolrIndexException(e);
        } finally {
            try {
                barrier.await();
                long stop = System.currentTimeMillis();
                LOGGER.info("Finished  in "+(stop - start)+" ms");
                MigrationUtils.commit(this.client, MigrationUtils.confiugredDestinationServer());
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                throw new MigrateSolrIndexException(e);
            } catch (BrokenBarrierException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                throw new MigrateSolrIndexException(e);
            }
        }
    }

    public static void main(String[] args) throws MigrateSolrIndexException {
        ParallelMigrateSolrIndexImpl migr = new ParallelMigrateSolrIndexImpl();
        migr.migrate();
    }
}
