package cz.incad.kramerius.rest.apiNew.client.v70.redirection.item;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestManager;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.ProxyHandlerException;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.ProxyHandlerSupport;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.conf.KConfiguration;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * This class is responsible for handing requests from client and pass through to kramerius instance
 */
public abstract class ProxyItemHandler extends ProxyHandlerSupport {

	public static final Logger LOGGER = Logger.getLogger(ProxyItemHandler.class.getName());
	
	public static final Boolean DEBUG_SESSION_ATTRIBUTES = true;

    /** Represents type of forward request HEAD or GET */
	public static enum RequestMethodName {
		head,get
	}
	
    protected String pid;

    public ProxyItemHandler(ReharvestManager reharvestManager, Instances instances, User user, Client client, SolrAccess solrAccess, String source, String pid, String remoteAddr) {
    	super(reharvestManager, instances,user,client,solrAccess,source, remoteAddr);
    	this.source = source;
        this.pid = pid;
    }


    /**
     * Returns forward response of IMG_FULL stream
     * @param method Request method
     * @return Returns Jersey response
     * @throws ProxyHandlerException
     */
    public abstract Response image(RequestMethodName method) throws ProxyHandlerException;

    /**
     * Returns forward response of IMG_PREVIEW stream
     * @param method Request method
     * @return
     * @throws ProxyHandlerException
     */
    public abstract Response imagePreview(RequestMethodName method) throws ProxyHandlerException;

    /**
     * Returns forward response of IMG_THUMB stream
     * @param method Request method
     * @return
     * @throws ProxyHandlerException
     */
    public abstract Response imageThumb(RequestMethodName method) throws ProxyHandlerException;

    /**
     * Returns forward response of IIIF descriptor
     * @param method Request method
     * @param pid
     * @return
     * @throws ProxyHandlerException
     */
    public abstract Response iiifInfo(RequestMethodName method, String pid) throws ProxyHandlerException;

    /**
     * Returns forward response of IIIF tile
     * @param method Req
     * @param pid
     * @param region
     * @param size
     * @param rotation
     * @param qf
     * @return
     * @throws ProxyHandlerException
     */
    public abstract Response iiifTile(RequestMethodName method, String pid,  String region,  String size, String rotation, String qf) throws ProxyHandlerException;
    
    
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

    
    // helper methods for direct access to dc stream
    public abstract InputStream directStreamDC() throws ProxyHandlerException;
    public abstract InputStream directStreamBiblioMods() throws ProxyHandlerException;
    
    public abstract boolean isStreamDCAvaiable() throws ProxyHandlerException;
    public abstract boolean isStreamBiblioModsAvaiable() throws ProxyHandlerException;
    
    
	public boolean imageThumbForceRedirection() {
	    boolean redirection = KConfiguration.getInstance().getConfiguration().getBoolean("cdk.collections.sources." + this.source + ".thumb", false);
		return redirection;
	}
}
