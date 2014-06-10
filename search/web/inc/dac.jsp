<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view"%>
<%@ page isELIgnored="false"%>
<%@page import="cz.incad.kramerius.utils.FedoraUtils"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%@page import="cz.incad.Kramerius.I18NServlet"%>
<%@page import="cz.incad.kramerius.service.*"%>
<%
	Injector ctxInj = (Injector)application.getAttribute(Injector.class.getName());
        KConfiguration kconfig = ctxInj.getProvider(KConfiguration.class).get();
        pageContext.setAttribute("kconfig", kconfig);
            LocalizationContext lctx = ctxInj.getProvider(LocalizationContext.class).get();
            pageContext.setAttribute("lctx", lctx);
            String i18nServlet = I18NServlet.i18nServlet(request) + "?action=bundle&lang="+lctx.getLocale().getLanguage()+"&country="+lctx.getLocale().getCountry()+"&name=labels";
            pageContext.setAttribute("i18nServlet", i18nServlet);
%>
<c:set var="wt" scope="request">json</c:set>
<%@ include file="searchParams-html.jsp" %>
<script src="js/underscore-min.js" type="text/javascript" ></script>
<script src="js/utils.js" type="text/javascript" ></script>
<script src="js/jcanvas.js" type="text/javascript" ></script>
<script src="js/canvasDa.js" type="text/javascript" ></script>
<script type="text/javascript">
    var ja = ${xml};
    var da;
    $(document).ready(function(){ 
        da = new Da('#canvasda', ja.facet_counts.facet_fields.rok, null);
        $(window).resize(function() {
        });
        da.render();
        da.setDatePicker();
        da.scrollToMax();
    });
</script>
<style>
    #canvasda{
        width:100%; 
        height:100%;
        background-color: #f3f3f3;
    }
    #canvasda .info{
        font: 12px Verdana;
        font-family: 'Verdana', sans-serif;
        font-weight: 500;
        color: #ffff00;
        background-color: rgba(23,45,89,1.0);
    }
    #canvasda .yearLabel{
        font: 12px Verdana;
        font-family: 'Verdana', sans-serif;
        font-weight: 500;
        color: rgba(23,45,89,0.8);
        background-color: rgba(23,45,89,0.3);
    }
    #canvasda .bar{
        background-color: #cccccc;
        height: 4px;
        left:-1000px;
        position:absolute;
    }
    #canvasda .bar .sel{
        background-color: #234589;
    }
</style>
<div id="da-inputs">
    <span style="float:left;"><fmt:message bundle="${lctx}">Od</fmt:message>:&nbsp;</span>
    <input style="float:left;" class="da_input" id="f1" size="10" type="text" value="" onkeyup="checkDoFilter(event)" />
    <span style="float:left;">&nbsp;<fmt:message bundle="${lctx}">Do</fmt:message>:&nbsp;</span>
    <input style="float:left;" class="da_input" id="f2" size="10" type="text" value="" onkeyup="checkDoFilter(event)"  /> 
<a href="javascript:doFilter();" style="float:right; width:16px;overflow:hidden;" ><span class="ui-icon ui-icon-search" title="<fmt:message bundle="${lctx}">dateaxis.use</fmt:message>" >a</span></a>
</div>
<div style="overflow:hidden; width:100%; height:100%;position: relative;left:0px;top:0px;padding:0px;">
<div id="canvasda" class="years" style="overflow:auto; width:100%; height:100%; position: relative;top:0; left:0;">
<div class="info"></div><div class="yearLabel"></div><div class="bar"><div class="sel"></div></div>
<canvas width="100" height="130" style="position: relative;"></canvas>
</div>
</div>