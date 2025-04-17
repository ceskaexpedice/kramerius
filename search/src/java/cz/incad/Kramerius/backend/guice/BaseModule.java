package cz.incad.Kramerius.backend.guice;

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
import cz.incad.kramerius.impl.*;
import cz.incad.kramerius.processes.GCScheduler;
import cz.incad.kramerius.processes.ProcessScheduler;
import cz.incad.kramerius.processes.database.Kramerius4ConnectionProvider;
import cz.incad.kramerius.processes.impl.GCSchedulerImpl;
import cz.incad.kramerius.processes.impl.ProcessSchedulerImpl;
import cz.incad.kramerius.relation.RelationService;
import cz.incad.kramerius.relation.impl.RelationServiceImpl;
import cz.incad.kramerius.rest.api.guice.HttpAsyncClientLifeCycleHook;
import cz.incad.kramerius.rest.api.guice.HttpAsyncClientProvider;
import cz.incad.kramerius.rest.apiNew.monitoring.APICallMonitor;
import cz.incad.kramerius.rest.apiNew.monitoring.impl.SolrAPICallMonitor;
import cz.incad.kramerius.service.GoogleAnalytics;
import cz.incad.kramerius.service.LifeCycleHook;
import cz.incad.kramerius.service.METSService;
import cz.incad.kramerius.service.impl.GoogleAnalyticsImpl;
import cz.incad.kramerius.service.impl.METSServiceImpl;
import cz.incad.kramerius.statistics.StatisticReport;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.statistics.accesslogs.dnnt.DNNTStatisticsAccessLogImpl;
import cz.incad.kramerius.statistics.accesslogs.solr.SolrStatisticsAccessLogImpl;
import cz.incad.kramerius.statistics.impl.*;
import org.apache.hc.client5.http.async.HttpAsyncClient;
import org.ehcache.CacheManager;

import javax.servlet.jsp.jstl.fmt.LocalizationContext;

import java.io.File;
import java.sql.Connection;
import java.util.Locale;

/**
 * Base kramerius module
 */
public class BaseModule extends AbstractModule {
	
	@Override
    protected void configure() {

        // logs statistics
        bind(StatisticsAccessLog.class).annotatedWith(Names.named("database")).to(SolrStatisticsAccessLogImpl.class).in(Scopes.SINGLETON);
        bind(StatisticsAccessLog.class).annotatedWith(Names.named("dnnt")).to(DNNTStatisticsAccessLogImpl.class).in(Scopes.SINGLETON);

        // api monitoring
        bind(APICallMonitor.class).to(SolrAPICallMonitor.class).asEagerSingleton();


        Multibinder<StatisticReport> reports = Multibinder.newSetBinder(binder(), StatisticReport.class);
        reports.addBinding().to(ModelStatisticReport.class);
        //reports.addBinding().to(DateDurationReport.class);
        reports.addBinding().to(AuthorReport.class);
        reports.addBinding().to(LangReport.class);
        reports.addBinding().to(LicenseReport.class);
        reports.addBinding().to(AnnualStatisticsReport.class);
        reports.addBinding().to(PidsReport.class);

        reports.addBinding().to(MultimodelReport.class);
        reports.addBinding().to(NKPLogReport.class);
        reports.addBinding().to(ModelSummaryReport.class);

        
        //bind(SolrAccess.class).to(SolrAccessImpl.class).in(Scopes.SINGLETON);
        bind(SolrAccess.class).annotatedWith(Names.named("new-index")).to(SolrAccessImplNewIndex.class).in(Scopes.SINGLETON);
        bind(SolrAccess.class).annotatedWith(Names.named("cachedSolrAccess")).to(CachedSolrAccessImpl.class).in(Scopes.SINGLETON);

        bind(METSService.class).to(METSServiceImpl.class);

        bind(Connection.class).annotatedWith(Names.named("kramerius4")).toProvider(Kramerius4ConnectionProvider.class);

        bind(Locale.class).toProvider(LocalesProvider.class);

        bind(ProcessScheduler.class).to(ProcessSchedulerImpl.class).in(Scopes.SINGLETON);
        bind(GCScheduler.class).to(GCSchedulerImpl.class).in(Scopes.SINGLETON);

        // TODO: MOVE
        bind(LocalizationContext.class).toProvider(CustomLocalizedContextProvider.class);

        bind(MostDesirable.class).to(MostDesirableImpl.class);

        // 
        //bind(Collection.class).toProvider(VirtualCollectionProvider.class);
        
        //bind(CollectionsManager.class).annotatedWith(Names.named("fedora")).to(FedoraCollectionsManagerImpl.class);

        bind(RelationService.class).to(RelationServiceImpl.class).in(Scopes.SINGLETON);
        bind(GoogleAnalytics.class).to(GoogleAnalyticsImpl.class).in(Scopes.SINGLETON);

        
        bind(RepositoryUrlManager.class).to(CachingFedoraUrlManager.class).in(Scopes.SINGLETON);

        bind(CacheManager.class).toProvider(CacheProvider.class).in(Scopes.SINGLETON);
        bind(HttpAsyncClient.class).toProvider(HttpAsyncClientProvider.class).in(Scopes.SINGLETON);

        Multibinder<LifeCycleHook> lfhooks = Multibinder.newSetBinder(binder(), LifeCycleHook.class);
        lfhooks.addBinding().to(CacheLifeCycleHook.class);
        lfhooks.addBinding().to(HttpAsyncClientLifeCycleHook.class);
//        lfhooks.addBinding().to(AudioLifeCycleHook.class);
        
    }

    @Provides
    @Named("fontsDir")
    public File getProcessFontsFolder() {
        String dirName = Constants.WORKING_DIR + File.separator + "fonts";
        return new File(dirName);
    }

}
