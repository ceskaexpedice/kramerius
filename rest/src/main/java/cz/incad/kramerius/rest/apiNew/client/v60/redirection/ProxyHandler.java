package cz.incad.kramerius.rest.apiNew.client.v60.redirection;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import javax.ws.rs.core.Response;

import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

/**
 * The class is responsible for handling requests from client
 */
public abstract class ProxyHandler {
	// source library
    protected String source;
    protected String pid;

    public ProxyHandler(String source, String pid) {
        this.source = source;
        this.pid = pid;
    }
    // vraci image
    public abstract String image() throws LexerException;
    //vraci preview
    public abstract String imagePreview() throws LexerException;

    public abstract String zoomifyImageProperties() throws LexerException;
    public abstract String zoomifyTile(String tileGroupStr, String tileStr) throws LexerException;
    public abstract String providedByLicenses() throws LexerException;
    public abstract String textOCR() throws LexerException;
    public abstract String altoOCR() throws LexerException;

    //{pid}/info/image
    public abstract boolean infoImageEndpointSupported();
    public abstract String infoImage() throws LexerException;
    
    public abstract String imageThumb() throws LexerException;
    
    
    public abstract Response buildResponse(String url) throws URISyntaxException, MalformedURLException;
    
    
    protected String baseUrl() throws LexerException {
        String baseurl = KConfiguration.getInstance().getConfiguration().getString("cdk.collections.sources." + this.source + ".baseurl");
        return baseurl;
    }

    public boolean imageThumbForceRedirection() {
	    boolean redirection = KConfiguration.getInstance().getConfiguration().getBoolean("cdk.collections.sources." + this.source + ".thumb", false);
		return redirection;
	}
}
