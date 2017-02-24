<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>

<%@ page isELIgnored="false"%>
<c:set var="quote">"</c:set>
<c:set var="escapedquote">&quot;</c:set>
<div  id="main_menu_in">
    <view:object name="buttons" clz="cz.incad.Kramerius.views.inc.MenuButtonsViewObject"></view:object>

    <c:forEach items="${buttons.languageItems}" var="langitm">
        <c:set var="escapedLink" >${fn:replace(langitm.link, quote, escapedquote)}</c:set>
        <a href="${escapedLink}">${langitm.name}</a>
    </c:forEach>
        <!-- Registrace pouze pro neprihlasene -->
        <scrd:notloggedusers>
            <view:kconfig var="showthisbutton" key="search.mainbuttons.showregistrationbutton"></view:kconfig>
            <c:if test="${showthisbutton == 'true'}">
                <a id="registerHref" href="javascript:registerUser.register();"><view:msg>registeruser.menu.title</view:msg></a>
            </c:if>
        </scrd:notloggedusers>

        <!--  show admin menu - only for logged users -->
        <scrd:loggedusers>
            <a id="adminHref" href="javascript:showAdminMenu();"><view:msg>administrator.menu</view:msg></a>
        </scrd:loggedusers>
        
        <!-- login - only for notlogged -->
        <scrd:notloggedusers>
            <a href="redirect.jsp?redirectURL=${searchFormViewObject.requestedAddress}"><view:msg>application.login</view:msg></a>
        </scrd:notloggedusers>
        
        <!-- logout - only for logged -->
        <scrd:loggedusers>
            
            <c:if test="${buttons.underShibbolethSession == false}">
                   <a href="logout.jsp?redirectURL=${searchFormViewObject.requestedAddress}"><fmt:message bundle="${lctx}">application.logout</fmt:message></a>
            </c:if>
            
            <c:if test="${buttons.underShibbolethSession == true}">
                <c:if test="${buttons.shibbLogoutEnabled}">
                     <a href="${buttons.shibbLogout}"><view:msg>application.logout</view:msg></a>
                </c:if>
           </c:if>
            
        </scrd:loggedusers>

<a href="javascript:showHelp('<c:out value="${param.language}" />');"><view:msg>application.help</view:msg>
</a>
<c:if test="${rows != 0}" ><a href="."><view:msg>application.home</view:msg></a></c:if>
</div>

<scrd:loggedusers>
    <%@include file="adminMenu.jsp" %>
</scrd:loggedusers>