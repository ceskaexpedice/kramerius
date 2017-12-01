package cz.incad.kramerius.solr;

import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.fedora.impl.Fedora4AccessImpl;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;

/**
 * Created by pstastny on 10/19/2017.
 */
public class SolrModule extends AbstractModule {


    @Override
    protected void configure() {}


    @Provides
    @Named("processingQuery")
    @Singleton
    public SolrClient processingQueryClient() {
        String solrUrl = KConfiguration.getInstance().getConfiguration().getString("processingSolrHost");
       return new HttpSolrClient.Builder(solrUrl).build();
    }

    @Provides
    @Named("processingUpdate")
    @Singleton
    public SolrClient processingUpdateClient() {
        String processingSolrHost = KConfiguration.getInstance().getProperty("processingSolrHost");
        return new ConcurrentUpdateSolrClient.Builder(processingSolrHost).withQueueSize(100).build();
    }
}
