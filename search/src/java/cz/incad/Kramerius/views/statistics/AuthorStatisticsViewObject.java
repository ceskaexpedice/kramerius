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

import javax.servlet.http.HttpServletRequest;

/**
 * @author pavels
 *
 */
public class AuthorStatisticsViewObject extends AbstractStatisticsViewObject {
    
    
    public String getNext() {
        HttpServletRequest request = this.servletRequestProvider.get();
        String offset = request.getParameter("offset") != null ? request.getParameter("offset") : "0";
        String size = request.getParameter("size") != null ? request.getParameter("size") : "20";
        String val = request.getParameter("val");
        int sizeInt = Integer.parseInt(size);
        int offsetInt = (Integer.parseInt(offset))+sizeInt;
        String type = "type";
        return "javascript:statistics.reloadAuthorsReport(_action(),_visibility(),$('#report_date_from').val(),$('#report_date_to').val(),'"+type+"','"+val+"',"+offsetInt+","+size+", _ip_address(), _ip_address_unique());";
    }
    
    public String getPrev() {
        HttpServletRequest request = this.servletRequestProvider.get();
        String val = request.getParameter("val");
        String offset = request.getParameter("offset") != null ? request.getParameter("offset") : "0";
        String size = request.getParameter("size") != null ? request.getParameter("size") : "20";
        int sizeInt = Integer.parseInt(size);
        int offsetInt = Math.max((Integer.parseInt(offset)-sizeInt), 0);
        String type = "type";
        return "javascript:statistics.reloadAuthorsReport(_action(),_visibility(),$('#report_date_from').val(),$('#report_date_to').val(),'"+type+"','"+val+"',"+offsetInt+","+size+", _ip_address(), _ip_address_unique());";
    }

    public String getGraphTitle() {
        return "";
    }

}
