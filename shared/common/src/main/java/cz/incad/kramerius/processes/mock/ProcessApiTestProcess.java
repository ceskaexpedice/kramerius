package cz.incad.kramerius.processes.mock;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import cz.incad.kramerius.processes.WarningException;
import cz.incad.kramerius.processes.starter.ProcessStarter;
import cz.incad.kramerius.processes.utils.ProcessUtils;
import net.sf.json.JSONObject;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Definice procesu je dale v shared/common/src/main/java/cz/incad/kramerius/processes/res/lp.st
 */
public class ProcessApiTestProcess {

    public static final Logger LOGGER = Logger.getLogger(ProcessApiTestProcess.class.getName());
    public static final String ID = "process-api-test";
    public static final String PARAM_DURATION = "duration";
    public static final String PARAM_PROCESSES_IN_BATCH = "processesInBatch";
    public static final String PARAM_FINAL_STATE = "finalState";

    //autentizacni hlavicky pro planovani podrpocesu
    public static final String API_AUTH_HEADER_CLIENT = "client";
    public static final String API_AUTH_HEADER_UID = "uid";
    public static final String API_AUTH_HEADER_ACCESS_TOKEN = "access-token";

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
        int argsIndex = 0;
        //TODO: vyresit batch_token.
        // Bud takto (proces ho dostane v argumentech a pak ho posila pri planovani dalsiho procesu v davce), coz ale neni idealni, protoze klient rika, jaky bude batch_token
        // Anebo nejake reseni podobne dosavadnimu, jenze tam to souviselo se session a z process_2_token se dalo sehnat mapovani session na process a batch
        String batchToken = args[argsIndex++];
        //TODO: autentizaci vyresit systematicky, zatim pres parametr tohohle konkretniho procesu
        //asi podobne, jako tabulka process_2_token, nebo primo do processes, kazdopadne kazdy proces by mel uchovavat client, uid, access-token
        String authClient = args[argsIndex++];
        String authUid = args[argsIndex++];
        String authAccessToken = args[argsIndex++];
        int durationInSeconds = Integer.valueOf(args[argsIndex++]);
        int processesInBatch = Integer.valueOf(args[argsIndex++]);
        FinalState finalState = FinalState.valueOf(args[argsIndex++]);

        //zmena nazvu
        ProcessStarter.updateName(String.format("Proces pro testování správy procesů (%s=%ds, %s=%s, processes_in_batch=%d)", PARAM_DURATION, durationInSeconds, PARAM_FINAL_STATE, finalState, processesInBatch));

        //cekani n sekund
        try {
            LOGGER.info(String.format("going to sleep for %d seconds now", durationInSeconds));
            Thread.sleep(durationInSeconds * 1000);
            LOGGER.info(String.format("waking up"));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (processesInBatch > 1) {
            scheduleNextProcessInBatch(batchToken, authClient, authUid, authAccessToken, durationInSeconds, processesInBatch - 1, finalState);
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

    public static void scheduleNextProcessInBatch(String batchToken, String authClient, String authUid, String authAccessToken, int durationInSeconds, int remainingProcessesInBatch, FinalState finalState) {
        //v starem api to funguje tak, ze proces zavola servlet, stejne jako to dela externi klient
        //viz IndexerProcessStarter.spawnIndexer
        //takze se musi predavat i batch token
        //TODO: takze to udelat podobne, coz ale bude znamenat, ze i samotne procesy budou muset volat nove API
        //tj. upravit postupne vsechny procesy
        //TODO: batch token - zatim se se spousti dalsi proces, jako by nebyl v davce

        Client client = Client.create();
        //TODO: zvazit, jestli batchToken nedat spis do URL, jakoze:
        //POST
        WebResource resource = client.resource(ProcessUtils.getNewAdminApiProcessesEndpoint() + "?batch_token=" + batchToken);
        //resource.addFilter(new IndexerProcessStarter.TokensFilter());

        JSONObject data = new JSONObject();
        data.put("id", ID);
        JSONObject params = new JSONObject();
        params.put(PARAM_DURATION, durationInSeconds);
        params.put(PARAM_PROCESSES_IN_BATCH, remainingProcessesInBatch);
        params.put(PARAM_FINAL_STATE, finalState.name());
        data.put("params", params);

        //TODO: otestovat, jestli se vypropagujou chybove hlasky, treba pri spatnem auth tokenu
        try {
            String response = resource
                    .accept(MediaType.APPLICATION_JSON)
                    .type(MediaType.APPLICATION_JSON)
                    .header(API_AUTH_HEADER_CLIENT, authClient)
                    .header(API_AUTH_HEADER_UID, authUid)
                    .header(API_AUTH_HEADER_ACCESS_TOKEN, authAccessToken)
                    .entity(data.toString(), MediaType.APPLICATION_JSON)
                    .post(String.class);
            //System.out.println("response: " + response);
        } catch (UniformInterfaceException e) {
            e.printStackTrace();
            ClientResponse errorResponse = e.getResponse();
            //System.err.printf("message: " + errorResponse.toString());
            System.err.printf(errorResponse.toString());
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
