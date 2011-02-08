<%@page import="cz.incad.Kramerius.views.adminmenu.AdminMenuViewObject"%>
<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ page isELIgnored="false"%>

<%
	pageContext.setAttribute("remoteUser", request.getRemoteUser());
	Injector searchFormInjector = (Injector)application.getAttribute(Injector.class.getName());
	Provider<Locale> localesProvider = searchFormInjector.getProvider(Locale.class);
	pageContext.setAttribute("lang",localesProvider.get().getLanguage());
	
	AdminMenuViewObject adminMenuViewObject = new AdminMenuViewObject();
	searchFormInjector.injectMembers(adminMenuViewObject);	
	pageContext.setAttribute("adminMenuViewObject",adminMenuViewObject);
%>


<%@page import="com.google.inject.Injector"%>
<%@page import="cz.incad.Kramerius.backend.guice.LocalesProvider"%>
<%@page import="java.util.Locale"%>
<%@page import="com.google.inject.Provider"%>
<c:choose>
    <c:when test="${empty param.q}" >
        <c:set var="qtext" ><fmt:message bundle="${lctx}">form.search</fmt:message></c:set>
        <c:set var="qclass" >searchQuery ui-corner-all</c:set>
    </c:when>
    <c:otherwise><c:set var="qtext" ><c:out value="${param.q}" /></c:set>
        <c:set var="qclass" >searchQuery ui-corner-all searching</c:set></c:otherwise>
</c:choose>
    <table class="header ui-corner-top-8" id="header">
        <tbody>
            <tr>
                <td width="230px" valign="middle" align="center"><a
                        href="."/><img alt="logo" src="img/logo.png" border="0" /></a></td>
                <td><input id="debug" name="debug" type="hidden"
                               value="<c:out value="${param.debug}" />" /> <input type="text"
                                                                       alt="" name="q" id="q"
                                                                       value="<c:out value="${qtext}" />" size="50"
                                                                       class="<c:out value="${qclass}" />" type="text" onclick="checkSearching();"> &nbsp;
                    <button class="submit" title="Vyhledat" type="submit"></button>
                <%@ include file="advancedSearch.jsp"%></td>
                <td><a href="javascript:toggleAdv();"
                           title="<fmt:message bundle="${lctx}">Pokročilé vyhledávání</fmt:message>"><fmt:message bundle="${lctx}">Pokročilé vyhledávání</fmt:message></a>
                </td>
                <td><c:if test="${rows != 0}" >:: <a href="."><fmt:message bundle="${lctx}">home</fmt:message></a>
                    </c:if>:: <a
                        href="javascript:showHelp('<c:out value="${param.language}" />');"><fmt:message bundle="${lctx}">nápověda</fmt:message></a>
                    :: <fmt:message bundle="${lctx}">odkazy</fmt:message> :: <br />
                    <%--:: <c:choose>
                        <c:when test="${lang == 'en'}">
                            <c:set var="lid" value="cs" />
                            <c:set var="lname" value="česky" />
                        </c:when>
                        <c:when test="${lang == 'cs'}">
                            <c:set var="lid" value="en" />
                            <c:set var="lname" value="english" />
                        </c:when>
                        <c:otherwise>
                            <c:set var="lid" value="en" />
                            <c:set var="lname" value="english" />
                        </c:otherwise>
                    </c:choose
                    >--%><a href="javascript:setLanguage('<c:out value="${lid}" />')"><c:out
                        value="${lname}" /></a> :: 
                    
                    <c:choose>
                        <c:when test="${remoteUser == null}">
                            <a href="redirect.jsp"><fmt:message bundle="${lctx}">application.login</fmt:message></a>
                        </c:when>
                        <c:otherwise>
                            <a href="logout.jsp"><fmt:message bundle="${lctx}">application.logout</fmt:message></a>
                        </c:otherwise>
                    </c:choose>
                    
                    <c:choose>
                        <c:when test="${remoteUser != null}">
                            :: <a id="adminHref" href="javascript:showAdminMenu();"><fmt:message bundle="${lctx}">administrator.menu</fmt:message></a>
                        </c:when>
                    </c:choose>
                </td>
            </tr>
        </tbody>
    </table>


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

<%@ include file="../dialogs/_indexer.jsp" %>