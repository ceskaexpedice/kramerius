package cz.incad.kramerius.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
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
    //private ExecutorService service;
    private CyclicBarrier cyclicBarrier;

    public ParallelMigrateSolrIndexImpl() throws MigrateSolrIndexException {
        super();
        this.client = Client.create();
        //this.service = Executors.newFixedThreadPool(MigrationUtils.configuredNumberOfThreads());
    }

    private void migrateUseQueryFilter(String address) throws MigrateSolrIndexException, IOException, SAXException, ParserConfigurationException, BrokenBarrierException, InterruptedException {
        List<SolrWorker>  worksWhatHasToBeDone = new ArrayList<>();
        String lastPid = null;
        String previousPid = null;
        do {
           Element element = MigrationUtils.pidsQueryFilterQuery(client, address,  lastPid);
            previousPid = lastPid;
            lastPid = MigrationUtils.findLastPid(element);
            worksWhatHasToBeDone.add(new SolrWorker(this.client, MigrationUtils.findAllPids(element)));
            if (worksWhatHasToBeDone.size() >= MigrationUtils.configuredNumberOfThreads()) {
                startWorkers(worksWhatHasToBeDone);
                worksWhatHasToBeDone.clear();
            }
        }while(lastPid != null  && !lastPid.equals(previousPid));
        if (!worksWhatHasToBeDone.isEmpty()) {
            startWorkers(worksWhatHasToBeDone);
        }
    }

    private void migrateUseCursorMark(String address) throws ParserConfigurationException, MigrateSolrIndexException, SAXException, IOException, InterruptedException, BrokenBarrierException {
        List<SolrWorker>  worksWhatHasToBeDone = new ArrayList<>();
        String cursorMark = null;
        String queryCursorMark = null;
        do {
            Element element = MigrationUtils.pidsCursorQuery(client, address, cursorMark);
            cursorMark = MigrationUtils.findCursorMark(element);
            queryCursorMark = MigrationUtils.findQueryCursorMark(element);
            worksWhatHasToBeDone.add(new SolrWorker(this.client, MigrationUtils.findAllPids(element)));
            if (worksWhatHasToBeDone.size() >= MigrationUtils.configuredNumberOfThreads()) {
                startWorkers(worksWhatHasToBeDone);
                worksWhatHasToBeDone.clear();
            }
        } while((cursorMark != null && queryCursorMark != null) && !cursorMark.equals(queryCursorMark));

        if (!worksWhatHasToBeDone.isEmpty()) {
            startWorkers(worksWhatHasToBeDone);
        }
    }

    private void startWorkers(List<SolrWorker> worksWhasHasToBeDone) throws BrokenBarrierException, InterruptedException {
        CyclicBarrier barrier = new CyclicBarrier(worksWhasHasToBeDone.size()+1);
        worksWhasHasToBeDone.stream().forEach(th->{
            th.setBarrier(barrier);
            new Thread(th).start();
        });
        barrier.await();
    }

    @Override
    public void migrate() throws MigrateSolrIndexException {
        long start = System.currentTimeMillis();
        try {
            if (MigrationUtils.configuredUseCursor()) {
                this.migrateUseCursorMark(MigrationUtils.configuredSourceServer());
            } else {
                this.migrateUseQueryFilter(MigrationUtils.configuredSourceServer());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new MigrateSolrIndexException(e);
        } finally {
                long stop = System.currentTimeMillis();
                LOGGER.info("Finished  in "+(stop - start)+" ms");
                MigrationUtils.commit(this.client, MigrationUtils.confiugredDestinationServer());
        }
    }

    public static void main(String[] args) throws MigrateSolrIndexException, IOException, SAXException, ParserConfigurationException {
        ParallelMigrateSolrIndexImpl migr = new ParallelMigrateSolrIndexImpl();
        migr.migrate();
    }
}
