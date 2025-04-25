package cz.incad.kramerius;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xml.sax.SAXException;


import antlr.StringUtils;
import cz.incad.kramerius.cdk.ChannelUtils;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestItem;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestItem.TypeOfReharvset;
import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.services.utils.kubernetes.KubernetesEnvSupport;
import cz.incad.kramerius.utils.ReharvestUtils;

public class KubernetesReharvestProcess {

    public static final int DEFAULT_MAX_ITEMS_TO_DELETE = 10000;
    
    public static final String ONLY_SHOW_CONFIGURATION = "ONLY_SHOW_CONFIGURATION";
    public static final String MAX_ITEMS_TO_DELETE = "MAX_ITEMS_TO_DELETE";

    public static final Logger LOGGER = Logger.getLogger(KubernetesReharvestProcess.class.getName());

    protected static CloseableHttpClient buildApacheClient() {
        return HttpClients.createDefault();
    }

    public static void main(String[] args)  {
        
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
        try (CloseableHttpClient closeableHttpClient = buildApacheClient()){

            if (reharvestMap.containsKey("url") && proxyMap.containsKey("url")) {
                String wurl = reharvestMap.get("url");
                if (!wurl.endsWith("/")) {
                    wurl = wurl + "/";
                }
                
                // Top item
                LOGGER.info("Requesting :"+wurl + "top?state=open");
                HttpGet get = new HttpGet(wurl + "top?state=open");
                try(CloseableHttpResponse response = closeableHttpClient.execute(get)) {
                    int code = response.getCode();
                    if (code == 200) {
                        HttpEntity entity = response.getEntity();
                        InputStream is = entity.getContent();
                        String str = IOUtils.toString(is, "UTF-8");
                        LOGGER.info(String.format("Response is %s", str));
                        JSONObject itemObject = new JSONObject(str);

                        String id = itemObject.getString("id");
                        idReference.set(id);
                        String pid = itemObject.getString("pid");
                        if (!onlyShowConfiguration) {
                            changeState(closeableHttpClient, wurl, id,"running");
                            String podname = env.get("HOSTNAME");
                            if (cz.incad.kramerius.utils.StringUtils.isAnyString(podname)) {
                                changePodname(closeableHttpClient, wurl, id, podname);
                            }
                        }

                        List<Pair<String,String>> allPidsList = new ArrayList<>();
                        try {
                            allPidsList = ReharvestUtils.findPidByType(iterationMap, closeableHttpClient, ReharvestItem.fromJSON(itemObject), maxItemsToDelete);
                            // check size; if size > 10000 - fail state
                            if (allPidsList.size() <  maxItemsToDelete) {

                                String proxyURl = proxyMap.get("url");
                                if (proxyURl != null) {
                                    ReharvestItem reharvestItem = ReharvestItem.fromJSON(itemObject);

                                    Map<String, JSONObject> configurations = libraryConfigurations(closeableHttpClient, proxyURl,reharvestItem);
                                    if (!reharvestItem.getTypeOfReharvest().isDeletingReharvest()) {
                                        // check channels before delete
                                        ChannelUtils.checkSolrChannelEndpoints(closeableHttpClient, configurations);
                                    }

                                    // delete all asociated pids from index
                                    String build = ReharvestUtils.deleteAllGivenPids(closeableHttpClient, destinationMap, allPidsList, onlyShowConfiguration);
                                    LOGGER.info(String.format("Deleted pids results %s",build));

                                    // reindex pids
                                    if (!reharvestItem.getTypeOfReharvest().isDeletingReharvest()) {

                                        if (reharvestItem.getTypeOfReharvest().isNewHarvest()) {
                                            // find properties from libs
                                            Map<String, Map<String, String>> foundRoots = ReharvestUtils.findRootsAndPidPathsFromLibs(closeableHttpClient,pid,configurations);
                                            LOGGER.info("Found roots:"+foundRoots);
                                            //String foundPid = null;
                                            String foundRootPid = null;
                                            String ownPidPath = null;
                                            for (String key : foundRoots.keySet()) {
                                                foundRootPid = foundRoots.get(key).get("root.pid");
                                                ownPidPath = foundRoots.get(key).get("own_pid_path");
                                            }
                                            if (ownPidPath != null) {
                                                LOGGER.info("Own pid path "+ownPidPath);
                                                reharvestItem.setOwnPidPath(ownPidPath);
                                            }
                                            if (foundRootPid != null) {
                                                LOGGER.info("root pid "+ownPidPath);
                                                reharvestItem.setRootPid(foundRootPid);
                                            }

                                            LOGGER.log(Level.INFO, "Reharvest item after check {0}", reharvestItem.toString());
                                        }


                                        ReharvestUtils.reharvestPIDFromGivenCollections(pid, configurations, ""+onlyShowConfiguration, destinationMap, iterationMap, reharvestItem);
                                    }
                                    if (!onlyShowConfiguration) {
                                        changeState(closeableHttpClient, wurl, id,"closed");
                                    }
                                } else {
                                    LOGGER.severe("No proxy configuration");
                                }
                            } else {
                                changeState(closeableHttpClient, wurl, id,"too_big");
                                String compare = String.format("delete.size()  %d >=  maxItemstoDelete %d", allPidsList.size(), maxItemsToDelete);
                                LOGGER.severe(String.format( "Too big to reharvest (%s)  -> manual change ", compare));
                            }

                        } catch(TooBigException ex) {
                            changeState(closeableHttpClient, wurl, id,"too_big");
                            String compare = String.format("delete.size()  %d >=  maxItemstoDelete %d", ex.getCounter(), maxItemsToDelete);
                            LOGGER.severe(String.format( "Too big to reharvest (%s)  -> manual change ", compare));
                        }
                    } else {
                        LOGGER.info("No item to reharvest");
                    }
                }
            } else {
                LOGGER.severe("No proxy or reharvest configuration");
            }
            
            
        } catch (Throwable thr) {
            if (idReference.get() != null) {
                //Client client = buildClient();
                String wurl = reharvestMap.get("url");
                if (!wurl.endsWith("/")) {
                    wurl = wurl + "/";
                }
                try {
                    changeState(buildApacheClient(), wurl, idReference.get(),"failed");
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    throw new RuntimeException(e);
                }
            }
            thr.printStackTrace();
            LOGGER.severe("Exception :" + thr.getMessage());
        }

    }


    private static void changeState(CloseableHttpClient apacheClient, String wurl, String id, String changeState) throws IOException {
        HttpPut put = new HttpPut(wurl + id+"/state?state="+changeState);
        try(CloseableHttpResponse response = apacheClient.execute(put)) {
            if (response.getCode() ==  200) {
                LOGGER.info(String.format("Change state for %s -> %s ", id, changeState));
            }
        }
    }

    private static void changePodname(CloseableHttpClient apacheClient, String wurl, String id, String podname) throws IOException {
        HttpPut put = new HttpPut(wurl + id+"/pod?pod="+podname);
        try(CloseableHttpResponse response = apacheClient.execute(put)) {
            if (response.getCode() ==  200) {
                LOGGER.info(String.format("Change podname for %s -> %s ", id, podname));
            }
        }
    }


    public static Map<String, JSONObject> libraryConfigurations(CloseableHttpClient closeableHttpClient, String proxyURl, ReharvestItem reharvestItem) {
        Map<String, JSONObject> configurations = new HashMap<>();
        HttpGet get = new HttpGet(proxyURl);
        try(CloseableHttpResponse response = closeableHttpClient.execute(get)) {
            int code = response.getCode();
            if (code == 200) {
                InputStream is = response.getEntity().getContent();
                String content = IOUtils.toString(is, "UTF-8");

                JSONObject responseAllConnectedObject = new JSONObject(content);
                for (Object key : responseAllConnectedObject.keySet()) {
                    JSONObject lib = responseAllConnectedObject.getJSONObject(key.toString());
                    if (lib.has("status") && lib.getBoolean("status")) {


                        String configURl = proxyURl;
                        if (!configURl.endsWith("/")) {
                            configURl += "/";
                        }
                        configURl += key + "/config";

                        HttpGet configGet = new HttpGet(configURl);
                        try(CloseableHttpResponse configResult = closeableHttpClient.execute(configGet)) {

                            InputStream configStream = configResult.getEntity().getContent();
                            String configContent =IOUtils.toString(configStream, "UTF-8");

                            boolean add = true;
                            if (reharvestItem.getLibraries() != null & !reharvestItem.getLibraries().isEmpty()) {
                                add = reharvestItem.getLibraries().contains(key.toString());
                            }
                            if (add) {
                                configurations.put(key.toString(),
                                        new JSONObject(configContent));
                            }
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return configurations;
    }
}
