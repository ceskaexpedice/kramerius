<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page isELIgnored="false"%>

<c:choose>
    <c:when test="${param.language != null}" >
        <fmt:setLocale value="${param.language}" />
    </c:when>
</c:choose>

<fmt:setBundle basename="labels" />
<fmt:setBundle basename="labels" var="bundleVar" />
<c:set var="fedoraHost" value="http://194.108.215.227:8080/fedora" />
<c:set var="pageUrl">
    <c:out value="${fedoraHost}" />/get/<c:out value="${param.pid}" />/IMG_FULL
</c:set>

<div>
    <b><fmt:message>Číslo stránky</fmt:message>:</b><br/>
    <dd>
        <c:out value="${param.page}" />
    </dd>
</div>
<div style="width:100%; height:500px;">
    <object width="100%" border="0" height="100%" style="border: 0px none ;" codebase="http://www.lizardtech.com/download/files/win/djvuplugin/en_US/DjVuControl_en_US.cab" classid="clsid:0e8d0700-75df-11d3-8b4a-0008c7450c4a" id="docframe" name="docframe">
        <param name="src" 
               value="<c:out value="${pageUrl}" />" />
        <param name="zoom" value="100" /> 
        <embed width="100%" height="100%"  
               src="<c:out value="${pageUrl}" />" type="image/vnd.djvu" id="docframe2" name="docframe2"/>
        If you don't see picture, your browser has no plugin to view DjVu picture files. You can install plugin from <a target="_blank" href="http://www.celartem.com/en/download/djvu.asp"><b>LizardTech</b></a>.<br/>
        <a href="<c:out value="${pageUrl}" />">File download</a><br/> <br/> <br/> 
    </object>
</div>
