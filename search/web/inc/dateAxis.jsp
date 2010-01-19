<%@ page import="java.util.*, cz.incad.Solr.CzechComparator, cz.incad.Solr.*" %>
<%@ page pageEncoding="UTF-8"%>
<%
            
            Facet dateAxisFacet = facets.get("rok");
            //jenom jeden, a prave 0. To nechceme
            if ((dateAxisFacet != null) &&
                    (dateAxisFacet.infos.size() > 0) &&
             !((dateAxisFacet.infos.size() == 1) && (dateAxisFacet.infos.get(0).displayName.equals("0")))) {
                
            ArrayList dates = new ArrayList();
            ArrayList modCounts = new ArrayList();
            String dateStr = "";
            
            for (int k = 0; k < dateAxisFacet.infos.size(); k++) {
                try {
                    
                    dates.add(dateAxisFacet.infos.get(k).displayName);
                    modCounts.add(new Integer(dateAxisFacet.infos.get(k).count));
                } catch (Exception ex) {
                } finally {
                    continue;
                }
            }
            
            String startMod = dateAxisFacet.infos.get(0).displayName;
            if(startMod.equals("0")){
                startMod = dateAxisFacet.infos.get(1).displayName;
            }
            
            
            String minYear;
            if (startMod.length() < 4) {
                System.out.println("Rok invalid: " + startMod);
                minYear = "1700";
            } else {
                minYear = startMod.substring(0, 2) + "00";
            }

            
            String endMod = dateAxisFacet.infos.get(dateAxisFacet.infos.size() - 1).displayName;
            
            String maxYear = "";
            if (endMod.length() < 4) {
                System.out.println("Rok invalid: " + endMod);
                maxYear = "2099";
            } else {
                maxYear = endMod.substring(0, 2) + "99";
            }

//String minYear = ((IModifier)sortedMods.get(0)).getName().substring(0,2) + "00";
//String maxYear = ((IModifier)sortedMods.get(sortedMods.size()-1)).getName().substring(0,2) + "99";


            int modMin = Integer.parseInt(minYear);
            int modMax = Integer.parseInt(maxYear);
            //int currentYear = Integer.parseInt(formatter.format(currentDate).substring(0, 4));
            //modMax = Math.min(modMax, currentYear);

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

            while (Integer.parseInt(current) <= modMax) {

                index = dates.indexOf(current);
                if (index > -1) {
                    //from = dates.get(index);
                    modCount = ((Integer) modCounts.get(index)).intValue();
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
        $("#content-slider").slider({
            animate: false,
            step: 1,
            change: scrollSliderChange,
            slide: scrollSliderSlide
        });
        $("#select-handle-left").draggable({
            containment: '#constraint_right',
            axis:'x',
            drag: selectHandleChangeLeft,
            stop: setSelectContainmentRight
        });
        $("#select-handle-right").draggable({
            containment: '#constraint_left',
            axis:'x',
            drag: selectHandleChangeRight,
            stop: setSelectContainmentLeft
        });
        initDateAxis();

    });
    
    var resizeTimer = null;
    $(window).bind('resize', function() {
        if(dateAxisActive){
            if (resizeTimer) clearTimeout(resizeTimer);
            resizeTimer = setTimeout(positionCurtains, 100);
        }
    });
    
    
</script>
<table id="daTable" style="visibility:hidden;height:1px;position:absolute;" align="center" >
<tr><td  valign="bottom"></td>
<td width="140px" align="right"></td></tr>
<tr><td style="">
<div id="content-scroll">
  <div id="content-holder">
  </div>
</div>
<div id="content-slider"></div>
</td>
<td width="140px" valign="top">
<br />
<div id="selectDiv" class="da_select" ></div>
Od: <input class="da_input" id="<%=fromField%>" 
        size="4" type="text" value="<%=fromValue%>" />
        
        Do: <input class="da_input" id="<%=toField%>" 
        size="4" type="text" value="<%=toValue%>"  />  
<br />
<a href="javascript:doFilter();" class="navEntryLink">použit</a>
<br />
<a id="zpet_link" href ="javascript:showDateAxis();" class="navEntryLink">zpět na text</a>
<%if(fromValue.length()>0){%>
<br />
<a href ="javascript:removeFilter();" class="navEntryLink" title=""><img border="0" src="img/x.png" /> zrušit</a>
<%}%>
<br />
<div id="da_zoom" style="display:none;" >
<a href="javascript:zoomOut();" >zoom out</a>
</div>
</td></tr></table>
<div id="select-handle-left" class="da_select_handle" ><img src="img/resize2.png" style="top:0px;position:absolute;left:-1px;" /></div>
<div id="select-handle-right" class="da_select_handle"><img src="img/resize2.png" style="top:0px;position:absolute;left:-1px;" /></div>
<div id="resizable-left" class="ui-state-active"></div>
<div id="resizable-right" class="ui-state-active"></div>
<div id="bubbleDiv" class="da_bubble" ><div id="bubbleText" ></div></div>
<div id="test"></div>
<div id="img_resize_right" class="da_resize"></div>
<div id="img_resize_left" class="da_resize"></div>
<div id="constraint_right" style="border:1px red none;position:absolute;z-index:0;"></div>
<div id="constraint_left" style="border:1px green none;position:absolute;z-index:0;"></div>

<%}%>