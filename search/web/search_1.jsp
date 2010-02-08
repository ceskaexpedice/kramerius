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

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="cs" lang="cs">    
    <%@ include file="inc/html_header.jsp" %>
    
    <body leftmargin="0" topmargin="0"  marginheight="0" marginwidth="0">
<%@ include file="inc/initVars.jsp" %><br/>
            1<c:out value="${fedoraSolr}" /><br/>
            a<c:out value="${kconfig}" /><br/>
            b<c:out value="${kconfig.solrHost}" />
        
</body></html>