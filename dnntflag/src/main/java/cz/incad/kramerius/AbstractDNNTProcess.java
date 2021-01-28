package cz.incad.kramerius;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.workers.DNNTWorker;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public abstract class AbstractDNNTProcess {

    public static final String DNNT_THREADS = "dnnt.threads";
    protected int numberofThreads = -1;
    protected boolean flag;

    protected  void startWorkers(List<DNNTWorker> worksWhasHasToBeDone) throws BrokenBarrierException, InterruptedException {
        CyclicBarrier barrier = new CyclicBarrier(worksWhasHasToBeDone.size()+1);
        worksWhasHasToBeDone.stream().forEach(th->{
            th.setBarrier(barrier);
            new Thread(th).start();
        });
        barrier.await();
    }

    protected void initializeFromProperties() {
        this.numberofThreads = KConfiguration.getInstance().getConfiguration().getInt(DNNT_THREADS,2);
    }

    protected abstract DNNTWorker createWorker(String pid, FedoraAccess fedoraAccess, Client client, boolean flag);
    protected abstract void initializeFromArgs(String[] args) throws IOException;
    public abstract void process(String[] args) throws IOException, BrokenBarrierException, InterruptedException;

}
