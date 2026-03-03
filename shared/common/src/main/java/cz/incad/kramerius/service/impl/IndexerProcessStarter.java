package cz.incad.kramerius.service.impl;

import cz.incad.kramerius.utils.BasicAuthenticationFilter;
import cz.incad.kramerius.utils.IPAddressUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Logger;

import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


//TODO: pepo
@Deprecated
public class IndexerProcessStarter {

    private static final Logger log = Logger.getLogger(IndexerProcessStarter.class.getName());


    private static final String AUTH_TOKEN_HEADER_KEY = "auth-token";
    private static final String TOKEN_ATTRIBUTE_KEY = "token";

    private static final String USER_TOKEN = "user";
    private static final String PSWD_TOKEN = "pswd";
    
    /* TODO migration
    public static class TokensFilter extends ClientFilter {
        
        public TokensFilter() {
        }

        public ClientResponse handle(ClientRequest clientRequest) throws ClientHandlerException {
            if (System.getProperties().containsKey("authToken")) {
                clientRequest.getHeaders().add(AUTH_TOKEN_HEADER_KEY, System.getProperty("authToken"));
            }
            if (System.getProperties().containsKey(TOKEN_ATTRIBUTE_KEY)) {
                clientRequest.getHeaders().add(TOKEN_ATTRIBUTE_KEY, System.getProperty(TOKEN_ATTRIBUTE_KEY));
            }            

            if ((System.getProperties().containsKey(USER_TOKEN)) && (System.getProperties().containsKey(PSWD_TOKEN))) {
                String uname = System.getProperties().getProperty(USER_TOKEN);
                String pwd = System.getProperties().getProperty(PSWD_TOKEN);
                BasicAuthenticationFilter.encodeUserAndPass(clientRequest, uname, pwd);
            }
            if (System.getProperties().containsKey(IPAddressUtils.X_IP_FORWARD)) {
                clientRequest.getHeaders().add(IPAddressUtils.X_IP_FORWARD, System.getProperty(IPAddressUtils.X_IP_FORWARD));
            }
            
            return getNext().handle(clientRequest);
        }
    }

     */

    @Provider
    public static class TokensFilter implements ClientRequestFilter {

        @Override
        public void filter(ClientRequestContext request) throws IOException {
            if (System.getProperties().containsKey("authToken")) {
                request.getHeaders().add(AUTH_TOKEN_HEADER_KEY, System.getProperty("authToken"));
            }
            if (System.getProperties().containsKey(TOKEN_ATTRIBUTE_KEY)) {
                request.getHeaders().add(TOKEN_ATTRIBUTE_KEY, System.getProperty(TOKEN_ATTRIBUTE_KEY));
            }
            if (System.getProperties().containsKey(USER_TOKEN) && System.getProperties().containsKey(PSWD_TOKEN)) {
                String uname = System.getProperty(USER_TOKEN);
                String pwd = System.getProperty(PSWD_TOKEN);
                String token = Base64.getEncoder().encodeToString((uname + ":" + pwd).getBytes(StandardCharsets.UTF_8));
                request.getHeaders().putSingle(HttpHeaders.AUTHORIZATION, "Basic " + token);
            }
            if (System.getProperties().containsKey(IPAddressUtils.X_IP_FORWARD)) {
                request.getHeaders().add(IPAddressUtils.X_IP_FORWARD, System.getProperty(IPAddressUtils.X_IP_FORWARD));
            }
        }
    }

    /*
        public static String planIndexProcess(String... args) {
            Client c = Client.create();
            WebResource r = null; //c.resource(ProcessUtils.getOldApiEndpointProcesses()+"?def=reindex");
            r.addFilter(new TokensFilter());

            JSONObject object = new JSONObject();
            object.put("parameters", JSONArray.fromObject(Arrays.asList(args)));

            log.info("authToken :" + System.getProperty("authToken"));
            log.info("token :" + System.getProperty(TOKEN_ATTRIBUTE_KEY));

    //        r.header(AUTH_TOKEN_HEADER_KEY, System.getProperty("authToken"));
    //        r.header(TOKEN_ATTRIBUTE_KEY, System.getProperty(TOKEN_ATTRIBUTE_KEY));

            String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).entity(object.toString(), MediaType.APPLICATION_JSON)
                    .post(String.class);
            return t;
        }
    */

    public static String planIndexProcess(String... args) {
        Client client = ClientBuilder.newBuilder()
                .register(new TokensFilter())   // filter registered here
                .build();
//        WebTarget target = client.target(ProcessUtils.getOldApiEndpointProcesses() + "?def=reindex");
        WebTarget target = null;
        JSONObject object = new JSONObject();
        object.put("parameters", JSONArray.fromObject(Arrays.asList(args)));

        log.info("authToken :" + System.getProperty("authToken"));
        log.info("token :" + System.getProperty(TOKEN_ATTRIBUTE_KEY));

        String response = target
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(object.toString(), MediaType.APPLICATION_JSON), String.class);
        client.close();
        return response;
    }

    public static void spawnIndexer(String name, String pswd, boolean incremental, String title, String uuid) throws UnsupportedEncodingException {
        System.setProperty(USER_TOKEN, name);
        System.setProperty(PSWD_TOKEN, pswd);
        spawnIndexer(incremental, title, uuid);
    }

    public static void spawnIndexer(boolean incremental, String title, String uuid) throws UnsupportedEncodingException {
        log.info("Spawn indexer: title: " + title + " pid: " + uuid);
        String base = null; //ProcessUtils.getLrServlet();
        if (base == null || uuid == null) {
            log.severe("Cannot start indexer, invalid arguments: base:" + base + " pid:" + uuid);
            return;
        }
        if (title == null || "".equals(title.trim())) {
            title = "untitled";
        }
        title = title.replaceAll(",", " ");
        String param = incremental ? "reindexDoc" : "fromKrameriusModel";

        planIndexProcess(param, uuid, URLEncoder.encode(title, "UTF-8"));
    }

    public static void spawnIndexerForModel(String... models) {
        log.info("Spawn indexer: model: " + Arrays.toString(models));
        String base = null;// ProcessUtils.getLrServlet();
        for (String model : models) {
            planIndexProcess("krameriusModel", model, model);
        }
    }

    public static void spawnIndexRemover(String uuid) {
        log.info("spawnIndexRemower:  pid: " + uuid);
        String base = null; //ProcessUtils.getLrServlet();
        if (base == null || uuid == null) {
            log.severe("Cannot start indexer, invalid arguments: base:" + base + " pid:" + uuid);
            return;
        }


        planIndexProcess("deleteDocument", uuid);
    }
}


