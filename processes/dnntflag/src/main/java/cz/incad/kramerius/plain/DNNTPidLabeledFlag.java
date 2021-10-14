package cz.incad.kramerius.plain;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.workers.DNNTLabelWorker;
import cz.incad.kramerius.workers.DNNTWorker;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.BrokenBarrierException;

public class DNNTPidLabeledFlag extends AbstractPlainDNNTProcess {

    private String label;

    @Override
    protected DNNTWorker createWorker(String pid, FedoraAccess fedoraAccess, Client client, boolean flag) {
        return new DNNTLabelWorker(pid, fedoraAccess, client, this.label, flag);
    }

    @Override
    protected void initializeFromArgs(String[] args) throws IOException {
        if (args.length < 2) throw new IllegalArgumentException("At least one argument");
        this.addRemoveFlag = Boolean.parseBoolean(args[0]);
        this.label = args[1];
        this.pids = Arrays.asList(args).subList(2, args.length);
    }

    public static void main(String[] args) throws BrokenBarrierException, InterruptedException, IOException {
        new DNNTPidLabeledFlag().process(args);
    }
}
