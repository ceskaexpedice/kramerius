package cz.incad.kramerius.rest.apiNew.client.v60.redirection;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;

import cz.incad.kramerius.utils.pid.LexerException;

public class V7RedirectHandler extends ProxyHandler{

	public static final Logger LOGGER = Logger.getLogger(V7RedirectHandler.class.getName());

    public V7RedirectHandler(String source, String pid) {
        super(source, pid);
    }

    
    
    @Override
	public String imagePreview() throws LexerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/image/preview";
        return url;
	}



	@Override
	public String textOCR() throws LexerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/ocr/text";
        return url;
	}

	
	@Override
	public String altoOCR() throws LexerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/ocr/alto";
        return url;
	}


	@Override
    public String image() throws LexerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/image";
        return url;
    }

    @Override
    public String zoomifyImageProperties() throws LexerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/image/zoomify/ImageProperties.xml";
        return url;
    }

    @Override
    public String zoomifyTile(String tileGroupStr, String tileStr) throws LexerException {
        String formatted = String.format("image/zoomify/%s/%s", tileGroupStr, tileStr);
        //String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + pid + "/" + endpoint;
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/"+formatted;
        return url;
    }

    @Override
    public String providedByLicenses() throws LexerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/info/providedByLicenses";
        return url;
    }

    
    @Override
	public boolean infoImageEndpointSupported() {
    	return true;
	}



	@Override
	public String infoImage() throws LexerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/info/image";
        return url;
	}

	@Override
	public String imageThumb() throws LexerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/image/thumb";
        return url;
	}

	@Override
	public Response buildResponse(String url) throws URISyntaxException, MalformedURLException {
        LOGGER.info(String.format("Redirecting to %s", url));
        return Response.temporaryRedirect(new URL(url).toURI()).build();
	}
    
}
