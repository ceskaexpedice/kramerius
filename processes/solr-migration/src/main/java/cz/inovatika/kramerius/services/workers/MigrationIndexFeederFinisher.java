package cz.inovatika.kramerius.services.workers;

import cz.inovatika.kramerius.services.config.ProcessConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

import java.util.concurrent.CyclicBarrier;

public abstract class MigrationIndexFeederFinisher {

    protected CloseableHttpClient client;
    protected CyclicBarrier barrier;
    protected ProcessConfig processConfig;

    
    public MigrationIndexFeederFinisher(ProcessConfig config, CloseableHttpClient client) {
        super();
        this.client = client;
        this.processConfig = config;
    }

    public void exceptionDuringCrawl(Exception ex) {}
    

    public abstract  void finish();

    public CyclicBarrier getBarrier() {
        return barrier;
    }

    public void setBarrier(CyclicBarrier barrier) {
        this.barrier = barrier;
    }
}
