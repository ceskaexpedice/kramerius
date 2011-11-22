<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<%@ page isELIgnored="false"%>
<scrd:loggedusers>
<view:object name="cols" clz="cz.incad.Kramerius.views.virtualcollection.VirtualCollectionViewObject"></view:object>
<table id="coll_table">
<c:forEach var="col" items="${cols.virtualCollectionsLocale}">
    <tr id="vc_${col.pid}" class="vcoll">
        <c:forEach items="${col.descriptions}" var="desc">
            <td><input type="checkbox" /></td>
            <td>${desc.text}</td>
        </c:forEach>
    </tr>
</c:forEach>
</table>
</scrd:loggedusers>

