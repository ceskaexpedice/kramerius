/*
 * Copyright (C) 2012 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package cz.incad.Kramerius.statistics;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.Kramerius.imaging.utils.FileNameUtils;
import cz.incad.Kramerius.statistics.formatters.main.StatisticsExportMainLogFormatter;
import cz.incad.Kramerius.statistics.formatters.report.StatisticsReportFormatter;
import cz.incad.Kramerius.statistics.formatters.utils.StringUtils;
import cz.incad.kramerius.statistics.ReportedAction;
import cz.incad.kramerius.statistics.StatisticReport;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.statistics.StatisticsAccessLogSupport;
import cz.incad.kramerius.statistics.StatisticsReportException;
import cz.incad.kramerius.statistics.StatisticsReportSupport;

/**
 * @author pavels
 *
 */
public class StatisticsExportServlet extends GuiceServlet {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(StatisticsExportServlet.class.getName());
    
    @Inject
    StatisticsAccessLog statisticAccessLog;
    
    @Inject
    Set<StatisticsExportMainLogFormatter> mainLogFormatters;

    @Inject
    Set<StatisticsReportFormatter> reportFormatters;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        String format = req.getParameter("format");
        String reportId = req.getParameter("report");
        String filteredValue = req.getParameter("filteredValue");

        if (reportId != null && (!reportId.equals(""))) {
            // report
            StatisticReport report = this.statisticAccessLog.getReportById(reportId);
            try {
                StatisticsReportFormatter selectedFormatter = null;
                for (StatisticsReportFormatter rf : this.reportFormatters) {
                    if (format.equals( rf.getFormat()) && (reportId.equals(rf.getReportId())))  {
                        selectedFormatter = rf;
                        break;
                    }
                }
                if (selectedFormatter != null) {
                    selectedFormatter.beforeProcess(resp);
                    resp.setContentType(selectedFormatter.getMimeType());
                    resp.setHeader("Content-disposition", "attachment; filename=export."+(format.toLowerCase()) );
                    report.processAccessLog(action != null ? ReportedAction.valueOf(action) : null, selectedFormatter, filteredValue);
                    selectedFormatter.afterProcess(resp);
                }
            } catch (StatisticsReportException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
            }
        } else {
            StatisticsExportMainLogFormatter selectedFormatter = null;
            for (StatisticsExportMainLogFormatter mainFormatter : this.mainLogFormatters) {
                if (format.equals( mainFormatter.getFormat()))  {
                    selectedFormatter = mainFormatter;
                    break;
                }
            }
            if (selectedFormatter != null) {
                selectedFormatter.beforeProcess(resp);
                resp.setContentType(selectedFormatter.getMimeType());
                resp.setHeader("Content-disposition", "attachment; filename=export."+(format.toLowerCase()) );
                this.statisticAccessLog.processAccessLog(action != null ? ReportedAction.valueOf(action) : null, selectedFormatter);
                selectedFormatter.afterProcess(resp);
                
            }
            // all format
            /*
            Format enumFormat = Format.valueOf(format);
            resp.setContentType(enumFormat.getMimeType());
            resp.setHeader("Content-disposition", "attachment; filename=export."+(format.toLowerCase()) );
            enumFormat.render(ReportedAction.valueOf(action),this.statisticAccessLog, resp.getOutputStream());
            */
        }
    }


}

