package cz.incad.kramerius.rest.apiNew.client.v70.redirection.user;

import java.util.List;

import cz.incad.kramerius.rest.apiNew.client.v70.redirection.DeleteTriggerSupport;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.cdk.cache.CDKRequestCacheSupport;
import cz.inovatika.cdk.cache.CDKRequestItem;
import cz.inovatika.monitoring.ApiCallEvent;
import org.apache.commons.lang3.tuple.Pair;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestManager;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.ProxyHandlerException;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.ProxyHandlerSupport;
import cz.incad.kramerius.security.User;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.jetbrains.annotations.NotNull;

public abstract class ProxyUserHandler extends ProxyHandlerSupport {

	public ProxyUserHandler(CDKRequestCacheSupport cacheSupport, ReharvestManager reharvestManager, Instances instances, User user,
							CloseableHttpClient apacheClient, SolrAccess solrAccess, String source,
							String remoteAddr) {
		super(cacheSupport,reharvestManager, instances, user,  apacheClient, null, solrAccess, source, remoteAddr);
	}

	@NotNull
	protected String cacheHit(String url, ApiCallEvent event) {
		long start = System.currentTimeMillis();
		List<Triple<String, Long, Long>> triples = event.getGranularTimeSnapshots() != null ? event.getGranularTimeSnapshots() : null;
		int days = KConfiguration.getInstance().getConfiguration().getInt("cdk.cache.user.providedBy",30);
		String userIdentification = this.userCacheIdentification();
		// find in cache
		List<CDKRequestItem> cdkRequestItems = this.cacheSupport.find(this.source, url, null, userIdentification);
		if (!cdkRequestItems.isEmpty() && !cdkRequestItems.get(0).isExpired(days)) {
			LOGGER.fine(String.format("Found in cache %s",cdkRequestItems.get(0)));
			long stop = System.currentTimeMillis();
			if (triples != null) {
				triples.add(Triple.of("cache", start,stop));
			}
			String data = (String) cdkRequestItems.get(0).getData();
			return data;
		}
		return null;
	}


	public abstract Pair<User, List<String>> user(ApiCallEvent event) throws ProxyHandlerException;
}
