<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ page isELIgnored="false"%>
<div>
<c:if test="${rows != 0}" ><a href="."><fmt:message bundle="${lctx}">home</fmt:message></a></c:if><a
    href="javascript:showHelp('<c:out value="${param.language}" />');"><fmt:message bundle="${lctx}">nápověda</fmt:message>
</a><c:choose>
    <c:when test="${remoteUser == null}">
        <a href="redirect.jsp?redirectURL=${searchFormViewObject.requestedAddress}"><fmt:message bundle="${lctx}">application.login</fmt:message></a>
    </c:when>
    <c:otherwise>
        <a href="logout.jsp?redirectURL=${searchFormViewObject.requestedAddress}"><fmt:message bundle="${lctx}">application.logout</fmt:message></a>
    </c:otherwise>
</c:choose>

<c:choose>
    <c:when test="${remoteUser != null}">
        :: <a id="adminHref" href="javascript:showAdminMenu();"><fmt:message bundle="${lctx}">administrator.menu</fmt:message></a>
    </c:when>
</c:choose>
</div>
