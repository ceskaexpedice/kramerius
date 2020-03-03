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
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import cz.incad.kramerius.statistics.ReportedAction;
import cz.incad.kramerius.statistics.StatisticReport;
import cz.incad.kramerius.statistics.StatisticsReportException;
import cz.incad.kramerius.statistics.impl.ModelStatisticReport;

/**
 * @author pavels
 */
public class ModelStatisticsViewObject extends AbstractStatisticsViewObject {
    
    public String getGraphTitle() throws IOException {
        return "Graf objektu typu :"+getSelectedModel();
    }
    
    
    public String getAllFilterOption() {
        return "All";
    }
    
    public String[] getActionFilterOptions() {
        List<String> names = new ArrayList<String>();
        ReportedAction[] actions = ReportedAction.values();
        for (ReportedAction ract : actions) {
            names.add(ract.name());
        }
        return (String[]) names.toArray(new String[names.size()]);
    }
    
    public String getSelectedModel() throws IOException {
        HttpServletRequest request = this.servletRequestProvider.get();
        String type = request.getParameter("val");
        
        ResourceBundle resBundle = resService.getResourceBundle("labels", this.localesProvider.get());
        String str = resBundle.getString("fedora.model."+type);
        return str;
    }



    public String getCurrentYear() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        return ""+year;
    }


    public String getPreviousYear() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        return ""+(year-1);
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
        return "javascript:statistics.reloadModelReport(_action(),_visibility(),$('#report_date_from').val(),$('#report_date_to').val(),'"+type+"','"+val+"',"+offsetInt+","+size+", _ip_address(), _ip_address_unique());";
    }
    
    public String getNext() {
        HttpServletRequest request = this.servletRequestProvider.get();
        String offset = request.getParameter("offset") != null ? request.getParameter("offset") : "0";
        String size = request.getParameter("size") != null ? request.getParameter("size") : "20";
        String val = request.getParameter("val");
        String type = request.getParameter("type");
        int sizeInt = Integer.parseInt(size);
        int offsetInt = (Integer.parseInt(offset))+sizeInt;
        return "javascript:statistics.reloadModelReport(_action(),_visibility(),$('#report_date_from').val(),$('#report_date_to').val(),'"+type+"','"+val+"',"+offsetInt+","+size+",_ip_address(), _ip_address_unique());";
    }

}
