<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div id="advSearch" class="shadow ui-widget ui-widget-content" style="display:none;z-index:110">
    <table class="advancedSearch">
        <col width="150px">
        <tbody>
            <tr><td colspan="2"><strong>Metadata</strong></td></tr>
            <tr>
                <td><fmt:message bundle="${lctx}" key="filter.query.isbnissn" /></td>
                <td><input type="text" value="<c:out value="${param.issn}" />" size="20" name="issn" id="issn"></td>
            </tr>
            <tr>
                <td><fmt:message bundle="${lctx}" key="filter.query.title" /></td>
                <td><input type="text" value="<c:out value="${param.title}" />" size="20" name="title" id="title"></td>
            </tr>
            <tr>
        	<td><fmt:message bundle="${lctx}" key="filter.query.keywords"/></td>
        	<td><input type="text" value="<c:out value="${param.keywords}" />" size="20" name="keywords" id="keywords"></td>
    	    </tr>
            <tr>
                <td><fmt:message bundle="${lctx}" key="filter.query.author" /></td>
                <td><input type="text" value="<c:out value="${param.author}" />" size="20" name="author" id="author"></td>
            </tr>
            <tr>
                <td><fmt:message bundle="${lctx}" key="filter.query.year" /></td>
                <td><input type="text" value="<c:out value="${param.rok}" />" size="10" name="rok" id="rok"></td>
            </tr>
            <tr>
                <td><fmt:message bundle="${lctx}" key="filter.query.mdt" /></td>
                <td><input type="text" value="<c:out value="${param.udc}" />" size="20" name="udc" id="udc"></td>
            </tr>
            <tr>
                <td><fmt:message bundle="${lctx}" key="filter.query.ddt" /></td>
                <td><input type="text" value="<c:out value="${param.ddc}" />" size="20" name="ddc" id="ddc"></td>
            </tr>
            <tr>
                <td><fmt:message bundle="${lctx}" key="Pouze veřejné dokumenty" /></td>
                <td><input type="checkbox" value="on" name="onlyPublic" <c:if test="${!empty param.onlyPublic}">checked="checked"</c:if>></td>
            </tr>
            <tr>
                <td colspan="2" align="right">
                    <input type="submit" value="OK" class="ui-state-default ui-corner-all" />
                    <input type="button" value="<fmt:message bundle="${lctx}" key="common.close" />" class="ui-state-default ui-corner-all"  onclick="$('#advSearch').hide();" />
                </td>
            </tr>
    </tbody></table>
</div>