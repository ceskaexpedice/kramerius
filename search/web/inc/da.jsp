<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view"%>
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
            Injector ctxInj = (Injector) application.getAttribute(Injector.class.getName());
            KConfiguration kconfig = ctxInj.getProvider(KConfiguration.class).get();
            pageContext.setAttribute("kconfig", kconfig);
            LocalizationContext lctx = ctxInj.getProvider(LocalizationContext.class).get();
            pageContext.setAttribute("lctx", lctx);
            FedoraAccess fedoraAccess = ctxInj.getInstance(com.google.inject.Key.get(FedoraAccess.class, com.google.inject.name.Names.named("securedFedoraAccess")));
            String i18nServlet = I18NServlet.i18nServlet(request) + "?action=bundle&lang=" + lctx.getLocale().getLanguage() + "&country=" + lctx.getLocale().getCountry() + "&name=labels";
            pageContext.setAttribute("i18nServlet", i18nServlet);
%>
<%@ include file="searchParams-html.jsp" %>
<%
    XSLService xsda = (XSLService) ctxInj.getInstance(XSLService.class);
    try {
        String xsl = "da.xsl";
        if (xsda.isAvailable(xsl)) {
            String text = xsda.transform(xml, xsl, lctx.getLocale());
            out.println(text);
            return;
        }
    } catch (Exception e) {
        out.println(e);
        out.println(xml);
    }
%>
<view:kconfig var="damin" key="search.dateaxis.min" defaultValue="1000" />
<jsp:useBean id="now" class="java.util.Date" scope="request" />
<c:set var="year"><fmt:formatDate value="${now}" pattern="yyyy" /></c:set>
<view:kconfig var="damax" key="search.dateaxis.max" defaultValue="${year}" />
<c:if test="${damax=='now'}"><c:set var="damax" value="${year}" /></c:if>
<c:catch var="exceptions">
    <c:url var="facetxslurl" value="results/xsl/da.xsl" />
    <c:import url="${facetxslurl}" var="facetxsl" charEncoding="UTF-8"  />

<div id="selectDiv" class="da_select" style="display:none;" ></div>

<div id="da-inputs">
    <span style="float:left;"><fmt:message bundle="${lctx}">Od</fmt:message>:&nbsp;</span>
    <input style="float:left;" class="da_input" id="f1" size="10" type="text" value="" onkeyup="checkDoFilter(event)" />
    <span style="float:left;">&nbsp;<fmt:message bundle="${lctx}">Do</fmt:message>:&nbsp;</span>
    <input style="float:left;" class="da_input" id="f2" size="10" type="text" value="" onkeyup="checkDoFilter(event)"  /> 
<a href="javascript:doFilter();" style="float:right; width:16px;overflow:hidden;" ><span class="ui-icon ui-icon-search" title="<fmt:message bundle="${lctx}">dateaxis.use</fmt:message>" >a</span></a>
</div>
<div id="content-resizable" style="position:relative;float:none;">
<div id="content-scroll" style="float:left;" >
    <div id="select-handle-top" class="da_select_handle" ><span class="ui-icon ui-icon-triangle-2-n-s" style="z-index:2;top:-7px;position:absolute;left:50%;">handle</span></div>
    <div id="select-handle-bottom" class="da_select_handle"><span class="ui-icon ui-icon-triangle-2-n-s" style="z-index:1;top:-7px;position:absolute;left:50%;">handle</span></div>
    <div id="resizable-top" class="ui-state-active da_resizable"></div>
    <div id="resizable-bottom" class="ui-state-active da_resizable"></div>
        <div id="img_resize_bottom" class="da_resize"></div>
        <div id="img_resize_top" class="da_resize"></div>
        <div id="constraint_bottom" class="da_constraint" ></div>
        <div id="constraint_top" class="da_constraint" style="top:0px;left:0px;" ></div>
    <div class="da_container" id="da_container">
        <div id="bubbleDiv" class="da_bubble" ><div id="bubbleText" ></div></div>
    <x:transform doc="${xml}"  xslt="${facetxsl}">
        <x:param name="bundle_url" value="${i18nServlet}"/>
        <x:param name="cfgmin" value="${damin}"/>
        <x:param name="cfgmax" value="${damax}"/>
    </x:transform>
    </div>
</div>
</div><div class="clear"></div>

<script>
    
    var times = new Array();
    var sizes = new Array();
    var maximums = new Array();
    var level = 0;
    var levels = 1;
    var shortMonths= ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    var longMonths= ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];
    var shortDays= ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
    var longDays= ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
    
    //var showStaticAxis = true;
    var maxCount;
  
    $(document).ready(function(){
        if(number_of_items<2){
            $("#dadiv").remove();
            return;
        }
        dateAxisActive = true;
        var a = new Array();
        $(".da_group").each(function(){
            var id = $(this).attr("id").split("_")[2];
            a[id + "0"] = [0, id + "0", id + "9"];
        });
        times[0] = a;
        
        var a2 = new Array();
        $(".da_bar").each(function(){
            var id = $(this).attr("id").split("_")[2];
            a2[id] = [0, id, id];
        });
        times[1] = a2;
        
        $(".da_bar_container").bind("mouseover", function(){
            var l = $(this).width()+$("#content-scroll").offset().left;
            var id = $(this).attr("id").split("_")[3];
            $("#bubbleDiv").css("left", l+ "px");
            $("#bubbleDiv").css("top", ($(this).offset().top-25) + "px");
            $("#bubbleText").html(id + " (" + $(this).text() + ")");
        });
        
        $(".da_bar_container").bind("mouseout", hideBubble);
        
        $(".da_bar_container").bind("click", function(){
            var id = $(this).attr("id").split("_")[3];
            //$("#" + fromField).val(formatDate(id));
            //$("#" + toField).val(formatDate(id));
            $("#" + fromField).val("01.01."+id);
            $("#" + toField).val("31.12."+id);
            if($(this).text()=="0"){
                return;
            }
            doFilter();
        });
        
        $("#select-handle-top").draggable({
            containment: '#constraint_bottom',
            axis:'y',
            drag: selectHandleChangeTop,
            stop: setSelectContainmentBottom
        });
        $("#select-handle-bottom").draggable({
            containment: '#constraint_top',
            axis:'y',
            drag: selectHandleChangeBottom,
            stop: setSelectContainmentTop
        });
        $("#content-scroll").bind('scroll', function() {
            daScrolled();
        });
      
        //initDateAxis();
        //$("#content-resizable").css("height", (containerHeight+7) + "px");
        //daScrollToMax();

    });
    function checkDoFilter(event){
        if( event.keyCode == 13){
            doFilter();
        }
    }
</script>


</c:catch>
<c:choose>
    <c:when test="${exceptions != null}">
        <c:out value="${exceptions}" />
    </c:when>
</c:choose>