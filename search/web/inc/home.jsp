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
        $("#dadiv").bind("yearChanged", function(event, params){
            daYearClicked(params);
        })
        
    });
    function resizeAll(){
        //return;
        var w = $(window).height() -
            $("#main").height() - 
            $("#footer").outerHeight(true);
        
        w = $(window).height() 
                - $("#header").outerHeight(true) 
                - $("#footer").outerHeight(true) 
                - $("#intro>ul.ui-tabs-nav").outerHeight(true) - 30;
        
        $("#intro>div.ui-tabs-panel").css("height", w);
        $("#homedabox>div.ui-tabs-panel").css("height", w);
        var wmax = w;
        $("#facets ul.facet").parent().css("height", "120px");
        $("div.ui-tabs-panel>div").each(function(){
            wmax = Math.max(wmax, $(this).height());
        });
        $("div.ui-tabs-panel").css("height", wmax);
        
        
        $("#facets ul.facet").parent().css("height", wmax/2 - 32);
        da.resize();
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

        var page = new PageQuery(window.location.search);
        page.setValue("offset", "0");
        page.setValue("forProfile", "dateaxis");
        //page.setValue(fromField, decodeDate($("#" + fromField).val()));
        //page.setValue(toField, decodeDate($("#" + toField).val()));

        page.setValue("da_od", decodeDate($("#" + fromField).val()));
        page.setValue("da_do", decodeDate($("#" + toField).val()));
        var newurl = "r.jsp?" + page.toString() + dateAxisAdditionalParams;

        document.location.href = newurl;

    }
</script>

