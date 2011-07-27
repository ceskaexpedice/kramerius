<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ page isELIgnored="false"%>
<c:if test="${rows != 0}" >:: <a href="."><fmt:message bundle="${lctx}">home</fmt:message></a>
</c:if>:: <a
    href="javascript:showHelp('<c:out value="${param.language}" />');"><fmt:message bundle="${lctx}">nápověda</fmt:message></a>
:: <fmt:message bundle="${lctx}">odkazy</fmt:message> :: <br />
<a href="javascript:setLanguage('<c:out value="${lid}" />')"><c:out
        value="${lname}" /></a> ::

<c:choose>
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
        <c:if test="${!empty pageContext.request.remoteUser}" >
    <div id="adminMenu" class="adminMenu ui-tabs ui-widget ui-corner-all facet shadow10">
        <div class="ui-state-default ui-corner-top ui-tabs-selected ui-state-active adminMenuHeader">
            <table width="100%">
                <tr>
                    <td align="center"><fmt:message bundle="${lctx}">administrator.menu</fmt:message></td>
                    <td width="20px">
                        <a href="javascript:hideAdminMenu();" class="ui-dialog-titlebar-close ui-corner-all" role="button" unselectable="on" style="-moz-user-select: none;"><span class="ui-icon ui-icon-closethick" unselectable="on" style="-moz-user-select: none;">close</span></a>
                    </td>
                </tr>
            </table>
        </div>
        <div id="adminMenuItems" class="adminMenuItems">
            <c:forEach var="item" items="${adminMenuViewObject.adminMenuItems}">
                ${item}
            </c:forEach>
        </div>
    </div>
    </c:if>