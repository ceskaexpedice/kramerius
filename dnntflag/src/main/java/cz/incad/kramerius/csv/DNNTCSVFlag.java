package cz.incad.kramerius.csv;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.workers.DNNTWorker;
import cz.incad.kramerius.workers.DNNTWorkerFlag;

import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;
import java.util.logging.Logger;

/**
 * Process for association of DNNT flag
 */
public class DNNTCSVFlag extends AbstractDNNTCSVProcess {

    public static final Logger LOGGER = Logger.getLogger(DNNTCSVFlag.class.getName());

    protected DNNTWorker createWorker(String pid, FedoraAccess fedoraAccess, Client client, boolean flag) {
        return new DNNTWorkerFlag(pid, fedoraAccess, client, flag);
    }

    protected void initializeFromArgs(String[] args) throws IOException {
        if (args.length == 0) throw new IllegalArgumentException("The process expects at least one parameter");
        if (args.length>0) {
            this.flag = Boolean.valueOf(args[0]);
        }
        if (args.length > 1) {
            csvFile = args[1];
        } else {
            defaultCSVFileInitialization(flag);
        }
    }


    public static void main(String[] args) throws IOException, BrokenBarrierException, InterruptedException {
        new DNNTCSVFlag().process(args);
    }

}
