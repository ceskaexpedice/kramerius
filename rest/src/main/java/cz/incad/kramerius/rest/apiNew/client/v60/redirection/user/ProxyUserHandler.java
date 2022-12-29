package cz.incad.kramerius.rest.apiNew.client.v60.redirection.user;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.sun.jersey.api.client.Client;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v60.redirection.ProxyHandlerException;
import cz.incad.kramerius.rest.apiNew.client.v60.redirection.ProxyHandlerSupport;
import cz.incad.kramerius.security.User;

public abstract class ProxyUserHandler extends ProxyHandlerSupport {

	public ProxyUserHandler(Instances instances, User user, Client client, SolrAccess solrAccess, String source,
			String remoteAddr) {
		super(instances, user, client, solrAccess, source, remoteAddr);
	}

	public abstract Pair<User, List<String>> user() throws ProxyHandlerException;
}
