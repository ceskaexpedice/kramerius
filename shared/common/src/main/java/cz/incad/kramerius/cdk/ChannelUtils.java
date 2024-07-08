package cz.incad.kramerius.cdk;

import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;


public class ChannelUtils {
    
    public static Logger LOGGER = Logger.getLogger(ChannelUtils.class.getName());
    
    private ChannelUtils() {
    }
    public static String userChannelUrl(String apiVersion, String channel) {
        String fullChannelUrl = null;
        if (apiVersion.toLowerCase().equals("v5")) {
            //channel = 
            fullChannelUrl = channel+(channel.endsWith("/") ? "" : "/")+"api/v5.0/cdk/forward/user";
            // channel+(channel.endsWith("/") ? "" : "/")+"api/v5.0/cdk/forward/sync/solr");
        } else {
            fullChannelUrl = channel+(channel.endsWith("/") ? "" : "/")+"api/cdk/v7.0/forward/user";
            //channel+(channel.endsWith("/") ? "" : "/")+"api/cdk/v7.0/forward/sync/solr");
        }
        return fullChannelUrl;
    }

    
    public static String solrChannelUrl(String apiVersion, String channel) {
        String fullChannelUrl = null;
        if (apiVersion.toLowerCase().equals("v5")) {
            //channel = 
            fullChannelUrl = channel+(channel.endsWith("/") ? "" : "/")+"api/v5.0/cdk/forward/sync/solr";
            // channel+(channel.endsWith("/") ? "" : "/")+"api/v5.0/cdk/forward/sync/solr");
        } else {
            fullChannelUrl = channel+(channel.endsWith("/") ? "" : "/")+"api/cdk/v7.0/forward/sync/solr";
            //channel+(channel.endsWith("/") ? "" : "/")+"api/cdk/v7.0/forward/sync/solr");
        }
        return fullChannelUrl;
    }
    
    public static void userChannelEndpoints(Client client, Map<String,JSONObject> collectionConfigurations) {
        for (String ac : collectionConfigurations.keySet()) {
            JSONObject colObject = collectionConfigurations.get(ac);
            String apiVersion = colObject.optString("api","v5");
            if (!colObject.has("forwardurl")) {
                LOGGER.severe(String.format("Skipping %s", ac));
                continue;
            }
            String channel = colObject.optString("forwardurl");
            String fullChannelUrl = userChannelUrl(apiVersion, channel);
            
            LOGGER.info(String.format("Checking %s", fullChannelUrl));
            checkUserChannelEndpoint(client, ac, fullChannelUrl);
            
//            WebResource configResource = client.resource(fullChannelUrl);
//            ClientResponse configReourceStatus = configResource.accept(MediaType.APPLICATION_JSON)
//                    .get(ClientResponse.class);
//            if (configReourceStatus.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
//                // ok - live channel
//            } else throw new IllegalStateException(String.format("Channel for %s(%s) doesnt work ", ac, channel));
        }
    }

    public static void checkUserChannelEndpoint(Client client,String ac,String fullChannelUrl) {
        WebResource configResource = client.resource(fullChannelUrl);
        ClientResponse configReourceStatus = configResource.accept(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);
        if (configReourceStatus.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
            // ok - live channel
        } else throw new IllegalStateException(String.format("Channel for %s(%s) doesnt work ", ac, fullChannelUrl));
        
    }
    
    public static void checkSolrChannelEndpoints(Client client, Map<String,JSONObject> collectionConfigurations) {
        for (String ac : collectionConfigurations.keySet()) {
            JSONObject colObject = collectionConfigurations.get(ac);
            String apiVersion = colObject.optString("api","v5");
            if (!colObject.has("forwardurl")) {
                LOGGER.severe(String.format("Skipping %s", ac));
                continue;
            }
            String channel = colObject.optString("forwardurl");
            String fullChannelUrl = solrChannelUrl(apiVersion, channel);
            
            LOGGER.info(String.format("Checking %s", fullChannelUrl));
            checkSolrChannelEndpoint(client, ac,fullChannelUrl);
            
//            WebResource configResource = client.resource(fullChannelUrl+"/select?q=*&rows=0&wt=json");
//            ClientResponse configReourceStatus = configResource.accept(MediaType.APPLICATION_JSON)
//                    .get(ClientResponse.class);
//            if (configReourceStatus.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
//                // ok - live channel
//            } else throw new IllegalStateException(String.format("Channel for %s(%s) doesnt work ", ac, channel));
        }
    }

    
    public static void checkSolrChannelEndpoint(Client client, String ac,String fullChannelUrl) {
        WebResource configResource = client.resource(fullChannelUrl+"/select?q=*&rows=0&wt=json");
        ClientResponse configReourceStatus = configResource.accept(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);
        if (configReourceStatus.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
            // ok - live channel
        } else throw new IllegalStateException(String.format("Channel for %s(%s) doesnt work ", ac, fullChannelUrl));
    }
    
}

