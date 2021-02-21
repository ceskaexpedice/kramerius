package cz.incad.kramerius.plain;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.AbstractDNNTProcess;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.workers.DNNTWorker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;

public abstract class AbstractPlainDNNTProcess extends AbstractDNNTProcess {

    protected  List<String> pids;


    protected void iteratePids() throws IOException, BrokenBarrierException, InterruptedException {
        FedoraAccess fedoraAccess = new FedoraAccessImpl(KConfiguration.getInstance(), null);
        Client client = Client.create();

        client.setReadTimeout(Integer.parseInt(KConfiguration.getInstance().getProperty("http.timeout", "10000")));
        client.setConnectTimeout(Integer.parseInt(KConfiguration.getInstance().getProperty("http.timeout", "10000")));

        final List<DNNTWorker> dnntWorkers = new ArrayList<>();
        for (String pid :  pids) {
            if (dnntWorkers.size() >= numberofThreads) {
                startWorkers(dnntWorkers);
                dnntWorkers.clear();
                dnntWorkers.add(createWorker(pid, fedoraAccess, client, flag));
            } else {
                dnntWorkers.add(createWorker(pid, fedoraAccess, client, flag));
            }
        }
        if (!dnntWorkers.isEmpty()) {
            startWorkers(dnntWorkers);
            dnntWorkers.clear();
        }

        this.commit(client);
    }


    protected void initializeFromProperties() {
        super.initializeFromProperties();
    }

    public void process(String[] args) throws IOException, BrokenBarrierException, InterruptedException {
        initializeFromProperties();
        initializeFromArgs(args);
        iteratePids();
    }

}
