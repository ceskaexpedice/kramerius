package org.kramerius;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import cz.incad.kramerius.relation.RelationService;
import cz.incad.kramerius.relation.impl.RelationServiceImpl;
import cz.incad.kramerius.service.FOXMLAppendLicenseService;
import cz.incad.kramerius.service.SortingService;
import cz.incad.kramerius.service.impl.FOXMLAppendLicenseImpl;
import cz.incad.kramerius.service.impl.SortingServiceImpl;

public class ImportModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(RelationService.class).to(RelationServiceImpl.class).in(Scopes.SINGLETON);
        bind(SortingService.class).to(SortingServiceImpl.class).in(Scopes.SINGLETON);
        bind(FOXMLAppendLicenseService.class).to(FOXMLAppendLicenseImpl.class).in(Scopes.SINGLETON);

        //bind(FedoraAccess.class).annotatedWith(Names.named("rawFedoraAccess")).to(FedoraAccessImpl.class).in(Scopes.SINGLETON);

        //bind(StatisticsAccessLog.class).annotatedWith(Names.named("database")).to(GenerateDeepZoomCacheModule.NoStatistics.class).in(Scopes.SINGLETON);
        //bind(StatisticsAccessLog.class).annotatedWith(Names.named("dnnt")).to(GenerateDeepZoomCacheModule.NoStatistics.class).in(Scopes.SINGLETON);
        //bind(AggregatedAccessLogs.class).to(GenerateDeepZoomCacheModule.NoStatistics.class).in(Scopes.SINGLETON);

        bind(RelationService.class).to(RelationServiceImpl.class).in(Scopes.SINGLETON);
        bind(SortingService.class).to(SortingServiceImpl.class).in(Scopes.SINGLETON);

    }
}
