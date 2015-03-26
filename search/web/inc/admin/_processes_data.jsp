<%@ page pageEncoding="UTF-8" %>

<!-- Vypis procesu urceny pro javascript -->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>


<%@ page trimDirectiveWhitespaces="true"%>

<%@page import="com.google.inject.Injector"%>
<%@page import="cz.incad.kramerius.processes.LRProcessManager"%>
<%@page import="cz.incad.kramerius.processes.DefinitionManager"%>

<%@page import="cz.incad.Kramerius.views.ProcessesViewObject"%>
<%@page import="cz.incad.kramerius.utils.database.Ordering"%>
<%@page import="cz.incad.kramerius.utils.database.Offset"%>

<%@ page isELIgnored="false"%>


<%@page import="cz.incad.kramerius.utils.database.Ordering"%>

<%@page import="cz.incad.Kramerius.LongRunningProcessServlet"%>
<%@page import="java.util.Locale"%>
<%@page import="cz.incad.kramerius.service.ResourceBundleService"%>

<view:object name="processView" clz="cz.incad.Kramerius.views.ProcessesViewObject"></view:object>



<scrd:securedContent action="manage_lr_process" sendForbidden="true">
<style type="text/css">
    #processes_list>div.header{
        height:20px;
        padding-top:5px;
        padding-right:10px;
    }
    #processes_list>div.header>div.title{
        float:left;
        padding-left: 30px;
        width:100px;
    }
    #processes_list>div.header>div.title>a{
        float:left;
    }
    #processes_list>div.header>div.buttons{
        float:right;
    }
    #processes_list>div.header>div.buttons>a{
        float:left;
    }
    #processes_list>div.separator{
        width:100%;
        border-top: 2px solid rgba(0, 30, 60, 0.9);
        margin-top: 3px;
    }
    #processes td{
        padding-bottom:3px;
        text-align: left;
    }
    #processes tr.subprocess{
        display: none;
    }
    #processes tr.subprocess>td.t1{
        border-left:solid 1px rgba(0, 30, 60, 0.9);
    }
    #processes tr.subprocess>td.t2{
        padding-left:10px;
    }

    /* filter */
    #processes div.filter{
        width:35%;
        display: none;
        padding-bottom: 20px;
        
        position: absolute; 
        top: 15px;
        right: 55px;
    }

    /* processline */
    #processes div.processesline{
        width:60%;
        display: none;
        padding-bottom: 20px;
        
        position: absolute; 
        top: 15px;
        right: 200px;
    }


    /* outputTemplates */
    #processes div.outputTemplate{
        width:250px;
        display: none;
        padding-bottom: 20px;
        position: absolute; 
        left: -255px;
    }

     
     
    #processes div.displayButton{ 
    }

    #processes div.displayButton div.dropdown{ 
        float:right;
        height:10px;
        padding-top:5px;
        padding-right:10px;
    }

    div.apply{ 
        float:right;
        height:15px;
    }

    div.applyContent{ 
        height:20px;
        border:1px solid black;
        width:100%;
    }
    .buttons{
        margin-bottom: 5px;
    }
    .buttons>a{
        margin-right:3px;
    }
    .buttons>a>.ui-button-text{
        padding:3px;
    }
    
     
         
</style>


<div id="processes_list" align="center">
<script type="text/javascript">

<!--
function _wait() {
	$("#processes").html('<div style="margin-top:30px;width:100%;text-align:center;"><img src="img/loading.gif" alt="loading" /></div>');
}
function _ref(ordering, page, size, type) {
    _wait();
	var refreshurl = "inc/admin/_processes_data.jsp?ordering="+ordering+"&page="+page+"&size="+size+"&type="+type+processes.currentFilter.filterPostfix();
	$.get(refreshurl, function(sdata) {
		$("#processes").html(sdata);

	    processes.repairDisplayed();
	});
}


function _toggle_filter() {
	$(".filter").toggle();
    $(".displayButton").toggle();

}

function _toggle_processline() {
	$(".processesline").toggle();
	
	$('#processesline_table_div').animate({
         scrollLeft:  $("#process_page_${processView.page}").offset().left-$('#processesline_table').offset().left
     }, 200);
}


function _change_buttons(from) {
	for(var i=0,j=from;i<25;i++) {
		var id = "processesline_button_"+i;
		$("processesline_button_"+id).text(""+j);
	}
}

function OutputTemplates() {}

OutputTemplates.prototype.show=function(uuid) {
    $(".outputTemplate").hide();
    $("#outputTemplate_"+uuid).toggle();
}

OutputTemplates.prototype.close=function(uuid) {
    $("#outputTemplate_"+uuid).hide();
} 

OutputTemplates.prototype.switch=function(uuid) {
    if ($("#outputTemplate_"+uuid).is(':visible')) { 
		this.close(uuid);
    } else {
		this.show(uuid);
    }
} 


OutputTemplates.prototype.select=function(templateId, processUUID) {
    var link = "lr?action=outputTemplate&templateId="+templateId+"&uuid="+processUUID;
    window.open(link);
    this.close(processUUID);
}



var outoutTemplates = new OutputTemplates();




$(document).ready(function(){
    // buttons in list
	$(".buttons>a").button();

    // page number title 
    var title = dictionary['administrator.menu.dialogs.lrprocesses.title']  + " - #"+${processView.pageLabel};
    processes.dialog.dialog('option', 'title',title);

});


//-->
</script>
<div class="header">

       <div class="buttons">  </div>


    <div class="buttons">
        <c:if test="${processView.hasPrevious}" >
            <a href="javascript:processes.modifyProcessDialogDataByPage('${processView.ordering}',${processView.firstPage},${processView.pageSize},'${processView.typeOfOrdering}');"><span class="ui-icon ui-icon-seek-prev">previous skip</span></a>

            <a title="<view:msg>administrator.processes.prev</view:msg>" href="javascript:processes.modifyProcessDialogDataByPage('${processView.ordering}',${processView.prevPageValue},${processView.pageSize},'${processView.typeOfOrdering}');"><span class="ui-icon ui-icon-arrowthick-1-w">previous</span></a>
        </c:if>

        
        &emsp;
        <c:if test="${processView.hasNext}">
            <a title="<view:msg>administrator.processes.next</view:msg>" href=" javascript:processes.modifyProcessDialogDataByPage('${processView.ordering}',${processView.nextPageValue},${processView.pageSize},'${processView.typeOfOrdering}');"><span class="ui-icon ui-icon-arrowthick-1-e">next</span></a>
            <a href="javascript:processes.modifyProcessDialogDataByPage('${processView.ordering}',${processView.lastPage},${processView.pageSize},'${processView.typeOfOrdering}');"><span class="ui-icon ui-icon-seek-next">next</span></a>
        </c:if>
        
        <a title="<view:msg>administrator.processes.refresh</view:msg>" href="javascript:_ref('${processView.ordering}',${processView.page},${processView.pageSize},'${processView.typeOfOrdering}');"><span class="ui-icon ui-icon-transferthick-e-w">refresh</span></a>
        <a href="javascript:_toggle_filter();" title="<view:msg>administrator.processes.filter</view:msg>"><span class="ui-icon ui-icon-scissors">filter</span></a>
        &nbsp;
    </div>
	
	

    <div class="buttons" style="padding-right: 20px;">
        <c:forEach items="${processView.smallSetOfDirectPages}" var="pageHref">${pageHref}</c:forEach>
		<c:if test="${processView.necessaryDisplayMorePages}">
	        <a title="<view:msg>administrator.processes.refresh</view:msg>" href="javascript:_toggle_processline();"><span class="ui-icon ui-icon-triangle-1-s">more processes</span></a>
		</c:if>
    </div>
    
</div>

<div class="processesline shadow ui-widget ui-widget-content">
    <div id="processesline_table_div" style="width:99%; overflow:auto; position:relative;">
		<table id="processesline_table" style="padding:10px; ">
			<tr>
		        <c:forEach items="${processView.largeSetOfDirectPates}" var="pageHref" varStatus="pageHrefStatus">
					<td>
						<div class="buttons">
							${pageHref}
						</div>
					</td>
		        </c:forEach>
			</tr>
		</table>
    </div>

	<hr></hr>

     <div class="apply" style="width:80px;">
        <button name="close" title='<view:msg>common.close</view:msg>' onclick="_toggle_processline();"> <view:msg>common.close</view:msg> </button>
     </div>
	
</div>

<div class="filter shadow ui-widget ui-widget-content" style="">
    
     <h3><view:msg>administrator.processes.filter.label</view:msg></h3>

    <table style="width: 100%">
        <thead>
            <tr>
                <td style="width: 10px;"></td>
                <td style="width:150px;"></td>
                <td></td>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td><span class="ui-icon ui-icon-triangle-1-e"></span></td>
                <td><label for="filter-state"><view:msg>administrator.processes.filter.state</view:msg>:</label></td>
                <td>
		         <select class="filter-vals eq" name="status"> 
		             <c:forEach var="item" items="${processView.statesForFilter}">
		                <option value="${item.val}" ${item.selected ? 'selected' : ''}>${item.name}</option> 
		            </c:forEach>                
		         </select>
                </td>
            </tr>

            <tr>
                <td><span class="ui-icon ui-icon-triangle-1-e"></span></td>
                <td><label for="filter-state"><view:msg>administrator.processes.filter.batch</view:msg>:</label></td>

                <td>
                    <select class="filter-vals eq" name="batch_status"> 
                        <c:forEach var="item" items="${processView.batchStatesForFilter}">
                           <option value="${item.val}" ${item.selected ? 'selected' : ''}>${item.name}</option> 
                       </c:forEach>                
                    </select>
                </td>
            </tr>

            <tr>
                <td><span class="ui-icon ui-icon-triangle-1-e"></span></td>
                <td><label for="filter-state"><view:msg>administrator.processes.filter.loginname</view:msg>:</label></td>
                <td><input type="text" name="loginname" class="filter-vals like" value="${processView.loginNameLike}"></input></td>
            </tr>

            <tr>
                <td><span class="ui-icon ui-icon-triangle-1-e"></span></td>
                <td><label for="filter-state"><view:msg>administrator.processes.filter.pname</view:msg>:</label></td>
                <td><input type="text" name="name" class="filter-vals like" value="${processView.nameLike}"></input></td>
            </tr>

            <tr>
                <td><span class="ui-icon ui-icon-triangle-1-e"></span></td>
                <td><label for="filter-planned-after"> <view:msg>administrator.processes.filter.plannedafter</view:msg>:</label></td>
    		    <td><input type="text" name="planned" class="filter-vals gt" id="planned-after" value="${processView.plannedAfter}"></input></td>
            </tr>

            <tr>
                <td><span class="ui-icon ui-icon-triangle-1-e"></span></td>
                <td><label for="planned"><view:msg>administrator.processes.filter.plannedbefore</view:msg>:</label></td>
                <td><input type="text" name="planned" class="filter-vals lt" id="planned-before" value="${processView.plannedBefore}"></input></td>
            </tr>

            <tr>
                <td><span class="ui-icon ui-icon-triangle-1-e"></span></td>
                <td><label for="filter-started-after"><view:msg>administrator.processes.filter.startedafter</view:msg>:</label></td>
                <td><input type="text" name="started" class="filter-vals gt" id="started-after" value="${processView.startedAfter}"></input></td>

            </tr>

            <tr>
                <td><span class="ui-icon ui-icon-triangle-1-e"></span></td>
                <td><label for="filter-started-before"><view:msg>administrator.processes.filter.startedbefore</view:msg>:</label></td>
                <td><input type="text" name="started" class="filter-vals lt" id="started-before" value="${processView.startedBefore}"></input></td>
            </tr>

            <tr>
                <td><span class="ui-icon ui-icon-triangle-1-e"></span></td>
                <td><label for="filter-started-after"><view:msg>administrator.processes.filter.finishedafter </view:msg>:</label></td>
                <td><input type="text" name="finished" class="filter-vals gt" id="finished-after" value="${processView.finishedAfter}"></input></td>
            </tr>

            <tr>
                <td><span class="ui-icon ui-icon-triangle-1-e"></span></td>
                <td><label for="planned"><view:msg>administrator.processes.filter.finishedbefore</view:msg>:</label></td>
                <td><input type="text" name="finished" class="filter-vals lt" id="finished-before" value="${processView.finishedBefore}"></input></td>
            </tr>


        </tbody>
    </table>    

    <script type="text/javascript">
    <!--
        $(function() {
                $( "#planned-after" ).datetimepicker({
                    dateFormat: 'mm/dd/yy',
                	timeFormat: 'hh:mm'  
                });
                $( "#planned-before" ).datetimepicker({
                    dateFormat: 'mm/dd/yy',
                        timeFormat: 'hh:mm'  
                });

                $( "#started-after" ).datetimepicker({
                    dateFormat: 'mm/dd/yy',
                        timeFormat: 'hh:mm'  
                });
                $( "#started-before" ).datetimepicker({
                    dateFormat: 'mm/dd/yy',
                        timeFormat: 'hh:mm'  
                });

                $( "#finished-after" ).datetimepicker({
                    dateFormat: 'mm/dd/yy',
                        timeFormat: 'hh:mm'  
                });
                $( "#finished-before" ).datetimepicker({
                    dateFormat: 'mm/dd/yy',
                        timeFormat: 'hh:mm'  
                });
                
        });

    
    //-->
    </script>    

     <div class="apply" style="width:170px;">
        <button name="apply" title='<view:msg>common.ok</view:msg>' onclick="processes.currentFilter.apply('${processView.ordering}',${processView.pageSize},'${processView.typeOfOrdering}'); "> <view:msg>common.apply</view:msg> </button>
        <button name="apply" title='<view:msg>common.apply</view:msg>' onclick="processes.currentFilter.close()"> <view:msg>common.close</view:msg> </button>
     </div>
     
 </div>



<table width="100%" style="width:100%; bottom:20px;" cellpadding="0" cellspacing="0">
    <thead style="border-bottom: dashed 1px;" >
        <tr>
            <td width="5px"><strong> </strong></td>
            <td width="20%">
                <table><tr>
                    <td><c:if test="${processView.nameOrdered}">${processView.orderingIcon}</c:if></td>
                    <td><strong>${processView.nameOrdering}</strong></td>   
                 </tr></table>
             </td>
            
            <td width="15px;">
             <strong>${processView.pidOrdering}</strong>  
            </td>
            
            <td width="5%">
                <table><tr>
                    <td><c:if test="${processView.stateOrdered}">${processView.orderingIcon}</c:if></td>
                    <td><strong>${processView.stateOrdering}</strong>  </td>
                </tr></table>
            </td>

            <td width="10%"> 
                <table><tr>
                    <td><c:if test="${processView.batchStateOrdered}">${processView.orderingIcon}</c:if></td>
                    <td><strong>${processView.batchStateOrdering} </strong>  </td>
                </tr></table>
            </td>

            <td width="20px;">
                <table><tr>
                    <td><c:if test="${processView.plannedDateOrdered}">${processView.orderingIcon}</c:if> </td>
                    <td><strong>${processView.plannedDateOrdering}</strong> </td>
                  </tr></table>
            </td>
            
            <td width="20px;"> 
                <table><tr>
                    <td><c:if test="${processView.startedDateOrdered}">${processView.orderingIcon}</c:if></td>
                    <td> <strong>${processView.dateOrdering}</strong> </td>
              </tr></table>
            </td>

              

            <td width="20px;"> 
                <table><tr>
                    <td><c:if test="${processView.finishedDateOrdered}">${processView.orderingIcon}</c:if></td>
                    <td> <strong>${processView.finishedDateOrdering}</strong> </td>
              </tr></table>
            </td>

            
            <td width="40px;">
            
              <table><tr>
                    <td><c:if test="${processView.userOrdered}">${processView.orderingIcon}</c:if></td>
                    <td> <strong>${processView.userOrdering}</strong> </td>
              </tr></table>
            
            </td>
            <td  width="10%"><strong><view:msg>administrator.processes.change</view:msg></strong></td>
            <td  width="24px"></td>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="lrProc" items="${processView.processes}" varStatus="i">
            <tr class="${(i.index mod 2 == 0) ? 'result ui-state-default': 'result '}">
                <td>${lrProc.treeIcon}</td>
                <td title="${lrProc.simpleProcessName}">${lrProc.formatedProcessName} </td>
                <td title="${lrProc.pid}">${lrProc.pid} &nbsp;</td>

                <c:choose>
                    <c:when test="${lrProc.failedState}">
                        <td title="${lrProc.processState}" style="color: red;"><strong> ${lrProc.processState}</strong>&nbsp;</td>
                    </c:when>
                    <c:when test="${lrProc.warningState}">
                        <td title="${lrProc.processState}" style="color: red;"><strong> ${lrProc.processState}</strong>&nbsp;</td>
                    </c:when>
                    <c:when test="${lrProc.runningState}">
                        <td title="${lrProc.processState}" style="color: green;"><strong> ${lrProc.processState}</strong>&nbsp;</td>
                    </c:when>
                    <c:otherwise><td title="${lrProc.processState}">${lrProc.processState}&nbsp;</td></c:otherwise>
                </c:choose>


                <c:choose>
                    <c:when test="${lrProc.failedBatchState}">
                        <td title="${lrProc.batchState}" style="color: red;"><strong> ${lrProc.batchState}</strong></td>
                    </c:when>

                    <c:when test="${lrProc.warningBatchState}">
                        <td title="${lrProc.batchState}" style="color: red;"><strong> ${lrProc.batchState}</strong></td>
                    </c:when>

                    <c:when test="${lrProc.runningdBatchState}">
                        <td title="${lrProc.batchState}" style="color: green;"><strong> ${lrProc.batchState}</strong></td>
                    </c:when>
                    <c:otherwise><td title="${lrProc.batchState}">${lrProc.batchState}</td></c:otherwise>
                </c:choose>


                <td title="${lrProc.planned}">${lrProc.planned}</td>
                <td title="${lrProc.start}">${lrProc.start}</td>
                <td title="${lrProc.finished} ( ${lrProc.duration} )">${lrProc.finished}</td>
                
                <td>${lrProc.startedBy}</td>
                <td>${lrProc.logsURLs} || ${lrProc.killURL} || ${lrProc.deleteURL} </td>

                <c:choose>
                    <c:when test="${lrProc.outputTemplatesDefined}">
                        <td id="outputTemplateCell_${lrProc.UUID}" class="buttons"> <a href="javascript:outoutTemplates.switch('${lrProc.UUID}');"><span class="ui-icon ui-icon-circle-triangle-s"></span> </a> 
                            <div style="position: relative;">
                               <div id="outputTemplate_${lrProc.UUID}" style="display: none;" class="outputTemplate shadow ui-widget ui-widget-content">
                                    <table style="width: 100%">
                                        <thead><tr><td align="center" style="text-align: center;"> <strong><view:msg>administrator.processes.templates.output.label</view:msg></strong></td> </thead>
                                        <tbody>
                                            <c:forEach items="${lrProc.outputTemplateViewObjects}" var="output" varStatus="status">
                                                <tr> <td align="center" style="text-align: center;"> <a href="javascript:outoutTemplates.select('${output.id}','${lrProc.UUID}');">${output.name}</a> </td></tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                               </div>
                            </div>
                        </td>
                    </c:when>                
                    <c:otherwise> 
                        <td>  </td>
                    </c:otherwise>
                </c:choose>            
            </tr>
            
            <c:if test="${lrProc.masterProcess}">
            <c:forEach var="childLrProc" items="${lrProc.childProcesses}" varStatus="ch">
                <tr class="${(ch.index mod 2 == 0) ? 'result r0 ': 'result r1 '} ${lrProc.UUID} subprocess">
                    <td class="t1"><strong> </strong></td>
                    <td class="t2" title="${childLrProc.simpleProcessName}">${childLrProc.formatedProcessName} </td>
                    <td title="${childLrProc.pid}">${childLrProc.pid} </td>


                   <c:choose>
                       <c:when test="${childLrProc.failedState}">
                           <td title="${childLrProc.processState}" style="color: red;"><strong> ${childLrProc.processState}</strong></td>
                       </c:when>
                       <c:when test="${childLrProc.warningState}">
                           <td title="${childLrProc.processState}" style="color: red;"><strong> ${childLrProc.processState}</strong></td>
                       </c:when>
                       <c:when test="${childLrProc.runningState}">
                           <td title="${childLrProc.processState}" style="color: green;"><strong> ${childLrProc.processState}</strong></td>
                       </c:when>
                       <c:otherwise><td title="${childLrProc.processState}">${childLrProc.processState}</td></c:otherwise>
                   </c:choose>


                <c:choose>
                    <c:when test="${childLrProc.failedBatchState}">
                        <td title="${childLrProc.batchState}" style="color: red;"><strong> ${childLrProc.batchState}</strong></td>
                    </c:when>
                    <c:when test="${childLrProc.runningdBatchState}">
                        <td title="${childLrProc.batchState}" style="color: green;"><strong> ${childLrProc.batchState}</strong></td>
                    </c:when>
                    <c:otherwise><td title="${childLrProc.batchState}">${childLrProc.batchState}</td></c:otherwise>
                </c:choose>


                    <td title="${childLrProc.start}">${childLrProc.start}</td>
                    <td title="${childLrProc.planned}">${childLrProc.planned}</td>
                    <td title="${childLrProc.finished} ( ${childLrProc.duration} )">${childLrProc.finished}</td>

                    <td>${childLrProc.startedBy}</td>
                    <td>${childLrProc.logsURLs} ||  ${childLrProc.killURL} ||  ${childLrProc.deleteURL} </td>
            
                    <c:choose>
                      <c:when test="${childLrProc.outputTemplatesDefined}">
                          <td class="buttons" id="outputTemplateCell_${childLrProc.UUID}"> <a href="javascript:_toggle_output_template('${childLrProc.UUID}');">
                            <span class="ui-icon ui-icon-circle-triangle-s"></span> </a> 
                            <div style="position: relative;">
                               <div id="outputTemplate_${childLrProc.UUID}" style="display: none; border: 1px solid black;"  class="outputTemplate shadow ui-widget ui-widget-content">
                                    <table style="width: 100%">
                                        <thead><tr><td colspan="3" align="center" style="text-align: center;"> <strong><view:msg>administrator.processes.templates.output.label</view:msg></strong></td> </thead>
                                        <tbody>
                                            <c:forEach items="${childLrProc.outputTemplateViewObjects}" var="output" varStatus="status">
                                                <tr> <td width="20px;"></td>  <td width="80%"> <a href="javascript:outoutTemplates.select('${output.id}','${lrProc.UUID}');">${output.name}</a> </td></tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                               </div>
                            </div>
                          </td>
                      </c:when>                
                      <c:otherwise> 
                          <td>  </td>
                      </c:otherwise>
                    </c:choose>

                </tr>
            </c:forEach>
                <tr class="${lrProc.UUID} subprocess"><td colspan="11" style="border-top:solid 1px rgba(0, 30, 60, 0.9);"></td></tr>
            </c:if>
        </c:forEach>
    </tbody>
</table>

</scrd:securedContent>
