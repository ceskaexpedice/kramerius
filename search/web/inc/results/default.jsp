<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>

    
    <div id="more_<c:out value="${uuid}"/>" 
    <c:if test="${status.count > 5}" >
        style="display:none;"
    </c:if>
    >
    <%--
    <% 
    //String imagePid = FedoraUtils.findFirstPagePid("uuid:" + uuid);
    String imagePid = "uuid:" + uuid;
    if(imagePid!=null){
        %>
    <img  src="thumb?uuid=<%=imagePid.substring(5) %>" height="75px" onerror="this.src='img/empty.gif'" />
    <%
    }
    %>
    --%>
    <x:if select="./arr[@name='dc.creator']/str">
    <x:forEach select="./arr[@name='dc.creator']/str">
        <x:out select="."/>;&#160;
    </x:forEach><br/>
    </x:if>
    <x:if select="./str[@name='datum']">
    <x:out select="./str[@name='datum']"/><br/>
    </x:if>
    <x:forEach select="./arr[@name='details']/str">
        <c:set var="s"><fmt:message><x:out select="."/></fmt:message></c:set>
        <c:out value="${fn:replace(s, '???', '')}" />&#160;
    </x:forEach>
    </div>
    <br/>