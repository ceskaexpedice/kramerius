<%--
    Download original dialog
--%>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>

<%@ page isELIgnored="false"%>

<view:object name="download" clz="cz.incad.Kramerius.views.DownloadOriginalViewObject"></view:object>

<div style="margin: 10px">

    <strong><view:msg>administrator.dialogs.menu.downloadOriginal.selectedObjects</view:msg>:</strong>

    <table style="width: 100%">

    <thead>
        <tr>
            <td><strong><view:msg>administrator.dialogs.menu.downloadOriginal.name</view:msg> </strong></td>
            <td width="50%"><strong><view:msg>administrator.dialogs.menu.downloadOriginal.type</view:msg></strong></td>
            <td><strong><view:msg>administrator.dialogs.menu.downloadOriginal.download</view:msg></strong></td>
        </tr>
    </thead>
    
    <c:forEach items="${download.downloadItems}" var="item" varStatus="i">
        <tr class="${(i.index mod 2 == 0) ? 'result r0': 'result r1'}">
            <td>${item.label}</td>
            <td><view:msg>document.type.${item.type}</view:msg> </td>
            <td>
                <c:choose>
                    <c:when test="${item.scanPresent && item.right}">
                        <a href="${item.href}"> Stahnout original </a>
                    </c:when>
                    <c:otherwise>
                        <c:if test="${!item.scanPresent && item.right}">Scan neni k dispozici </c:if>
                        <c:if test="${!item.right}">Nemate dostatecna prava</c:if>
                    </c:otherwise>
                </c:choose>
            </td>
        </tr>
    </c:forEach>
    
    </table>
    
</div>
