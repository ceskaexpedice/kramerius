package cz.incad.kramerius.statistics;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import cz.incad.kramerius.statistics.accesslogs.AggregatedAccessLogs;
import cz.incad.kramerius.statistics.impl.NullStatisticsAccessLogImpl;

/**
 * Created by pstastny on 10/19/2017.
 */
public class NullStatisticsModule extends AbstractModule {

    @Override
    protected void configure() {
        this.bind(StatisticsAccessLog.class).annotatedWith(Names.named("database")).to(NullStatisticsAccessLogImpl.class).in(Scopes.SINGLETON);
        this.bind(StatisticsAccessLog.class).annotatedWith(Names.named("dnnt")).to(NullStatisticsAccessLogImpl.class).in(Scopes.SINGLETON);

        this.bind(AggregatedAccessLogs.class).in(Scopes.SINGLETON);
    }
}
