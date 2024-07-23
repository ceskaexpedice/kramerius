package cz.inovatika.cdk.sync;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

import cz.incad.kramerius.rest.apiNew.admin.v10.reharvest.ReharvestItem;
import cz.incad.kramerius.rest.apiNew.admin.v10.reharvest.ReharvestItem.TypeOfReharvset;
import cz.incad.kramerius.services.iterators.IterationItem;
import cz.incad.kramerius.services.iterators.ProcessIterator;
import cz.incad.kramerius.services.iterators.solr.SolrCursorIterator;
import cz.incad.kramerius.services.iterators.solr.SolrFilterQueryIterator;
import cz.incad.kramerius.services.iterators.solr.SolrPageIterator;
import cz.incad.kramerius.services.iterators.solr.SolrIteratorFactory.TypeOfIteration;
import cz.incad.kramerius.services.utils.SolrUtils;
import cz.incad.kramerius.services.utils.kubernetes.KubernetesEnvSupport;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

import static cz.incad.kramerius.utils.ReharvestUtils.*;


public class KubernetesSyncProcess {
    
    
    
    public static final Logger LOGGER = Logger.getLogger(KubernetesSyncProcess.class.getName());

    public static final String ONLY_SHOW_CONFIGURATION = "ONLY_SHOW_CONFIGURATION";

    protected static Client buildClient() {
        // Client client = Client.create();
        ClientConfig cc = new DefaultClientConfig();
        cc.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
        cc.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        return Client.create(cc);
    }

    /** Find all pids by given root.pid 
     * @throws IOException 
     * @throws SAXException 
     * @throws ParserConfigurationException */
    public static void comparePids(Map<String, String> iterationMap,Map<String, String> comparingMap,Map<String, String> reharvestMap, JSONObject libs, String dl, String model, Client client) throws ParserConfigurationException, SAXException, IOException {

        String reharvestUrl = reharvestMap.get("url");
        
        HashSet<String> source = identifiers(iterationMap, model, client);
        TreeSet<String> sortedSource = new TreeSet<>(source);
        
        if (!comparingMap.containsKey("api")) {
            JSONObject libObject = libs.getJSONObject(dl);
            JSONObject configObject = libObject.getJSONObject("config");
            comparingMap.put("api", configObject.getString("api"));
        }
        if (!comparingMap.containsKey("url")) {
            JSONObject libObject = libs.getJSONObject(dl);
            JSONObject configObject = libObject.getJSONObject("config");
            String forwardUrl = configObject.getString("forwardurl");
            String api = comparingMap.get("api");
            switch (api) {
                case "v7":
                    String v7url= forwardUrl+(forwardUrl.endsWith("/")?"":"/")+ "api/cdk/v7.0/forward/sync/solr";
                    comparingMap.put("url", v7url);
                    break;

                case "v5":
                    String v5url= forwardUrl+(forwardUrl.endsWith("/")?"":"/")+ "api/v5.0/cdk/forward/sync/solr";
                    comparingMap.put("url", v5url);
                    break;

                default:
                    String defaulturl= forwardUrl+(forwardUrl.endsWith("/")?"":"/")+ "api/cdk/v7.0/forward/sync/solr";
                    comparingMap.put("url", defaulturl);
                    break;
            }
            //comparingMap.put("url", configObject.getString("forwardurl"));
        }       

        HashSet<String> comparing = identifiers(comparingMap, model, client);
        TreeSet<String> sortedComparing = new TreeSet<>(comparing);
        
        LOGGER.info(String.format("--- Model %s ---", model));
        
        while(!sortedSource.isEmpty()) {
            String sourceTop = sortedSource.first();
            if (comparing.contains(sourceTop)) {
                sortedComparing.remove(sourceTop);
            } else {
                
                String url = iterationMap.get("url");
                String endpoint = iterationMap.containsKey("endpoint") ? iterationMap.get("endpoint") : "select";
                
                if (!url.endsWith("/")) { url = url+"/";  }
                url = url + endpoint;
                
                
                ReharvestItem reharvestItem = new ReharvestItem(UUID.randomUUID().toString(), "Sync trigger - probably deleted- whole reharvest","open", sourceTop, sourceTop);
                reharvestItem.setTypeOfReharvest(TypeOfReharvset.root);
                reharvestItem.setState("waiting_for_approve");
                reharvestItem.setRootPid(sourceTop);
                reharvestItem.setOwnPidPath(sourceTop);
                
                reharvestItem.setLibraries(allEnabledLibraries(libs));

                
                WebResource r = client.resource(reharvestUrl);
                ClientResponse resp = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).entity(reharvestItem.toJSON().toString(), MediaType.APPLICATION_JSON).put(ClientResponse.class);
                if (resp.getStatus() != Response.Status.OK.getStatusCode()) {
                    String errorMsg = resp.getEntity(String.class);
                    LOGGER.warning(String.format("%s",errorMsg));
                }
            }
            sortedSource.remove(sourceTop);
        }

        
        while(!sortedComparing.isEmpty()) {
            String compTop = sortedComparing.first();
            if (source.contains(compTop)) {
                sortedSource.remove(compTop);
            } else {
                
                if (exists(compTop, iterationMap, client)) {

                    ReharvestItem reharvestItem = new ReharvestItem(UUID.randomUUID().toString(), "Sync trigger - missing DL","open", compTop, "none");
                    reharvestItem.setTypeOfReharvest(TypeOfReharvset.root);
                    reharvestItem.setState("waiting_for_approve");
                    reharvestItem.setRootPid(compTop);
                    reharvestItem.setLibraries(allEnabledLibraries(libs));
                    
                    
                    WebResource r = client.resource(reharvestUrl);
                    ClientResponse resp = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).entity(reharvestItem.toJSON().toString(), MediaType.APPLICATION_JSON).put(ClientResponse.class);
                    LOGGER.info("Status:"+resp.getStatus());
                    if (resp.getStatus() != Response.Status.OK.getStatusCode()) {
                        String errorMsg = resp.getEntity(String.class);
                        LOGGER.warning(String.format("%s",errorMsg));
                    }

                } else {
                    ReharvestItem reharvestItem = new ReharvestItem(UUID.randomUUID().toString(), "Sync trigger - missing title","open", compTop, "none");
                    reharvestItem.setTypeOfReharvest(TypeOfReharvset.new_root);
                    reharvestItem.setState("waiting_for_approve");
                    reharvestItem.setRootPid(compTop);
                    reharvestItem.setLibraries(Arrays.asList(dl));
                    
                    
                    WebResource r = client.resource(reharvestUrl);
                    ClientResponse resp = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).entity(reharvestItem.toJSON().toString(), MediaType.APPLICATION_JSON).put(ClientResponse.class);
                    LOGGER.info("Status:"+resp.getStatus());
                    if (resp.getStatus() != Response.Status.OK.getStatusCode()) {
                        String errorMsg = resp.getEntity(String.class);
                        LOGGER.warning(String.format("%s",errorMsg));
                    }
                }
            }
            sortedComparing.remove(compTop);
        }
    }

    private static List<String> allEnabledLibraries(JSONObject libs) {
        List<String> libsArray = new ArrayList<>();
        Set keys = libs.keySet();
        for (Object key : keys) {
            JSONObject libObject =  libs.getJSONObject(key.toString());
            boolean status = libObject.optBoolean("status",false);
            if (status) {
                JSONObject config = libObject.getJSONObject("config");
                if (config.optBoolean("licenses",false)) {
                    libsArray.add(key.toString());
                }
            }
        }
        return libsArray;
    }

    private static boolean exists(String pid, Map<String, String> map,  Client client) throws ParserConfigurationException, SAXException, IOException {
        String iterationUrl = map.get("url");
        
        String solrEndpoint = solrEndpoint(map);
        
        String masterQuery = URLEncoder.encode(String.format("%s:\"%s\"", "pid", pid), "UTF-8");
        
        
        Element response = SolrUtils.executeQuery(client, iterationUrl, solrEndpoint+"?q="+masterQuery+"&wt=xml", "", "");
        List<String> findAllPids = SolrUtils.findAllPids(response);
        
        return !findAllPids.isEmpty();
    }    
    
    
    private static HashSet<String> identifiers(Map<String, String> map, String model, Client client) {
        String iterationUrl = map.get("url");
        String api = map.get("api");

        String masterQuery = "*:*";
        String filterQuery = "";
        if (map.containsKey("fq")) {
            filterQuery = String.format("(%s AND %s)", map.get("fq"), modelQuery(model, api));
        } else {
            filterQuery = modelQuery(model, api);
        }
        String sRows = map.containsKey("rows")   ? map.get("rows")  : ITERATION_ROWS_STRING_VALUE  ;
        String identifier = identifier(map);
        TypeOfIteration typeOfIteration = typeOfIteration(map);
        
        
        ProcessIterator processIterator = null;
        switch (typeOfIteration) {
            case CURSOR: {
                processIterator =  new SolrCursorIterator(iterationUrl, masterQuery, filterQuery, solrEndpoint(map), identifier(map), sort(map),Integer.parseInt(sRows));
                break;
            }
            case FILTER: {
                processIterator  = new SolrFilterQueryIterator( iterationUrl, masterQuery, filterQuery, solrEndpoint(map), identifier(map), sort(map),Integer.parseInt(sRows));
                break;
            }
            case PAGINATION: {
                processIterator = new SolrPageIterator( iterationUrl, masterQuery, filterQuery, solrEndpoint(map), identifier(map), sort(map),Integer.parseInt(sRows));
                break;
            }
        }
        LOGGER.info(String.format("Solr iterator %s, filter query %s, source url %s" , processIterator.getClass().getName(), filterQuery, iterationUrl));
        final HashSet<String> sourcePids  = new HashSet<>();
        processIterator.iterate(client, (list)-> {
            for (IterationItem it : list) {
                String id = it.getPid();
                if (identifier.equals("compositeId")) {
                    String[] arr = id.split("!");
                    if (arr.length == 2) { 
                        String pid = arr[1];
                        sourcePids.add(pid);
                    } else {
                        sourcePids.add(id);
                    }
                } else {
                    sourcePids.add(id);
                }
            }
            
        }, ()->{});

        LOGGER.info(String.format("Number of results %d" , sourcePids.size()));

        return sourcePids;
    }

    private static TypeOfIteration typeOfIteration(Map<String, String> iterationMap) {
        return iterationMap.containsKey("type")  ? TypeOfIteration.valueOf(iterationMap.get("type")) : TypeOfIteration.CURSOR;
    }

    private static String identifier(Map<String, String> iterationMap) {
        return iterationMap.containsKey("identifier")   ? iterationMap.get("identifier")  : "compositeId";
    }

    private static String sort(Map<String, String> iterationMap) {
        return iterationMap.containsKey("sort")   ? iterationMap.get("sort")  :  String.format("%s asc", identifier(iterationMap));
    }

    private static String solrEndpoint(Map<String, String> iterationMap) {
        return iterationMap.containsKey("endpoint")   ? iterationMap.get("endpoint")  : "select";
    }

    private static String modelQuery(String model, String api) {
        switch(api) {
            case "v5":
                return String.format("fedora.model:%s", model);
            case "v7":
                return String.format("model:%s", model);
        }
        return String.format("model:%s", model);
    }

    
    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException  {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Prague"));
        
        Client buildClient = buildClient();
        Map<String, String> env = System.getenv();
        Map<String, String> iterationMap = KubernetesEnvSupport.iterationMap(env);
        if (!iterationMap.containsKey("batch")) {
            iterationMap.put("batch", "45");
        }
        if (!iterationMap.containsKey("dl")) {
            throw new RuntimeException("DL expecting");
        }
        
        if (!iterationMap.containsKey("fq")) {
            iterationMap.put("fq", String.format("cdk.collection:%s", iterationMap.get("dl")));
        }
        
        Map<String, String> reharvestMap = KubernetesEnvSupport.reharvestMap(env);
        Map<String, String> comparingSyncMap = KubernetesEnvSupport.comparingMap(env);
        if (!comparingSyncMap.containsKey("batch")) {
            comparingSyncMap.put("batch", "45");
        }
        
        
        Map<String,String> proxyMap = KubernetesEnvSupport.proxyMap(env);
        String proxyUrl = proxyMap.get("url");
        if (proxyUrl == null) throw new IllegalStateException("expecting PROXY_API_URL");

        JSONObject librariesInfo = librariesInfo(buildClient, proxyUrl);
        List<String> topLevelModels = Lists.transform(KConfiguration.getInstance().getConfiguration().getList("fedora.topLevelModels"), Functions.toStringFunction());
        for (String topLevelModel : topLevelModels) {
            comparePids(iterationMap, comparingSyncMap,reharvestMap, librariesInfo,  iterationMap.get("dl"), topLevelModel, buildClient);
        }
    }


    
    private static JSONObject librariesInfo(Client buildClient, String proxyUrl) {
        WebResource r = buildClient.resource(proxyUrl);
        ClientResponse resp = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        if (resp.getStatus() == Response.Status.OK.getStatusCode()) {
            String proxyURLREsp = resp.getEntity(String.class);
            JSONObject libraries = new JSONObject(proxyURLREsp);
            Set keys = libraries.keySet();
            for (Object key : keys) {
                JSONObject libObject = libraries.getJSONObject(key.toString());
                WebResource keyr = buildClient.resource(proxyUrl+"/"+key.toString()+"/config");
                ClientResponse configResp = keyr.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).get(ClientResponse.class);
                if (configResp.getStatus() == Response.Status.OK.getStatusCode()) {
                    String cionfigResp = configResp.getEntity(String.class);
                    JSONObject configRespJSON = new JSONObject(cionfigResp);
                    libObject.put("config", configRespJSON);
                } 
            }
            return libraries;
        } else {
            throw new RuntimeException(String.format("Error response from %s (status %d)",proxyUrl, resp.getStatus()));
        }
    }

}
