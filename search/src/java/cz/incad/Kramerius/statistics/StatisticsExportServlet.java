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
import java.util.Set;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;

import com.google.inject.Provider;
import com.google.inject.name.Named;
import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.statistics.formatters.main.StatisticsExportMainLogFormatter;
import cz.incad.kramerius.statistics.formatters.report.StatisticsReportFormatter;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.statistics.ReportedAction;
import cz.incad.kramerius.statistics.StatisticReport;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.statistics.StatisticsReportException;
import cz.incad.kramerius.statistics.filters.*;
import cz.incad.kramerius.statistics.filters.VisibilityFilter.VisbilityType;
import cz.incad.kramerius.utils.StringUtils;

/**
 * @author pavels
 *
 */
public class StatisticsExportServlet extends GuiceServlet {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(StatisticsExportServlet.class.getName());


    public static final String MODEL_ATTRIBUTE = "filteredValue";
    public static final String REPORT_ID_ATTRIBUTE = "report";
    public static final String DATE_TO_ATTRIBUTE = "dateTo";
    public static final String DATE_FROM_ATTRIBUTE = "dateFrom";
    public static final String FORMAT_ATTRIBUTE = "format";
    public static final String ACTION_ATTRIBUTE = "action";
    public static final String VISIBILITY_ATTRIBUTE = "visibility";
    public static final String IP_ATTRIBUTE = "ipaddresses";
    public static final String UNIQUE_IP_ATTRIBUTE = "uniqueipaddresses";
    public static final String ANNUAL_YEAR = "annualyear";
    public static final String PIDS_ATTRIBUTE = "pids";

    public static final String FILE_ATTRIBUTE = "file";
    

    
    @Inject
    @Named("database")
    StatisticsAccessLog statisticAccessLog;
    
    @Inject
    Set<StatisticsExportMainLogFormatter> mainLogFormatters;

    @Inject
    Set<StatisticsReportFormatter> reportFormatters;


    @Inject
    RightsResolver actionAllowed;

    @Inject
    Provider<User> userProvider;


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String action = req.getParameter(ACTION_ATTRIBUTE);
        String format = req.getParameter(FORMAT_ATTRIBUTE);
        String dateFrom = req.getParameter(DATE_FROM_ATTRIBUTE);
        String dateTo = req.getParameter(DATE_TO_ATTRIBUTE);
        
        String reportId = req.getParameter(REPORT_ID_ATTRIBUTE);
        String filteredValue = req.getParameter(MODEL_ATTRIBUTE);
        String visibilityValue = req.getParameter(VISIBILITY_ATTRIBUTE);
        String ipAddresses = req.getParameter(IP_ATTRIBUTE);
        String uniqueIpAddresses = req.getParameter(UNIQUE_IP_ATTRIBUTE);
        String pids = req.getParameter(PIDS_ATTRIBUTE);


        String file = req.getParameter(FILE_ATTRIBUTE);

        String annual = req.getParameter(ANNUAL_YEAR);
        AnnualYearFilter annualYearFilter = new AnnualYearFilter();
        annualYearFilter.setAnnualYear(annual);
        
        DateFilter dateFilter = new DateFilter();
        dateFilter.setFromDate(dateFrom != null && (!dateFrom.trim().equals("")) ? dateFrom : null);
        dateFilter.setToDate(dateTo != null && (!dateTo.trim().equals("")) ? dateTo : null);
        
        ModelFilter modelFilter = new ModelFilter();
        modelFilter.setModel(filteredValue);
        
        UniqueIPAddressesFilter uniqueIPFilter = new UniqueIPAddressesFilter();
        uniqueIPFilter.setUniqueIPAddressesl(Boolean.valueOf(uniqueIpAddresses));
        
        PidsFilter pidsFilter = new PidsFilter();
        pidsFilter.setPids(pids);
        
        IPAddressFilter ipAddr = new IPAddressFilter();
        if (ipAddresses != null && !ipAddresses.isEmpty()) {
            ipAddresses = ipAddresses.replace(",", "|");
            ipAddresses = ipAddresses.replace("*", "%");
            ipAddresses = ipAddresses.replace(" ", "");   
            ipAddr.setIpAddress(ipAddresses);
        }
        else {
            String ipConfigVal = ipAddr.getValue();
            if (ipConfigVal != null) {
                ipConfigVal = ipConfigVal.replace("*", "%");
            }
            ipAddr.setIpAddress(ipConfigVal);
        }
        
        if (action != null && action.equals("null")) {
            action = null;
        }
        
        if (dateFrom == null) {
            dateFrom = "";
        }
        
        if (dateTo == null) {
            dateTo = "";
        }
        
        MultimodelFilter multimodelFilter = new MultimodelFilter();

        VisibilityFilter visFilter = null;
        if (visibilityValue != null && StringUtils.isAnyString(visibilityValue))  {
            visibilityValue = visibilityValue.toUpperCase();
            visFilter = new VisibilityFilter();
            visFilter.setSelected(VisbilityType.valueOf(visibilityValue));
        }


        if (permit(userProvider.get())) {
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
                        // check if it is possible to render report
                        if (report.verifyFilters(action != null ? ReportedAction.valueOf(action) : null,new StatisticsFiltersContainer(new StatisticsFilter []{dateFilter,modelFilter, ipAddr, multimodelFilter, annualYearFilter, pidsFilter}))) {
                            String info = null;
                            info = ((annual == null) ? "" : annual + ", ") + ((filteredValue == null) ? "" : filteredValue + ", ") + ((dateFrom.equals("")) ? "" : "od: " + dateFrom + ", ") + ((dateTo.equals("")) ? "" : "do: " + dateTo + ", ")
                                    + "akce: " + ((action == null) ? "ALL" : action) + ", viditelnosti: " + visibilityValue + ", "
                                    + ((ipAddr.getIpAddress().equals("")) ? "" : "zakázané IP adresy: " + ipAddr.getIpAddress() + ", ")
                                    + "unikátní IP adresy: " + uniqueIpAddresses + ".";
                            OutputStream responseOutputStream = resp.getOutputStream();
                            selectedFormatter.addInfo(responseOutputStream, info);
                            selectedFormatter.beforeProcess(responseOutputStream);
                            resp.setCharacterEncoding("UTF-8");
                            resp.setContentType(selectedFormatter.getMimeType());
                            if (file != null && StringUtils.isAnyString(file)) {
                                resp.setHeader("Content-disposition", "attachment; filename="+file);
                            } else {
                                resp.setHeader("Content-disposition", "attachment; filename=export."+(format.toLowerCase()) );
                            }
                            //TODO: Syncrhonization
                            report.prepareViews(action != null ? ReportedAction.valueOf(action) : null,new StatisticsFiltersContainer(new StatisticsFilter []{dateFilter,modelFilter, ipAddr, multimodelFilter, annualYearFilter, pidsFilter}));
                            report.processAccessLog(action != null ? ReportedAction.valueOf(action) : null, selectedFormatter,new StatisticsFiltersContainer(new StatisticsFilter []{dateFilter,modelFilter,visFilter,ipAddr, multimodelFilter, annualYearFilter, uniqueIPFilter, pidsFilter}));
                            selectedFormatter.afterProcess(responseOutputStream);
                        } else {
                            // TODO: Expecting filters
                            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        }
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
                    String info = null;
                    info = ((annual == null) ? "" : annual + ", ") + ((filteredValue == null) ? "" : filteredValue + ", ") + ((dateFrom.equals("")) ? "" : "od: " + dateFrom + ", ") + ((dateTo.equals("")) ? "" : "do: " + dateTo + ", ")
                            + "akce: " + ((action == null) ? "ALL" : action) + ", viditelnosti: " + visibilityValue + ", "
                            + ((ipAddr.getIpAddress().equals("")) ? "" : "zakázané IP adresy: " + ipAddr.getIpAddress() + ", ")
                            + "unikátní IP adresy: " + uniqueIpAddresses + ".";

                    OutputStream responseOutputStream = resp.getOutputStream();
                    selectedFormatter.addInfo(responseOutputStream, info);
                    selectedFormatter.beforeProcess(responseOutputStream);
                    resp.setCharacterEncoding("UTF-8");
                    resp.setContentType(selectedFormatter.getMimeType());
                    if (file != null && StringUtils.isAnyString(file)) {
                        resp.setHeader("Content-disposition", "attachment; filename="+file);
                    } else {
                        resp.setHeader("Content-disposition", "attachment; filename=export."+(format.toLowerCase()) );
                    }
                    this.statisticAccessLog.processAccessLog(action != null ? ReportedAction.valueOf(action) : null, selectedFormatter);
                    selectedFormatter.afterProcess(responseOutputStream);
                }
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
    }



    boolean permit(User user) {
        if (user != null)
            return this.actionAllowed.isActionAllowed(user,
                    SecuredActions.SHOW_STATISTICS.getFormalName(),
                    SpecialObjects.REPOSITORY.getPid(), null,
                    ObjectPidsPath.REPOSITORY_PATH).flag();
        else
            return false;
    }


}

