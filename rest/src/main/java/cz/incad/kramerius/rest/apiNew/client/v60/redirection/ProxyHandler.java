package cz.incad.kramerius.rest.apiNew.client.v60.redirection;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import javax.ws.rs.core.Response;

import com.sun.jersey.api.client.Client;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.fedora.om.RepositoryException;
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
    protected Client client;
    protected SolrAccess solrAccess;
    
    
    public ProxyHandler(Client client, SolrAccess solrAccess, String source, String pid) {
        this.source = source;
        this.pid = pid;
        this.client = client;
        this.solrAccess = solrAccess;
    }
    
    
    public abstract String image() throws ProxyHandlerException;
    public abstract String imagePreview() throws ProxyHandlerException;
    public abstract String imageThumb() throws ProxyHandlerException;

    public abstract String zoomifyImageProperties() throws ProxyHandlerException;
    public abstract String zoomifyTile(String tileGroupStr, String tileStr) throws ProxyHandlerException;
    public abstract String textOCR() throws ProxyHandlerException;
    public abstract String altoOCR() throws ProxyHandlerException;
    public abstract String mods() throws ProxyHandlerException;
    public abstract String dc() throws ProxyHandlerException;


    
    public abstract Response info() throws ProxyHandlerException;

    public abstract Response infoImage() throws ProxyHandlerException;
    public abstract Response infoStructure() throws ProxyHandlerException;
    public abstract Response infoData() throws ProxyHandlerException;
    public abstract Response providedByLicenses() throws ProxyHandlerException;
    
    public abstract Response audioMP3() throws ProxyHandlerException;
    public abstract Response audioOGG() throws ProxyHandlerException;
    public abstract Response audioWAV() throws ProxyHandlerException;
    
    
    public abstract Response buildResponse(String url) throws ProxyHandlerException;
    
    
    protected String baseUrl()  {
        String baseurl = KConfiguration.getInstance().getConfiguration().getString("cdk.collections.sources." + this.source + ".baseurl");
        return baseurl;
    }

    public boolean imageThumbForceRedirection() {
	    boolean redirection = KConfiguration.getInstance().getConfiguration().getBoolean("cdk.collections.sources." + this.source + ".thumb", false);
		return redirection;
	}
}
