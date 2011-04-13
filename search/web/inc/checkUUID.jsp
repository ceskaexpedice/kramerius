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
<c:url var="url" value="${kconfig.solrHost}/select/select" >
    <c:param name="q" >PID:${param.pid}</c:param>
    <c:param name="rows" value="0" />
</c:url>
<c:catch var="exceptions">
    <c:import url="${url}" var="xml" charEncoding="UTF-8" />
    <x:parse var="doc" xml="${xml}"  />
    <c:set var="numDocs" >
        <x:out select="$doc/response/result/@numFound" />
    </c:set>
    <c:if test="${numDocs==0}" >
        <c:redirect url="${kconfig.applicationURL}?error=uuid_not_found" />
    </c:if>
</c:catch>
<c:if test="${exceptions != null}">
    <c:redirect url="${kconfig.applicationURL}?error=uuid_not_found" />
</c:if>