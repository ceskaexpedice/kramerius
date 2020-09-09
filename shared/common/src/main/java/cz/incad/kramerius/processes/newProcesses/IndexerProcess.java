package cz.incad.kramerius.processes.newProcesses;

import cz.incad.kramerius.processes.WarningException;
import cz.incad.kramerius.processes.starter.ProcessStarter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Definice procesu je dale v shared/common/src/main/java/cz/incad/kramerius/processes/res/lp.st
 */
public class IndexerProcess {

    public static final Logger LOGGER = Logger.getLogger(IndexerProcess.class.getName());
    public static final String ID = "new_indexer";
    public static final String PARAM_PID = "pid";
    public static final String PARAM_TYPE = "type";

    public static final String API_AUTH_HEADER_AUTH_TOKEN = "process-auth-token";

    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();
        //args
        LOGGER.info("args: " + Arrays.asList(args));
        int argsIndex = 0;
        String authToken = args[argsIndex++]; //auth token always first, but still suboptimal solution, best would be if it was outside the scope of this as if ProcessHelper.scheduleProcess() similarly to changing name (ProcessStarter)
        String type = args[argsIndex++];
        String pid = args[argsIndex++];

        //TODO: spousteni procesu (new_indexer, new_process-api-test) nefunguje a procesy zustavaji scheduled. Asi bude problem v poslednim merge master, viz historie ProcessStarter

        //zmena nazvu
        //TODO: mozna spis abstraktni proces s metodou updateName() a samotny kod procesu by mel callback na zjisteni nazvu, kterym by se zavolal updateName()
        ProcessStarter.updateName(String.format("Indexace (typ %s, objekt %s)", type, pid));

        //cekani n sekund
        try {
            int durationInSeconds = new Random().nextInt(10);
            LOGGER.info(String.format("going to sleep for %d seconds now", durationInSeconds));
            Thread.sleep(durationInSeconds * 1000);
            LOGGER.info(String.format("waking up"));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        LOGGER.info("TODO: samotna indexace");
        //TODO: zmenit nazev procesu - doplnit nazev objektu (bud odsud, nebo lepe callbackem z procesu)

        LOGGER.info("total duration: " + Utils.formatTime(System.currentTimeMillis() - start));
    }
}
