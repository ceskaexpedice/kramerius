package cz.incad.kramerius.plain;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.AbstractDNNTProcess;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.imaging.lp.guice.GenerateDeepZoomCacheModule;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.workers.DNNTWorker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;

public abstract class AbstractPlainDNNTProcess extends AbstractDNNTProcess {

    protected  List<String> pids;


    protected void iteratePids() throws IOException, BrokenBarrierException, InterruptedException {
        Client client = null;
        try {

            // TODO: Replace deep zoom cache module
            Injector injector = Guice.createInjector( new SolrModule(), new RepoModule(), new NullStatisticsModule());
            FedoraAccess fedoraAccess = injector.getInstance(Key.get(FedoraAccess.class, Names.named("rawFedoraAccess")));
            client = Client.create();

            client.setReadTimeout(Integer.parseInt(KConfiguration.getInstance().getProperty("http.timeout", "10000")));
            client.setConnectTimeout(Integer.parseInt(KConfiguration.getInstance().getProperty("http.timeout", "10000")));

            final List<DNNTWorker> dnntWorkers = new ArrayList<>();
            for (String pid :  pids) {
                if (dnntWorkers.size() >= numberofThreads) {
                    startWorkers(dnntWorkers);
                    dnntWorkers.clear();
                    dnntWorkers.add(createWorker(pid, fedoraAccess, client, addRemoveFlag));
                } else {
                    dnntWorkers.add(createWorker(pid, fedoraAccess, client, addRemoveFlag));
                }
            }
            if (!dnntWorkers.isEmpty()) {
                startWorkers(dnntWorkers);
                dnntWorkers.clear();
            }
        } finally {
            if (client != null) this.commit(client);
        }

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
