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
            request.getParameter("stdFrom"), request.getParameter("stdErr"), request.getParameter("count"), lrProces);
    pageContext.setAttribute("processLogs", processLogs);
    pageContext.setAttribute("labels", resBundleServ.getResourceBundle("labels", locale));

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
        <link type="text/css" href="../../css/smoothness/jquery-ui-1.8.11.custom.css" rel="stylesheet" />
        <link rel="stylesheet" href="../../css/styles.css" type="text/css" />

        <script src="../../js/jquery-1.5.1.min.js" type="text/javascript" ></script>
        <script src="../../js/jquery-ui-1.8.11.custom.min.js" language="javascript" type="text/javascript"></script>

        <script type="text/javascript">
            var processUUID = '<%= uuid%>';
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
		$("#processes_log>div.header").css('width',$(window).width()-50);
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
        
        <style type="text/css">
    #processes_log>div.header{
        height:20px;
        padding-top:5px;
        padding-right:10px;
    }
    #processes_log>div.header>div.title{
        float:left;
        padding-left: 30px;
        font-size: 1.2em;
        font-weight: bold;
    }
    #processes_log>div.header>div.buttons{
        float:right;
        
    }
    #processes_log>div.header>div.buttons>a{
        float:left;
    }
    #processes_log>div.separator{
        width:100%;
        border-top: 2px solid #E66C00;
        margin-top: 3px;
    }
     
         
</style>
    </head>
    <body>
    <scrd:securedContent action="manage_lr_process">
        <div id="processes_log" align="center">
            <div class="header">
                <div class="title"><%=resBundleServ.getResourceBundle("labels", locale).getString("administrator.processes.logs.stdout")%></div>
                <div class="buttons" >
                <a href="javascript:stdLeft();" title="<%=resBundleServ.getResourceBundle("labels", locale).getString("administrator.processes.logs.prev")%>"><span class="ui-icon ui-icon-arrowthick-1-w">previous</span></a>
                <a href="javascript:stdRight();" title="<%=resBundleServ.getResourceBundle("labels", locale).getString("administrator.processes.logs.next")%>"><span class="ui-icon ui-icon-arrowthick-1-e">next</span></a>
                </div>
            </div>
            <div align="center"><textarea id="stdTextArea">
                </textarea>
            </div>
            <div class="separator"></div>
            <div class="header">
                <div class="title"><%=resBundleServ.getResourceBundle("labels", locale).getString("administrator.processes.logs.errout")%></div>
                <div class="buttons" >
                <a href="javascript:errLeft();" title="<%=resBundleServ.getResourceBundle("labels", locale).getString("administrator.processes.logs.prev")%>"><span class="ui-icon ui-icon-arrowthick-1-w">previous</span></a>
                <a href="javascript:errRight();" title="<%=resBundleServ.getResourceBundle("labels", locale).getString("administrator.processes.logs.next")%>"><span class="ui-icon ui-icon-arrowthick-1-e">next</span></a>
                </div>
            </div>
            <div align="center">
                <textarea id="errTextArea" rows="40" cols="140"> </textarea>
            </div>
        </div>
    </scrd:securedContent>
    </body>
</html>