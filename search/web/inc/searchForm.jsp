<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>

<form name="searchForm" method="GET" action="./">
    <table class="header ui-corner-top-8" >
        <tbody><tr><td width="230px" style="cursor:pointer;" onclick="window.location.href='./'"><img src="img/logo.png" /></td>
                <td>
                    <input id="debug" name="debug" type="hidden" value="<c:out value="${param.debug}" />" />
                    <input type="text"  alt="Hledaný výraz" name="q" id="q"
                           value="<c:out value="${param.q}" />" size="50" class="searchQuery ui-corner-all" type="text">
                    &nbsp;<button class="submit" title="Vyhledat" type="submit" ></button>
                </td>
                <td><a href="javascript:toggleAdv();" title="<fmt:message>Pokročilé vyhledávání</fmt:message>"><fmt:message>Pokročilé vyhledávání</fmt:message></a>
                    <div id="advSearch" class="advSearch">
                        <table class="advancedSearch">
                            <col width="150px">
                            <tbody>
                                <tr><td colspan="2"><strong>Metadata<strong></strong></strong></td></tr>
                                <tr>
                                    <td>ISSN/ISBN</td>
                                    <td><input type="text" value="<c:out value="${param.issn}" />" size="20" name="issn"></td>
                                </tr>
                                <tr>
                                    <td>Název titulu</td>
                                    <td><input type="text" value="<c:out value="${param.title}" />" size="20" name="title"></td>
                                </tr>
                                <tr>
                                    <td>Autor</td>
                                    <td><input type="text" value="<c:out value="${param.author}" />" size="20" name="author"></td>
                                </tr>
                                <tr>
                                    <td>Rok</td>
                                    <td><input type="text" value="<c:out value="${param.rok}" />" size="10" name="rok"></td>
                                </tr>
                                <tr>
                                    <td>MDT</td>
                                    <td><input type="text" value="<c:out value="${param.udc}" />" size="20" name="udc"></td>
                                </tr>
                                <tr>
                                    <td>DDT</td>
                                    <td><input type="text" value="<c:out value="${param.ddc}" />" size="20" name="ddc"></td>
                                </tr>
                                <tr>
                                    <td>Pouze veřejné dokumenty</td>
                                    <td><input type="checkbox" value="on" name="onlyPublic"></td>
                                </tr>
                        </tbody></table>
                    </div>
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