package cz.incad.kramerius.statistics;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import cz.incad.kramerius.statistics.impl.DatabaseStatisticsAccessLogImpl;
import cz.incad.kramerius.statistics.impl.NullStatisticsAccessLogImpl;

/**
 * Created by pstastny on 10/19/2017.
 */
public class NullStatisticsModule extends AbstractModule {

    @Override
    protected void configure() {
        this.bind(StatisticsAccessLog.class).to(NullStatisticsAccessLogImpl.class).in(Scopes.SINGLETON);
    }
}
