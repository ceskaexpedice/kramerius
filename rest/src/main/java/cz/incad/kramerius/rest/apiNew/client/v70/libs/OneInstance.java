package cz.incad.kramerius.rest.apiNew.client.v70.libs;

import java.util.Map;

import com.sun.jersey.api.client.Client;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.apiNew.ConfigManager;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.DeleteTriggerSupport;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.item.ProxyItemHandler;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.user.ProxyUserHandler;
import cz.incad.kramerius.security.User;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

/**
 * Represents one instance library
 * @author happy
 */
public interface OneInstance {
		
    public static final String NAME_CZE = "name_cze";
    public static final String NAME_ENG = "name_eng";
    
    
    
	/** type of switch */
	public static enum  TypeOfChangedStatus {
		user,  automat;
	}
	
	/** type of instance */
	public static enum InstanceType {
		V5, V7;
		public static InstanceType load(String lt) {
			String upper = lt.toUpperCase();
			return InstanceType.valueOf(upper);
		}
	}


	/**
	 * Name of library
	 * @return
	 */
	public String getName();

	/**
	 * Flag for full acces
	 * @return
	 */
	public boolean hasFullAccess();

	/**
	 * Return type of instance V5 or V7
	 * @return
	 */
	public InstanceType getInstanceType();

	/**
	 * Return true if the instance is connected
	 * @return
	 */
	boolean isConnected();

	/**
	 * Sets connected flag
	 * @param connected Connected flag
	 * @param status Type of status - user or automat
	 */
	void setConnected(boolean connected, TypeOfChangedStatus status);

	/**
	 * Returns type of changed flag = How the flag has been changed. If it has been changed by user or automat (system)
	 * @return
	 */
	TypeOfChangedStatus getType();

	/**
	 * Creates item's proxy handler; The class responsible for handling requests from items resource and forwarding them to destination DL
	 * @param user Requesting user
	 * @param solrAccess Access to solr
	 * @param source Digital library
	 * @param pid PID
	 * @param remoteAddr Remote addr
	 * @return
	 */
	public ProxyItemHandler createProxyItemHandler(User user, CloseableHttpClient closeableHttpClient, DeleteTriggerSupport triggerSupport, SolrAccess solrAccess, String source, String pid, String remoteAddr);

	/**
	 * Creates user's proxy handler; The class responsible for handling requests from item resource forwarding them to destination DL
	 * @param user  Requesting user
	 * @param solrAccess Access to solr
	 * @param source Digital library
	 * @param remoteAddr
	 * @return
	 */
	public ProxyUserHandler createProxyUserHandler(User user, CloseableHttpClient closeableHttpClient,  SolrAccess solrAccess, String source, String remoteAddr);



	//TODO:  Remove in future
	public Map<String, String> getRegistrInfo();
	public void setRegistrInfo(String key, String value);
	public void removeRegistrInfo(String key);
	
}
