package cz.incad.kramerius;

import com.sun.jersey.api.client.*;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.workers.DNNTWorker;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Logger;

public abstract class AbstractDNNTProcess {

    public static Logger LOGGER = Logger.getLogger(AbstractDNNTProcess.class.getName());

    public static final String DNNT_THREADS = "dnnt.threads";
    protected int numberofThreads = -1;

    protected boolean addRemoveFlag;


    protected  void startWorkers(List<DNNTWorker> worksWhasHasToBeDone) throws BrokenBarrierException, InterruptedException {
        CyclicBarrier barrier = new CyclicBarrier(worksWhasHasToBeDone.size()+1);
        worksWhasHasToBeDone.stream().forEach(th->{
            th.setBarrier(barrier);
            new Thread(th).start();
        });
        barrier.await();
    }


    protected void commit(Client client) throws UniformInterfaceException, ClientHandlerException {
        String updateUrl = KConfiguration.getInstance().getSolrHost();
        updateUrl = updateUrl  + (updateUrl.endsWith("/") ? ""  : "/") + "update?commit=true";
        WebResource r = client.resource(updateUrl);
        r.accept(MediaType.TEXT_XML).entity("<commit/>").type(MediaType.TEXT_XML).post(ClientResponse.class);
        LOGGER.info("Commited changes");
    }

    protected void initializeFromProperties() {
        this.numberofThreads = KConfiguration.getInstance().getConfiguration().getInt(DNNT_THREADS,2);
    }

    protected abstract DNNTWorker createWorker(String pid, FedoraAccess fedoraAccess, Client client, boolean flag);
    protected abstract void initializeFromArgs(String[] args) throws IOException;
    public abstract void process(String[] args) throws IOException, BrokenBarrierException, InterruptedException;

}
