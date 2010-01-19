<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>

<c:choose>
    <c:when test="${param.language != null}" >
        <fmt:setLocale value="${param.language}" />
    </c:when>
</c:choose>

<fmt:setBundle basename="labels" />
<fmt:setBundle basename="labels" var="bundleVar" />
<c:set var="fedoraHost" value="http://194.108.215.227:8080/fedora" />
<%--
var indexUrl = "http://194.108.215.227:8080/fedoragsearch/rest?operation=updateIndex&action=fromPid&value=" + pid +
        "&pagesCount=" + count;
--%> 
<c:url var="url" value="http://194.108.215.227:8080/fedoragsearch/rest" >
    <c:param name="operation" value="updateIndex" />
    <c:param name="action" value="fromPid" />
    <c:param name="value" value="${param.pid}" />
    <c:param name="pagesCount" value="${param.pagesCount}" />
</c:url>
<c:catch var="exceptions"> 
    <c:import url="${url}" var="xml" charEncoding="UTF-8" />
    success
</c:catch>
<c:if test="${exceptions != null}" >
    error
</c:if>
