<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<%@ page import="java.util.*, cz.incad.Kramerius.*, cz.incad.Solr.*" %>

<c:choose>
    <c:when test="${param.language != null}" >
        <fmt:setLocale value="${param.language}" />
    </c:when>
</c:choose>

<div id="res_<c:out value="${uuid}"/>">
    <img src="img/empty.gif" 
    <c:if test="${status.count > 5}" >
    class="plus" onclick="$('#more_<c:out value="${uuid}"/>').toggle();$(this).toggleClass('minus')" 
    </c:if>
    />
    <a href="<c:out value="${itemUrl}" escapeXml="false" />" ><b><x:out select="./str[@name='dc.title']"/></b></a>
    <span class="textpole">(<fmt:message bundle="${lctx}">monograph</fmt:message>)</span>
    <span id="pages_<c:out value="${uuid}"/>" class="pages"><x:out select="./int[@name='pages_count']"/></span>
    <div id="more_<c:out value="${uuid}"/>" 
    <c:if test="${status.count > 5}" >
        style="display:none;"
    </c:if>
    ><% 
    String imagePid = FedoraUtils.findFirstPagePid("uuid:" + uuid);
    if(imagePid!=null){
        %>
    <img  src="thumb?uuid=<%=imagePid.substring(5) %>&scaledHeight=100&rawdata=true" onerror="this.src='img/empty.gif'" />
    <%
    }
    %>
    <x:forEach select="./arr[@name='dc.creator']/str">
        <x:out select="."/>;&#160;
    </x:forEach>
    <br/><x:out select="./str[@name='datum']"/>
    </div>
    <br/>
    
</div>