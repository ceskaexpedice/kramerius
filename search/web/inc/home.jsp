<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="java.util.Locale"%>
<%@page import="com.google.inject.Provider"%>
<%@page import="cz.incad.Kramerius.backend.guice.LocalesProvider"%>
<%@page import="java.io.*, cz.incad.kramerius.service.*"  %>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%@page import="cz.incad.kramerius.FedoraAccess"%>
<%
    String[] dtsa = kconfig.getPropertyList("search.home.document.types");
    String dts = "";
    for(String dt : dtsa){
        dts += "[" + dt + "]";
    }
    pageContext.setAttribute("dts", dts);
%>
<table style="width: 990px;"><tr><td valign="top">
<div id="homedabox" style="float:left;width:239px;margin-left:4px;padding:3px;">
    <ul><li><a href="#dadiv"><fmt:message bundle="${lctx}" key="Časová osa" /></a></li></ul>
    <div id="dadiv" style="overflow:hidden; width:100%; height:300px;position: relative;padding:0;">
        <%@ include file="dac.jsp" %>
    </div>
</div>
</td><td style="width:745px;"> 
<div style="float:left;width:740px;">

<%@ include file="home/tabs.jsp" %>
</div>
</td></tr></table>
<script type="text/javascript">
    $(window).resize(function(event){
        resizeAll();
    });
    $(document).ready(function(){
        setTimeout(resizeAll, 1000);
        $("#dadiv").bind("yearChanged", function(event, params){
            daYearClicked(params);
        })
        
    });
    function resizeAll(){
        var w = $("#intro>div.ui-tabs-panel:first").height() +
            $(window).height() -
            $("#main").height() - 
            $("#footer").outerHeight(true);
        $("#intro>div.ui-tabs-panel").css("height", w);
        $("#homedabox>div.ui-tabs-panel").css("height", w);
    }
</script>

