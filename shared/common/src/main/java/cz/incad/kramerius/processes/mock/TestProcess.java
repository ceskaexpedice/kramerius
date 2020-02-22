package cz.incad.kramerius.processes.mock;

import cz.incad.kramerius.processes.WarningException;
import cz.incad.kramerius.processes.starter.ProcessStarter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Definice procesu je dale v shared/common/src/main/java/cz/incad/kramerius/processes/res/lp.st
 */
public class TestProcess {

    public static final Logger LOGGER = Logger.getLogger(TestProcess.class.getName());

    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();

        //logovani
        LOGGER.fine("Logger.fine()");
        LOGGER.info("Logger.info");
        LOGGER.warning("Logger.warning()");
        LOGGER.severe("Logger.severe()");
        System.out.println("Logger.out.println()");
        System.err.println("Logger.err.println()");

        //args
        LOGGER.info("args: " + Arrays.asList(args));
        int durationInSeconds = Integer.valueOf(args[0]);
        int processesInBatch = Integer.valueOf(args[1]);
        FinalState finalState = FinalState.valueOf(args[2]);

        //zmena nazvu
        ProcessStarter.updateName(String.format("Proces pro testování správy procesů (duration=%ds, final_state=%s, processes_in_batch=%d)", durationInSeconds, finalState, processesInBatch));

        //cekani n sekund
        try {
            LOGGER.info(String.format("going to sleep for %d seconds now", durationInSeconds));
            Thread.sleep(durationInSeconds * 1000);
            LOGGER.info(String.format("waking up"));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (processesInBatch > 1) {
            //TODO: naplanuj dalsi procesy v davce
        }

        LOGGER.info("total duration: " + formatTime(System.currentTimeMillis() - start));

        //final state
        if (finalState == FinalState.RANDOM) {
            Random random = new Random();
            finalState = FinalState.values()[random.nextInt(3)];
        }
        LOGGER.info("process state should be " + finalState);
        switch (finalState) {
            case FINISHED:
                break;
            case FAILED:
                throw new RuntimeException("failed");
            case WARNING:
                throw new WarningException("warning");//TODO: tohle vypada, ze nefunguje
        }
    }

    public static String formatTime(long millis) {
        long hours = millis / (60 * 60 * 1000);
        long minutes = millis / (60 * 1000) - hours * 60;
        long seconds = (millis / 1000) % 60;
        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }

    public enum FinalState {
        FINISHED, FAILED, WARNING, RANDOM
    }
}
