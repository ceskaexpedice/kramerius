package cz.incad.kramerius.rest.apiNew.client.v60.redirection.item;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.function.Consumer;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import com.sun.jersey.api.client.Client;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v60.redirection.ProxyHandlerException;
import cz.incad.kramerius.rest.apiNew.client.v60.redirection.ProxyHandlerSupport;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.BasicAuthenticationClientFilter;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

/**
 * The class is responsible for handling requests from client
 */
public abstract class ProxyItemHandler extends ProxyHandlerSupport {

	public static final Logger LOGGER = Logger.getLogger(ProxyItemHandler.class.getName());
	
	public static final Boolean DEBUG_SESSION_ATTRIBUTES = true;
	
	public static enum RequestMethodName {
		head,get
	}
	
    protected String pid;

    public ProxyItemHandler(Instances instances, User user, Client client, SolrAccess solrAccess, String source, String pid, String remoteAddr) {
    	super(instances,user,client,solrAccess,source, remoteAddr);
    	this.source = source;
        this.pid = pid;
    }
    
    public abstract Response image(RequestMethodName method) throws ProxyHandlerException;
    public abstract Response imagePreview(RequestMethodName method) throws ProxyHandlerException;
    public abstract Response imageThumb(RequestMethodName method) throws ProxyHandlerException;

    public abstract Response zoomifyImageProperties(RequestMethodName method) throws ProxyHandlerException;
    public abstract Response zoomifyTile(String tileGroupStr, String tileStr) throws ProxyHandlerException;

    public abstract Response textOCR(RequestMethodName method) throws ProxyHandlerException;
    public abstract Response altoOCR(RequestMethodName method) throws ProxyHandlerException;

    public abstract Response mods(RequestMethodName method) throws ProxyHandlerException;
    public abstract Response dc(RequestMethodName method) throws ProxyHandlerException;
    
    public abstract Response info() throws ProxyHandlerException;

    public abstract Response infoImage() throws ProxyHandlerException;
    public abstract Response infoStructure() throws ProxyHandlerException;
    public abstract Response infoData() throws ProxyHandlerException;
    public abstract Response providedByLicenses() throws ProxyHandlerException;
    
    public abstract Response audioMP3() throws ProxyHandlerException;
    public abstract Response audioOGG() throws ProxyHandlerException;
    public abstract Response audioWAV() throws ProxyHandlerException;
    
	public boolean imageThumbForceRedirection() {
	    boolean redirection = KConfiguration.getInstance().getConfiguration().getBoolean("cdk.collections.sources." + this.source + ".thumb", false);
		return redirection;
	}
}
