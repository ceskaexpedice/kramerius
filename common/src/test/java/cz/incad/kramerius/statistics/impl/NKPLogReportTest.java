package cz.incad.kramerius.statistics.impl;

import cz.incad.kramerius.statistics.ReportedAction;
import cz.incad.kramerius.statistics.accesslogs.database.DatabaseStatisticsAccessLogImpl;
import junit.framework.Assert;
import org.antlr.stringtemplate.StringTemplate;
import org.junit.Test;

public class NKPLogReportTest {

    @Test
    public void testTemplate() {
        StringTemplate statRecord = DatabaseStatisticsAccessLogImpl.stGroup.getInstanceOf("nkpLogsReport");
        statRecord.setAttribute("action", "READ");
        statRecord.setAttribute("action", ReportedAction.READ.name());
        statRecord.setAttribute("fromDefined", true);
        statRecord.setAttribute("toDefined", true);
        Assert.assertNotNull(statRecord.toString());
        Assert.assertTrue(statRecord.toString().contains("date"));
        Assert.assertTrue(statRecord.toString().contains(">"));
        Assert.assertTrue(statRecord.toString().contains("?"));
    }
}
