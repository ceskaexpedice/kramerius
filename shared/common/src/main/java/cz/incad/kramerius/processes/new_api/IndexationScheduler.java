package cz.incad.kramerius.processes.new_api;

import com.hazelcast.internal.json.Json;
import com.hazelcast.internal.json.JsonValue;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import cz.incad.kramerius.utils.conf.KConfiguration;
import net.sf.json.JSONObject;

import javax.ws.rs.core.MediaType;

/**
 * To be used by processes, no matter what module are they defined in, to schedule indexation within same process-batch
 * TODO: replace cz.incad.kramerius.service.impl.IndexerProcessStarter
 */
public class IndexationScheduler {
    //TODO: cleanup

    public static void scheduleIndexation(String pid, String title, boolean includingDescendants, String parentProcessAuthToken) {
        Client client = Client.create();
        WebResource resource = client.resource(getNewAdminApiEndpoint() + "/processes");
        JSONObject data = new JSONObject();
        data.put("defid", "new_indexer_index_object");
        JSONObject params = new JSONObject();
        params.put("type", includingDescendants ? "TREE_AND_FOSTER_TREES" : "OBJECT");
        params.put("pid", pid);
        params.put("title", title);
        params.put("ignoreInconsistentObjects", true);
        data.put("params", params);

        try {
            String response = resource
                    .header("parent-process-auth-token", parentProcessAuthToken)
                    .entity(data.toString(), MediaType.APPLICATION_JSON)
                    .post(String.class);
            //System.out.println("response: " + response);
        } catch (UniformInterfaceException e) {
            ClientResponse errorResponse = e.getResponse();
            String responseBody = errorResponse.getEntity(String.class);
            String bodyToPrint = responseBody;
            if (responseBody != null) {
                try {
                    JsonValue jsonBody = Json.parse(responseBody);
                    bodyToPrint = jsonBody.asString();
                } catch (Throwable pe) {
                    //not JSON
                }
            }
            throw new RuntimeException(errorResponse.toString() + ": " + bodyToPrint, e);
        }
    }

    private static String getNewAdminApiEndpoint() {
        String applicationURL = KConfiguration.getInstance().getApplicationURL();
        if (applicationURL.endsWith("/")) { //normalize to "../search", not "../search/"
            applicationURL = applicationURL.substring(0, applicationURL.length() - 1);
        }
        return applicationURL + "/api/admin/v7.0";
    }

}

