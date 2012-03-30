<%@ page pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

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
	Injector inj = (Injector) application.getAttribute(Injector.class
			.getName());
	LRProcessManager lrProcessMan = inj
			.getInstance(LRProcessManager.class);

	DefinitionManager defMan = inj.getInstance(DefinitionManager.class);
	LRProcess lrProces = lrProcessMan.getLongRunningProcess(uuid);

	ImportsViewObject impView = new ImportsViewObject(lrProces);
	pageContext.setAttribute("imported", impView);
%>

<%@page import="cz.incad.Kramerius.views.ProcessLogsViewObject"%>
<%@page import="cz.incad.kramerius.processes.LRProcess"%>
<%@page import="cz.incad.Kramerius.processes.imported.views.ImportsViewObject"%>

	<table style="width: 100%;">
		<tbody>
			<tr>
				<td style="width:20%">
                       <h3>Importovaná data</h3>
				</td>
                <td></td>
			</tr>
        
			<tr>
                 <td width="20%"><strong>Data</strong></td>
                 <td><strong>URL</strong></td>
    
			</tr>

             <tr>
                 <c:forEach var="item" items="${imported.items}" varStatus="i">
                     <tr class="${(i.index mod 2 == 0) ? 'result r0': 'result r1'}">
                         <td width="20%">${item.data}</td>
                         <td><a href="../../handle/${item.pid}" target="_blank">${item.name}</a>
                         </td>
                     </tr>
                 </c:forEach>
             </tr>


            <tr><td colspan="2"><hr/></td></tr>	
 
            <tr>
                 <td width="20%"><h3>Chyby při importu</h3></td>
                 <td></td>
            </tr>

            <tr>
                      <td width="20%"><strong>Data</strong></td>
                      <td><strong>Chyba</strong></td>
            </tr>

             <tr>
                 <c:forEach var="item" items="${imported.fails}" varStatus="i">
                     <tr class="${(i.index mod 2 == 0) ? 'result r0': 'result r1'}">
                         <td width="20%">${item.name}</td>
                         <td id="import_excp_id_${i.index}">
                         ${item.exception}</td>
                     </tr>
                 </c:forEach>
             </tr>

		</tbody>
	</table>

