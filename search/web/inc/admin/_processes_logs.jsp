<%@ page pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
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
<%@page import="cz.incad.kramerius.processes.TypeOfOrdering"%>

<%@ page isELIgnored="false"%>

<%
	String uuid = request.getParameter("uuid");
	Injector inj = (Injector) application.getAttribute(Injector.class.getName());
	LRProcessManager lrProcessMan = inj.getInstance(LRProcessManager.class);

	Locale locale = inj.getInstance(Locale.class);
	ResourceBundleService resBundleServ = inj.getInstance(ResourceBundleService.class);
	 
	DefinitionManager defMan = inj.getInstance(DefinitionManager.class);
	LRProcess lrProces = lrProcessMan.getLongRunningProcess(uuid);

	ProcessLogsViewObject processLogs = new ProcessLogsViewObject(
			request.getParameter("stdFrom"), request
					.getParameter("stdErr"), request
					.getParameter("count"), lrProces);
	pageContext.setAttribute("processLogs", processLogs);
	pageContext.setAttribute("labels", resBundleServ.getResourceBundle("labels",locale));
	
%>


<%@page import="cz.incad.Kramerius.views.ProcessLogsViewObject"%>
<%@page import="cz.incad.kramerius.processes.LRProcess"%>

<%@page import="java.util.Locale"%>
<%@page import="cz.incad.kramerius.service.ResourceBundleService"%>

<html>

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
<link rel="stylesheet" href="../../css/styles.css" type="text/css" />
    
    <script src="../../js/jquery-1.5.1.min.js" type="text/javascript" ></script>
    <script src="../../js/jquery-ui-1.8.11.custom.min.js" language="javascript" type="text/javascript"></script>
<script src="../js/jquery.cookie.js" type="text/javascript"></script> 
<script src="../js/jquery.history.js" type="text/javascript"></script> 

<script type="text/javascript">
	var processUUID = '<%= uuid %>';
	var stdFrom = 0;
	var errFrom = 0;
	var count = 20;	

	function errLeft() {
		if (errFrom > 20) {
			errFrom = errFrom - 40;
			refresh();
		} else {
			errFrom = 0;
			refresh();
		}
	}
	function errRight() {
		errFrom = errFrom + 40;
		refresh();
	}

	function stdLeft() {
		if (stdFrom > 20) {
			stdFrom = stdFrom - 40;
			refresh();
		} else {
			stdFrom = 0;
			refresh();
		}

	}
	function stdRight() {
		stdFrom = stdFrom + 40;
		refresh();
	
	}
	
	$(document).ready(function(){
		refresh();
		$("#stdOutHeader").css('width',$(window).width()-50);
		$("#errOutHeader").css('width',$(window).width()-50);
		$("#stdTextArea").css('width',$(window).width()-50);
		$("#stdTextArea").css('height',($(window).height()/2)-60);		
		$("#errTextArea").css('width',$(window).width()-50);
		$("#errTextArea").css('height',($(window).height()/2)-60);		
		
	});

	function refresh() {
		var errUrl = "_processes_logs_err_txts.jsp?uuid="+processUUID+"&stdErr="+errFrom;
		$.get(errUrl, function(data) {
			if (data == "" ) {
				$("textarea#errTextArea").val("--none--");
			} else {
				$("textarea#errTextArea").val(data);
			}
		});

		var stdUrl = "_processes_logs_std_txts.jsp?uuid="+processUUID+"&stdFrom="+stdFrom;
		$.get(stdUrl, function(data) {
			if (data == "" ) {
				$("textarea#stdTextArea").val("--none--");
			} else {
				$("textarea#stdTextArea").val(data);
			}
		});
	}
</script>	
</head>
<body>

<scrd:securedContent action="manage_lr_process">

<table width="100%">
	<tbody>
		<tr>
			<td align="center">

				<div id="stdOutHeader" class="ui-tabs ui-widget-content ui-corner-all facet">
						<table width="100%">
							<tr>
								<td width="80%" style="padding-left: 15px;">
								<h3><%=resBundleServ.getResourceBundle("labels",locale).getString("administrator.processes.logs.stdout") %> </h3>
								</td>
								<td align="right"><a href="javascript:stdLeft();"><img border="0" style="top: 3px; position: relative;"
									src="../img/prev_arr.png" alt="prev" />
                                                                        <span><%=resBundleServ.getResourceBundle("labels",locale).getString("administrator.processes.logs.prev") %></span></a>
                                                                        &emsp;
                                                                        <a href="javascript:stdRight();"><span><%=resBundleServ.getResourceBundle("labels",locale).getString("administrator.processes.logs.next") %></span>
								<img border="0" src="../img/next_arr.png" style="top: 3px; position: relative;" alt="next" /> </a></td>
							</tr>
						</table>
				</div>

			</td>
		</tr>

		<tr>
			<td align="center">
			<div><textarea id="stdTextArea">
				</textarea>
			</div>
			</td>
		</tr>

		<tr align="center">
			<td>
				<div id="errOutHeader" class="ui-tabs ui-widget-content ui-corner-all facet">
						<table width="100%">
							<tr>
								<td width="80%" style="padding-left: 15px;">
								<h3><%=resBundleServ.getResourceBundle("labels",locale).getString("administrator.processes.logs.errout") %> </h3>
								</td>
								<td align="right"><a href="javascript:errLeft();">
                                                                        <img border="0" src="../img/prev_arr.png"  style="top: 3px; position: relative;" alt="prev"> <span><%=resBundleServ.getResourceBundle("labels",locale).getString("administrator.processes.logs.prev") %></span></a> &emsp;
                                                                        <a href="javascript:errRight();"><span><%=resBundleServ.getResourceBundle("labels",locale).getString("administrator.processes.logs.next") %></span>
								<img border="0" src="../img/next_arr.png"  style="top: 3px; position: relative;" alt="next"> </a></td>
							</tr>
						</table>
				</div>
			</td>
		</tr>

		<tr>
			<td align="center">
				<div>
					<textarea id="errTextArea" rows="40" cols="140"> </textarea>
				</div>
			</td>
		</tr>
	</tbody>
</table>
</scrd:securedContent>

</body>
</html>