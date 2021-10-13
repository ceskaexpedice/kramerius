package cz.incad.kramerius.csv;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.workers.DNNTLabelWorker;
import cz.incad.kramerius.workers.DNNTWorker;

import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;

public class DDNTCSVLabeledFlag extends AbstractDNNTCSVProcess {

    private String label;

    public static void main(String[] args) throws BrokenBarrierException, InterruptedException, IOException {
        new DDNTCSVLabeledFlag().process(args);
    }

    @Override
    protected void initializeFromArgs(String[] args) throws IOException {
        if (args.length < 2)  throw new IllegalArgumentException("The process expects at least two parameters");
        if (args.length>0) {
            this.addRemoveFlag = Boolean.valueOf(args[0]);
        }
        if (args.length>1) {
            this.label = args[1];
        }
        if (args.length > 2) {
            csvFile = args[2];
        } else {
            defaultCSVFileInitialization(addRemoveFlag);
        }

    }

    @Override
    protected DNNTWorker createWorker(String pid, FedoraAccess fedoraAccess, Client client, boolean flag) {
        return new DNNTLabelWorker(pid, fedoraAccess, client, this.label, flag);
    }
}
