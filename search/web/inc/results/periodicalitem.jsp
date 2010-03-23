<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<x:out select="./str[@name='dc.title']"/><br/>
<x:forEach select="./arr[@name='details']/str">
    <c:set var="s"><fmt:message><x:out select="."/></fmt:message></c:set>
    <c:out value="${fn:replace(s, '???', '')}" />&#160;
</x:forEach>
