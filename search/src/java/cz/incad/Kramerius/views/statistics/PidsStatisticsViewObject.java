/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.Kramerius.views.statistics;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Gabriela Melingerová
 */
public class PidsStatisticsViewObject extends AbstractStatisticsViewObject {
    
    
    public String getNext() {
        HttpServletRequest request = this.servletRequestProvider.get();
        String offset = request.getParameter("offset") != null ? request.getParameter("offset") : "0";
        String size = request.getParameter("size") != null ? request.getParameter("size") : "20";
        int sizeInt = Integer.parseInt(size);
        int offsetInt = (Integer.parseInt(offset))+sizeInt;
        return "javascript:statistics.reloadPidsReport(_action(), _pids(), _visibility(),$('#report_date_from_pid').val(),$('#report_date_to_pid').val(),"+offsetInt+","+size+", _ip_address(), _ip_address_unique());";
    }

    public String getPrev() {
        HttpServletRequest request = this.servletRequestProvider.get();
        String offset = request.getParameter("offset") != null ? request.getParameter("offset") : "0";
        String size = request.getParameter("size") != null ? request.getParameter("size") : "20";
        int sizeInt = Integer.parseInt(size);
        int offsetInt = Math.max((Integer.parseInt(offset)-sizeInt), 0);
        return "javascript:statistics.reloadPidsReport(_action(), _pids(), _visibility(),$('#report_date_from_pid').val(),$('#report_date_to_pid').val(),"+offsetInt+","+size+", _ip_address(), _ip_address_unique());";
    } 

    public String getGraphTitle() {
        return "";
    }

}
