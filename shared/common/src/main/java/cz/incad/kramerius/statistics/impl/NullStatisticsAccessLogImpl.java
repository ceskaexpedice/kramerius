package cz.incad.kramerius.statistics.impl;

import cz.incad.kramerius.statistics.ReportedAction;
import cz.incad.kramerius.statistics.StatisticReport;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.statistics.StatisticsAccessLogSupport;
import cz.incad.kramerius.statistics.accesslogs.AggregatedAccessLogs;

import java.io.IOException;
import java.util.Map;

/**
 * Created by pstastny on 10/19/2017.
 */
public class NullStatisticsAccessLogImpl extends AggregatedAccessLogs {

    public NullStatisticsAccessLogImpl() {
    }

    @Override
    public void reportAccess(String pid, String streamName, String actionName) throws IOException {

    }

    @Override
    public void reportAccess(String pid, String streamName, String actionName, Map<String, String> extraFields) throws IOException {
        // no-op: processes do not log accesses
    }

    @Override
    public void processAccessLog(ReportedAction reportedAction, StatisticsAccessLogSupport sup) {

    }

    @Override
    public StatisticReport[] getAllReports() {
        return new StatisticReport[0];
    }

    @Override
    public StatisticReport getReportById(String reportId) {
        return null;
    }
}
