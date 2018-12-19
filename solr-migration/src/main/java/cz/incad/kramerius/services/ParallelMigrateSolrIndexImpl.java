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
                IterationUtils.cursorIteration(client,MigrationUtils.configuredSourceServer(),masterQuery,(element, t) ->{
                    try {
                        worksWhatHasToBeDone.add(new SolrWorker(this.client, MigrationUtils.findAllPids(element)));
                        if (worksWhatHasToBeDone.size() >= MigrationUtils.configuredNumberOfThreads()) {
                            startWorkers(worksWhatHasToBeDone);
                            worksWhatHasToBeDone.clear();
                        }
                    } catch (MigrateSolrIndexException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    } catch (BrokenBarrierException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    } catch (InterruptedException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    }
                }, ()-> {
                    try {
                        if (!worksWhatHasToBeDone.isEmpty()) {
                            startWorkers(worksWhatHasToBeDone);
                        }
                    } catch (BrokenBarrierException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    } catch (InterruptedException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    }
                });
            } else {
                IterationUtils.queryFilterIteration(this.client,MigrationUtils.configuredSourceServer(),masterQuery, (element, t) ->{
                    try {
                        worksWhatHasToBeDone.add(new SolrWorker(client, MigrationUtils.findAllPids(element)));
                        if (worksWhatHasToBeDone.size() >= MigrationUtils.configuredNumberOfThreads()) {
                            startWorkers(worksWhatHasToBeDone);
                            worksWhatHasToBeDone.clear();
                        }
                    } catch (MigrateSolrIndexException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    } catch (BrokenBarrierException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    } catch (InterruptedException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    }
                }, ()-> {
                    try {
                        if (!worksWhatHasToBeDone.isEmpty()) {
                            startWorkers(worksWhatHasToBeDone);
                        }
                    } catch (BrokenBarrierException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    } catch (InterruptedException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    }

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

    public static void main(String[] args) throws MigrateSolrIndexException, IOException, SAXException, ParserConfigurationException {
        ParallelMigrateSolrIndexImpl migr = new ParallelMigrateSolrIndexImpl();
        migr.migrate();
    }
}
