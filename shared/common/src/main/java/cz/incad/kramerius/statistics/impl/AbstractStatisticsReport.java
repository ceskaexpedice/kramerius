package cz.incad.kramerius.statistics.impl;

import cz.incad.kramerius.statistics.StatisticReport;
import cz.incad.kramerius.utils.conf.KConfiguration;

public abstract class AbstractStatisticsReport implements StatisticReport {

    protected String logsEndpoint() {
        String loggerPoint = KConfiguration.getInstance().getProperty("k7.log.solr.point","http://localhost:8983/solr/logs");
        String selectEndpoint = loggerPoint + (loggerPoint.endsWith("/") ? "" : "/" ) +"";
        return selectEndpoint;
    }

}
