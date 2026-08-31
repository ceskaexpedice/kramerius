package cz.incad.kramerius.statistics.accesslogs;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import cz.incad.kramerius.statistics.ReportedAction;
import cz.incad.kramerius.statistics.StatisticReport;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.statistics.StatisticsAccessLogSupport;

import java.io.IOException;
import java.util.Date;

public class AggregatedAccessLogs implements StatisticsAccessLog {


    @Inject
    @Named("solr-statistics")
    StatisticsAccessLog databaseAccessLog;


    public AggregatedAccessLogs() {
    }

    @Override
    public void reportAccess(String pid, String streamName, String actionName) throws IOException {
        try {
            databaseAccessLog.reportAccess(pid, streamName, actionName);
        } catch (Throwable e) {
            //reporting failure should not break main flow (not even runtime exceptions like org.ceskaexpedice.akubra.RepositoryException)
            e.printStackTrace();
        }
    }

    @Override
    public void processAccessLog(ReportedAction reportedAction, StatisticsAccessLogSupport sup) {
        this.databaseAccessLog.processAccessLog(reportedAction, sup);
    }

    @Override
    public StatisticReport[] getAllReports() {
        return this.databaseAccessLog.getAllReports();
    }

    @Override
    public StatisticReport getReportById(String reportId) {
        return this.databaseAccessLog.getReportById(reportId);
    }

    @Override
    public int cleanData(Date dateFrom, Date dateTo) {
        throw new UnsupportedOperationException("unsupported");
    }

    @Override
    public void refresh() throws IOException {
    }

}
