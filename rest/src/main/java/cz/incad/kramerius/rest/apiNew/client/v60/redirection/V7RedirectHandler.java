package cz.incad.kramerius.rest.apiNew.client.v60.redirection;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;

import com.sun.jersey.api.client.Client;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.utils.pid.LexerException;

public class V7RedirectHandler extends ProxyHandler{

	public static final Logger LOGGER = Logger.getLogger(V7RedirectHandler.class.getName());
    
    public V7RedirectHandler(Client client, SolrAccess solrAccess, String source, String pid) {
		super(client, solrAccess, source, pid);
	}



	@Override
	public String imagePreview() throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/image/preview";
        return url;
	}



	@Override
	public String textOCR() throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/ocr/text";
        return url;
	}

	
	@Override
	public String altoOCR() throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/ocr/alto";
        return url;
	}


	@Override
    public String image() throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/image";
        return url;
    }

    @Override
    public String zoomifyImageProperties() throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/image/zoomify/ImageProperties.xml";
        return url;
    }

    @Override
    public String zoomifyTile(String tileGroupStr, String tileStr) throws ProxyHandlerException {
        String formatted = String.format("image/zoomify/%s/%s", tileGroupStr, tileStr);
        //String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + pid + "/" + endpoint;
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/"+formatted;
        return url;
    }

    
    
    @Override
	public Response infoData() throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/info/data";
        return  buildResponse(url);
	}




	@Override
	public Response infoImage() throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/info/image";
        return buildResponse(url);
	}

	@Override
	public String imageThumb() throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/image/thumb";
        return url;
	}

	
	@Override
	public String mods() throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/metadata/mods";
        return url;
	}



	@Override
	public String dc() throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/metadata/dc";
        return url;
	}

	


	@Override
	public Response info() throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/info";
        return buildResponse(url);
	}


	@Override
	public Response infoStructure() throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/info/structure";
        return buildResponse(url);
	}



	@Override
	public Response providedByLicenses() throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/info/providedByLicenses";
        return buildResponse(url);
	}



	@Override
	public Response buildResponse(String url) throws ProxyHandlerException {
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
        return buildResponse(url);
	}



	@Override
	public Response audioOGG() throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/audio/ogg";
        return buildResponse(url);
	}



	@Override
	public Response audioWAV() throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/audio/wav";
        return buildResponse(url);
	}
    
}
