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
package cz.incad.Kramerius.views.statistics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.Kramerius.statistics.StatisticsExportServlet;
import cz.incad.Kramerius.utils.JSONUtils;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.statistics.ReportedAction;
import cz.incad.kramerius.statistics.StatisticReport;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.statistics.StatisticsReportException;
import cz.incad.kramerius.statistics.filters.DateFilter;
import cz.incad.kramerius.statistics.filters.IPAddressFilter;
import cz.incad.kramerius.statistics.filters.ModelFilter;
import cz.incad.kramerius.statistics.filters.StatisticsFilter;
import cz.incad.kramerius.statistics.filters.StatisticsFiltersContainer;
import cz.incad.kramerius.statistics.filters.UniqueIPAddressesFilter;
import cz.incad.kramerius.statistics.filters.VisibilityFilter;
import cz.incad.kramerius.statistics.filters.VisibilityFilter.VisbilityType;
import cz.incad.kramerius.statistics.impl.ModelStatisticReport;
import cz.incad.kramerius.utils.database.Offset;

/**
 * @author pavels
 *
 */
public abstract class AbstractStatisticsViewObject {

    public static final Logger LOGGER = Logger.getLogger(AbstractStatisticsViewObject.class.getName());
    static final int MAX_TITLE_LIMIT = 18;

    @Inject
    StatisticsAccessLog statisticsAccessLog;

    @Inject
    Provider<HttpServletRequest> servletRequestProvider;
    
    @Inject
    Provider<Locale> localesProvider;
    
    @Inject
    ResourceBundleService resService;

    protected List<Map<String,Object>> data = null;

    public int getMaxValue() throws StatisticsReportException {
        int max = -1;
        List<Map<String, Object>> report = getReport();
        for (Map<String, Object> map : report) {
            boolean containsCount = map.containsKey("count");
            if (containsCount) {
                Integer val = (Integer) map.get("count");
                if (max < val) {
                    max = val;
                }
            }
        }
        return max;
    }

    public VisibilityFilter getVisibilityFilter() throws IOException {
        HttpServletRequest request = this.servletRequestProvider.get();
        String visibility = request.getParameter("visibility");
        VisibilityFilter filter = new VisibilityFilter();
        filter.setSelected(VisibilityFilter.VisbilityType.valueOf(visibility.toUpperCase()));
        return filter;
    }

    public DateFilter getDateFilter() throws IOException {
        HttpServletRequest request = this.servletRequestProvider.get();
        String dFrom = request.getParameter(StatisticsExportServlet.DATE_FROM_ATTRIBUTE);
        String dTo = request.getParameter( StatisticsExportServlet.DATE_TO_ATTRIBUTE);
        DateFilter dFilter = new DateFilter();
        if (dFrom != null && (!dFrom.trim().equals(""))) {
            dFilter.setFromDate(dFrom);
        }
        if (dTo != null && (!dTo.trim().equals(""))) {
            dFilter.setToDate(dTo);
        }
        return dFilter;
    }

    public VisibilityFilter getVisbilityFilter() {
        HttpServletRequest request = this.servletRequestProvider.get();
        String vis = request.getParameter(StatisticsExportServlet.VISIBILITY_ATTRIBUTE);
        if (vis != null) vis = vis.toUpperCase();
        VisibilityFilter filter = new VisibilityFilter();
        filter.setSelected(VisbilityType.valueOf(vis));
        return filter;
    }
    
    public synchronized List<Map<String,Object>> getReport() throws StatisticsReportException {
        try {
            if (this.data == null) {
                HttpServletRequest request = this.servletRequestProvider.get();
                String type = request.getParameter("type");
                String val = request.getParameter("val");
                String ip = request.getParameter("ipaddresses");
                
                String uniqueIP = request.getParameter("uniqueipaddresses");

                String actionFilter = request.getParameter("action");
                String offset = request.getParameter("offset") != null ? request.getParameter("offset") : "0";
                String size = request.getParameter("size") != null ? request.getParameter("size") : "20";

                DateFilter dateFilter = getDateFilter();
                ModelFilter modelFilter = new ModelFilter();
                modelFilter.setModel(val);
                VisibilityFilter visFilter = getVisbilityFilter();
                
                UniqueIPAddressesFilter uniqueIPFilter = new UniqueIPAddressesFilter();
                uniqueIPFilter.setUniqueIPAddressesl(Boolean.valueOf(uniqueIP));

                IPAddressFilter ipAddr = new IPAddressFilter();
                if (ip != null && !ip.isEmpty()) {
                   ip = ip.replace(",", "|");
                   ip = ip.replace("*", "%");
                   ip = ip.replace(" ", "");
                   ipAddr.setIpAddress(ip);
                }
                else {
                    String ipConfigVal = ipAddr.getValue();
                    if (ipConfigVal != null) {
                        ipConfigVal = ipConfigVal.replace("*", "%"); 
                    }
                    ipAddr.setIpAddress(ipConfigVal);
                }

                StatisticReport report = statisticsAccessLog.getReportById(type);
                Offset reportOff = new Offset(offset, size);
                report.prepareViews(actionFilter != null ? ReportedAction.valueOf(actionFilter) : null ,new StatisticsFiltersContainer(new StatisticsFilter[] {dateFilter,modelFilter, visFilter, ipAddr}));
                this.data = report.getReportPage(actionFilter != null ? ReportedAction.valueOf(actionFilter) : null ,new StatisticsFiltersContainer(new StatisticsFilter[] {dateFilter,modelFilter, visFilter,ipAddr, uniqueIPFilter}), reportOff);
            }
            return this.data;
        } catch (IOException e) {
            throw new StatisticsReportException(e);
        }
    }

    public int getPageIndex() {
        HttpServletRequest request = this.servletRequestProvider.get();
        String offset = request.getParameter("offset") != null ? request.getParameter("offset") : "0";
        String size = request.getParameter("size") != null ? request.getParameter("size") : "20";
        int sizeInt = Integer.parseInt(size);
        int offsetInt = Integer.parseInt(offset);
        int pagei  = (offsetInt /sizeInt)+1;
        return pagei;
    }
    

    public boolean getDisplayFirstFlag() {
        return getPageIndex() > 1;
    }

    public boolean getDisplayLastFlag() throws StatisticsReportException {
        HttpServletRequest request = this.servletRequestProvider.get();
        String size = request.getParameter("size") != null ? request.getParameter("size") : "20";
        List<Map<String, Object>> report = getReport();
        return report.size() >= Integer.parseInt(size);
    }

    public List<Map<String,Object>> getJsonAwareReport() throws StatisticsReportException {
        List<Map<String, Object>> newResult = new ArrayList<Map<String,Object>>();
        List<Map<String, Object>> report = getReport();
        for (Map<String, Object> map : report) {
            Map<String, Object> jsonedMap = jsoned(map);
            newResult.add(jsonedMap);
        }
        return newResult;
    }    
    
    private Map<String, Object> jsoned(Map<String, Object> map) {
        Map<String, Object> retval = new HashMap<String, Object>();
        Set<String> keys = map.keySet();
        for (String key : keys) {
            Object val = map.get(key);
            if (val instanceof String) {
                if (key.equals(ModelStatisticReport.TITLE_KEY)) {
                    if (val.toString().length() > MAX_TITLE_LIMIT) {
                        val = ((String)val).substring(0, MAX_TITLE_LIMIT)+" ...";
                    }
                }
                retval.put(key, JSONUtils.escaped((String) val));
            } else {
                retval.put(key, val);
            }
        }
        return retval;
    }


}
