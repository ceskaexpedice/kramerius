package cz.incad.Kramerius.views.statistics;

import javax.servlet.http.HttpServletRequest;

public class PidsViewObject extends AbstractStatisticsViewObject {

	public String getNext() {
        HttpServletRequest request = this.servletRequestProvider.get();
        String offset = request.getParameter("offset") != null ? request.getParameter("offset") : "0";
        String size = request.getParameter("size") != null ? request.getParameter("size") : "20";
        String val = request.getParameter("val");
        String type = request.getParameter("type");
        int sizeInt = Integer.parseInt(size);
        int offsetInt = (Integer.parseInt(offset))+sizeInt;
        return "javascript:statistics.reloadPidsReport(_action(),'"+type+"','"+val+"',"+offsetInt+","+size+");";
    }
    
    public String getPrev() {
        HttpServletRequest request = this.servletRequestProvider.get();
        String val = request.getParameter("val");
        String offset = request.getParameter("offset") != null ? request.getParameter("offset") : "0";
        String size = request.getParameter("size") != null ? request.getParameter("size") : "20";
        String type = request.getParameter("type");
        int sizeInt = Integer.parseInt(size);
        int offsetInt = Math.max((Integer.parseInt(offset)-sizeInt), 0);
        return "javascript:statistics.reloadPidsReport(_action(),'"+type+"','"+val+"',"+offsetInt+","+size+");";
    }
    

    public String getFilteringValue() {
        HttpServletRequest request = this.servletRequestProvider.get();
        String val = request.getParameter("val");
        return val;
    }
    
    public String getGraphTitle() {
        return "";
    }

}
