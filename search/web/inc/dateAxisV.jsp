<%@ page import="java.util.*, cz.incad.Solr.CzechComparator, cz.incad.Solr.*" %>
<%@ page pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@include file="proccessFacets.jsp" %>
<%
            
            Facet dateAxisFacet = facets.get("rok");
            //jenom jeden, a prave 0. To nechceme
            if (dateAxisFacet != null)
                Collections.sort(dateAxisFacet.infos, new CzechComparator());
            if ((dateAxisFacet != null) &&
                    (dateAxisFacet.infos.size() > 1)){
                
            //ArrayList dates = new ArrayList();
            //ArrayList modCounts = new ArrayList();
            String dateStr = "";
            String startMod = dateAxisFacet.infos.get(0).displayName;
            if(startMod.equals("0")){
                startMod = dateAxisFacet.infos.get(1).displayName;
            }
            
            
            String minYear;
            if (startMod.length() < 4) {
                System.out.println("Rok invalid: " + startMod);
                minYear = "1700";
            } else {
                minYear = startMod.substring(0, 3) + "0";
            }

            
            String endMod = dateAxisFacet.infos.get(dateAxisFacet.infos.size() - 1).displayName;
            
            String maxYear = "";
            Calendar cal = Calendar.getInstance();
            int cur_year = cal.get(Calendar.YEAR);
            if (endMod.length() < 4 || Integer.parseInt(endMod)>cur_year) {
                System.out.println("Rok invalid: " + endMod);
                maxYear = Integer.toString(cur_year).substring(0, 3) + "9";
                //maxYear = "2019";
            } else {
                maxYear = endMod.substring(0, 3) + "9";
            }

            int modMin = Integer.parseInt(minYear);
            int modMax = Integer.parseInt(maxYear);
            

            String days = "{";
            int maxDaysCount = 0;

            ArrayList months = new ArrayList();
            ArrayList monthsValues = new ArrayList();
            int maxCountMonths = 0;

            ArrayList years = new ArrayList();
            ArrayList yearsValues = new ArrayList();
            int maxCountYears = 0;

            String month = "";
//int monthInt = 0;
            String year = "";
            String value = "";
            int index = 0;
            int pocet = 0;
            String from = "";
            String to = "";
            int tempInt;

            String current = minYear;
            int currentInt = modMin;
            int modCount = 0;
            String currentDay = "01";
            FacetInfo fInfo;

            while (Integer.parseInt(current) <= modMax) {
                index = dateAxisFacet.infos.indexOf(current);                
                //index = dates.indexOf(current);
                //if (index > -1) {
                    //modCount = ((Integer) modCounts.get(index)).intValue();
                fInfo = dateAxisFacet.getFacetInfoByName(current);
                if(fInfo!=null){
                    modCount = fInfo.count;
                } else {
                    modCount = 0;
                }
                from = current;
                to = from;

                currentInt++;
                current = Integer.toString(currentInt);

                days += "\"" + from + "\":[" + modCount + ", '" + from + "', '" + to + "']";
                if (Integer.parseInt(current) <= modMax) {
                    days += ", ";
                }
                maxDaysCount = Math.max(maxDaysCount, modCount);


                //Proccess decades	

                month = from.substring(0, 3);
                index = months.indexOf(month);
                if (index > -1) {
                    pocet = ((Integer) monthsValues.get(index)).intValue();
                    pocet += modCount;
                    monthsValues.set(index, new Integer(pocet));
                    maxCountMonths = Math.max(maxCountMonths, pocet);
                } else {
                    months.add(month);
                    monthsValues.add(new Integer(modCount));
                    maxCountMonths = Math.max(maxCountMonths, modCount);
                }

                //Proccess centuries	
                year = from.substring(0, 2);
                index = years.indexOf(year);
                if (index > -1) {
                    pocet = ((Integer) yearsValues.get(index)).intValue();
                    pocet += modCount;
                    yearsValues.set(index, new Integer(pocet));
                    maxCountYears = Math.max(maxCountYears, pocet);
                } else {
                    years.add(year);
                    yearsValues.add(new Integer(modCount));
                    maxCountYears = Math.max(maxCountYears, modCount);
                }
            }

            days += "}";
            String monthsStr = "{";
            if (months.size() > 0) {
                monthsStr = "{";
                for (int d = 0; d < months.size() - 1; d++) {
                    month = (String) months.get(d);
                    value = month + "0','" + month + "9";
                    monthsStr += "\"" + month + "0\":[" + monthsValues.get(d) + ", '" + value + "'], ";
                }
                month = (String) months.get(months.size() - 1);
                value = month + "0','" + month + "9";
                monthsStr += "\"" + month + "0\":[" + monthsValues.get(months.size() - 1) + ", '" + value + "']}";
            }

            String yearsStr = "";
            if (years.size() > 0) {
                yearsStr = "{";
                for (int d = 0; d < years.size() - 1; d++) {
                    year = (String) years.get(d);
                    value = year + "00','" + year + "99";
                    yearsStr += "\"" + year + "00\":[" + yearsValues.get(d) + ", '" + value + "'], ";
                }
                year = (String) years.get(years.size() - 1);
                value = year + "00','" + year + "99";
                yearsStr += "\"" + year + "00\":[" + yearsValues.get(years.size() - 1) + ", '" + value + "']}";
            }

%>
<script>
    
    var times = new Array();
    var sizes = new Array();
    var maximums = new Array();
    times[0] = <%=yearsStr%>;
    maximums[0] = <%=maxCountYears%>;  
    times[1] = <%=monthsStr%>;
    maximums[1] = <%=maxCountMonths%>;  
    times[2] = <%=days%>;
    maximums[2] = <%=maxDaysCount%>;
    var level = 1;
    var levels = 2;
    var zooms = new Array();
    zooms[0] = [<%=minYear%>, <%=maxYear%>];
    zooms[1] = [<%=minYear%>, <%=maxYear%>];
    var startTime = <%=startMod%>;
    var endTime = <%=endMod%>;
    var shortMonths= ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    var longMonths= ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];
    var shortDays= ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
    var longDays= ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
    dateAxisActive = true;
    
    var showStaticAxis = true;
    var maxCount;
    startTime = zooms[0][0];
    endTime = zooms[0][1];
  
$(document).ready(function(){
 /*
        $("#content-slider").slider({
            animate: false,
            step: 1,
            orientation: 'vertical',
            change: scrollSliderChange,
            slide: scrollSliderSlide
        });
*/        
        $("#select-handle-top").draggable({
            containment: '#constraint_bottom',
            axis:'y',
            drag: selectHandleChangeTop,
            stop: setSelectContainmentBottom
        });
        $("#select-handle-bottom").draggable({
            containment: '#constraint_top',
            axis:'y',
            drag: selectHandleChangeBottom,
            stop: setSelectContainmentTop
        });
        $("#content-scroll").bind('scroll', function() {
            daScrolled();
        });
      
        initDateAxis();

    });
</script>

<div id="selectDiv" class="da_select" style="display:none;" ></div>

<div id="da-inputs">
<fmt:message bundle="${lctx}">Od</fmt:message>: <input class="da_input" id="f1" size="4" type="text" value="" />
<fmt:message bundle="${lctx}">Do</fmt:message>: <input class="da_input" id="f2" size="4" type="text" value=""  /> 
<a href="javascript:doFilter();" ><img align="top" src="img/lupa_orange.png" border="0" alt="<fmt:message bundle="${lctx}">použit</fmt:message>" title="<fmt:message bundle="${lctx}">použit</fmt:message>" /></a>
</div>
<div id="content-resizable" style="position:relative;float:none;">
<div id="content-scroll" style="float:left;" >
    <div class="da_container" id="da_container">
<div id="select-handle-top" class="da_select_handle" ><img src="img/resize.png" style="top:-4px;position:absolute;left:0px;" /></div>
<div id="select-handle-bottom" class="da_select_handle"><img src="img/resize.png" style="top:-4px;position:absolute;left:0px;" /></div>
<div id="resizable-top" class="ui-state-active"></div>
<div id="resizable-bottom" class="ui-state-active"></div>
<div id="bubbleDiv" class="da_bubble" ><div id="bubbleText" ></div></div>
<div id="img_resize_bottom" class="da_resize"></div>
<div id="img_resize_top" class="da_resize"></div>
<div id="constraint_bottom" style="border:1px red solid;position:absolute;z-index:0;visibility:hidden;"></div>
<div id="constraint_top" style="border:1px red solid;position:absolute;z-index:0;visibility:hidden;"></div>
    </div>
</div>
</div><div class="clear"></div>
<div id="da_zoom" style="display:none;" >
<a href="javascript:zoomOut();" >zoom out</a>
</div>
<div id="kk"></div>
<%}else{
%>
<script>
     $('#dateAxis').hide();
</script>
<%}%>
