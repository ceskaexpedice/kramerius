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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.Kramerius.utils.JSONUtils;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.statistics.StatisticReport;
import cz.incad.kramerius.statistics.StatisticReportOffset;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.statistics.impl.ModelStatisticReport;

/**
 * @author pavels
 *
 */
public abstract class AbstractStatisticsViewObject {

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

    public int getMaxValue() {
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

    public synchronized List<Map<String,Object>> getReport() {
        if (this.data == null) {
            HttpServletRequest request = this.servletRequestProvider.get();
            String type = request.getParameter("type");
            String val = request.getParameter("val");
            String offset = request.getParameter("offset") != null ? request.getParameter("offset") : "0";
            String size = request.getParameter("size") != null ? request.getParameter("size") : "20";
            StatisticReport report = statisticsAccessLog.getReportById(type);
            StatisticReportOffset reportOff = new StatisticReportOffset(Integer.parseInt(offset), Integer.parseInt(size), val);
            this.data = report.getReportPage(reportOff);
        }
        return this.data;
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

    public boolean getDisplayLastFlag() {
        HttpServletRequest request = this.servletRequestProvider.get();
        String size = request.getParameter("size") != null ? request.getParameter("size") : "20";
        List<Map<String, Object>> report = getReport();
        return report.size() >= Integer.parseInt(size);
    }

    public List<Map<String,Object>> getJsonAwareReport() {
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
