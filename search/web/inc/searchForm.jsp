<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>

<form name="searchForm" method="GET" action="./">
    <input id="debug" name="debug" type="hidden" value="<c:out value="${param.debug}" />" />
    <input id="language" name="language" type="hidden" value="<c:out value="${param.language}" />" />
    <table>
        <tbody><tr>
                <td>
                    <input type="text"  alt="Hledaný výraz" name="q" id="q"
                       value="<c:out value="${param.q}" />" size="50" class="text" type="text"></td>
                <td><input type="submit" class="submit" title="<fmt:message>Vyhledat</fmt:message>" alt="&gt;" value="<fmt:message>Vyhledat</fmt:message>" name="submit" id="ar_submit"/>
                </td>
            </tr>
    </tbody></table>
</form>
