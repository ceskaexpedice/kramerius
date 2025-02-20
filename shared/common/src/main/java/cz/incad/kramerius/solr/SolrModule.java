package cz.incad.kramerius.solr;

import com.google.inject.*;
import com.google.inject.name.Named;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;

/**
 * Created by pstastny on 10/19/2017.
 */
public class SolrModule extends AbstractModule {

    @Override
    protected void configure() {
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


    //    @Provides
//    @Named("processingUpdate")
//    @Singleton
//    public SolrClient processingUpdateClient() {
//        String processingSolrHost = KConfiguration.getInstance().getSolrProcessingHost();
//        Builder async = new Http2SolrClient.Builder(processingSolrHost);
//        return new ConcurrentUpdateHttp2SolrClient.Builder(processingSolrHost, async.build()).withQueueSize(100).build();
//    }
}
