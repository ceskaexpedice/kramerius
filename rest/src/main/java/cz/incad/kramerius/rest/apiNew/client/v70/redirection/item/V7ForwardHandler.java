package cz.incad.kramerius.rest.apiNew.client.v70.redirection.item;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.Response.ResponseBuilder;

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
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestManager;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.ProxyHandlerException;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.item.ProxyItemHandler.RequestMethodName;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class V7ForwardHandler extends V7RedirectHandler {

    public static final Logger LOGGER = Logger.getLogger(V5ForwardHandler.class.getName());

    public V7ForwardHandler(ReharvestManager reharvestManager, Instances instances,  User user, Client client, SolrAccess solrAccess, String source, String pid, String remoteAddr) {
        super(reharvestManager,instances, user, client, solrAccess, source, pid, remoteAddr);
	}

    protected String forwardUrl() {
        String baseurl = KConfiguration.getInstance().getConfiguration()
                .getString("cdk.collections.sources." + this.source + ".forwardurl");
        return baseurl;
    }

    
    @Override
    public Response info() throws ProxyHandlerException {

        
        //http://tunel/search/api/cdk/v7.0/forward/providedBy/uuid:dfc71c54-0fff-4e8a-a59e-b235274da271
        JSONArray licenses = null;
        String baseurl = this.forwardUrl();
        String providedByUrl = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/providedBy/" + this.pid;
        LOGGER.info("Provided by url "+providedByUrl);
        
        WebResource.Builder providedByBuilder = buidForwardResponse(providedByUrl, true);
        ClientResponse providedBy = providedByBuilder.get(ClientResponse.class);
        if (providedBy.getStatus() == 200) {
            String content = providedBy.getEntity(String.class);
            //{"licenses":["dnnto"]}
            JSONObject providedByJSON = new JSONObject(content);
            licenses = providedByJSON.optJSONArray("licenses");
        }
        //TOD
        LOGGER.info("Provided by result "+licenses);
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/info";
        // enhance by providedBy
        WebResource.Builder b = buidForwardResponse(url);
        ClientResponse response = b.get(ClientResponse.class);
        if (response.getStatus() == 200) {
            
            //"providedByLicenses": ["dnnto"],
            String infoContent = response.getEntity(String.class);
            JSONObject infoContentJSON = new JSONObject(infoContent);
            if (licenses != null)  infoContentJSON.put("providedByLicenses", licenses);
            
            
            ResponseBuilder respEntity = Response.status(200).entity(infoContentJSON.toString());
            
            return respEntity.build();
        } else {
            if (response.getStatus() == 404) {
                this.deleteTriggeToReharvest(this.pid);
            }
            return Response.status(response.getStatus()).build();
        }

        //return super.info();
    }

    @Override
    public Response image(RequestMethodName method) throws ProxyHandlerException {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/item/" + this.pid
                + "/streams/IMG_FULL";
        if (method == RequestMethodName.head) {
            return buildForwardResponseHEAD(url);
        } else {
            return buildForwardResponseGET(url, null, true);
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
            return buildForwardResponseGET(url, null,false);
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
            LOGGER.info("MODS URL "+url);
            return buildForwardResponseGET(url,null,this.pid, true, false);
        }
    }

    @Override
    public Response zoomifyImageProperties(RequestMethodName method) throws ProxyHandlerException {
        if (method == RequestMethodName.head) {
            return super.zoomifyImageProperties(method);
        } else {
            
            String baseurl = forwardUrl();
            String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/zoomify/" + this.pid
                    + "/ImageProperties.xml";
            return buildForwardResponseGET(url, null, true);
        }
    }
    
    @Override
    public Response zoomifyTile(String tileGroupStr, String tileStr) throws ProxyHandlerException {
        //String formatted = String.format("image/zoomify/%s/%s", tileGroupStr, tileStr);
        //String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + pid + "/" + endpoint;
        String baseurl = forwardUrl();
        String formatted = String.format("api/cdk/v7.0/forward/zoomify/%s/%s/%s", this.pid, tileGroupStr, tileStr);
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + formatted;
        return buildForwardResponseGET(url, null, false);
    }

    
    @Override
    public Response textOCR(RequestMethodName method) throws ProxyHandlerException {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/item/" + this.pid
                + "/streams/TEXT_OCR";

        if (method == RequestMethodName.head) {
            return buildForwardResponseHEAD(url);
        } else {
            return buildForwardResponseGET(url, null, false);
        }
    }

    @Override
    public Response altoOCR(RequestMethodName method) throws ProxyHandlerException {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/item/" + this.pid
                + "/streams/ALTO";
        if (method == RequestMethodName.head) {
            return buildForwardResponseHEAD(url);
        } else {
            //LOGGER.info("buildForwardResponseGET( url = "+url+", mimetype=\"application/xml;charset=utf-8\", pid=null, deleteTrigger=false)");
            return buildForwardResponseGET(url, "application/xml;charset=utf-8",null,false, true);
        }
    }

    protected JSONArray providedByLicense() {
        try {
            String baseurl = super.baseUrl();
            String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/providedBy/" + this.pid;
            WebResource.Builder r = buidForwardResponse(url);
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
