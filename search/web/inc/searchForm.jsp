<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ page isELIgnored="false"%>

<%
	pageContext.setAttribute("remoteUser", request.getRemoteUser());
%>

<form name="searchForm" method="GET" action="./">
<table class="header ui-corner-top-8" id="header">
	<tbody>
		<tr>
			<td width="230px"><a
				href="./?language=<c:out value="${param.language}" />"><img
				src="img/logo.png" border="0" /></a></td>
			<td><input id="debug" name="debug" type="hidden"
				value="<c:out value="${param.debug}" />" /> <input type="text"
				alt="Hledaný výraz" name="q" id="q"
				value="<c:out value="${param.q}" />" size="50"
				class="searchQuery ui-corner-all" type="text"> &nbsp;
			<button class="submit" title="Vyhledat" type="submit"></button>
			<%@ include file="advancedSearch.jsp"%></td>
			<td><a href="javascript:toggleAdv();"
				title="<fmt:message bundle="${lctx}">Pokročilé vyhledávání</fmt:message>"><fmt:message bundle="${lctx}">Pokročilé vyhledávání</fmt:message></a>
			</td>
			<td>:: <a href="./?language=<c:out value="${param.language}" />"><fmt:message bundle="${lctx}">home</fmt:message></a>
			:: <a
				href="javascript:showHelp('<c:out value="${param.language}" />');"><fmt:message bundle="${lctx}">nápověda</fmt:message></a>
			:: <fmt:message bundle="${lctx}">odkazy</fmt:message> :: <br />
			:: <c:choose>
				<c:when test="${sessionLang == 'en'}">
					<c:set var="lid" value="cs" />
					<c:set var="lname" value="česky" />
				</c:when>
				<c:when test="${sessionLang == 'cs'}">
					<c:set var="lid" value="en" />
					<c:set var="lname" value="english" />
				</c:when>
				<c:otherwise>
					<c:set var="lid" value="en" />
					<c:set var="lname" value="english" />
				</c:otherwise>
			</c:choose
			><a href="javascript:setLanguage('<c:out value="${lid}" />')"><c:out
				value="${lname}" /></a> :: 
				
				<c:choose>
					<c:when test="${remoteUser == null}">
						<a href="redirect.jsp"><fmt:message bundle="${lctx}">přihlášení</fmt:message></a>
					</c:when>
					<c:otherwise>
						<a href="logout.jsp"><c:out value="${remoteUser}"></c:out></a>
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
</form>

<div id="adminMenu" class="adminMenu">
    <div class="adminMenuHeader">
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
        <div align="left"> <a href="javascript:processes(); javascript:hideAdminMenu();"><fmt:message bundle="${lctx}">administrator.menu.dialogs.lrprocesses.title</fmt:message></a> </div>	
        <div align="left"> <a href="javascript:importMonographs(); javascript:hideAdminMenu();"><fmt:message bundle="${lctx}">administrator.menu.dialogs.importMonograph.title</fmt:message></a> </div>	
        <div align="left"> <a href="javascript:importPeriodicals(); javascript:hideAdminMenu();"><fmt:message bundle="${lctx}">administrator.menu.dialogs.importPeriodical.title</fmt:message></a> </div>	
        <div align="left"> <a href="javascript:showIndexerAdmin();"><fmt:message bundle="${lctx}">administrator.menu.dialogs.indexDocuments.title</fmt:message></a> </div>
	<div align="left"> <a href="javascript:enumerator(); javascript:hideAdminMenu();"><fmt:message bundle="${lctx}">administrator.menu.dialogs.enumerator.title</fmt:message></a> </div>	
	<div align="left"> <a href="javascript:replicationrights(); javascript:hideAdminMenu();"><fmt:message bundle="${lctx}">administrator.menu.dialogs.replicationRights.title</fmt:message></a> </div>	
    </div>
</div>

<%@ include file="../dialogs/_indexer.jsp" %>