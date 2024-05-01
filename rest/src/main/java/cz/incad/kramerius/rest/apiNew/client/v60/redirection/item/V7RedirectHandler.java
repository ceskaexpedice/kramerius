package cz.incad.kramerius.rest.apiNew.client.v60.redirection.item;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.apiNew.admin.v10.reharvest.ReharvestItem;
import cz.incad.kramerius.rest.apiNew.admin.v10.reharvest.ReharvestManager;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v60.redirection.ProxyHandlerException;
import cz.incad.kramerius.rest.apiNew.client.v60.redirection.item.ProxyItemHandler.RequestMethodName;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.pid.LexerException;

public class V7RedirectHandler extends ProxyItemHandler{

	public static final Logger LOGGER = Logger.getLogger(V7RedirectHandler.class.getName());
    
    public V7RedirectHandler(ReharvestManager reharvestManager, Instances instances, User user, Client client, SolrAccess solrAccess, String source, String pid,String remoteAddr) {
		super(reharvestManager, instances, user,client, solrAccess, source, pid, remoteAddr);
	}



	@Override
	public Response imagePreview(RequestMethodName method) throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/image/preview";
        return buildRedirectResponse(url);
	}



	@Override
	public Response textOCR(RequestMethodName method) throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/ocr/text";
        return buildRedirectResponse(url);
	}

	
	@Override
	public Response altoOCR(RequestMethodName method) throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/ocr/alto";
        return buildRedirectResponse(url);
	}


	@Override
    public Response image(RequestMethodName method) throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/image";
        return buildRedirectResponse(url);
    }

    @Override
    public Response zoomifyImageProperties(RequestMethodName method) throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/image/zoomify/ImageProperties.xml";
        return buildRedirectResponse(url);
    }

    @Override
    public Response zoomifyTile(String tileGroupStr, String tileStr) throws ProxyHandlerException {
        String formatted = String.format("image/zoomify/%s/%s", tileGroupStr, tileStr);
        //String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + pid + "/" + endpoint;
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/"+formatted;
        return buildRedirectResponse(url);
    }

    
    
    @Override
	public Response infoData() throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/info/data";
        return  buildRedirectResponse(url);
	}




	@Override
	public Response infoImage() throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/info/image";
        return buildRedirectResponse(url);
	}

	@Override
	public Response imageThumb(RequestMethodName method) throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/image/thumb";
        return buildRedirectResponse(url);
	}

	
	@Override
	public Response mods(RequestMethodName method) throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/metadata/mods";
        return buildRedirectResponse(url);
	}



	@Override
	public Response dc(RequestMethodName method) throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/metadata/dc";
        return buildRedirectResponse(url);
	}

	


	@Override
	public Response info() throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/info";
        return buildRedirectResponse(url);
	}


	@Override
	public Response infoStructure() throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/info/structure";
        return buildRedirectResponse(url);
	}



	@Override
	public Response providedByLicenses() throws ProxyHandlerException {
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
	public Response audioMP3() throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/audio/mp3";
        return buildRedirectResponse(url);
	}



	@Override
	public Response audioOGG() throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/audio/ogg";
        return buildRedirectResponse(url);
	}



	@Override
	public Response audioWAV() throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/audio/wav";
        return buildRedirectResponse(url);
	}


	

    @Override
    public InputStream directStreamDC() throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/metadata/dc";
        WebResource.Builder b = buidFowrardResponse(url);
        ClientResponse response = b.get(ClientResponse.class);
        if (response.getStatus() == 200) {
            InputStream is = response.getEntityInputStream();
            return is;
        } else return null;
    }



    @Override
    public Response iiifInfo(RequestMethodName method, String pid) throws ProxyHandlerException {
        // TODO Auto-generated method stub
        return null;
    }



    @Override
    public Response iiifTile(RequestMethodName method, String pid, String region, String size, String rotation,
            String qf) throws ProxyHandlerException {
        // TODO Auto-generated method stub
        return null;
    }



 
	
}
