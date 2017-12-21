<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd"%>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view"%>

<%@ page isELIgnored="false"%>
<view:object name="statistics"
    clz="cz.incad.Kramerius.views.statistics.ModelStatisticsViewObject"></view:object>


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
var pids=[
    <c:forEach var="report" items="${statistics.report}" varStatus="status"> '${report.pid}' ${not status.last ? ',' : ''} </c:forEach>
];
</script>



<script type="text/javascript">

$(document).ready(function(){
    $(".buttons>a").button();
    var options = {
            legend: {
                show: false,
                location: 'e',
                placement: 'outside'
            },  
            
            series:[
                    <c:forEach var="report" items="${statistics.jsonAwareReport}" varStatus="status"> 
                        {label:'${report.title}', rendererOptions: { barWidth: 15 }} ${not status.last ? ',' : ''} 
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
                        ticks:[]   
                    },
                    yaxis: {
                        showTicks: false,
                        showTickMarks: true,
                        max: ${statistics.maxValue+1},
                        min: 0
                    }
                },
                cursor: {
                    show:true,
                    showTooltip:true
                    
                    },
            highlighter:{tooltipFadeSpeed:'slow', tooltipLocation:'n'}
                
         }
    
    var plot1 = $.jqplot('chart1', data, options);

    $('#chart1').bind('jqplotDataUnhighlight', function()  {
        $("#chart1tooltip").hide();
    });
    
    $('#chart1').bind('jqplotDataHighlight', 
            function (ev, seriesIndex, pointIndex, data ) {
                var mouseX = ev.pageX; //these are going to be how jquery knows where to put the div that will be our tooltip
                var mouseY = ev.pageY;
                // max left position
                var max = $('#chart1').width() - 200;

                var calculatedY=mouseY - $('#chart1').offset().top-70 ;
                var calculatedX = mouseX-$('#chart1').offset().left+70;
                calculatedX = Math.min(calculatedX,max);
                
                $("#chart1tooltip").show();
                $("#chart1tooltip").css("position", "absolute").css("top",""+calculatedY+"px").css("left",""+calculatedX+"px");
                
                $("#chart1tooltip").html('<div style="margin:5px;"><span><i><view:msg>common.waitplease</view:msg>...</i></span></div>');
                $('#chart1tooltip').show();
                $.get("inc/admin/_statistics_model_details.jsp?pid="+pids[seriesIndex],function(dat) {
                    $("#chart1tooltip").html(dat);
                    var text = '<div style="margin:5px;"><strong>'+statisticsDetail.title+' </strong><br/>';
                    text +='<strong>Pocet zobrazeni:'+data[1]+'</strong><br/>';
                    text+="<ul>";
                    statisticsDetail.items.forEach(function(item) {
                        text +='<li><i>'+item+'</i></li>';
                    });
                    text += '</ul></div>';
                    $("#chart1tooltip").html(text);
                });
            }
        );    

});
</script>
        
    <div id="model_report_buttons">

        <div style="text-align: center;" >
        <h2> <view:msg>statistics.report.model</view:msg> (${statistics.selectedModel})</h2>
        </div>

        <div style="position: relative;" class="buttons">
            
            <span style="float: left;"> <view:msg>common.page</view:msg>:<strong>${statistics.pageIndex}</strong></span>
            <c:if test="${statistics.displayLastFlag}">
                <a style="float: right;" href="${statistics.next}"><span class="ui-icon ui-icon-arrowthick-1-e"></span></a>
            </c:if>

            <c:if test="${statistics.displayFirstFlag}">
                <a  style="float: right;"href="${statistics.prev}"><span class="ui-icon ui-icon-arrowthick-1-w button"></span></a>
            </c:if>
            <div style="clear: both;"></div>
            
        </div>
    </div>    

    <div id="model_report_graph" style="padding-top: 10px; position: relative;">
        <div id="chart1" style="width:750px; height:370px;"></div>
        <div id="chart1tooltip" style="position:absolute;font-style: normal; display: none;" class="shadow ui-widget ui-widget-content"></div>
    </div>



</scrd:securedContent>