<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>

<div id="<c:out value="${uuidSimple}"/>">
    <a href="./item.jsp?pid=<c:out value="${uuid}"/>&model=<x:out select="./str[@name='fedora.model']"/>"><b><x:out select="./str[@name='dc.title']"/></b></a>
    <span class="textpole">(<fmt:message><x:out select="./str[@name='fedora.model']"/></fmt:message>)</span>
    <span id="pages_<c:out value="${uuidSimple}"/>" class="pages"><x:out select="./int[@name='pages_count']"/></span>
    <br/>
<a href="javascript:browseInTree('<c:out value="${uuid}"/>', '<x:out select="./str[@name='fedora.model']"/>', '<c:out value="${uuidSimple}"/>');">
                    browse</a> 
</div>