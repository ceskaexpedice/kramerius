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

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.sun.corba.se.pept.transport.ContactInfo;

import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.statistics.StatisticReport;
import cz.incad.kramerius.statistics.StatisticReportOffset;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.statistics.impl.ModelStatisticReport;

/**
 * @author pavels
 */
public class ModelStatisticsViewObject extends AbstractStatisticsViewObject {
    
    public String getGraphTitle() throws IOException {
        return "Graf objektu typu :"+getSelectedModel();
    }
    
    public String getSelectedModel() throws IOException {
        HttpServletRequest request = this.servletRequestProvider.get();
        String type = request.getParameter("val");
        ResourceBundle resBundle = resService.getResourceBundle("labels", this.localesProvider.get());
        String str = resBundle.getString("fedora.model."+type);
        return str;
    }
    
    
    
    public List<String> getModels() {
        StatisticReport report = statisticsAccessLog.getReportById(ModelStatisticReport.REPORT_ID);
        return report.getOptionalValues();
    }

    
    public String getPrev() {
        HttpServletRequest request = this.servletRequestProvider.get();
        String val = request.getParameter("val");
        String type = request.getParameter("type");
        String offset = request.getParameter("offset") != null ? request.getParameter("offset") : "0";
        String size = request.getParameter("size") != null ? request.getParameter("size") : "20";
        int sizeInt = Integer.parseInt(size);
        int offsetInt = Math.max((Integer.parseInt(offset)-sizeInt), 0);
        return "javascript:statistics.reloadModelReport('"+type+"','"+val+"',"+offsetInt+","+size+");";
    }

    public String getNext() {
        HttpServletRequest request = this.servletRequestProvider.get();
        String offset = request.getParameter("offset") != null ? request.getParameter("offset") : "0";
        String size = request.getParameter("size") != null ? request.getParameter("size") : "20";
        String val = request.getParameter("val");
        String type = request.getParameter("type");
        int sizeInt = Integer.parseInt(size);
        int offsetInt = (Integer.parseInt(offset))+sizeInt;
        return "javascript:statistics.reloadModelReport('"+type+"','"+val+"',"+offsetInt+","+size+");";
    }

}
