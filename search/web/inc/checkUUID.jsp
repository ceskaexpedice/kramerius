<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<%--
    Document   : checkUUID
    Created on : 28.3.2011, 12:55:55
    Author     : Alberto
--%>
<%@page import="com.google.inject.Injector"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%@page import="cz.incad.Kramerius.I18NServlet"%>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>
<c:url var="url" value="${kconfig.solrHost}/select" >
    <c:param name="q" >PID:"${param.pid}"</c:param>
    <c:param name="rows" value="1" />
</c:url>
<c:catch var="exceptions">
    <c:import url="${url}" var="xml" charEncoding="UTF-8" />
    <x:parse var="doc" xml="${xml}"  />
    <c:set var="numDocs" >
        <x:out select="$doc/response/result/@numFound" />
    </c:set>
    <c:if test="${numDocs==0}" >
        <c:redirect url="search.jsp?error=uuid_not_found" />
    </c:if>
    <c:set var="pid_path"><x:out select="$doc/response/result/doc/arr[@name='pid_path']/str" /></c:set>
    <c:set var="model_path"><x:out select="$doc/response/result/doc/arr[@name='model_path']/str" /></c:set>
    <c:set var="path">${model_path}</c:set>
    <c:set var="model"><x:out select="$doc/response/result/doc/str[@name='fedora.model']" /></c:set>
    <c:set var="root_pid"><x:out select="$doc/response/result/doc/str[@name='root_pid']" /></c:set>
    <%--:set var="root_model"><x:out select="$doc/response/result/doc/str[@name='root_model']" /></c:set--%>
    <c:set var="root_model"><c:out value="${fn:split(model_path, '/')[0]}" /></c:set>
    <c:set var="viewable"><x:out select="$doc/response/result/doc/bool[@name='viewable']" /></c:set>
    <c:set var="dc_creator"><x:out select="$doc/response/result/doc/arr[@name='dc.creator']/str" /></c:set>
    <c:set var="dostupnost"><x:out select="$doc/response/result/doc/str[@name='dostupnost']" /></c:set>
</c:catch>
<c:if test="${exceptions != null}">
    <%--
    <c:redirect url="${kconfig.applicationURL}?error=uuid_not_found" />
    --%>
    ${exceptions}<br/>
    ${url}<br/>
    ${xml}<br/>
</c:if>