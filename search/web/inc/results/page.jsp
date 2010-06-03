<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<div>
    <!-- rdf.kramerius.hasPage:"info:fedora/PID" -->
    <img src="<c:out value="${kconfig.fedoraHost}" />/get/uuid:<x:out select="./str[@name='PID']"/>/IMG_THUMB" height="72px" onerror="this.src='img/empty.gif';this.height='1px';" />
    <span>
        <a href="<c:out value="${itemUrl}" escapeXml="false" />" ><b><x:out select="./str[@name='root_title']"/></b></a>
        (<fmt:message bundle="${lctx}">page</fmt:message> 
        <fmt:message bundle="${lctx}"><x:out select="./str[@name='root_model']"/></fmt:message>)
        <br/>
        <fmt:message bundle="${lctx}">page</fmt:message>: <x:out select="./str[@name='dc.title']"/>
    </span>
    
</div>
