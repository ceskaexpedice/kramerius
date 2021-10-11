package cz.incad.kramerius.plain;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.workers.DNNTWorker;
import cz.incad.kramerius.workers.DNNTWorkerFlag;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.BrokenBarrierException;

@Deprecated
public class DNNTPidFlag extends AbstractPlainDNNTProcess {

    @Override
    protected DNNTWorker createWorker(String pid, FedoraAccess fedoraAccess, Client client, boolean flag) {
        return new DNNTWorkerFlag(pid, fedoraAccess, client, flag);
    }

    protected void initializeFromArgs(String[] args) throws IOException {
        if (args.length < 1) throw new IllegalArgumentException("At least one argument");
        this.addRemoveFlag = Boolean.parseBoolean(args[0]);
        this.pids = Arrays.asList(args).subList(1, args.length);
    }

    public static void main(String[] args) throws BrokenBarrierException, InterruptedException, IOException {
        new DNNTPidFlag().process(args);
    }

}
