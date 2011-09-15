<%@ page pageEncoding="UTF-8" %>

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
<%@page import="cz.incad.kramerius.processes.TypeOfOrdering"%>

<%@ page isELIgnored="false"%>

<%
	String uuid = request.getParameter("uuid");
	Injector inj = (Injector)application.getAttribute(Injector.class.getName());
	LRProcessManager lrProcessMan= inj.getInstance(LRProcessManager.class);
	
	DefinitionManager defMan = inj.getInstance(DefinitionManager.class);
	LRProcess lrProces = lrProcessMan.getLongRunningProcess(uuid);
	
	ProcessLogsViewObject processLogs = new ProcessLogsViewObject(request.getParameter("stdFrom"), request.getParameter("stdErr"),request.getParameter("count"),lrProces,defMan.getLongRunningProcessDefinition(lrProces.getDefinitionId()));
	pageContext.setAttribute("processLogs", processLogs);
%>


<%@page import="cz.incad.Kramerius.views.ProcessLogsViewObject"%>
<%@page import="cz.incad.kramerius.processes.LRProcess"%>

<c:out value="${processLogs.errOutData}"></c:out>

