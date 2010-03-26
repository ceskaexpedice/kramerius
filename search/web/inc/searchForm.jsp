<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>

<form name="searchForm" method="GET" action="./">
    <table class="header ui-corner-top-8" >
        <tbody><tr><td width="230px" style="cursor:pointer;" onclick="window.location.href='./'"><img src="img/logo_knav.png" /></td>
                <td>
                    <input id="debug" name="debug" type="hidden" value="<c:out value="${param.debug}" />" />
                    <input type="text"  alt="Hledaný výraz" name="q" id="q"
                           value="<c:out value="${param.q}" />" size="50" class="searchQuery ui-corner-all" type="text">
                    &nbsp;<button class="submit" title="Vyhledat" type="submit" ></button>
                    <%@ include file="advancedSearch.jsp" %>
                </td>
                <td><a href="javascript:toggleAdv();" title="<fmt:message>Pokročilé vyhledávání</fmt:message>"><fmt:message>Pokročilé vyhledávání</fmt:message></a>
                    
                </td>
                <td><div style="display:none;"><select name="language" id="language" onchange="setLanguage(this.value);">
                            <option value="cs" <c:if test="${param.language == 'cs'}">selected="selected"</c:if> >cesky</option>
                            <option value="en" <c:if test="${param.language == 'en'}">selected="selected"</c:if>  >english</option>
                </select></div></td><td><fmt:message>nápověda</fmt:message> :: 
                    <c:choose>
                        <c:when test="${param.language == 'en'}"><c:set var="lid" value="cs" /><c:set var="lname" value="česky"  /></c:when>
                        <c:when test="${param.language == 'cs'}"><c:set var="lid" value="en" /><c:set var="lname" value="english"  /></c:when>
                        <c:otherwise><c:set var="lid" value="en" /><c:set var="lname" value="english"  /></c:otherwise>
                </c:choose><a href="javascript:setLanguage('<c:out value="${lid}" />')"><c:out value="${lname}" /></a> :: 
                <fmt:message>přihlášení</fmt:message></td>
    </tr></tbody></table>
</form>