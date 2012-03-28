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
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="cs" lang="cs">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta http-equiv="Pragma" content="no-cache" />
<meta http-equiv="Cache-Control" content="no-cache" />
<meta name="description"
	content="National Library of Czech Republic digitized documents (periodical, monographs) access aplication." />
<meta name="keywords"
	content="periodical, monograph, library, National Library of Czech Republic, book, publication, kramerius" />
<meta name="AUTHOR" content="INCAD, www.incad.cz" />

<link rel="icon" href="img/favicon.ico" />
<link rel="shortcut icon" href="img/favicon.ico" type="image/x-icon" />


<link type="text/css" href="../../css/themes/base/ui.base.css"
	rel="stylesheet" />
<link type="text/css" href="../../css/themes/base/ui.theme.css"
	rel="stylesheet" />
<link type="text/css" href="../../css/themes/base/ui.dialog.css"
	rel="stylesheet" />
<link type="text/css" href="../../css/themes/base/ui.slider.css"
	rel="stylesheet" />
<!--
    <link type="text/css" href="js/jquery.lightbox-0.5/css/jquery.lightbox-0.5.css" rel="stylesheet" />
    -->
<link rel="stylesheet" href="../../css/dateAxisV.css" type="text/css" />
<link rel="stylesheet" href="../../css/dtree.css" type="text/css" />
<link rel="StyleSheet" href="../../css/styles.css" type="text/css" />

</head>
<body>

	<table style="width: 100%;">
		<tbody>
			<tr>
				<td align="center">
				<div class="ui-tabs ui-widget-content ui-corner-all facet"
					style="width: 600px;">
				<h3>Importovaná data</h3>
				</div>
				</td>
			</tr>
			<tr>
				<td align="center">
				<div style="width: 600px">
				<table width="600px">
					<thead>
						<tr>
							<td width="20%">Data</td>
							<td>URL</td>
						</tr>
					</thead>
					<tbody style="background-color: white;">
						<tr>
							<c:forEach var="item" items="${imported.items}" varStatus="i">
								<tr class="${(i.index mod 2 == 0) ? 'result r0': 'result r1'}">
									<td width="20%">${item.data}</td>
									<td><a href="../../handle/${item.pid}" target="_blank">${item.name}</a>
									</td>
								</tr>
							</c:forEach>
						</tr>
					</tbody>
				</table>
				</div>
				</td>
			</tr>

	
			<tr>
				<td align="center">
				<div class="ui-tabs ui-widget-content ui-corner-all facet"
					style="width: 600px">
					<h3>Chyby při importu</h3>
				</div>
				</td>
			</tr>
	
			<tr>
				<td align="center">
				<div style="width: 600px;">
				<table style="width: 600px; table-layout: fixed;">
					<thead>
						<tr>
							<td width="20%">Data</td>
							<td>Chyba</td>
						</tr>
					</thead>
					<tbody>
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
				</div>
				</td>
			</tr>
		</tbody>
	</table>

</body>
</html>