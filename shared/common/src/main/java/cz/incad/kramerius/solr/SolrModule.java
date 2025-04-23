package cz.incad.kramerius.solr;

import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.inject.name.Names;import antlr.StringUtils;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.impl.CachedSolrAccessImpl;
import cz.incad.kramerius.impl.SolrAccessImplNewIndex;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.http.clients.ApacheHTTPSolrClientProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;

/**
 * Created by pstastny on 10/19/2017.
 */
public class SolrModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(SolrAccess.class).annotatedWith(Names.named("new-index")).to(SolrAccessImplNewIndex.class).in(Scopes.SINGLETON);

        // Client for solr access - consider to use solrj - HttpSolrClient
        bind(CloseableHttpClient.class).annotatedWith(Names.named("solr-client")).toProvider(ApacheHTTPSolrClientProvider.class).asEagerSingleton();
    }

    @Provides
    @Named("processingQuery")
    @Singleton
    public SolrClient processingQueryClient() {
        String processingSolrHost = KConfiguration.getInstance().getSolrProcessingHost();
        return new HttpSolrClient.Builder(processingSolrHost).build();
    }

    
    // change processing update client
    @Provides
    @Named("processingUpdate")
    @Singleton
    public SolrClient processingUpdateClient() {
        String processingSolrHost = KConfiguration.getInstance().getSolrProcessingHost();
        return new ConcurrentUpdateSolrClient.Builder(processingSolrHost).withQueueSize(100).build();
    }
    
    @Provides
    @Named("proxyUpdate")
    @Singleton
    public SolrClient proxyUpdateClient() {
        String proxyUpdateUrl = KConfiguration.getInstance().getSolrUpdatesHost();
        if (cz.incad.kramerius.utils.StringUtils.isAnyString(proxyUpdateUrl)) {
            return new ConcurrentUpdateSolrClient.Builder(proxyUpdateUrl).withQueueSize(100).build();
        } else {
        	return null;
        }
    }



}
