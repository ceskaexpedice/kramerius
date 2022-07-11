package cz.incad.kramerius.services;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.service.MigrateSolrIndex;
import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.utils.IterationUtils;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        final List<SolrWorker>  worksWhatHasToBeDone = new ArrayList<>();
        final String masterQuery = "*:*";
        long start = System.currentTimeMillis();
        try {
            if (MigrationUtils.configuredUseCursor()) {
                IterationUtils.cursorIteration(client, MigrationUtils.configuredSourceServer(), masterQuery, (Element element, String t) -> {
                    addNewWorkToWorkers(worksWhatHasToBeDone, element);
                }, () -> {
                    finishRestWorkers(worksWhatHasToBeDone);
                });
            } else if (MigrationUtils.configuredPagination()) {
                IterationUtils.queryPaginationIteration(this.client,MigrationUtils.configuredSourceServer(),masterQuery, (element, t) ->{
                    addNewWorkToWorkers(worksWhatHasToBeDone, element);
                }, ()-> {
                    finishRestWorkers(worksWhatHasToBeDone);

                });

            } else {
                IterationUtils.queryFilterIteration(this.client,MigrationUtils.configuredSourceServer(),masterQuery, (element, t) ->{
                    addNewWorkToWorkers(worksWhatHasToBeDone, element);
                }, ()-> {
                    finishRestWorkers(worksWhatHasToBeDone);

                });
                //this.migrateUseQueryFilter(MigrationUtils.configuredSourceServer(),"*:*");
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

    private void addNewWorkToWorkers(List<SolrWorker> worksWhatHasToBeDone, Element element) {
        try {
            worksWhatHasToBeDone.add(new SolrWorker(client, IterationUtils.findAllPids(element)));
            if (worksWhatHasToBeDone.size() >= MigrationUtils.configuredNumberOfThreads()) {
                startWorkers(worksWhatHasToBeDone);
                worksWhatHasToBeDone.clear();
            }
        } catch (MigrateSolrIndexException | BrokenBarrierException | InterruptedException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    private void finishRestWorkers(List<SolrWorker> worksWhatHasToBeDone) {
        try {
            if (!worksWhatHasToBeDone.isEmpty()) {
                startWorkers(worksWhatHasToBeDone);
            }
        } catch (BrokenBarrierException | InterruptedException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    public static void main(String[] args) throws MigrateSolrIndexException {
        ParallelMigrateSolrIndexImpl migr = new ParallelMigrateSolrIndexImpl();
        migr.migrate();
    }
}
