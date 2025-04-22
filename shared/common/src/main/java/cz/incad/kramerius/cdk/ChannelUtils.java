package cz.incad.kramerius.cdk;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
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
    
    public static void userChannelEndpoints(CloseableHttpClient client, Map<String,JSONObject> collectionConfigurations) {
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

    public static JSONObject checkUserChannelEndpoint(CloseableHttpClient client, String ac,String fullChannelUrl, boolean header) {
        HttpGet get = new HttpGet(fullChannelUrl);
        get.setHeader("Accept", "application/json");
        if (header) {
            String headerText = "header_shib-session-id=_dd68cbd66641c9b647b05509ac0241fa|header_shib-session-expires=1592847906|header_shib-identity-provider=https://shibboleth.mzk.cz/simplesaml/metadata.xml|header_shib-authentication-method=urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport|header_shib-handler=https://dnnt.mzk.cz/Shibboleth.sso|header_eppn=test|header_entitlement=cdk.entitlement|header_eduPersonEntitlement=urn:mace:dir:entitlement:common-lib-terms|header_knav_type=validuser|header_knav_session_eppn=test|header_displayName=Inovatika|header_expiration_time=1720851072|header_knav_entitlement=urn:mace:dir:entitlement:common-lib-terms|header_entitlement=urn:mace:dir:entitlement:common-lib-terms|header_remote_user=f4ca5f6c5859d882f16aea477cd64a4c5887d3df824b91c7ab29c66091fccfff.aadzzwnyzxqx1rezk8w/sgpucjhb9hbrkxz8las3xof3hlpgnr6/ocwgyi82t6vwjepzgkru4iayuqkirk8dfilp68/i9ffozxdb25+wbrr8ij10tbowcfqgooztwhoiezaug/qijsyq1iftbo9cm5zq4z+h2ivqutjhv9trbjsdtnx0svpgtim=|header_eduPersonScopedAffiliation=[employee@lib.cas.cz, member@lib.cas.cz]|header_token_id=e96c9aec-a262-4fc6-8fe1-4104bdaa8dc8|header_affiliation=[employee@lib.cas.cz, member@lib.cas.cz]|header_knav_affiliation=[employee@lib.cas.cz, member@lib.cas.cz]|header_eduPersonPrincipalName=principalname@lib.cas.cz|header_knav_dnnt_user=test|header_eduPersonUniqueId=eduperson@lib.cas.cz|header_expires_in=1801|header_preffered_user_name=f4ca5f6c5859d882f16aea477cd64a4c5887d3df824b91c7ab29c66091fccfff.aadzzwnyzxqx1rezk8w/sgpucjhb9hbrkxz8las3xof3hlpgnr6/ocwgyi82t6vwjepzgkru4iayuqkirk8dfilp68/i9ffozxdb25+wbrr8ij10tbowcfqgooztwhoiezaug/qijsyq1iftbo9cm5zq4z+h2ivqutjhv9trbjsdtnx0svpgtim=|header_email=xxx@time.com|header_authentication_time=1720849271|header_ip_address=xx.xx.xx.xx";
            get.setHeader("CDK_TOKEN_PARAMETERS", headerText);
        }
        try (CloseableHttpResponse response = client.execute(get)) {
            int code = response.getCode();
            if (code == 200) {
                HttpEntity entity = response.getEntity();
                String content = IOUtils.toString(entity.getContent(), Charset.forName("UTF-8"));
                return new JSONObject(content);
            } else {
                throw new IllegalStateException(String.format("Channel for %s(%s) doesnt work ", ac, fullChannelUrl));
            }
        }catch(IOException ex) {
            LOGGER.log(Level.SEVERE,ex.getMessage(),ex);
            throw new IllegalStateException(String.format("Channel for %s(%s) doesnt work ", ac, fullChannelUrl));
        }
    }
    
    public static void checkSolrChannelEndpoints(CloseableHttpClient client, Map<String,JSONObject> collectionConfigurations) {
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


    public static void checkSolrChannelEndpoint(CloseableHttpClient apacheClient, String ac, String fullChannelUrl) {
        HttpGet head = new HttpGet(fullChannelUrl+"/select?q=*&rows=0&wt=json");
        try (CloseableHttpResponse response = apacheClient.execute(head)) {
            int code = response.getCode();
            if (code == 200) {
                // ok - live channel
            } else throw new IllegalStateException(String.format("Channel for %s(%s) doesnt work ", ac, fullChannelUrl));
        } catch(IOException ex) {
            LOGGER.log(Level.SEVERE,ex.getMessage(),ex);
            throw new IllegalStateException(String.format("Channel for %s(%s) doesnt work ", ac, fullChannelUrl));
        }
    }

    public static String solrChannelPidExistence(CloseableHttpClient apacheClient, String ac, String fullChannelUrl, String apiVersion, String pid) throws UnsupportedEncodingException {
        String query = null;
        String url = null;
        if (apiVersion.toLowerCase().equals("v5")) {
            query = URLEncoder.encode("PID:\"" + pid + "\"", "UTF-8");
            url = fullChannelUrl + "/select?q=" + query + "&rows=10&wt=json&fl=PID,fedora.model,pid_path,root_pid";
        } else {
            query = URLEncoder.encode( "pid:\""+pid+"\"", "UTF-8");
            url = fullChannelUrl+"/select?q="+query+"&rows=10&wt=json&fl=pid,model,pid_paths,root.pid";
        }
        HttpGet head = new HttpGet(url);
        try (CloseableHttpResponse response = apacheClient.execute(head)) {
            int code = response.getCode();
            if (code == 200) {
                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();
                return IOUtils.toString(is, Charset.forName("UTF-8"));
            } else {
                LOGGER.log(Level.SEVERE, String.format("Bad response %d", code));
            }
        } catch(IOException ex) {
            LOGGER.log(Level.SEVERE,ex.getMessage(),ex);
        }
        return null;
    }
    
}

