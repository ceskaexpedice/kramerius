package cz.incad.kramerius.service.impl;

import cz.incad.kramerius.intconfig.InternalConfiguration;
import cz.incad.kramerius.processes.impl.ProcessStarter;
import cz.incad.kramerius.processes.utils.ProcessUtils;
import cz.incad.kramerius.utils.BasicAuthenticationFilter;
import cz.incad.kramerius.utils.IPAddressUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.core.util.Base64;

public class IndexerProcessStarter {

    private static final Logger log = Logger.getLogger(IndexerProcessStarter.class.getName());


    private static final String AUTH_TOKEN_HEADER_KEY = "auth-token";
    private static final String TOKEN_ATTRIBUTE_KEY = "token";

    private static final String USER_TOKEN="user";
    private static final String PSWD_TOKEN="pswd";
    
    
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
    
    
    
    public static String planIndexProcess(String...args) {
        Client c = Client.create();
        WebResource r = c.resource(ProcessUtils.getApiPoint()+"?def=reindex");
        r.addFilter(new TokensFilter());

        JSONObject object = new JSONObject();
        object.put("parameters", JSONArray.fromObject(Arrays.asList(args)));

        log.info("authToken :"+System.getProperty("authToken"));
        log.info("token :"+System.getProperty(TOKEN_ATTRIBUTE_KEY));
        
//        r.header(AUTH_TOKEN_HEADER_KEY, System.getProperty("authToken"));
//        r.header(TOKEN_ATTRIBUTE_KEY, System.getProperty(TOKEN_ATTRIBUTE_KEY));

        String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).entity(object.toString(), MediaType.APPLICATION_JSON)
                .post(String.class);
        return t;
    }

    
    public static void spawnIndexer(String name, String pswd, boolean incremental, String title, String uuid) throws UnsupportedEncodingException {
        System.setProperty(USER_TOKEN, name);
        System.setProperty(PSWD_TOKEN, pswd);
        spawnIndexer(incremental, title, uuid);
    }
    
    public static void spawnIndexer(boolean incremental, String title, String uuid) throws UnsupportedEncodingException {
        log.info("Spawn indexer: title: "+title+" pid: "+uuid);
        String base = ProcessUtils.getLrServlet();
        if (base == null || uuid == null){
            log.severe("Cannot start indexer, invalid arguments: base:"+base+" pid:"+uuid);
            return;
        }
        if (title == null || "".equals(title.trim())){
            title = "untitled";
        }
        title = title.replaceAll(",", " ");
        String param = incremental?"reindexDoc":"fromKrameriusModel";

        planIndexProcess(param,uuid,URLEncoder.encode(title, "UTF-8"));
    }

    public static void spawnIndexRemover(String pid_path, String uuid) {
        log.info("spawnIndexRemower: pid_path: "+pid_path+" pid: "+uuid);
        String base = ProcessUtils.getLrServlet();
        if (base == null || pid_path == null || uuid == null){
            log.severe("Cannot start indexer, invalid arguments: base:"+base+" pid:"+uuid+" pid_path:"+pid_path);
            return;
        }
        if (pid_path.endsWith("/")){
            pid_path = pid_path.substring(0,pid_path.length()-1);
        }

        planIndexProcess("deleteDocument",pid_path,uuid);
    }
}


