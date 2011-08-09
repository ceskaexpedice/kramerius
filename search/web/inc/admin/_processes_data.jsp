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

	LRProcessOrdering lrProcOrder = LRProcessOrdering.valueOf(ordering);
	LRProcessOffset lrOffset = new LRProcessOffset(offset, size);
	ProcessesViewObject viewObj = new ProcessesViewObject(lrProcessMan, defMan, lrProcOrder,TypeOfOrdering.valueOf(type), lrOffset,LongRunningProcessServlet.lrServlet(request), bundle, loc);
	pageContext.setAttribute("processView", viewObj);
	
%>

<%@page import="cz.incad.kramerius.processes.TypeOfOrdering"%>

<%@page import="cz.incad.Kramerius.LongRunningProcessServlet"%>
<%@page import="java.util.Locale"%>
<%@page import="cz.incad.kramerius.service.ResourceBundleService"%>


<scrd:securedContent action="manage_lr_process">
<div> 
<script type="text/javascript">

function _ref(ordering, offset, size, type) {
	$('#animation').attr('src', 'img/refresh_ani.gif');
	var refreshurl = "inc/admin/_processes_data.jsp?ordering="+ordering+"&offset="+offset+"&size="+size+"&type="+type;
	$.get(refreshurl, function(sdata) {
		$('#animation').attr('src', 'img/refresh.png'); 
		$("#processes").html(sdata);
	});
}

</script>

<table width="100%">
<tr>
    <td width="80%">${processView.prevAHREF} &emsp;  ${processView.nextAHREF}</td> 
    <td style="text-align: center;">
        <a href="javascript:_ref('<%= ordering %>',<%= offset %>,<%= size %>,'<%= type %>');"> <img  border="0" id="animation" src="img/refresh.png" alt="refresh"></img> refresh   </a>
    </td>
    
</tr>
</table>
</div>
<table width="100%" style="width:100%; bottom:20px;">
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
        </c:forEach>
    </tbody>
</table>

</scrd:securedContent>
