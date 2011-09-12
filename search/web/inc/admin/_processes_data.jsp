<%@ page pageEncoding="UTF-8" %>

<!-- Vypis procesu urceny pro javascript -->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>

<%@ page trimDirectiveWhitespaces="true"%>

<%@page import="com.google.inject.Injector"%>
<%@page import="cz.incad.kramerius.processes.LRProcessManager"%>
<%@page import="cz.incad.kramerius.processes.DefinitionManager"%>
<%@page import="cz.incad.kramerius.processes.LRProcessOrdering"%>
<%@page import="cz.incad.kramerius.processes.LRProcessOffset"%>

<%@page import="cz.incad.Kramerius.views.ProcessesViewObject"%>
<%@page import="cz.incad.kramerius.processes.LRProcessOrdering"%>
<%@page import="cz.incad.kramerius.processes.LRProcessOffset"%>

<%@ page isELIgnored="false"%>

<%

//TODO: Move to jstl
	Injector inj = (Injector)application.getAttribute(Injector.class.getName());
	LRProcessManager lrProcessMan= inj.getInstance(LRProcessManager.class);
	DefinitionManager defMan = inj.getInstance(DefinitionManager.class);
	Locale loc = inj.getInstance(Locale.class);
	ResourceBundleService bundle = inj.getInstance(ResourceBundleService.class);
	
	
	String ordering = request.getParameter("ordering");
	if ((ordering == null) || (ordering.trim().equals(""))) {
		ordering = LRProcessOrdering.PLANNED.name();
	}
	String offset = request.getParameter("offset");
	if ((offset == null) || (offset.trim().equals(""))) {
		offset = "0";
	}

	String type = request.getParameter("type");
	if ((type == null) || (type.trim().equals(""))) {
		type = "DESC";
	}

	String size = request.getParameter("size");
	if ((size == null) || (size.trim().equals(""))) {
		size = "5";
	}

	String filter = request.getParameter("filter");
	
	LRProcessOrdering lrProcOrder = LRProcessOrdering.valueOf(ordering);
	LRProcessOffset lrOffset = new LRProcessOffset(offset, size);
	ProcessesViewObject viewObj = new ProcessesViewObject(lrProcessMan, defMan, lrProcOrder,TypeOfOrdering.valueOf(type), lrOffset,LongRunningProcessServlet.lrServlet(request), bundle, loc, filter);
    
	pageContext.setAttribute("processView", viewObj);
	
%>

<%@page import="cz.incad.kramerius.processes.TypeOfOrdering"%>

<%@page import="cz.incad.Kramerius.LongRunningProcessServlet"%>
<%@page import="java.util.Locale"%>
<%@page import="cz.incad.kramerius.service.ResourceBundleService"%>


<scrd:securedContent action="manage_lr_process">
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
        border-top: 2px solid #E66C00;
        margin-top: 3px;
    }
    #processes td{
        padding-bottom:3px;
    }
    #processes tr.subprocess{
        display: none;
    }
    #processes tr.subprocess>td.t1{
        border-left:solid 1px #E66C00;
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
        right: 15px;
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
     
         
</style>
<div id="processes_list" align="center">
<script type="text/javascript">

function _ref(ordering, offset, size, type) {
    $("#processes").html('<div style="margin-top:30px;width:100%;text-align:center;"><img src="img/loading.gif" alt="loading" /></div>');
	var refreshurl = "inc/admin/_processes_data.jsp?ordering="+ordering+"&offset="+offset+"&size="+size+"&type="+type;

	$.get(refreshurl, function(sdata) {
		$("#processes").html(sdata);
	});

	/* function filter() { } */
}


function _toggle_filter() {
	$(".filter").toggle();
    $(".displayButton").toggle();
}


</script>
<div class="header">
    <div class="title">
        <c:if test="${processView.offsetValue>0}">
            <a href="javascript:processes.modifyProcessDialogData('${processView.ordering}',${processView.offsetValue-processView.pageSize},${processView.pageSize},'${processView.typeOfOrdering}');"><span class="ui-icon ui-icon-arrowthick-1-w">previous</span></a>
        </c:if>
        &emsp;
        <c:if test="${processView.hasNext}">
            <a href="javascript:processes.modifyProcessDialogData('${processView.ordering}',${processView.offsetValue+processView.pageSize},${processView.pageSize},'${processView.typeOfOrdering}');"><span class="ui-icon ui-icon-arrowthick-1-e">next</span></a>
        </c:if>

        
    </div>
    <div class="buttons">
        <a href="javascript:_ref('${processView.ordering}',${processView.offsetValue},${processView.pageSize},'${processView.typeOfOrdering}');"><span class="ui-icon ui-icon-transferthick-e-w">refresh</span></a>
    </div>
    
</div>

<div class="displayButton">
    <div class="dropdown">
        <a href="javascript:_toggle_filter();">Filtr</a>
    </div>
</div>

<div class="filter shadow" style="">
    
     <h3>Pouzity filtr:</h3>

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
                <td><label for="filter-state">Stav:</label></td>
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
                <td><label for="filter-state">Jmeno procesu obsahuje:</label></td>
                <td><input type="text" name="name" class="filter-vals like"></input></td>
            </tr>

            <tr>
                <td><span class="ui-icon ui-icon-triangle-1-e"></span></td>
                <td><label for="filter-planned-after">Naplanovano po:</label></td>
    		    <td><input type="text" name="planned" class="filter-vals gt" id="planned-after" value="${processView.plannedAfter}"></input></td>
            </tr>

            <tr>
                <td><span class="ui-icon ui-icon-triangle-1-e"></span></td>
                <td><label for="planned">Naplanovano pred:</label></td>
                <td><input type="text" name="planned" class="filter-vals lt" id="planned-before" value="${processView.plannedBefore}"></input></td>
            </tr>

            <tr>
                <td><span class="ui-icon ui-icon-triangle-1-e"></span></td>
                <td><label for="filter-started-after">Spusteno po:</label></td>
                <td><input type="text" name="started" class="filter-vals gt" id="started-after" value="${processView.startedAfter}"></input></td>
            </tr>

            <tr>
                <td><span class="ui-icon ui-icon-triangle-1-e"></span></td>
                <td><label for="filter-started-before">Spusteno pred:</label></td>
                <td><input type="text" name="started" class="filter-vals gt" id="started-before" value="${processView.startedBefore}"></input></td>
            </tr>

        </tbody>
        
    </table>    

    <script type="text/javascript">
        $(function() {
                $( "#planned-after" ).datetimepicker();
                $( "#planned-before" ).datetimepicker();

                $( "#started-after" ).datetimepicker();
                $( "#started-before" ).datetimepicker();
          });
    </script>    
    <!--
     <div>
        <span class="ui-icon ui-icon-triangle-1-e"></span>
         <label for="planned">Naplanovano pred:</label> 
         <input type="text" name="planned" class="filter-vals lt"></input>
    </div>     
     <div>
        <span class="ui-icon ui-icon-triangle-1-e"></span>
         <label for="started">Sputeno po:</label> 
         <input type="text" name="started" class="filter-vals gt"></input>
    </div>     
     <div>
        <span class="ui-icon ui-icon-triangle-1-e"></span>
         <label for="started">Sputeno pred:</label> 
         <input type="text" name="started" class="filter-vals lt"></input>
    </div>     
     <div>
        <span class="ui-icon ui-icon-triangle-1-e"></span>
         <label for="loginname">Sputeno kym:</label> 
         <input type="text" name="loginname" class="filter-vals eq"></input>
    </div>     
    -->

    <!--
     <div>
         <label for="name">Jmeno procesu obsahuje:</label> 
         <input type="text" name="name"></input>
         <label for="dateplanning">Datum spusteni:</label> 
         <input name="dateplanning" type="text"></input>
         <label for="datestarting">Datum spusteni:</label> 
         <input name="datestating" type="text"></input>
         <label for="user">Uzivatel:</label> 
         <input name="user" type="text"></input>
     </div>
-->

     <div class="apply" style="width:70px;">
        <button name="apply" title="Pouzit" onclick="processes.currentFilter.apply('${processView.ordering}',0,${processView.pageSize},'${processView.typeOfOrdering}')"> Pouzit </button>
     </div>
     
 </div>

<table width="100%" style="width:100%; bottom:20px;" cellpadding="0" cellspacing="0">
    <thead style="border-bottom: dashed 1px;background-image:url('img/bg_processheader.png');
                  background-repeat:  repeat-x;" >
        <tr>
            <td width="5px"><strong> </strong></td>
            <td width="40%"><strong>${processView.nameOrdering}</strong></td>
            <td width="5%"><strong>${processView.pidOrdering}</strong></td>
            <td width="10%"><strong>${processView.stateOrdering}</strong></td>
            <td><strong>${processView.dateOrdering}</strong></td>
            <td><strong>${processView.plannedDateOrdering}</strong></td>
            <td><strong>${processView.userOrdering}</strong></td>
            <td  width="10%"><strong>Akce</strong></td>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="lrProc" items="${processView.processes}" varStatus="i">
            <tr class="${(i.index mod 2 == 0) ? 'result r0': 'result r1'}">
                <td>${lrProc.treeIcon}</td>
                <td>${lrProc.processName} </td>
                <td>${lrProc.pid} </td>
                <td>${lrProc.processState}</td>
                <td>${lrProc.start}</td>
                <td>${lrProc.planned}</td>
                <td>${lrProc.startedBy}</td>
                <td>${lrProc.killURL}${lrProc.actionsURLs}${lrProc.deleteURL}</td>
            </tr>
            <c:if test="${lrProc.masterProcess}">
            <c:forEach var="childLrProc" items="${lrProc.childProcesses}" varStatus="ch">
                <tr class="${(ch.index mod 2 == 0) ? 'result r0 ': 'result r1 '} ${lrProc.UUID} subprocess">
                    <td class="t1"><strong> </strong></td>
                    <td class="t2">${childLrProc.processName} </td>
                    <td>${childLrProc.pid} </td>
                    <td>${childLrProc.processState}</td>
                    <td>${childLrProc.start}</td>
                    <td>${childLrProc.planned}</td>
                    <td>${childLrProc.startedBy}</td>
                    <td>${childLrProc.killURL}${childLrProc.actionsURLs}${childLrProc.deleteURL}</td>
                </tr>
            </c:forEach>
                <tr class="${lrProc.UUID} subprocess"><td colspan="8" style="border-top:solid 1px #E66C00;"></td></tr>
            </c:if>
        </c:forEach>
    </tbody>
</table>

</scrd:securedContent>
