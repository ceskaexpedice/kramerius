package cz.incad.kramerius.rest.apiNew.client.v70.redirection.item;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestManager;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.DeleteTriggerSupport;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.ProxyHandlerException;
import cz.incad.kramerius.security.User;
import cz.inovatika.cdk.cache.CDKRequestCacheSupport;
import cz.inovatika.monitoring.ApiCallEvent;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

public class V7RedirectHandler extends ProxyItemHandler{

	public static final Logger LOGGER = Logger.getLogger(V7RedirectHandler.class.getName());
    
    public V7RedirectHandler(CDKRequestCacheSupport cacheSupport, ReharvestManager reharvestManager, Instances instances, User user, CloseableHttpClient closeableHttpClient, DeleteTriggerSupport deleteTriggerSupport, SolrAccess solrAccess, String source, String pid, String remoteAddr) {
		super(cacheSupport, reharvestManager, instances, user,closeableHttpClient, deleteTriggerSupport, solrAccess, source, pid, remoteAddr);
	}



	@Override
	public Response imagePreview(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/image/preview";
        return buildRedirectResponse(url);
	}



	@Override
	public Response textOCR(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/ocr/text";
        return buildRedirectResponse(url);
	}

	
	@Override
	public Response altoOCR(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/ocr/alto";
        return buildRedirectResponse(url);
	}


	@Override
    public Response image(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/image";
        return buildRedirectResponse(url);
    }

    @Override
    public Response zoomifyImageProperties(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/image/zoomify/ImageProperties.xml";
        return buildRedirectResponse(url);
    }

    @Override
    public Response zoomifyTile(String tileGroupStr, String tileStr, ApiCallEvent event) throws ProxyHandlerException {
        String formatted = String.format("image/zoomify/%s/%s", tileGroupStr, tileStr);
        //String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + pid + "/" + endpoint;
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/"+formatted;
        return buildRedirectResponse(url);
    }

    
    
    @Override
	public Response infoData(ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/info/data";
        return  buildRedirectResponse(url);
	}




	@Override
	public Response infoImage(ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/info/image";
        return buildRedirectResponse(url);
	}

	@Override
	public Response imageThumb(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = baseUrl();
        //TODO: forward + cache
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/image/thumb";
        return buildRedirectResponse(url);
	}

	
	@Override
	public Response mods(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/metadata/mods";
        return buildRedirectResponse(url);
	}



	@Override
	public Response dc(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/metadata/dc";
        return buildRedirectResponse(url);
	}

	


	@Override
	public Response info(ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/info";
        return buildRedirectResponse(url);
	}


	@Override
	public Response infoStructure(ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/info/structure";
        return buildRedirectResponse(url);
	}



	@Override
	public Response providedByLicenses(ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/info/providedByLicenses";
        return buildRedirectResponse(url);
	}



	@Override
    public Response buildRedirectResponse(String url) throws ProxyHandlerException {
        try {
            LOGGER.info(String.format("Redirecting to %s", url));
            return Response.temporaryRedirect(new URL(url).toURI()).build();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new ProxyHandlerException(e);
        }
    }


	@Override
	public Response audioMP3(ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/audio/mp3";
        return buildRedirectResponse(url);
	}



	@Override
	public Response audioOGG(ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/audio/ogg";
        return buildRedirectResponse(url);
	}



	@Override
	public Response audioWAV(ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/audio/wav";
        return buildRedirectResponse(url);
	}


	

    @Override
    public InputStream directStreamDC(ApiCallEvent event) throws ProxyHandlerException {
        throw new UnsupportedOperationException("unsupported");
//        String baseurl = this.forwardUrl();
//        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/item/" + this.pid
//                + "/streams/BIBLIO_MODS";
//
//        return inputStream("dc");
    }



    @Override
    public InputStream directStreamBiblioMods(ApiCallEvent event) throws ProxyHandlerException {
        throw new UnsupportedOperationException("unsupported");
        //return inputStream("mods");
    }


    // TODO: Consider if this should be
//    protected InputStream directStream(String stream) {
//        String baseurl = baseUrl();
//        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/metadata/"+stream;
//
//        WebResource.Builder b = buidForwardResponse(url);
//        ClientResponse response = b.get(ClientResponse.class);
//        if (response.getStatus() == 200) {
//            InputStream is = response.getEntityInputStream();
//            return is;
//        } else return null;
//    }


//    private boolean isStreamAvailable(String stream) throws ProxyHandlerException {
//        String baseurl = super.baseUrl();
//        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/metadata/"+stream;
//        WebResource.Builder b = buidForwardResponse(url);
//        ClientResponse response = b.head();
//        if (response.getStatus() == 200) {
//            return true;
//        } else return false;
//    }
    
    @Override
    public boolean isStreamDCAvaiable(ApiCallEvent event) throws ProxyHandlerException {
        //return exists("dc");
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isStreamBiblioModsAvaiable(ApiCallEvent event) throws ProxyHandlerException {
        //return exists("mods");
        throw new UnsupportedOperationException();
    }


    @Override
    public Response iiifInfo(RequestMethodName method, String pid, ApiCallEvent event) throws ProxyHandlerException {
        // TODO Auto-generated method stub
        return null;
    }



    @Override
    public Response iiifTile(RequestMethodName method, String pid, String region, String size, String rotation,
            String qf, ApiCallEvent event) throws ProxyHandlerException {
        // TODO Auto-generated method stub
        return null;
    }


}
