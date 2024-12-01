package cz.incad.kramerius.rest.apiNew.client.v70.libs;

import java.util.Map;

import com.sun.jersey.api.client.Client;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.item.ProxyItemHandler;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.user.ProxyUserHandler;
import cz.incad.kramerius.security.User;

/**
 * Represents one instance library
 * @author happy
 */
public interface OneInstance {
		
    public static final String NAME_CZE = "name_cze";
    public static final String NAME_ENG = "name_eng";
    
    
    
	/** type of switch */
	public static enum TypeOfChangedStatus {
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
	
	
	public String getName();

	public boolean hasFullAccess();
	
	public InstanceType getInstanceType();
	
	boolean isConnected();
	void setConnected(boolean connected, TypeOfChangedStatus status);
	
	TypeOfChangedStatus getType();
	
	public ProxyItemHandler createProxyItemHandler(User user, Client client, SolrAccess solrAccess, String source, String pid,String remoteAddr);
	
	public ProxyUserHandler createProxyUserHandler(User user, Client client, SolrAccess solrAccess, String source, String remoteAddr);

	
	public Map<String, String> getRegistrInfo();
	
	public void setRegistrInfo(String key, String value);
	
	public void removeRegistrInfo(String key);
	
	//public ProxyHandler createNoPidProxyHandler(User user, Client client, SolrAccess solrAccess, String source, String remoteAddr);
}
