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

<div>
    <a href="./item.jsp?pid=<c:out value="${uuid}"/>&model=info:fedora/model:monograph"><b><x:out select="./arr[@name='dc.title']"/></b></a>
    <span class="textpole">(<fmt:message><x:out select="./str[@name='fedora.model']"/></fmt:message>)</span>
    <span id="pages_<c:out value="${uuidSimple}"/>" class="pages"><x:out select="./str[@name='pages_count']"/></span>
    <span class="textpole"><a href="javascript:reIndex('<c:out value="${uuid}"/>', '<c:out value="${uuidSimple}"/>', false);">re-index</a></span>
    <span class="textpole"><a href="javascript:reIndex('<c:out value="${uuid}"/>', '<c:out value="${uuidSimple}"/>', true);">re-index full</a></span>
    <span id="reindex_<c:out value="${uuidSimple}"/>"></span>
</div>