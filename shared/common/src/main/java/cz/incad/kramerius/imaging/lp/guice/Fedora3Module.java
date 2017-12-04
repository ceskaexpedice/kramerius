package cz.incad.kramerius.imaging.lp.guice;

import java.io.IOException;
import java.sql.Connection;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

import cz.incad.kramerius.imaging.DiscStrucutreForStore;
import cz.incad.kramerius.imaging.impl.Fedora3StreamsDiscStructure;
import cz.incad.kramerius.statistics.StatisticsAccessLog;

public class Fedora3Module extends AbstractModule {

    @Override
    protected void configure() {
        bind(Connection.class).annotatedWith(Names.named("fedora3")).toProvider(RAWFedoraDatabaseConnectionProvider.class);
        bind(DiscStrucutreForStore.class).to(Fedora3StreamsDiscStructure.class);
        //bind(StatisticsAccessLog.class).to(NoStatistics.class).in(Scopes.SINGLETON);
    }

//    public static class NoStatistics implements StatisticsAccessLog {
//
//        @Override
//        public void reportAccess(String pid, String streamName) throws IOException {
//        }
//
//        @Override
//        public boolean isReportingAccess(String pid, String streamName) {
//            return true;
//        }
//    }
}
