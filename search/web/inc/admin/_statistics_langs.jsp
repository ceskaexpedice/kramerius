<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd"%>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view"%>

<%@ page isELIgnored="false"%>
<view:object name="statistics"
    clz="cz.incad.Kramerius.views.statistics.LanguageStatisticsViewObject"></view:object>


<scrd:securedContent action="show_statictics">

<link rel="stylesheet" type="text/css" href="css/graph/jquery.jqplot.min.css" />
<script src="js/graph/jquery.jqplot.min.js" type="text/javascript" ></script>
<script type="text/javascript" src="js/graph/jqplot.barRenderer.min.js"></script>
<script type="text/javascript" src="js/graph/jqplot.categoryAxisRenderer.min.js"></script>
<script type="text/javascript" src="js/graph/jqplot.pointLabels.min.js"></script>

<script type="text/javascript">
var data=[
    <c:forEach var="report" items="${statistics.report}" varStatus="status"> [${report.count}] ${not status.last ? ',' : ''} </c:forEach>
];
</script>


<script type="text/javascript">

$(document).ready(function(){

    $("#lang_report").tabs();

    var options = {
            title:"${statistics.graphTitle}",
            legend: {
                show: true,
                location: 'e',
                placement: 'outside'
            },  
            
            series:[
                    <c:forEach var="report" items="${statistics.jsonAwareReport}" varStatus="status"> 
                        {label:'${report.lang}', rendererOptions: { barWidth: 15 }} ${not status.last ? ',' : ''} 
                    </c:forEach>
            ],
            
            seriesDefaults:{
                renderer:$.jqplot.BarRenderer},
                rendererOptions:{
                    barWidth:5
                },
                highlighter:{tooltipFadeSpeed:'slow', tooltipLocation:'n'},
                axes:{
                    xaxis:{
                        renderer:$.jqplot.CategoryAxisRenderer,
                        showTicks: true,
                        showTickMarks: true,
                        ticks:['Jazyky']   
                    },
                    yaxis: {
                        showTicks: false,
                        showTickMarks: true,
                        max: ${statistics.maxValue+1},
                        min: 0
                    }
                },
            highlighter:{tooltipFadeSpeed:'slow', tooltipLocation:'n'}
         }
    
    var plot1 = $.jqplot('chart4', data, options);

    $('#chart4').bind('jqplotDataHighlight', 
            function (ev, seriesIndex, pointIndex, da ) {
                var mouseX = ev.pageX; //these are going to be how jquery knows where to put the div that will be our tooltip
                var mouseY = ev.pageY;
                var text = '<div style="margin:5px;"><strong>'+options.series[seriesIndex].label+' </strong><br/>';
                text +='<strong>Pocet zobrazeni:'+da[1]+'</strong>';
                $('#chart4tooltip').html(text);
                $('#chart4tooltip').show();

           }
        );    

    /*
        $('#chart4').bind('jqplotDataUnhighlight', 
            function (ev) {
                $('#chart4tooltip').html('');
                $('#chart4tooltip').hide();
            }
        );
    */
});
</script>
        
    <div id="lang_buttons">
        <div style="position: relative;">
            <span style="float: left;"><view:msg>common.page</view:msg>:<strong>${statistics.pageIndex}</strong></span>
            <c:if test="${statistics.displayLastFlag}">
                <a style="float: right;" href="${statistics.next}" class="ui-icon ui-icon-arrowthick-1-e"></a>
            </c:if>

            <c:if test="${statistics.displayFirstFlag}">
                <a  style="float: right;"href="${statistics.prev}" class="ui-icon ui-icon-arrowthick-1-w"></a>
            </c:if>
            <div style="clear: both;"></div>
        </div>
    </div>    
    <div id="lang_report">
    <ul>
        <li><a title="<view:msg>statistics.report.model.graph</view:msg>" href="#lang_report_graph"><view:msg>statistics.report.model.graph</view:msg></a></li>
        <li><a title="<view:msg>statistics.report.model.table</view:msg>" href="#lang_report_table"><view:msg>statistics.report.model.table</view:msg></a></li>
    </ul>

    <div id="lang_report_graph" style="position:relative">
        <div id="chart4" style="width:500px; height:370px;"></div>
        <div id="chart4tooltip" style="padding-left: 25px;font-style: normal;"></div>
    </div>


    <div id="lang_report_table" class="filter shadow ui-widget ui-widget-content" style="">
    <table style="width:100%">
        <thead><tr style="width:10%">
            <td><strong><view:msg>statistics.report.model.table.count</view:msg></strong></td>
            <td><strong><view:msg>statistics.report.model.table.lang</view:msg></strong></td>
        </tr></thead>

        <tbody>
            <c:forEach var="report" items="${statistics.report}" varStatus="status"> 
                <tr class="${(status.index mod 2 == 0) ? 'result ui-state-default': 'result '}"> 
                    <td> ${report.count}</td> 
                    <td>${report.lang}</td> 
                    </tr>
            </c:forEach>
        </tbody>
    </table>
    </div>

</div>

</scrd:securedContent>
