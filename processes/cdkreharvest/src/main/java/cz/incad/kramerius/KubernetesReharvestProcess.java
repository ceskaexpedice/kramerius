package cz.incad.kramerius;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

import antlr.StringUtils;
import cz.incad.kramerius.rest.apiNew.admin.v10.reharvest.ReharvestItem;
import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.services.utils.kubernetes.KubernetesEnvSupport;
import cz.incad.kramerius.utils.ReharvestUtils;

public class KubernetesReharvestProcess {

    public static final int DEFAULT_MAX_ITEMS_TO_DELETE = 10000;
    
    public static final String ONLY_SHOW_CONFIGURATION = "ONLY_SHOW_CONFIGURATION";
    public static final String MAX_ITEMS_TO_DELETE = "MAX_ITEMS_TO_DELETE";

    public static final Logger LOGGER = Logger.getLogger(KubernetesReharvestProcess.class.getName());

    protected static Client buildClient() {
        // Client client = Client.create();
        ClientConfig cc = new DefaultClientConfig();
        cc.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
        cc.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        return Client.create(cc);
    }

    public static void main(String[] args) throws ParserConfigurationException, IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchMethodException, MigrateSolrIndexException, IOException, SAXException {
        
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Prague"));

        Map<String, String> env = System.getenv();
        Map<String, String> iterationMap = KubernetesEnvSupport.iterationMap(env);
        if (!iterationMap.containsKey("batch")) {
            iterationMap.put("batch", "45");
        }
        Map<String, String> reharvestMap = KubernetesEnvSupport.reharvestMap(env);
        Map<String, String> destinationMap = KubernetesEnvSupport.destinationMap(env);
        Map<String, String> proxyMap = KubernetesEnvSupport.proxyMap(env);
        boolean onlyShowConfiguration = env.containsKey(ONLY_SHOW_CONFIGURATION);
        int maxItemsToDelete = env.containsKey(MAX_ITEMS_TO_DELETE) ? Integer.parseInt(env.get(MAX_ITEMS_TO_DELETE)) : DEFAULT_MAX_ITEMS_TO_DELETE;

        AtomicReference<String> idReference = new AtomicReference<>();
        try {

            if (reharvestMap.containsKey("url") && proxyMap.containsKey("url")) {
                Client client = buildClient();
                String wurl = reharvestMap.get("url");
                if (!wurl.endsWith("/")) {
                    wurl = wurl + "/";
                }
                
                // Top item 
                WebResource topWebResource = client.resource(wurl + "top?state=open");
                ClientResponse topItemFrom = topWebResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
                if (topItemFrom.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
                    String t = topItemFrom.getEntity(String.class);
                    JSONObject itemObject = new JSONObject(t);
                    String id = itemObject.getString("id");
                    idReference.set(id);
                    String pid = itemObject.getString("pid");
                    if (!onlyShowConfiguration) {
                        changeState(client, wurl, id,"running");
                        String podname = env.get("HOSTNAME");
                        if (cz.incad.kramerius.utils.StringUtils.isAnyString(podname)) {
                            changePodname(client, wurl, id, podname);
                        }
                    }

                    // find all pids 
                    List<Pair<String,String>> allPidsList = ReharvestUtils.findPidByType(iterationMap, client, ReharvestItem.fromJSON(itemObject));
                    // check size; if size > 10000 - fail state
                    if (allPidsList.size() <  maxItemsToDelete) {
                        // delete all pids 
                        ReharvestUtils.deleteAllGivenPids(client, destinationMap, allPidsList, onlyShowConfiguration);

                        // List<String> acronyms = new ArrayList<String>();
                        // https://api.val.ceskadigitalniknihovna.cz/search/api/admin/v7.0/connected/
                        String proxyURl = proxyMap.get("url");
                        if (proxyURl != null) {
                            Map<String, JSONObject> configurations = libraryConfigurations(client, proxyURl);

                            ReharvestUtils.reharvestPIDFromGivenCollections(pid, configurations, ""+onlyShowConfiguration, destinationMap, iterationMap, ReharvestItem.fromJSON(itemObject));

                            if (!onlyShowConfiguration) {
                                changeState(client, wurl, id,"closed");
                            }
                        } else {
                            LOGGER.severe("No proxy configuration");
                        }
                    } else {
                        changeState(client, wurl, id,"too_big");
                        String compare = String.format("delete.size()  %d >=  maxItemstoDelete %d", allPidsList.size(), maxItemsToDelete);
                        LOGGER.severe(String.format( "Too big to reharvest (%s)  -> manual change ", compare));
                    }
                }  else if (topItemFrom.getStatus() == ClientResponse.Status.NOT_FOUND.getStatusCode()) {
                    LOGGER.info("No item to harvest");
                }
            } else {
                LOGGER.severe("No proxy or reharvest configuration");
            }
            
        } catch (Throwable thr) {
            if (idReference.get() != null) {
                Client client = buildClient();
                String wurl = reharvestMap.get("url");
                if (!wurl.endsWith("/")) {
                    wurl = wurl + "/";
                }
                changeState(client, wurl, idReference.get(),"failed");
            }
            LOGGER.severe("Exception ex" + thr.getMessage());
        }
        
    }

    private static void changeState(Client client, String wurl, String id, String changeState) {
        //String changeState = "closed";
        WebResource deleteWebResource = client.resource(wurl + id+"/state?state="+changeState);
        ClientResponse deleteResponse = deleteWebResource.accept(MediaType.APPLICATION_JSON).put(ClientResponse.class);
        if (deleteResponse.getStatus() ==  ClientResponse.Status.OK.getStatusCode()) {
            LOGGER.info(String.format("Change state for %s -> %s ", id, changeState));
        }
    }

    private static void changePodname(Client client, String wurl, String id, String podname) {
        //String changeState = "closed";
        WebResource changePodname = client.resource(wurl + id+"/pod?pod="+podname);
        ClientResponse deleteResponse = changePodname.accept(MediaType.APPLICATION_JSON).put(ClientResponse.class);
        if (deleteResponse.getStatus() ==  ClientResponse.Status.OK.getStatusCode()) {
            LOGGER.info(String.format("Change podname for %s -> %s ", id, podname));
        }
    }
    
    public static Map<String, JSONObject> libraryConfigurations(Client client, String proxyURl) {
        Map<String, JSONObject> configurations = new HashMap<>();
        WebResource proxyWebResource = client.resource(proxyURl);
        ClientResponse allConnectedItems = proxyWebResource.accept(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);
        if (allConnectedItems.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
            String responseAllConnected =allConnectedItems.getEntity(String.class);
            JSONObject responseAllConnectedObject = new JSONObject(responseAllConnected);
            for (Object key : responseAllConnectedObject.keySet()) {
                JSONObject lib = responseAllConnectedObject.getJSONObject(key.toString());
                if (lib.has("status") && lib.getBoolean("status")) {
                    
                    String configURl = proxyURl;
                    if (!configURl.endsWith("/")) {
                        configURl += "/";
                    }
                    configURl += key + "/config";

                    WebResource configResource = client.resource(configURl);
                    ClientResponse configReourceStatus = configResource.accept(MediaType.APPLICATION_JSON)
                            .get(ClientResponse.class);
                    if (configReourceStatus.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
                        
                        configurations.put(key.toString(),
                                new JSONObject(configReourceStatus.getEntity(String.class)));
                    }
                }
            }
        }
        return configurations;
    }
}
