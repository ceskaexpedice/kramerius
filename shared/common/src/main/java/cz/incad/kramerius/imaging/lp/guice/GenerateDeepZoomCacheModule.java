package cz.incad.kramerius.imaging.lp.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

import cz.incad.kramerius.fedora.AkubraRepositoryProvider;
import cz.incad.kramerius.fedora.SecuredAkubraRepositoryProvider;
import cz.incad.kramerius.imaging.DeepZoomCacheService;
import cz.incad.kramerius.imaging.DeepZoomFlagService;
import cz.incad.kramerius.imaging.DeepZoomTileSupport;
import cz.incad.kramerius.imaging.impl.DeepZoomFlagServiceImpl;
import cz.incad.kramerius.imaging.impl.FileSystemCacheServiceImpl;
import cz.incad.kramerius.imaging.impl.SimpleMemoryCacheServiceWrapper;
import cz.incad.kramerius.imaging.impl.TileSupportImpl;
import org.ceskaexpedice.akubra.AkubraRepository;

public class GenerateDeepZoomCacheModule extends AbstractModule {

    @Override
    protected void configure() {
        // mapped plain fedoraAccess as secured. In this process it is not
        // necessary to have checked access to fedora.
        bind(AkubraRepository.class).toProvider(SecuredAkubraRepositoryProvider.class).in(Scopes.SINGLETON);

//        bind(StatisticsAccessLog.class).annotatedWith(Names.named("database")).to(GenerateDeepZoomCacheModule.NoStatistics.class).in(Scopes.SINGLETON);
//        bind(StatisticsAccessLog.class).annotatedWith(Names.named("dnnt")).to(GenerateDeepZoomCacheModule.NoStatistics.class).in(Scopes.SINGLETON);

        bind(DeepZoomTileSupport.class).to(TileSupportImpl.class);

        bind(DeepZoomCacheService.class).annotatedWith(Names.named("fileSystemCache"))
                .to(FileSystemCacheServiceImpl.class).in(Scopes.SINGLETON);
        bind(DeepZoomCacheService.class).annotatedWith(Names.named("memoryCacheForward"))
                .to(SimpleMemoryCacheServiceWrapper.class).in(Scopes.SINGLETON);
        bind(DeepZoomFlagService.class).to(DeepZoomFlagServiceImpl.class).in(Scopes.SINGLETON);
    }

//    public static class NoStatistics extends AggregatedAccessLogs {
//
//        @Override
//        public StatisticReport[] getAllReports() {
//            return new StatisticReport[0];
//        }
//
//        @Override
//        public StatisticReport getReportById(String reportId) {
//            return null;
//        }
//
//        @Override
//        public void reportAccess(String pid, String streamName) throws IOException {
//        }
//
//        @Override
//        public boolean isReportingAccess(String pid, String streamName) {
//            return true;
//        }
//
//        @Override
//        public void processAccessLog(ReportedAction reportedAction, StatisticsAccessLogSupport sup) {
//        }
//
//        @Override
//        public void reportAccess(String pid, String streamName, String actionName) throws IOException {
//        }
//    }
}
