package cz.incad.kramerius.cdk;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;


import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

import cz.incad.kramerius.utils.StringUtils;


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
            checkUserChannelEndpoint(client, ac, fullChannelUrl,false);
        }
    }

    public static JSONObject checkUserChannelEndpoint(Client client, String ac,String fullChannelUrl, boolean header) {
        WebResource configResource = client.resource(fullChannelUrl);
        Builder builder = configResource.accept(MediaType.APPLICATION_JSON);
        if (header) {
            String headerText = "header_shib-session-id=_dd68cbd66641c9b647b05509ac0241fa|header_shib-session-expires=1592847906|header_shib-identity-provider=https://shibboleth.mzk.cz/simplesaml/metadata.xml|header_shib-authentication-method=urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport|header_shib-handler=https://dnnt.mzk.cz/Shibboleth.sso|header_eppn=test|header_entitlement=cdk.entitlement|header_eduPersonEntitlement=urn:mace:dir:entitlement:common-lib-terms|header_knav_type=validuser|header_knav_session_eppn=test|header_displayName=Inovatika|header_expiration_time=1720851072|header_knav_entitlement=urn:mace:dir:entitlement:common-lib-terms|header_entitlement=urn:mace:dir:entitlement:common-lib-terms|header_remote_user=f4ca5f6c5859d882f16aea477cd64a4c5887d3df824b91c7ab29c66091fccfff.aadzzwnyzxqx1rezk8w/sgpucjhb9hbrkxz8las3xof3hlpgnr6/ocwgyi82t6vwjepzgkru4iayuqkirk8dfilp68/i9ffozxdb25+wbrr8ij10tbowcfqgooztwhoiezaug/qijsyq1iftbo9cm5zq4z+h2ivqutjhv9trbjsdtnx0svpgtim=|header_eduPersonScopedAffiliation=[employee@lib.cas.cz, member@lib.cas.cz]|header_token_id=e96c9aec-a262-4fc6-8fe1-4104bdaa8dc8|header_affiliation=[employee@lib.cas.cz, member@lib.cas.cz]|header_knav_affiliation=[employee@lib.cas.cz, member@lib.cas.cz]|header_eduPersonPrincipalName=principalname@lib.cas.cz|header_knav_dnnt_user=test|header_eduPersonUniqueId=eduperson@lib.cas.cz|header_expires_in=1801|header_preffered_user_name=f4ca5f6c5859d882f16aea477cd64a4c5887d3df824b91c7ab29c66091fccfff.aadzzwnyzxqx1rezk8w/sgpucjhb9hbrkxz8las3xof3hlpgnr6/ocwgyi82t6vwjepzgkru4iayuqkirk8dfilp68/i9ffozxdb25+wbrr8ij10tbowcfqgooztwhoiezaug/qijsyq1iftbo9cm5zq4z+h2ivqutjhv9trbjsdtnx0svpgtim=|header_email=xxx@time.com|header_authentication_time=1720849271|header_ip_address=xx.xx.xx.xx";
            builder = builder.header("CDK_TOKEN_PARAMETERS", headerText);
            LOGGER.info("CDK_TOKEN_PARAMETERS = "+headerText+";");
        }
        ClientResponse userRes = builder.get(ClientResponse.class);
        if (userRes.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
            String t = userRes.getEntity(String.class);
            return new JSONObject(t);
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
    
    public static String solrChannelPid(Client client, String ac, String fullChannelUrl, String apiVersion, String pid) throws UnsupportedEncodingException {
        if (apiVersion.toLowerCase().equals("v5")) {
            String query = URLEncoder.encode( "PID:\""+pid+"\"", "UTF-8");
            String url = fullChannelUrl+"/select?q="+query+"&rows=0&wt=json";
            LOGGER.info("SOLR URL "+url);
            WebResource configResource = client.resource(url);
            ClientResponse solrResource = configResource.accept(MediaType.APPLICATION_JSON)
                    .get(ClientResponse.class);
            if (solrResource.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
                String entity = solrResource.getEntity(String.class);
                return entity;
            } 
        } else {
            String query = URLEncoder.encode( "pid:\""+pid+"\"", "UTF-8");
            String url = fullChannelUrl+"/select?q="+query+"\"&rows=0&wt=json";
            LOGGER.info("SOLR URL "+url);
            WebResource configResource = client.resource(url);
            ClientResponse solrResource = configResource.accept(MediaType.APPLICATION_JSON)
                    .get(ClientResponse.class);
            if (solrResource.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
                String entity = solrResource.getEntity(String.class);
                return entity;
            } 
        }
        return null;
    }
    
}

