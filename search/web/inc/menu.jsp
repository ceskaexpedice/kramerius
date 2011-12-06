<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>

<%@ page isELIgnored="false"%>
<div  id="main_menu_in">
    <view:object name="buttons" clz="cz.incad.Kramerius.views.inc.MenuButtonsViewObject"></view:object>

    <c:forEach items="${buttons.languageItems}" var="langitm">
        <a href="${langitm.link}">${langitm.name}</a>
    </c:forEach>

        <scrd:notloggedusers>
            <a id="registerHref" href="javascript:registerUser.register();">register.user</a>
        </scrd:notloggedusers>

        <!--  show admin menu - only for logged users -->
        <scrd:loggedusers>
            <a id="adminHref" href="javascript:showAdminMenu();"><fmt:message bundle="${lctx}">administrator.menu</fmt:message></a>
        </scrd:loggedusers>
        
        <!-- login - only for notlogged -->
        <scrd:notloggedusers>
            <a href="redirect.jsp?redirectURL=${searchFormViewObject.requestedAddress}"><fmt:message bundle="${lctx}">application.login</fmt:message></a>
        </scrd:notloggedusers>
        
        <!-- logout - only for logged -->
        <scrd:loggedusers>
            <c:choose>
                <c:when test="${empty buttons.shibbLogout}">
                            <a href="logout.jsp?redirectURL=${searchFormViewObject.requestedAddress}"><fmt:message bundle="${lctx}">application.logout</fmt:message></a>
                </c:when>
                <c:otherwise>
                            <a href="${buttons.shibbLogout}"><fmt:message bundle="${lctx}">application.logout</fmt:message></a>
                </c:otherwise>
            </c:choose>
        </scrd:loggedusers>

<a href="javascript:showHelp('<c:out value="${param.language}" />');"><fmt:message bundle="${lctx}">application.help</fmt:message>
</a>
<c:if test="${rows != 0}" ><a href="."><fmt:message bundle="${lctx}">application.home</fmt:message></a></c:if>
</div>

<scrd:loggedusers>
    <%@include file="adminMenu.jsp" %>
</scrd:loggedusers>