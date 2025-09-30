package cz.incad.kramerius.processes.mock;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import cz.incad.kramerius.processes.WarningException;
import cz.incad.kramerius.processes.utils.ProcessUtils;
import cz.incad.kramerius.processes.utils.Utils;
import net.sf.json.JSONObject;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Deklarace procesu je v shared/common/src/main/java/cz/incad/kramerius/processes/res/lp.st (new_process_api_test)
 */
public class ProcessApiTestProcess {

    public static final Logger LOGGER = Logger.getLogger(ProcessApiTestProcess.class.getName());

    public static final String API_AUTH_HEADER_AUTH_TOKEN = "parent-process-auth-token"; //just for processes
    //public static final String API_AUTH_HEADER_AUTH_TOKEN = "process-auth-token"; //just for processes
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
        String authToken = args[argsIndex++]; //auth token always first, but still suboptimal solution, best would be if it was outside the scope of this as if ProcessHelper.scheduleProcess() similarly to changing name (ProcessStarter)
        //Kramerius
        //String krameriusApiAuthClient = args[argsIndex++];
        //String krameriusApiAuthUid = args[argsIndex++];
        //String krameriusApiAuthAccessToken = args[argsIndex++];

        int durationInSeconds = Integer.valueOf(args[argsIndex++]);
        int processesInBatch = Integer.valueOf(args[argsIndex++]);
        FinalState finalState = FinalState.valueOf(args[argsIndex++]);

        //zmena nazvu
        // TODO pepo ProcessStarter.updateName(String.format("Proces pro testování správy procesů (duration=%ds, processesInBatch=%s, finalState=%s)", durationInSeconds, processesInBatch, finalState));

        //cekani n sekund
        try {
            LOGGER.info(String.format("going to sleep for %d seconds now", durationInSeconds));
            Thread.sleep(durationInSeconds * 1000);
            LOGGER.info(String.format("waking up"));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (processesInBatch > 1) {
            scheduleNextProcessInBatch(authToken, durationInSeconds, processesInBatch - 1, finalState);
        }

        LOGGER.info("total duration: " + Utils.formatTime(System.currentTimeMillis() - start));

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

    public static void scheduleNextProcessInBatch(String authToken, int durationInSeconds, int remainingProcessesInBatch, FinalState finalState) {
        //v starem api to funguje tak, ze proces zavola servlet (lr), stejne jako to dela externi klient, dokonce i pro zmeny stavu procesu apod.
        //viz IndexerProcessStarter.spawnIndexer
        //tohle ted mame podobne, akorat se mi nelibi, jakym zpusobem volaji procesy lr servlet
        //veci jako process-auth-token by mely byt mimo primy dosah procesu (ale metody ProcessHelper/ProcessStarter), toho nezajima infrastruktura, co ho pousit apod.
        //tj. TODO: upravovat postupne vsechny procesy v ramci presunu jejich spousteni pres nove API
        //takze treba ProcessHelper.scheduleProcess(defid, args), ProcessHelper.changeMyName(newName)
        //cili bych omezil to, co muze byt proces.
        //Ted obecna trida, co ma main() a tak nema jinou moznost (pro zmenu nazvu apod.), nez volat staticke metody odevsad, cimz tak k sobe ve vysledku muze nabalit pulku Krameria kvuli zavislostem
        Client client = Client.create();
        WebResource resource = client.resource(ProcessUtils.getNewAdminApiEndpoint() + "/processes");
        JSONObject data = new JSONObject();
        data.put("defid", "new_process_api_test");
        JSONObject params = new JSONObject();
        params.put("duration", durationInSeconds);
        params.put("processesInBatch", remainingProcessesInBatch);
        params.put("finalState", finalState.name());
        data.put("params", params);

        try {
            String response = resource
                    .accept(MediaType.APPLICATION_JSON)
                    .type(MediaType.APPLICATION_JSON)
                    .header(API_AUTH_HEADER_AUTH_TOKEN, authToken)
                    //.header(API_AUTH_HEADER_CLIENT, krameriusApiAuthClient)
                    //.header(API_AUTH_HEADER_UID, krameriusApiAuthUid)
                    //.header(API_AUTH_HEADER_ACCESS_TOKEN, krameriusApiAuthAccessToken)
                    .entity(data.toString(), MediaType.APPLICATION_JSON)
                    .post(String.class);
            //System.out.println("response: " + response);
        } catch (UniformInterfaceException e) {
            ClientResponse errorResponse = e.getResponse();
            String responseBody = errorResponse.getEntity(String.class);
            String bodyToPrint = responseBody;
            if (responseBody != null) {
                /* TODO
                try {
                    JsonValue jsonBody = Json.parse(responseBody);
                    bodyToPrint = jsonBody.asString();
                } catch (Throwable pe) {
                    //not JSON
                }

                 */
            }
            throw new RuntimeException(errorResponse.toString() + ": " + bodyToPrint, e);
        }
    }


    public enum FinalState {
        FINISHED, FAILED, WARNING, RANDOM
    }
}
