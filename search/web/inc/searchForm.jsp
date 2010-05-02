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
				src="img/logo_knav.png" border="0" /></a></td>
			<td><input id="debug" name="debug" type="hidden"
				value="<c:out value="${param.debug}" />" /> <input type="text"
				alt="Hledaný výraz" name="q" id="q"
				value="<c:out value="${param.q}" />" size="50"
				class="searchQuery ui-corner-all" type="text"> &nbsp;
			<button class="submit" title="Vyhledat" type="submit"></button>
			<%@ include file="advancedSearch.jsp"%></td>
			<td><a href="javascript:toggleAdv();"
				title="<fmt:message>Pokročilé vyhledávání</fmt:message>"><fmt:message>Pokročilé vyhledávání</fmt:message></a>
			</td>
			<td>:: <a href="./?language=<c:out value="${param.language}" />"><fmt:message>home</fmt:message></a>
			:: <a
				href="javascript:showHelp('<c:out value="${param.language}" />');"><fmt:message>nápověda</fmt:message></a>
			:: <fmt:message>odkazy</fmt:message> :: <br />
			:: <c:choose>
				<c:when test="${param.language == 'en'}">
					<c:set var="lid" value="cs" />
					<c:set var="lname" value="česky" />
				</c:when>
				<c:when test="${param.language == 'cs'}">
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
						<a href="redirect.jsp" /><fmt:message>přihlášení</fmt:message></a>
					</c:when>
					<c:otherwise>
						<a href="logout.jsp" /><c:out value="${remoteUser}"></c:out></a>
					</c:otherwise>
				</c:choose>

				<c:choose>
					<c:when test="${remoteUser != null}">
						:: <a id="adminHref" href="javascript:showAdminMenu();" /><fmt:message>administrator.menu</fmt:message></a>
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
			<td align="center">
				<fmt:message>administrator.menu</fmt:message>
			</td>
			<td width="20px">
				<a href="javascript:hideAdminMenu();"><img border="0px" src="img/x.png"></img></a>
			</td>
		</tr>
	</table>
	</div>
	
	<div id="adminMenuItems" class="adminMenuItems">
		<div align="left"> <a href="javascript:processes(); javascript:hideAdminMenu();">Správa dlouhotrvajících procesů ... </a> </div>	
		<div align="left"> <a href="javascript:processes(); javascript:hideAdminMenu();">Import dat ... </a> </div>	
	</div>
</div>

