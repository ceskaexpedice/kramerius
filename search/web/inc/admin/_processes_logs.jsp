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
<script src="../../js/jquery-ui-1.8.11.custom.min.js"
 language="javascript" type="text/javascript"></script>

<script type="text/javascript">
<!--
var processUUID = '${process.processUUID}';
var stdFrom = 0;
var errFrom = 0;

var count = 4096;

var errSize = ${process.errorFileSize};	
var stdSize = ${process.stdFileSize};  

var errScrollFirst = false;
var stdScrollFirst = false;
var errScrollLast = false;
var stdScrollLast = false;
            
function stdEnd() {
    errScrollFirst = false;
    stdScrollFirst = false;
    errScrollLast = false;
    stdScrollLast = true;

	stdFrom = Math.max(0, stdSize - count);
    refresh();
}

function errEnd() {
    errScrollFirst = false;
    stdScrollFirst = false;
    errScrollLast = true;
    stdScrollLast = false;
 
    errFrom = Math.max(0, errSize - count);
    refresh();
}

function stdStart() {
    errScrollFirst = false;
    stdScrollFirst = true;
    errScrollLast = false;
    stdScrollLast = false;
 
    stdFrom = 0;
    refresh();
}

function errStart() {
    errScrollFirst = true;
    stdScrollFirst = false;
    errScrollLast = false;
    stdScrollLast = false;
 
    errFrom = 0;
    refresh();
}
        
function errLeft() {
    if (errFrom > count) {
	    errFrom = errFrom - count;
	    refresh();
    } else {
   	   errFrom = 0;
       refresh();
    }
}

function errRight() {
    errFrom = errFrom + count;
    refresh();
}


function stdLeft() {
    if (stdFrom > count) {
        stdFrom = stdFrom - count;
        refresh();
     } else {
         stdFrom = 0;
         refresh();
     }
}

function repairScrolls() {

    function stdOutScroll() {
        $("textarea#stdTextArea").each(function(index,textArea) {
            textArea.scrollTop = textArea.scrollHeight;
            if (stdScrollLast) {
                textArea.scrollTop = textArea.scrollHeight;
            } else if (stdScrollFirst) {
                textArea.scrollTop = 0;
            }
        });
    }

    function errOutScroll() {
        $("textarea#errTextArea").each(function(index,textArea) {
            if (errScrollLast) {
                textArea.scrollTop = textArea.scrollHeight;
            } else if (errScrollFirst) {
                textArea.scrollTop = 0;
            }
        });
    }
    stdOutScroll(); 
    errOutScroll(); 
}

function stdRight() {
     stdFrom = stdFrom + count;
     refresh();
}

crListeners.add(function() {
    stdEnd();
});

$(document).ready(function(){
    //refresh();

    
    $("#processes_log>div.header").css('width',$(window).width()-50);
    $("#stdTextArea").css('width',$(window).width()-50);
    $("#stdTextArea").css('height',($(window).height()/2)-60);		
    $("#errTextArea").css('width',$(window).width()-50);
    $("#errTextArea").css('height',($(window).height()/2)-60);		
});


function refresh() {
    
	 
	var errJson = "_processes_logs_err_json.jsp?uuid="+processUUID;   
    var errUrl ="_processes_logs_err_txts.jsp?uuid="+processUUID+"&stdErr="+errFrom;
    $.getJSON(errJson, function(jsonval) {
        errSize = jsonval.size;              
        $.get(errUrl, function(data) {
            if (data == "" ) {
                $("textarea#errTextArea").val("--none--");
            } else {
                $("textarea#errTextArea").val(data);
                $("textarea#errTextArea").each(function(index,textArea) {
                    if (errScrollLast) {
                        textArea.scrollTop = textArea.scrollHeight;
                    } else if (errScrollFirst) {
                        textArea.scrollTop = 0;
                    }
                });
            }
         });
    });
    

    
    
    var stdJson = "_processes_logs_std_json.jsp?uuid="+processUUID;   
    var stdUrl = "_processes_logs_std_txts.jsp?uuid="+processUUID+"&stdFrom="+stdFrom;
    $.getJSON(stdJson, function(jsonval) {
        stdSize = jsonval.size;
        $.get(stdUrl, function(data) {
            if (data == "" ) {
                $("textarea#stdTextArea").val("--none--");
            } else {
                $("textarea#stdTextArea").val(data);
                $("textarea#stdTextArea").each(function(index,textArea) {
                	textArea.scrollTop = textArea.scrollHeight;
                    if (stdScrollLast) {
                    	textArea.scrollTop = textArea.scrollHeight;
                    } else if (stdScrollFirst) {
                        textArea.scrollTop = 0;
                    }
                });
            }
        });
    });

}
//-->

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
 <div id="processes_log" align="center">
 <div class="header">
 <div class="title"><%=resBundleServ.getResourceBundle("labels", locale).getString("administrator.processes.logs.stdout")%>
 '${process.stdOutDirectory.absolutePath}'</div>

 <div class="buttons"><a href="javascript:stdStart();"
  title="<%=resBundleServ.getResourceBundle("labels", locale).getString("administrator.processes.logs.first")%>"><span
  class="ui-icon ui-icon-seek-prev">first</span></a> 
  
  <a href="javascript:stdLeft();"
  title="<%=resBundleServ.getResourceBundle("labels", locale).getString("administrator.processes.logs.prev")%>"><span
  class="ui-icon ui-icon-arrowthick-1-w">previous</span></a> 
  <a href="javascript:stdRight();"
  title="<%=resBundleServ.getResourceBundle("labels", locale).getString("administrator.processes.logs.next")%>"><span
  class="ui-icon ui-icon-arrowthick-1-e">next</span></a> 
  <a href="javascript:stdEnd();"
  title="<%=resBundleServ.getResourceBundle("labels", locale).getString("administrator.processes.logs.last")%>"><span
  class="ui-icon ui-icon-seek-next">last</span></a></div>
 </div>

 <div align="center"><textarea id="stdTextArea" readonly="readonly" rows="40" cols="140" >
                </textarea></div>
 <div class="separator"></div>


 <div class="header">
 <div class="title"><%=resBundleServ.getResourceBundle("labels", locale).getString("administrator.processes.logs.errout")%>
 '${process.errOutDirectory.absolutePath}'</div>


 <div class="buttons">
 
 <a href="javascript:errStart();"
  title="<%=resBundleServ.getResourceBundle("labels", locale).getString("administrator.processes.logs.first")%>"><span
  class="ui-icon ui-icon-seek-prev">first</span></a> 
  
  
 
 <a href="javascript:errLeft();"
  title="<%=resBundleServ.getResourceBundle("labels", locale).getString("administrator.processes.logs.prev")%>"><span
  class="ui-icon ui-icon-arrowthick-1-w">previous</span></a> 

<a href="javascript:errRight();"
  title="<%=resBundleServ.getResourceBundle("labels", locale).getString("administrator.processes.logs.next")%>"><span
  class="ui-icon ui-icon-arrowthick-1-e">next</span></a>

  <a href="javascript:errEnd();"
  title="<%=resBundleServ.getResourceBundle("labels", locale).getString("administrator.processes.logs.last")%>"><span
  class="ui-icon ui-icon-seek-next">last</span></a></div>
  
  </div>
 </div>
 <div></div>
 <div align="center"><textarea id="errTextArea" readonly="readonly" rows="40"  
  cols="140"> </textarea></div>
</scrd:securedContent>
</body>
</html>
