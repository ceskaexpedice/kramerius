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
<table id="homecontent"><tr><td valign="top">
<div id="homedabox" style="float:left;width:239px;margin-left:4px;">
    <ul><li><a href="#dadiv"><fmt:message bundle="${lctx}" key="Časová osa" /></a></li></ul>
    <div id="dadiv" style="padding:3px;">
        <p style="text-align: center;">
            <img src="img/loading.gif" alt="loading date axis" /><br/>Time line loading...
        </p>
    </div>
</div>
</td><td style="width:913px;" valign="top"> 
<div style="float:left;width:908px;">
<div id="dt_home">
    <c:url var="dtxslurl" value="inc/home/dt.xsl" />
    <c:import url="${dtxslurl}" var="facetxsl" charEncoding="UTF-8"  />
    <x:transform doc="${xml}"  xslt="${facetxsl}">
        <x:param name="bundle_url" value="${i18nServlet}"/>
        <x:param name="dts" value="${dts}"/>
    </x:transform>
</div>
<%@ include file="home/tabs.jsp" %>
</div>
</td></tr></table>
<script type="text/javascript">
    $(window).resize(function(event){
        resizeAll();
    });
    $(document).ready(function(){
        setTimeout(resizeAll, 1000);
        $.get("inc/da.jsp", function(data){
            $("#dadiv").html(data);
            initDateAxis();
            //$("#content-resizable").css("height", "200px");
            daScrollToMax();
            if($("#content-resizable").length>0){
                resizeDateAxisContent();
                $("#intro>div.ui-tabs-panel").css("height", $("#dadiv").height());
            }
            //setTimeout(resizeAll, 2000);
        });
        
    });
    function resizeAll(){
        //return;
        var w = $(window).height() -
            $("#main").height() - 
            $("#footer").outerHeight(true);
        //w = w + $("#intro>div.ui-tabs-panel:first").height();
        w = $(window).height() - $("#header").outerHeight(true) - $("#intro>ul.ui-tabs-nav").outerHeight(true);
        
        $("#intro>div.ui-tabs-panel").css("height", w);
        var wmax = w;
        wmax = Math.max(wmax, $(this).height());
        $("#intro>div.ui-tabs-panel>div").each(function(){
            wmax = Math.max(wmax, $(this).height());
        });
        //wmax = Math.max(wmax, $("#homecontent>tbody>tr>td").height() - $("#intro>ul.ui-tabs-nav").outerHeight(true));
        $("#intro>div.ui-tabs-panel").css("height", wmax);
        if($("#content-resizable").length>0){
            //w = w -35;
            //$("#content-resizable").css("height", w);
            resizeDateAxisContent();
            wmax = Math.max(wmax, $("#dadiv").height());
            $("#intro>div.ui-tabs-panel").css("height", $("#dadiv").height());
        }
        $("#facets ul.facet").css("height", "38%");
    }
    function addTypeFilter(value){
        var page = new PageQuery(window.location.search);
        page.setValue("offset", "0");
        page.setValue("forProfile", "facet");
                
        var f = "fq=model_path:" + value + "*";
        if(window.location.search.indexOf(f)==-1){
            window.location = "r.jsp?" +
            page.toString() + "&" + f;
        }
    }
</script>

