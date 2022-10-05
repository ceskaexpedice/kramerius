package cz.incad.kramerius.rest.apiNew.client.v60.redirection;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;

import cz.incad.kramerius.utils.pid.LexerException;

public class V5RedirectHandler extends ProxyHandler{
	
	public static final Logger LOGGER = Logger.getLogger(V5RedirectHandler.class.getName());
	
    public V5RedirectHandler(String source, String pid) {
        super(source, pid);
    }

    @Override
    public String image() throws LexerException {
        String baseurl = super.baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid + "/streams/IMG_FULL";
        return url;
    }

    
    
    @Override
	public String imagePreview() throws LexerException {
        String baseurl = super.baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid + "/streams/IMG_PREVIEW";
        return url;
	}

	@Override
	public String textOCR() throws LexerException {
        String baseurl = super.baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid + "/streams/TEXT_OCR";
        return url;
	}
	

	@Override
	public String altoOCR() throws LexerException {
        String baseurl = super.baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid + "/streams/ALTO";
        return url;
	}

	@Override
    public String zoomifyImageProperties() throws LexerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "zoomify/" + this.pid + "/ImageProperties.xml";
        return url;
    }

    @Override
    public String zoomifyTile(String tileGroupStr, String tileStr) throws LexerException {
        String baseurl = baseUrl();
        String formatted = String.format("zoomify/%s/%s/%s", this.pid,  tileGroupStr, tileStr);
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + formatted;
        return url;
    }

    
    @Override
	public boolean infoImageEndpointSupported() {
		return false;
	}

	@Override
	public String infoImage() {
		return null;
	}

	
	@Override
	public String imageThumb() throws LexerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid + "/thumb";
        return url;
	}

	@Override
	public Response buildResponse(String url) throws URISyntaxException, MalformedURLException {
        LOGGER.info(String.format("Redirecting to %s", url));
        return Response.temporaryRedirect(new URL(url).toURI()).build();
	}

	@Override
    public String providedByLicenses() throws LexerException {
        return "";
    }
}
