package cz.incad.kramerius.rest.api.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import cz.incad.kramerius.Constants;
import cz.incad.kramerius.MostDesirable;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.audio.CacheLifeCycleHook;
import cz.incad.kramerius.audio.urlMapping.CachingFedoraUrlManager;
import cz.incad.kramerius.audio.urlMapping.RepositoryUrlManager;
import cz.incad.kramerius.database.provider.CDKCacheConnectionProvider;
import cz.incad.kramerius.database.provider.Kramerius4ConnectionProvider;
import cz.incad.kramerius.database.provider.UsersConnectionProvider;
import cz.incad.kramerius.impl.CachedSolrAccessImpl;
import cz.incad.kramerius.impl.MostDesirableImpl;
import cz.incad.kramerius.processes.scheduler.ProcessScheduler;
import cz.incad.kramerius.processes.scheduler.ProcessSchedulerImpl;
import cz.incad.kramerius.relation.RelationService;
import cz.incad.kramerius.relation.impl.RelationServiceImpl;
import cz.incad.kramerius.rest.api.k5.client.SolrMemoization;
import cz.incad.kramerius.rest.api.k5.client.impl.SolrMemoizationImpl;
import cz.incad.kramerius.rest.api.serialization.SimpleJSONMessageBodyReader;
import cz.incad.kramerius.rest.api.serialization.SimpleJSONMessageBodyWriter;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestManager;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.impl.SolrReharvestManagerImpl;
import cz.incad.kramerius.rest.apiNew.client.v70.ApacheCDKForwardClientProvider;
import cz.incad.kramerius.rest.apiNew.client.v70.ApacheCDKForwardPoolManagerProvider;
import cz.incad.kramerius.rest.apiNew.client.v70.ClientProvider;
import cz.incad.kramerius.rest.apiNew.client.v70.filter.DefaultFilter;
import cz.incad.kramerius.rest.apiNew.client.v70.filter.ProxyFilter;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.properties.DefaultPropertiesInstances;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.DeleteTriggerSupport;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.impl.DeleteTriggerSupportImpl;
import cz.incad.kramerius.rest.apiNew.monitoring.impl.SolrAPICallMonitor;
import cz.incad.kramerius.service.GoogleAnalytics;
import cz.incad.kramerius.service.LifeCycleHook;
import cz.incad.kramerius.service.impl.GoogleAnalyticsImpl;
import cz.incad.kramerius.statistics.StatisticReport;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.statistics.accesslogs.dnnt.DNNTStatisticsAccessLogImpl;
import cz.incad.kramerius.statistics.accesslogs.solr.SolrStatisticsAccessLogImpl;
import cz.incad.kramerius.statistics.impl.*;
import cz.incad.kramerius.timestamps.TimestampStore;
import cz.incad.kramerius.timestamps.impl.SolrTimestampStore;
import cz.inovatika.cdk.cache.CDKRequestCacheSupport;
import cz.inovatika.cdk.cache.impl.CDKRequestCacheSupportImpl;
import cz.inovatika.folders.db.FolderDatabase;
import cz.inovatika.monitoring.APICallMonitor;
import jakarta.ws.rs.client.Client;
import org.apache.hc.client5.http.async.HttpAsyncClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.ehcache.CacheManager;

import java.io.File;
import java.sql.Connection;
import java.util.Locale;

/**
 * Base Rest Kramerius module
 * @author ppodsednik
 */
public class RestBaseModule extends AbstractModule {

    @Override
    protected void configure() {
        // cdk forward client
        bind(CloseableHttpClient.class).annotatedWith(Names.named("forward-client")).toProvider(ApacheCDKForwardClientProvider.class).asEagerSingleton();
        bind(PoolingHttpClientConnectionManager.class).annotatedWith(Names.named("forward-client")).toProvider(ApacheCDKForwardPoolManagerProvider.class).asEagerSingleton();
        bind(Client.class).annotatedWith(Names.named("forward-client")).toProvider(ClientProvider.class).asEagerSingleton();

        // solr apache client
        bind(SolrAccess.class).annotatedWith(Names.named("cachedSolrAccess")).to(CachedSolrAccessImpl.class).in(Scopes.SINGLETON);

        bind(cz.incad.kramerius.rest.apiNew.admin.v70.files.GenerateDownloadLinks.class).asEagerSingleton();
        bind(FolderDatabase.class);
        bind(SolrMemoization.class).to(SolrMemoizationImpl.class).asEagerSingleton();

        // simple reader & writer
        bind(SimpleJSONMessageBodyReader.class);
        bind(SimpleJSONMessageBodyWriter.class);
        bind(TimestampStore.class).to(SolrTimestampStore.class).asEagerSingleton();
        bind(Instances.class).to(DefaultPropertiesInstances.class).asEagerSingleton();
        bind(ReharvestManager.class).to(SolrReharvestManagerImpl.class).asEagerSingleton();
        bind(ProxyFilter.class).to(DefaultFilter.class);

    }

}
