package cz.incad.kramerius.processes.new_api;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import cz.incad.kramerius.utils.conf.KConfiguration;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;


/**
 * To be used by processes, no matter what module are they defined in, to schedule indexation within same process-batch
 * TODO: replace cz.incad.kramerius.service.impl.IndexerProcessStarter
 */
//TODO: Rename to more generic name
public class ProcessScheduler {
    
    public static final Logger LOGGER = Logger.getLogger(ProcessScheduler.class.getName());
    
    
    public static void schedule(String processPayload, String parentProcessAuthToken) {

        LOGGER.info(String.format("Starting process with payload %s", processPayload));
        
        Client client = Client.create();
        WebResource resource = client.resource(getNewAdminApiEndpoint() + "/processes");
        try {
            String response = resource
                    .header("parent-process-auth-token", parentProcessAuthToken)
                    .entity(processPayload.toString(), MediaType.APPLICATION_JSON)
                    .post(String.class);
            //System.out.println("response: " + response);
        } catch (UniformInterfaceException e) {
            ClientResponse errorResponse = e.getResponse();
            String responseBody = errorResponse.getEntity(String.class);
            String bodyToPrint = responseBody;
            if (responseBody != null) {
                /* TODO AK_NEW Json
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
    
    
    public static void scheduleImport(String folder,String parentProcessAuthToken) {
        JSONObject data = new JSONObject();
        data.put("defid", "import");
        JSONObject params = new JSONObject();
        params.put("inputDataDir", folder);
        params.put("startIndexer", true);
        data.put("params", params);
        schedule(data.toString(), parentProcessAuthToken);
    }

  //TODO: cleanup
    public static void scheduleAddCollection(String pid, String title, boolean includingDescendants, String parentProcessAuthToken) {
        JSONObject data = new JSONObject();
        data.put("defid", "new_indexer_index_object");
        JSONObject params = new JSONObject();
        params.put("type", includingDescendants ? "TREE_AND_FOSTER_TREES" : "OBJECT");
        params.put("pid", pid);
        params.put("title", title);
        params.put("ignoreInconsistentObjects", true);
        data.put("params", params);
        schedule(data.toString(), parentProcessAuthToken);
    }
    
    
    //TODO: cleanup
    public static void scheduleIndexation(String pid, String title, boolean includingDescendants, String parentProcessAuthToken) {
        JSONObject data = new JSONObject();
        data.put("defid", "new_indexer_index_object");
        JSONObject params = new JSONObject();
        params.put("type", includingDescendants ? "TREE_AND_FOSTER_TREES" : "OBJECT");
        params.put("pid", pid);
        params.put("title", title);
        params.put("ignoreInconsistentObjects", true);
        data.put("params", params);
        schedule(data.toString(), parentProcessAuthToken);
    }
    
    //TODO: cleanup
    public static void scheduleIndexation(List<String> pidlist, String title, boolean includingDescendants, String parentProcessAuthToken) {
        JSONObject data = new JSONObject();
        data.put("defid", "new_indexer_index_object");
        JSONObject params = new JSONObject();
        params.put("type", includingDescendants ? "TREE_AND_FOSTER_TREES" : "OBJECT");
        
        JSONArray jsonArray = new JSONArray();
        pidlist.stream().forEach(jsonArray::add);
        
        params.put("pidlist", jsonArray);
        params.put("title", title);
        params.put("ignoreInconsistentObjects", true);
        data.put("params", params);
        schedule(data.toString(), parentProcessAuthToken);
    }

    //TODO: cleanup
    public static void scheduleIndexation(File pidListFile, String title, boolean includingDescendants, String parentProcessAuthToken) {
        JSONObject data = new JSONObject();
        data.put("defid", "new_indexer_index_object");
        JSONObject params = new JSONObject();
        params.put("type", includingDescendants ? "TREE_AND_FOSTER_TREES" : "OBJECT");
        
        params.put("pidlist_file", pidListFile.getAbsolutePath());
        params.put("title", title);
        params.put("ignoreInconsistentObjects", true);
        data.put("params", params);
        schedule(data.toString(), parentProcessAuthToken);
    }

    private static String getNewAdminApiEndpoint() {
        String applicationURL = KConfiguration.getInstance().getApplicationURL();
        if (applicationURL.endsWith("/")) { //normalize to "../search", not "../search/"
            applicationURL = applicationURL.substring(0, applicationURL.length() - 1);
        }
        return applicationURL + "/api/admin/v7.0";
    }

}

