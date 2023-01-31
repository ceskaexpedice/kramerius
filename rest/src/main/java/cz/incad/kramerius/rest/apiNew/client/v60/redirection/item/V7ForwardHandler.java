package cz.incad.kramerius.rest.apiNew.client.v60.redirection.item;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v60.redirection.ProxyHandlerException;
import cz.incad.kramerius.rest.apiNew.client.v60.redirection.item.ProxyItemHandler.RequestMethodName;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class V7ForwardHandler extends V7RedirectHandler {

    public static final Logger LOGGER = Logger.getLogger(V5ForwardHandler.class.getName());

    public V7ForwardHandler(Instances instances,  User user, Client client, SolrAccess solrAccess, String source, String pid, String remoteAddr) {
        super(instances, user, client, solrAccess, source, pid, remoteAddr);
	}

    protected String forwardUrl() {
        String baseurl = KConfiguration.getInstance().getConfiguration()
                .getString("cdk.collections.sources." + this.source + ".forwardurl");
        return baseurl;
    }

    
    @Override
    public Response image(RequestMethodName method) throws ProxyHandlerException {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/item/" + this.pid
                + "/streams/IMG_FULL";
        if (method == RequestMethodName.head) {
            return buildForwardResponseHEAD(url);
        } else {
            return buildForwardResponseGET(url);
        }
    }

    @Override
    public Response imagePreview(RequestMethodName method) throws ProxyHandlerException {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/item/" + this.pid
                + "/streams/IMG_PREVIEW";

        if (method == RequestMethodName.head) {
            return buildForwardResponseHEAD(url);
        } else {
            return buildForwardResponseGET(url);
        }
    }

    @Override
    public Response mods(RequestMethodName method) throws ProxyHandlerException {
        if (method == RequestMethodName.head) {
            return super.mods(method);
        } else {
            String baseurl = this.forwardUrl();
            String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/item/" + this.pid
                    + "/streams/BIBLIO_MODS";
            return buildForwardResponseGET(url);
        }
    }

    @Override
    public Response zoomifyImageProperties(RequestMethodName method) throws ProxyHandlerException {
        if (method == RequestMethodName.head) {
            return super.zoomifyImageProperties(method);
        } else {
            
            String baseurl = super.baseUrl();
            String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid
                    + "/image/zoomify/ImageProperties.xml";
            return buildForwardResponseGET(url);
        }
    }
    
    @Override
    public Response zoomifyTile(String tileGroupStr, String tileStr) throws ProxyHandlerException {
        //String formatted = String.format("image/zoomify/%s/%s", tileGroupStr, tileStr);
        //String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + pid + "/" + endpoint;
        String baseurl = forwardUrl();
        String formatted = String.format("api/cdk/v7.0/forward/zoomify/%s/%s/%s", this.pid, tileGroupStr, tileStr);
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + formatted;
        return buildRedirectResponse(url);
    }

    
    @Override
    public Response textOCR(RequestMethodName method) throws ProxyHandlerException {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/item/" + this.pid
                + "/streams/TEXT_OCR";

        if (method == RequestMethodName.head) {
            return buildForwardResponseHEAD(url);
        } else {
            return buildForwardResponseGET(url);
        }
    }

    @Override
    public Response altoOCR(RequestMethodName method) throws ProxyHandlerException {
        String baseurl = super.baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/item/" + this.pid
                + "/streams/TEXT_OCR";
        if (method == RequestMethodName.head) {
            return buildForwardResponseHEAD(url);
        } else {
            return buildForwardResponseGET(url);
        }
    }

    protected JSONArray providedByLicense() {
        try {
            String baseurl = super.baseUrl();
            String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/providedBy/" + this.pid;
            WebResource.Builder r = buidFowrardResponse(url);
            ClientResponse response = r.get(ClientResponse.class);
            if (response.getStatus() == 200) {
                InputStream entityStream = response.getEntityInputStream();
                String jsonString = IOUtils.toString(entityStream, "UTF-8");
                JSONObject jsonObject = new JSONObject(jsonString);
                if (jsonObject.has("licenses")) {
                    JSONArray licenses = jsonObject.getJSONArray("licenses");
                    return licenses;
                }
            }
        } catch (UniformInterfaceException | ClientHandlerException | JSONException | IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
        return new JSONArray();
        
    }

}
