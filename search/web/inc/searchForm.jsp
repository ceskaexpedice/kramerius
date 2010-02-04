<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>

<form name="searchForm" method="GET" action="./">
    <input id="debug" name="debug" type="hidden" value="<c:out value="${param.debug}" />" />
    <input type="text"  alt="Hledaný výraz" name="q" id="q"
                       value="<c:out value="${param.q}" />" size="50" class="text" type="text">
    <input type="submit" class="submit" title="<fmt:message>Vyhledat</fmt:message>" alt="&gt;" value="<fmt:message>Vyhledat</fmt:message>"/>
    <select name="language" id="language" onchange="setLanguage(this.value);">
        <option value="cs" <c:if test="${param.language == 'cs'}">selected="selected"</c:if> >cesky</option>
        <option value="en" <c:if test="${param.language == 'en'}">selected="selected"</c:if>  >english</option>
    </select>
</form>
