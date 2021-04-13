package cz.incad.kramerius.statistics.accesslogs;

import cz.incad.kramerius.statistics.ReportedAction;
import cz.incad.kramerius.statistics.StatisticsAccessLog;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractStatisticsAccessLog implements StatisticsAccessLog {

    static Map<String, ReportedAction> ACTIONS = new HashMap<>();
    static {
        AbstractStatisticsAccessLog.ACTIONS.put("img", ReportedAction.READ);
        AbstractStatisticsAccessLog.ACTIONS.put("pdf", ReportedAction.PDF);
        AbstractStatisticsAccessLog.ACTIONS.put("print", ReportedAction.PRINT);
        AbstractStatisticsAccessLog.ACTIONS.put("zoomify", ReportedAction.READ);
    }

    protected ThreadLocal<ReportedAction> reportedAction = new ThreadLocal<ReportedAction>();


}
