package cz.incad.kramerius.rest.apiNew.client.v60.redirection.item;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.apiNew.admin.v10.reharvest.x;
import cz.incad.kramerius.rest.apiNew.client.v60.filter.ProxyFilter;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v60.redirection.ProxyHandlerException;
import cz.incad.kramerius.rest.apiNew.client.v60.redirection.item.ProxyItemHandler.RequestMethodName;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class V5ForwardHandler extends V5RedirectHandler {

    public static final Logger LOGGER = Logger.getLogger(V5ForwardHandler.class.getName());

    
    
    protected String forwardUrl() {
        String baseurl = KConfiguration.getInstance().getConfiguration()
                .getString("cdk.collections.sources." + this.source + ".forwardurl");
        return baseurl;
    }

    
    public V5ForwardHandler(ReharvestManager reharvestManager, Instances instances, User user, Client client, SolrAccess solrAccess, String source,
            String pid, String remoteAddr) {
        super(reharvestManager, instances, user, client, solrAccess, source, pid, remoteAddr);
    }

    @Override
    public Response image(RequestMethodName method) throws ProxyHandlerException {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/cdk/forward/item/" + this.pid
                + "/streams/IMG_FULL";
        if (method == RequestMethodName.head) {
            return buildForwardResponseHEAD(url);
        } else {
            return buildForwardResponseGET(url, null);
        }
    }

    @Override
    public Response imagePreview(RequestMethodName method) throws ProxyHandlerException {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/cdk/forward/item/" + this.pid
                + "/streams/IMG_PREVIEW";

        if (method == RequestMethodName.head) {
            return buildForwardResponseHEAD(url);
        } else {
            return buildForwardResponseGET(url, null);
        }
    }

    @Override
    public Response mods(RequestMethodName method) throws ProxyHandlerException {
        if (method == RequestMethodName.head) {
            String baseurl = this.baseUrl();
            String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid
                    + "/streams/BIBLIO_MODS";
            return buildRedirectResponse(url);
        } else {
            String baseurl = this.forwardUrl();
            String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/cdk/forward/item/" + this.pid
                    + "/streams/BIBLIO_MODS";
            // String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/"
            // + this.pid + "/streams/BIBLIO_MODS";
            return buildForwardResponseGET(url, this.pid);
        }
    }

    @Override
    public Response zoomifyImageProperties(RequestMethodName method) throws ProxyHandlerException {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/cdk/forward/zoomify/" + this.pid
                + "/ImageProperties.xml";

        if (method == RequestMethodName.head) {
            return buildForwardResponseHEAD(url);
        } else {
            return buildForwardResponseGET(url, null);
        }
    }

    @Override
    public Response zoomifyTile(String tileGroupStr, String tileStr) throws ProxyHandlerException {
        String baseurl = this.forwardUrl();
        String formatted = String.format("api/v5.0/cdk/forward/zoomify/%s/%s/%s", this.pid, tileGroupStr, tileStr);
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + formatted;
        return buildForwardResponseGET(url, null);
    }

    @Override
    public Response textOCR(RequestMethodName method) throws ProxyHandlerException {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/cdk/forward/" + this.pid
                + "/streams/TEXT_OCR";

        if (method == RequestMethodName.head) {
            return buildForwardResponseHEAD(url);
        } else {
            return buildForwardResponseGET(url,null);
        }
    }

    @Override
    public Response altoOCR(RequestMethodName method) throws ProxyHandlerException {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/cdk/forward/" + this.pid
                + "/streams/ALTO";

        if (method == RequestMethodName.head) {
            return buildForwardResponseHEAD(url);
        } else {
            return buildForwardResponseGET(url, null);
        }
    }

    protected JSONArray providedByLicense() {
        JSONArray retVal = new JSONArray();
        try {
            String baseurl = this.forwardUrl();
            String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/cdk/forward/providedBy/" + this.pid;
            WebResource.Builder r = buidFowrardResponse(url);
            ClientResponse response = r.get(ClientResponse.class);
            if (response.getStatus() == 200) {
                InputStream entityStream = response.getEntityInputStream();
                String jsonString = IOUtils.toString(entityStream, "UTF-8");
                JSONObject jsonObject = new JSONObject(jsonString);
                if (jsonObject.has("providedByLabel")) {
                    retVal.put(jsonObject.getString("providedByLabel"));
                }
            }
        } catch (JSONException | IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return retVal;
    }


    @Override
    public Response iiifInfo(RequestMethodName method, String pid) throws ProxyHandlerException {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/cdk/forward/iiif/" + this.pid
                + "/info.json";

        if (method == RequestMethodName.head) {
            return buildForwardResponseHEAD(url);
        } else {
            return buildForwardResponseGET(url, null);
        }
    }

    

    @Override
    public Response iiifTile(RequestMethodName method, String pid, String region, String size, String rotation, String qf)
            throws ProxyHandlerException {

        String defaultMime = IIIF_SUPPORTED_MIMETYPES.get("jpg");

        String baseurl = this.forwardUrl();
        String  postfix =  String.format("/%s/%s/%s/%s", region,size, rotation,qf);
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/cdk/forward/iiif/" + this.pid+postfix;
        if (method == RequestMethodName.head) {
            return buildForwardResponseHEAD(url);
        } else {
            String mime = defaultMime;
            String[] splited = qf.split("\\.");
            if (splited.length > 1) {
                mime =  IIIF_SUPPORTED_MIMETYPES.containsKey(splited[1]) ? IIIF_SUPPORTED_MIMETYPES.get(splited[1]) :  defaultMime;
            }
            return buildForwardResponseGET(url, null, mime);
        }
    }

    
    static Map<String, String> IIIF_SUPPORTED_MIMETYPES = new HashMap<>();
    static  {
        IIIF_SUPPORTED_MIMETYPES.put("jpg", "image/jpeg");
        IIIF_SUPPORTED_MIMETYPES.put("tif", "image/tiff");
        IIIF_SUPPORTED_MIMETYPES.put("png", "image/png");
        IIIF_SUPPORTED_MIMETYPES.put("jp2", "image/jp2");
        IIIF_SUPPORTED_MIMETYPES.put("pdf", "application/pdf");
        IIIF_SUPPORTED_MIMETYPES.put("webp", "image/webp");
    }
    

}
