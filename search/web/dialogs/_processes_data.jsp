<%@ page pageEncoding="UTF-8" %>

<!-- Vypis procesu urceny pro javascript -->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

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
	Injector inj = (Injector)application.getAttribute(Injector.class.getName());
	LRProcessManager lrProcessMan= inj.getInstance(LRProcessManager.class);
	DefinitionManager defMan = inj.getInstance(DefinitionManager.class);
	
	String ordering = request.getParameter("ordering");
	if ((ordering == null) || (ordering.trim().equals(""))) {
		ordering = LRProcessOrdering.NAME.name();
	}
	String offset = request.getParameter("offset");
	if ((offset == null) || (offset.trim().equals(""))) {
		offset = "0";
	}

	String type = request.getParameter("type");
	if ((type == null) || (type.trim().equals(""))) {
		type = "ASC";
	}

	String size = request.getParameter("size");
	if ((size == null) || (size.trim().equals(""))) {
		size = "5";
	}

	LRProcessOrdering lrProcOrder = LRProcessOrdering.valueOf(ordering);
	LRProcessOffset lrOffset = new LRProcessOffset(offset, size);
	ProcessesViewObject viewObj = new ProcessesViewObject(lrProcessMan, defMan, lrProcOrder,TypeOfOrdering.valueOf(type), lrOffset);
	pageContext.setAttribute("processView", viewObj);
%>

<%@page import="cz.incad.kramerius.processes.TypeOfOrdering"%>
<div> 
<table width="100%">
<tr>
	<td width="80%">${processView.prevAHREF} &emsp;  ${processView.nextAHREF}</td> 
	<td style="text-align: center;"><a href="javascript:refreshProcesses('<%= ordering %>',<%= offset %>,<%= size %>,'<%= type %>');"> <img src="img/refresh.png"></img> refresh   </a></td>
</tr>
</table>
<!-- 
${processView.prevAHREF}  ${processView.nextAHREF}
 -->
</div>
<table width="100%">
	<thead style="border-bottom: dashed 1px;background-image:url('img/bg_processheader.png');
	 		      background-repeat:  repeat-x;" >
		<tr>
			<td><strong>${processView.nameOrdering}</strong></td>
			<td><strong>${processView.pidOrdering}</strong></td>
			<td><strong>${processView.stateOrdering}</strong></td>
			<td><strong>${processView.dateOrdering}</strong></td>
			<td><strong>Akce</strong></td>
		</tr>
	</thead>
	<tbody>
		<c:forEach var="lrProc" items="${processView.processes}" varStatus="i">
			<tr class="${(i.index mod 2 == 0) ? 'result r0': 'result r1'}">
				<td>${lrProc.processName} </td>
				<td>${lrProc.pid} </td>
				<td>${lrProc.processState}</td>
				<td>${lrProc.start}</td>
				<td>${lrProc.killURL}</td>
			</tr>
		</c:forEach>
	</tbody>
</table>
