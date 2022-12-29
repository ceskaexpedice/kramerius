package cz.incad.kramerius.rest.apiNew.client.v60.libs;

import com.sun.jersey.api.client.Client;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.apiNew.client.v60.redirection.item.ProxyItemHandler;
import cz.incad.kramerius.rest.apiNew.client.v60.redirection.user.ProxyUserHandler;
import cz.incad.kramerius.security.User;

/**
 * Represents one instance library
 * @author happy
 */
public interface OneInstance {
		
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
	
	//public ProxyHandler createNoPidProxyHandler(User user, Client client, SolrAccess solrAccess, String source, String remoteAddr);
}
