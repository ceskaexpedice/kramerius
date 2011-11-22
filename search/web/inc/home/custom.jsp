<%@page import="com.google.inject.Injector"%>
<%@page import="java.util.Locale"%>
<%@page import="com.google.inject.Provider"%>
<%@page import="cz.incad.Kramerius.backend.guice.LocalesProvider"%>
<%@page import="java.io.*, cz.incad.kramerius.service.*"  %>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<%
            Injector ctxInj = (Injector) application.getAttribute(Injector.class.getName());
            KConfiguration kconfig = ctxInj.getProvider(KConfiguration.class).get();
            pageContext.setAttribute("kconfig", kconfig);
            String[] pids = kconfig.getPropertyList("search.home.tab.custom.uuids");
            pageContext.setAttribute("pids", pids); 
%>
<c:forEach varStatus="status" var="pid" items="${pids}">
    <c:set var="pid" value="${pid}" />
    <c:url var="url" value="${kconfig.solrHost}/select" >
        <c:param name="q" value="PID:\"${pid}\"" />
        <c:param name="fl" value="root_title" />
    </c:url>
    <c:catch var="exceptions">
        <c:import url="${url}" var="xml" charEncoding="UTF-8" />
        <x:parse var="doc" xml="${xml}"  />
    </c:catch>
    <x:forEach varStatus="status" select="$doc/response/result/doc">
        <c:set var="t"><x:out select="./str[@name='root_title']"/></c:set>
        <div align="center" style="overflow:hidden; border:1px solid #eeeeee; height:100px; padding-left:10px; padding-right:10px; float:left; margin:5px;">
            <a href="i.jsp?pid=${pid}" >
                <img align="middle" vspace="2" id="img_${uuid}" src="img?uuid=${pid}&stream=IMG_THUMB&action=SCALE&scaledHeight=96" border="0"
                     title="${t}" alt="${t}" />
            </a>
        </div>
    </x:forEach>
</c:forEach>