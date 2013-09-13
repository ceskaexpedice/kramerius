<%@ page pageEncoding="UTF-8" %>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="java.util.Locale"%>
<%@page import="com.google.inject.Provider"%>
<%@page import="cz.incad.Kramerius.backend.guice.LocalesProvider"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%
            Injector ctxInj = (Injector) application.getAttribute(Injector.class.getName());
            LocalizationContext lctx = ctxInj.getProvider(LocalizationContext.class).get();
            pageContext.setAttribute("lctx", lctx);
            String i18nServlet = cz.incad.Kramerius.I18NServlet.i18nServlet(request) + "?action=bundle&lang=" + lctx.getLocale().getLanguage() + "&country=" + lctx.getLocale().getCountry() + "&name=labels";
            pageContext.setAttribute("i18nServlet", i18nServlet);
%>
<%@ page isELIgnored="false"%>

<view:kconfig var="solrHost" key="solrHost" />
<c:url var="url" value="${solrHost}/select" >
    <c:param name="q" >PID:"${param.pid}"</c:param>
    <c:param name="rows" value="1" />
</c:url>
<c:catch var="searchException">
    <c:import url="${url}" var="xml" charEncoding="UTF-8" />
    <x:parse var="doc" xml="${xml}"  />
    <x:if select="$doc/response/result/@numFound > 0" >
        <c:set var="root_pid"><x:out select="$doc/response/result/doc/str[@name='root_pid']" /></c:set>
        <c:set var="pid_path"></c:set>
        <x:forEach select="$doc/response/result/doc/arr[@name='pid_path']/str">
            <c:set var="pp"><x:out select="." /></c:set>
            <c:set var="parents" value="${fn:split(pp, '/')}" />
            <c:forEach items="${parents}" var="parent" varStatus="status">
                <c:if test="${status.count < fn:length(parents)}">
                    <c:if test="${!fn:contains(pid_path,parent)}">
                        <c:set var="pid_path">${pid_path}/${parent}</c:set>
                    </c:if>
                </c:if>
            </c:forEach>
            
        </x:forEach>
        <c:set var="q">${q} <x:forEach select="$doc/response/result/doc/arr[@name='keywords']/str">keywords:"<x:out select="." />" </x:forEach></c:set>
        <c:set var="q">${q} <x:forEach select="$doc/response/result/doc/arr[@name='dc.creator']/str">dc.creator:"<x:out select="." />" </x:forEach></c:set>
        <%--
        <c:set var="q">${q} <x:forEach select="$doc/response/result/doc/arr[@name='dc.creator']/str"><c:set var="c"><x:out select="." /></c:set><c:set var="c"><c:out value="${fn:replace(c,' ',' +')}" /></c:set>(${c}) </x:forEach></c:set>
        --%>
        <c:set var="q">${q} <x:forEach select="$doc/response/result/doc/arr[@name='dc.creator']/str">"<x:out select="." />"~3 </x:forEach></c:set>
        <c:set var="q" value="${fn:trim(q)}" />
        <c:choose>
            <c:when test="${q!=''}">
                <c:url var="url" value="${solrHost}/select" >
                    <c:param name="q.op">OR</c:param>
                    <c:param name="q.alt">${q} kjkjxcxcmkmk</c:param>
                    <c:param name="fq">NOT root_pid:"${root_pid}"</c:param>
                    <c:param name="rows">10</c:param>
                    <c:param name="collapse.field" value="root_pid" />
                    <c:param name="collapse.type" value="normal" />
                    <c:param name="collapse.threshold" value="1" />
                    <c:param name="defType" value="dismax" />
                    <c:param name="qf" value="keywords^1.0 dc.creator^1.3 text^0.2" />
                </c:url>
                <c:import url="${url}" var="suggestResponse" charEncoding="UTF-8" />
                <x:parse var="doc2" xml="${suggestResponse}"  />
                <x:choose>
                    <x:when select="$doc2/response/result/@numFound > 0" >
                        <c:import url="xsl/suggest.xsl" var="xsltPage" charEncoding="UTF-8"  />
                        <x:transform doc="${suggestResponse}"  xslt="${xsltPage}"  >
                            <x:param name="bundle_url" value="${i18nServlet}"/>
                        </x:transform>
                    </x:when>
                    <x:otherwise>
                        <c:if test="${param.noparents!='true'}">
                        <c:set var="parents" value="${fn:split(pid_path, '/')}" />
                        <c:set var="imp" value="true" />
                        <c:set var="l" value="${fn:length(parents)}"  />
                        <c:forEach var="i" items="${parents}" varStatus="status">
                            <c:if test="${imp}">
                                <c:import url="suggest.jsp?pid=${parents[l-status.count]}&noparents=true" var="res" charEncoding="UTF-8"  />
                                <c:if test="${res!=''}">
                                   ${res}<c:set var="imp" value="false" />
                                </c:if>
                            </c:if>
                        </c:forEach>
                        </c:if>
                    </x:otherwise>
                </x:choose>
            </c:when>
            <c:otherwise>
                <c:if test="${param.noparents!='true'}">
                <c:set var="parents" value="${fn:split(pid_path, '/')}" />
                <c:set var="imp" value="true" />
                <c:set var="l" value="${fn:length(parents)}"  />
                <c:forEach var="i" items="${parents}" varStatus="status">
                    <c:if test="${imp}">
                        <c:import url="suggest.jsp?pid=${parents[l-status.count]}&noparents=true" var="res" charEncoding="UTF-8"  />
                        <c:if test="${res!=''}">
                           ${res}<c:set var="imp" value="false" />
                        </c:if>
                    </c:if>
                </c:forEach>
                </c:if>
            </c:otherwise>
        </c:choose>
        
    </x:if>
    
</c:catch>
<c:choose>
    <c:when test="${searchException!=null}">
        <fmt:message bundle="${lctx}" key="search.error" />:
        ${searchException}
    </c:when>
</c:choose>