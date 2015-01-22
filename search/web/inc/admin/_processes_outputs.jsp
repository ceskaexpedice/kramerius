<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN"> 
<%@ page pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd"%>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view"%>


<%@ page trimDirectiveWhitespaces="true"%>

<%@page import="com.google.inject.Injector"%>
<%@page import="cz.incad.kramerius.processes.LRProcessManager"%>
<%@page import="cz.incad.kramerius.processes.DefinitionManager"%>

<%@page import="cz.incad.Kramerius.views.ProcessesViewObject"%>

<%@page import="cz.incad.Kramerius.views.ProcessesViewObject"%>
<%@page import="cz.incad.kramerius.utils.database.Ordering"%>
<%@page import="cz.incad.kramerius.utils.database.Offset"%>


<%@ page isELIgnored="false"%>

<%
    String uuid = request.getParameter("uuid");
    Injector inj = (Injector) application.getAttribute(Injector.class.getName());
    LRProcessManager lrProcessMan = inj.getInstance(LRProcessManager.class);

    Locale locale = inj.getInstance(Locale.class);
    ResourceBundleService resBundleServ = inj.getInstance(ResourceBundleService.class);

    DefinitionManager defMan = inj.getInstance(DefinitionManager.class);
    LRProcess lrProces = lrProcessMan.getLongRunningProcess(uuid);

    ProcessLogsViewObject processLogs = new ProcessLogsViewObject(request.getParameter("stdFrom"), request.getParameter("stdErr"), request.getParameter("count"), lrProces, defMan.getLongRunningProcessDefinition(lrProces.getDefinitionId()), resBundleServ.getResourceBundle("labels",locale));

    pageContext.setAttribute("processLogs", processLogs);
    pageContext.setAttribute("labels", resBundleServ.getResourceBundle("labels", locale));

    pageContext.setAttribute("process", processLogs);
%>

<view:object name="themes" clz="cz.incad.Kramerius.views.themes.ThemeViewObject"></view:object>

<%@page import="cz.incad.Kramerius.views.ProcessLogsViewObject"%>
<%@page import="cz.incad.kramerius.processes.LRProcess"%>

<%@page import="java.util.Locale"%>
<%@page import="cz.incad.kramerius.service.ResourceBundleService"%>



<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" >
<meta http-equiv="Pragma" content="no-cache">
<meta http-equiv="Cache-Control" content="no-cache">
<meta name="description"
 content="National Library of Czech Republic digitized documents (periodical, monographs) access aplication." />
<meta name="keywords"
 content="periodical, monograph, library, National Library of Czech Republic, book, publication, kramerius" />
<meta name="AUTHOR" content="INCAD, www.incad.cz" />

<link rel="icon" href="img/favicon.ico" />
<link rel="shortcut icon" href="img/favicon.ico" type="image/x-icon" />
<link type="text/css" href="../../css/${themes.selectedTheme}/jquery-ui.custom.css" rel="stylesheet" />

<link rel="stylesheet" href="../../css/styles.css" type="text/css" />

<script src="../../js/jquery-1.5.1.min.js" type="text/javascript"></script>
<script src="../../js/jquery-ui-1.8.11.custom.min.js" language="javascript" type="text/javascript"></script>

<script type="text/javascript">
<!--
function CreatedListeners() {
    this.listeners = []; 
}
CreatedListeners.prototype.add = function(listener) {
	this.listeners.push(listener);
}
CreatedListeners.prototype.remove = function(listener) {
    this.listeners.rm(listener);
}

CreatedListeners.prototype.fire = function() {
	this.listeners.forEach(function(f) {
		f.apply(this,[]);
    }); 
}

var crListeners  = new CreatedListeners(); 

$(document).ready(function(){
    $( "#output_tabs" ).tabs({
    	show: function(event, ui) {
    		   crListeners.fire();
        }
    });
});

-->
</script>

<style type="text/css">
#processes_log>div.header {
    height: 20px;
    padding-top: 5px;
    padding-right: 10px;
}

#processes_log>div.header>div.title {
    float: left;
    padding-left: 30px;
    font-size: 1.2em;
    font-weight: bold;
}

#processes_log>div.header>div.buttons {
    float: right;
}

#processes_log>div.header>div.buttons>a {
    float: left;
}

#processes_log>div.separator {
    width: 100%;
    border-top: 2px solid rgba(0, 30, 60, 0.9);
    margin-top: 3px;
}

#processes_log span.ui-icon {
        float:left;
}

div.buttons {
        float:right;
}



</style>


</head>
 
 
 <body>
    <scrd:securedContent action="manage_lr_process" sendForbidden="true">
        <div id="output_tabs">
            <ul>
                <c:if test="${process.actionsDefined}">
                    <c:forEach items="${process.actionURLs}" var="act">
                        <li> <a href="#${act.name}">${act.i18nName}</a> </li>
                    </c:forEach>
                </c:if>
                <li> <a href="#default_tab"><view:msg>administrator.processes.logs</view:msg></a> </li>
            </ul>
                <c:if test="${process.actionsDefined}">
                    <c:forEach items="${process.actionURLs}" var="act">
                        <div id="${act.name}">
                            <jsp:include page="../../${act.url}"></jsp:include>
                        </div>
                    </c:forEach>
                </c:if>
            <div id="default_tab">
                <jsp:include page="_processes_logs.jsp"></jsp:include>
            </div>        
        </div>
    </scrd:securedContent>
 </body>