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


    var options = {
            title:"${statistics.graphTitle}",
            legend: {
                show: false,
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

    $('#chart4').bind('jqplotDataUnhighlight', function()  {
        $("#chart4tooltip").hide();
    });
    
    $('#chart4').bind('jqplotDataHighlight', 
            function (ev, seriesIndex, pointIndex, da ) {
                var mouseX = ev.pageX; //these are going to be how jquery knows where to put the div that will be our tooltip
                var mouseY = ev.pageY;

                var max = $('#chart4').width() - 200;

                var calculatedY=mouseY - $('#chart4').offset().top-70 ;
                var calculatedX = mouseX-$('#chart4').offset().left+70;
                calculatedX = Math.min(calculatedX,max);

                $("#chart4tooltip").show();
                $("#chart4tooltip").css("position", "absolute").css("top",""+calculatedY+"px").css("left",""+calculatedX+"px");
                
                var text = '<div style="margin:5px;"><strong> Jazyk:'+options.series[seriesIndex].label+' </strong><br/>';
                text +='<i>Pocet zobrazeni:'+da[1]+'</i>';
                $('#chart4tooltip').html(text);
                $('#chart4tooltip').show();

           }
        );    
});
</script>
        
    <div id="lang_buttons">

        <div style="text-align: center;" >
            <h2> <view:msg>statistics.report.lang</view:msg> </h2>
        </div>

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

    <div id="lang_report_graph" style="position:relative">
        <div id="chart4" style="width:750px; height:370px;"></div>
        <div id="chart4tooltip" style="position:absolute;font-style: normal; display: none;" class="shadow ui-widget ui-widget-content"></div>
    </div>


</scrd:securedContent>
